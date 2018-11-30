/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by informer-gen. DO NOT EDIT.

package externalversions

import (
	"fmt"

	v1alpha1 "github.com/enmasseproject/enmasse/pkg/apis/enmasse/v1alpha1"
	iotv1alpha1 "github.com/enmasseproject/enmasse/pkg/apis/iot/v1alpha1"
	schema "k8s.io/apimachinery/pkg/runtime/schema"
	cache "k8s.io/client-go/tools/cache"
)

// GenericInformer is type of SharedIndexInformer which will locate and delegate to other
// sharedInformers based on type
type GenericInformer interface {
	Informer() cache.SharedIndexInformer
	Lister() cache.GenericLister
}

type genericInformer struct {
	informer cache.SharedIndexInformer
	resource schema.GroupResource
}

// Informer returns the SharedIndexInformer.
func (f *genericInformer) Informer() cache.SharedIndexInformer {
	return f.informer
}

// Lister returns the GenericLister.
func (f *genericInformer) Lister() cache.GenericLister {
	return cache.NewGenericLister(f.Informer().GetIndexer(), f.resource)
}

// ForResource gives generic access to a shared informer of the matching type
// TODO extend this to unknown resources with a client pool
func (f *sharedInformerFactory) ForResource(resource schema.GroupVersionResource) (GenericInformer, error) {
	switch resource {
	// Group=enmasse.io, Version=v1alpha1
	case v1alpha1.SchemeGroupVersion.WithResource("addressspaces"):
		return &genericInformer{resource: resource.GroupResource(), informer: f.Enmasse().V1alpha1().AddressSpaces().Informer()}, nil

		// Group=iot.enmasse.io, Version=v1alpha1
	case iotv1alpha1.SchemeGroupVersion.WithResource("iotprojects"):
		return &genericInformer{resource: resource.GroupResource(), informer: f.Iot().V1alpha1().IoTProjects().Informer()}, nil

	}

	return nil, fmt.Errorf("no informer found for %v", resource)
}
