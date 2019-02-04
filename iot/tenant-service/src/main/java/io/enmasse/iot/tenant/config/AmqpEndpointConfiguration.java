/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.enmasse.iot.tenant.config;

import org.eclipse.hono.config.ServiceConfigProperties;
import org.eclipse.hono.service.tenant.TenantAmqpEndpoint;
import org.eclipse.hono.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.enmasse.iot.tenant.impl.TenantAmqpService;
import io.vertx.core.Vertx;

@Configuration
public class AmqpEndpointConfiguration {

    private static final String BEAN_NAME_TENANT_AMQP_SERVICE = "tenantAmqpService";

    private Vertx vertx;

    @Autowired
    public void setVertx(final Vertx vertx) {
        this.vertx = vertx;
    }

    @Bean
    @Scope("prototype")
    public TenantAmqpEndpoint tenantAmqpEndpoint() {
        return new TenantAmqpEndpoint(this.vertx);
    }

    @Qualifier(Constants.QUALIFIER_AMQP)
    @Bean
    @ConfigurationProperties(prefix = "enmasse.iot.tenant.endpoint.amqp")
    public ServiceConfigProperties amqpEndpointProperties() {
        return new ServiceConfigProperties();
    }

    @Bean(BEAN_NAME_TENANT_AMQP_SERVICE)
    @Scope("prototype")
    public TenantAmqpService tenantAmqpService() {
        return new TenantAmqpService();
    }

    @Bean
    public ObjectFactoryCreatingFactoryBean tenantAmqpServiceFactory() {
        final ObjectFactoryCreatingFactoryBean factory = new ObjectFactoryCreatingFactoryBean();
        factory.setTargetBeanName(BEAN_NAME_TENANT_AMQP_SERVICE);
        return factory;
    }
}
