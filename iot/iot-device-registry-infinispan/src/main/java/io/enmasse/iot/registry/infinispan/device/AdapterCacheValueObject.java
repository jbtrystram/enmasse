package io.enmasse.iot.registry.infinispan.device;

import java.io.Serializable;
import java.util.UUID;

public class AdapterCacheValueObject implements Serializable {

    private static final long serialVersionUID = 1L;

    // A Json Object containing the credential.
    private String crentential;

    private String deviceId;

    // Whether this entry is synced with the values in management cache.
    private boolean inSync= false;

    // resource version for the credentials
    private String version;

    private String managementCacheExpectedVersion;

    public AdapterCacheValueObject(String crentential, String deviceId, String managementCacheExpectedVersion) {
        this.crentential = crentential;
        this.deviceId = deviceId;
        this.managementCacheExpectedVersion = managementCacheExpectedVersion;
    }

    public void setInSync(boolean inSync) {
        this.inSync = inSync;
    }

    public void setManagementCacheExpectedVersion(String newVersion) {
        this.managementCacheExpectedVersion = newVersion;
    }
}
