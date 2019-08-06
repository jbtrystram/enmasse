/*
 * Copyright 2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.enmasse.iot.registry.infinispan.config;

import io.enmasse.iot.registry.infinispan.CacheProvider;
import org.eclipse.hono.deviceregistry.ApplicationConfig;
import org.eclipse.hono.service.management.tenant.TenantManagementHttpEndpoint;
import org.eclipse.hono.service.tenant.TenantAmqpEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Scope;

/**
 * Spring Boot configuration for the Device Registry application.
 *
 */
@Configuration
public class InfinispanRegistryConfig extends ApplicationConfig {

    /**
     * Creates a new instance of the CacheProvider.
     *
     * @return The provider.
     */
    @Bean
    @Scope("prototype")
    public CacheProvider cacheProvider(InfinispanProperties props) {
        return new CacheProvider(props);
    }

    /**
     * Creates a new instance of an AMQP 1.0 protocol handler for Hono's <em>Tenant</em> API.
     *
     * @return The handler.
     */
    @Bean
    @Override
    @Scope("prototype")
    @ConditionalOnBean(name="CacheTenantService")
    public TenantAmqpEndpoint tenantAmqpEndpoint() {
        return super.tenantAmqpEndpoint();
    }

    /**
     * Creates a new instance of an HTTP protocol handler for Hono's <em>Tenant</em> API.
     *
     * @return The handler.
     */
    @Bean
    @Override
    @Scope("prototype")
    @ConditionalOnBean(name="CacheTenantService")
    public TenantManagementHttpEndpoint tenantHttpEndpoint() {
        return super.tenantHttpEndpoint();
    }
}
