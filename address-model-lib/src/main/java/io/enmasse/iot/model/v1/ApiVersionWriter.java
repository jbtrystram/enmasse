/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.enmasse.iot.model.v1;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

public class ApiVersionWriter extends VirtualBeanPropertyWriter {

    private static final long serialVersionUID = 1L;

    public ApiVersionWriter() {
    }

    public ApiVersionWriter(final BeanPropertyDefinition definition, final Annotations annotations,
            final JavaType type) {
        super(definition, annotations, type);
    }

    @Override
    protected Object value(final Object bean, final JsonGenerator gen, final SerializerProvider prov) throws Exception {
        return bean.getClass().getAnnotation(ApiVersion.class).value();
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(final MapperConfig<?> config, final AnnotatedClass declaringClass,
            final BeanPropertyDefinition propDef, final JavaType type) {
        return new ApiVersionWriter(propDef, null, type);
    }

}