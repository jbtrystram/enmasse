/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.iot.model.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.Test;

import io.enmasse.iot.model.v1.CustomResource.Plural;
import io.enmasse.iot.model.v1.CustomResource.Singular;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;

public class CustomResourceTest {

    @ApiVersion("v1alpha1")
    @CustomResource(group = "iot.enmasse.io")
    private static class Foo {
    }

    @ApiVersion("v1alpha1")
    @CustomResource(group = "iot.enmasse.io", shortNames = { "b", "ba" })
    @Singular("baa")
    private static class Bar {
    }

    @ApiVersion("v1alpha1")
    @CustomResource(group = "iot.enmasse.io")
    @Singular
    @Plural("bazzes")
    private static class Baz {
    }

    @Test
    public void testResourceDefinition1() {
        final CustomResourceDefinition definition = CustomResources.fromClass(Foo.class);

        assertEquals("foos.iot.enmasse.io", definition.getMetadata().getName());

        assertEquals("v1alpha1", definition.getSpec().getVersion());
        assertEquals("iot.enmasse.io", definition.getSpec().getGroup());

        assertEquals("Foo", definition.getSpec().getNames().getKind());
        assertEquals("foo", definition.getSpec().getNames().getSingular());
        assertEquals("foos", definition.getSpec().getNames().getPlural());
        assertEquals(Arrays.asList(), definition.getSpec().getNames().getShortNames());
    }

    @Test
    public void testResourceDefinition2() {
        final CustomResourceDefinition definition = CustomResources.fromClass(Bar.class);

        assertEquals("baas.iot.enmasse.io", definition.getMetadata().getName());

        assertEquals("v1alpha1", definition.getSpec().getVersion());
        assertEquals("iot.enmasse.io", definition.getSpec().getGroup());

        assertEquals("Bar", definition.getSpec().getNames().getKind());
        assertEquals("baa", definition.getSpec().getNames().getSingular());
        assertEquals("baas", definition.getSpec().getNames().getPlural());
        assertEquals(Arrays.asList("b", "ba"), definition.getSpec().getNames().getShortNames());
    }

    @Test
    public void testResourceDefinition3() {
        final CustomResourceDefinition definition = CustomResources.fromClass(Baz.class);

        assertEquals("bazzes.iot.enmasse.io", definition.getMetadata().getName());

        assertEquals("v1alpha1", definition.getSpec().getVersion());
        assertEquals("iot.enmasse.io", definition.getSpec().getGroup());

        assertEquals("Baz", definition.getSpec().getNames().getKind());
        assertNull(definition.getSpec().getNames().getSingular());
        assertEquals("bazzes", definition.getSpec().getNames().getPlural());
        assertEquals(Arrays.asList(), definition.getSpec().getNames().getShortNames());
    }

}
