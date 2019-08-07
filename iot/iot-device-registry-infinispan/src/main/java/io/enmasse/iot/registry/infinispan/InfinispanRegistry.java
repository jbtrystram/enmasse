/*
 * Copyright 2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.enmasse.iot.registry.infinispan;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import java.util.Objects;
import org.eclipse.hono.service.AbstractApplication;
import org.eclipse.hono.service.HealthCheckProvider;
import org.eclipse.hono.service.auth.AuthenticationService;
import org.eclipse.hono.service.credentials.BaseCredentialsService;
import org.eclipse.hono.service.deviceconnection.BaseDeviceConnectionService;
import org.eclipse.hono.service.registration.BaseRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("org.eclipse.hono.service.auth")
@ComponentScan( "io.enmasse.iot.registry.infinispan")
@ComponentScan( "io.enmasse.iot.registry.infinispan.config")
@Configuration
@EnableAutoConfiguration
public class InfinispanRegistry extends AbstractApplication {

    private AuthenticationService authenticationService;
    private BaseCredentialsService credentialsService;
    private BaseRegistrationService registrationService;
    private BaseDeviceConnectionService deviceConnectionService;

    /**
     * Sets the credentials service implementation this server is based on.
     *
     * @param credentialsService The service implementation.
     * @throws NullPointerException if service is {@code null}.
     */
    @Autowired
    public final void setCredentialsService(final BaseCredentialsService credentialsService) {
        this.credentialsService = Objects.requireNonNull(credentialsService);
    }

    /**
     * Sets the registration service implementation this server is based on.
     *
     * @param registrationService The registrationService to set.
     * @throws NullPointerException if service is {@code null}.
     */
    @Autowired
    public final void setRegistrationService(final BaseRegistrationService registrationService) {
        this.registrationService = Objects.requireNonNull(registrationService);
    }

    /**
     * Sets the authentication service implementation this server is based on.
     *
     * @param authenticationService The authenticationService to set.
     * @throws NullPointerException if service is {@code null}.
     */
    @Autowired
    public final void setAuthenticationService(final AuthenticationService authenticationService) {
        this.authenticationService = Objects.requireNonNull(authenticationService);
    }

    @Autowired
    public void setDeviceConnectionService(BaseDeviceConnectionService deviceConnectionService) {
        this.deviceConnectionService = deviceConnectionService;
    }

    @Override
    protected final Future<Void> deployRequiredVerticles(final int maxInstances) {

        final Future<Void> result = Future.future();
        CompositeFuture.all(
                deployAuthenticationService(), // we only need 1 authentication service
                deployRegistrationService(),
                deployCredentialsService(),
                deployDeviceConnectionService()
            ).setHandler(ar -> {
            if (ar.succeeded()) {
                result.complete();
            } else {
                result.fail(ar.cause());
            }
        });
        return result;
    }

    private Future<String> deployDeviceConnectionService() {
        final Future<String> result = Future.future();
        log.info("Starting device connection service {}", deviceConnectionService);
        getVertx().deployVerticle(deviceConnectionService, result);
        return result;
    }

    private Future<String> deployCredentialsService() {
        final Future<String> result = Future.future();
        log.info("Starting credentials service {}", credentialsService);
        getVertx().deployVerticle(credentialsService, result);
        return result;
    }

    private Future<String> deployAuthenticationService() {
        final Future<String> result = Future.future();
        if (!Verticle.class.isInstance(authenticationService)) {
            result.fail("authentication service is not a verticle");
        } else {
            log.info("Starting authentication service {}", authenticationService);
            getVertx().deployVerticle((Verticle) authenticationService, result);
        }
        return result;
    }

    private Future<String> deployRegistrationService() {
        final Future<String> result = Future.future();
        log.info("Starting registration service {}", registrationService);
        getVertx().deployVerticle(registrationService, result);
        return result;
    }


    /**
     * Registers any additional health checks that the service implementation components provide.
     *
     * @return A succeeded future.
     */
    @Override
    protected Future<Void> postRegisterServiceVerticles() {
        if (HealthCheckProvider.class.isInstance(authenticationService)) {
            registerHealthchecks((HealthCheckProvider) authenticationService);
        }
        if (HealthCheckProvider.class.isInstance(credentialsService)) {
            registerHealthchecks((HealthCheckProvider) credentialsService);
        }
        if (HealthCheckProvider.class.isInstance(registrationService)) {
            registerHealthchecks((HealthCheckProvider) registrationService);
        }
        if (HealthCheckProvider.class.isInstance(deviceConnectionService)) {
            registerHealthchecks((HealthCheckProvider) deviceConnectionService);
        }
        return Future.succeededFuture();
    }

    /**
     * Starts the Device Registry Server.
     *
     * @param args command line arguments to pass to the server.
     */
    public static void main(final String[] args) {
        SpringApplication.run(InfinispanRegistry.class, args);
    }
}
