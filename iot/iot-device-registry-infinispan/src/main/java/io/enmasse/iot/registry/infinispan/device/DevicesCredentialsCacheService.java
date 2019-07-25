package io.enmasse.iot.registry.infinispan.device;

import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import io.enmasse.iot.registry.infinispan.credentials.CredentialsKey;
import io.enmasse.iot.registry.infinispan.util.Versioned;
import io.opentracing.Span;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import org.eclipse.hono.deviceregistry.FileBasedRegistrationService;
import org.eclipse.hono.service.credentials.CredentialsService;
import org.eclipse.hono.service.management.Id;
import org.eclipse.hono.service.management.OperationResult;
import org.eclipse.hono.service.management.Result;
import org.eclipse.hono.service.management.credentials.CommonCredential;
import org.eclipse.hono.service.management.credentials.CredentialsManagementService;
import org.eclipse.hono.service.management.device.Device;
import org.eclipse.hono.service.management.device.DeviceManagementService;
import org.eclipse.hono.util.CredentialsConstants;
import org.eclipse.hono.util.CredentialsResult;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO : add authorisation calls
// TODO : add logging
public class DevicesCredentialsCacheService implements CredentialsManagementService, DeviceManagementService,
        CredentialsService {

    // Adapter cache :
    // <( tenantId + authId + type), (credential + deviceId + sync-flag + registration data version)>
    private RemoteCache<CredentialsKey, AdapterCacheValueObject> adapterCache;
    // Management cache
    // <(TenantId+DeviceId), (Device information + version + credentials)>
    private RemoteCache<RegistrationKey, ManagementCacheValueObject> managementCache;


    //fixme : log or span ?
    private static final Logger log = LoggerFactory.getLogger(FileBasedRegistrationService.class);

    // AMQP API

    @Override
    public void get(String tenantId, String type, String authId, Span span,
            Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        get(tenantId, type, authId, null, span, resultHandler);
    }

    @Override
    public void get(String tenantId, String type, String authId, JsonObject clientContext, Span span,
            Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        final CredentialsKey key = new CredentialsKey(tenantId, authId, type);

        credentials.getAsync(key).thenAccept(credential -> {
            if (credential == null) {
                log.debug("Credential not found [tenant-id: {}, auth-id: {}, type: {}]", tenantId, authId, type);
                resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HTTP_NOT_FOUND)));
            } else if (clientContext != null && !clientContext.isEmpty()) {
                if (contextMatches(clientContext, new JsonObject(credential))) {
                    log.debug("Retrieve credential, context matches [tenant-id: {}, auth-id: {}, type: {}]", tenantId,
                            authId, type);
                    resultHandler.handle(Future.succeededFuture(
                            CredentialsResult.from(HTTP_OK, new JsonObject(credential))));
                } else {
                    log.debug("Context mismatch [tenant-id: {}, auth-id: {}, type: {}]", tenantId, authId, type);
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HTTP_NOT_FOUND)));
                }
            } else {
                log.debug("Retrieve credential [tenant-id: {}, auth-id: {}, type: {}]", tenantId, authId, type);
                resultHandler.handle(Future.succeededFuture(
                        CredentialsResult.from(HTTP_OK, new JsonObject(credential))));
            }
        });
    }

    // MANAGEMENT API

    @Override
    //fixme : nested Async call
    public void set(String tenantId, String deviceId, Optional<String> resourceVersion,
            List<CommonCredential> credentials, Span span, Handler<AsyncResult<OperationResult<Void>>> resultHandler) {

        final RegistrationKey regKey = new RegistrationKey(tenantId, deviceId);

        deviceInformation.containsKeyAsync(regKey).thenAccept(exist -> {
            if (exist) {

                final ArrayList<CredentialsKey> credKeysList = new ArrayList<>();
                for (CommonCredential cred : credentials) {
                    final JsonObject credentialObject = JsonObject.mapFrom(cred);
                    final String type = credentialObject.getString(CredentialsConstants.FIELD_TYPE);

                    final CredentialsKey credKey = new CredentialsKey(tenantId, cred.getAuthId(), type);
                    credKeysList.add(credKey);

                    this.credentials.putAsync(credKey, JsonObject.mapFrom(cred).encode());
                }

                final Versioned<List<CredentialsKey>> entry = new Versioned<>(credKeysList);
                credentialsKeysForDevice.putAsync(regKey, entry).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(
                            OperationResult.ok(HTTP_OK, null, Optional.empty(), Optional.of(entry.getVersion()))));
                });

            } else {
                resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_NOT_FOUND)));
            }
        });

    }

    @Override
    //fixme : nested Async calls
    public void get(String tenantId, String deviceId, Span span,
            Handler<AsyncResult<OperationResult<List<CommonCredential>>>> resultHandler) {

        final RegistrationKey regKey = new RegistrationKey(tenantId, deviceId);

        managementCache.getAsync(regKey).thenAccept( mgmtObject -> {
            if (mgmtObject != null) {
                resultHandler.handle(Future.succeededFuture(
                    OperationResult.ok(HTTP_OK,
                            mgmtObject.getCredentialsList(),
                            Optional.empty(),
                            Optional.of(mgmtObject.getVersion()))));
            } else {
                resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_NOT_FOUND)));
            }
        });
    }

    @Override
    public void createDevice(String tenantId, Optional<String> optionalDeviceId, Device device, Span span,
            Handler<AsyncResult<OperationResult<Id>>> resultHandler) {

        final String deviceId = optionalDeviceId.orElseGet(() -> generateDeviceId(tenantId));

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        final ManagementCacheValueObject val = new ManagementCacheValueObject(JsonObject.mapFrom(device).encode());

        managementCache.putIfAbsentAsync(key, val).thenAccept(result -> {
                if ( result == null){

                    resultHandler.handle(Future.succeededFuture(
                            OperationResult.ok(HTTP_CREATED,
                                    Id.of(deviceId),
                                    Optional.empty(), Optional.of(val.getVersion()))));
                } else {
                    resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_CONFLICT)));
                }
        });
    }

    @Override
    public void readDevice(String tenantId, String deviceId, Span span,
            Handler<AsyncResult<OperationResult<Device>>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        managementCache.getAsync(key).thenAccept(res -> {
           if (res != null) {
               resultHandler.handle(Future.succeededFuture(
                       OperationResult.ok(HTTP_OK,
                               res.getDevinceInfoAsJson().mapTo(Device.class),
                               Optional.empty(),
                               Optional.of(res.getVersion()))));
           } else {
               resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_NOT_FOUND)));
           }
        });
    }

    @Override
    //fixme : nested Async call
    public void updateDevice(String tenantId, String deviceId, Device device,
            Optional<String> resourceVersion, Span span, Handler<AsyncResult<OperationResult<Id>>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        managementCache.getAsync(key).thenAccept(result -> {
            if ( result != null){

                if (result.isVersionMatch(resourceVersion)){
                    final String newVersion = UUID.randomUUID().toString();
                    result.getCredentialsKeys(tenantId).forEach(credKey -> {
                        //fixme : is there a "get, process, update method?
                       adapterCache.getAsync(credKey).thenAccept(adapterVal -> {
                           adapterVal.setInSync(false);
                           adapterVal.setManagementCacheExpectedVersion(newVersion);
                           adapterCache.put(credKey, adapterVal);
                       });
                    });
                    // fixme : do it when the previous operations are done.
                    managementCache.put(key, new ManagementCacheValueObject(JsonObject.mapFrom(device).encode()));
                } else {
                    resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_PRECON_FAILED)));
                }
            } else {
                resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_NOT_FOUND)));
            }
        });
    }

    @Override
    //fixme : 2 nested Async calls
    public void deleteDevice(String tenantId, String deviceId, Optional<String> resourceVersion, Span span,
            Handler<AsyncResult<Result<Void>>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        managementCache.getAsync(key).thenAccept(result -> {
            if ( result != null){

                if (result.isVersionMatch(resourceVersion)){
                    result.getCredentialsKeys(tenantId).forEach(credKey -> {
                            adapterCache.removeAsync(credKey).thenAccept(r -> {
                                managementCache.remove(key);
                            });
                });
                } else {
                    resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_PRECON_FAILED)));
                }
            } else {
                resultHandler.handle(Future.succeededFuture(OperationResult.empty(HTTP_NOT_FOUND)));
            }

        });
    }

    // PRIVATE UTIL METHODS

    private static boolean contextMatches(final JsonObject clientContext, final JsonObject storedCredential) {
        final AtomicBoolean match = new AtomicBoolean(true);
        clientContext.forEach(field -> {
            if (storedCredential.containsKey(field.getKey())) {
                if (!storedCredential.getString(field.getKey()).equals(field.getValue())) {
                    match.set(false);
                }
            } else {
                match.set(false);
            }
        });
        return match.get();
    }

    /**
     * Generate a random device ID.
     */
    private String generateDeviceId(final String tenantId) {

        String tempDeviceId;
        do {
            tempDeviceId = UUID.randomUUID().toString();
        } while (managementCache.containsKey(new RegistrationKey(tenantId, tempDeviceId)));
        return tempDeviceId;
    }
}



