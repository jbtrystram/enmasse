/*
 * Copyright 2018-2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by client-gen. DO NOT EDIT.

package fake

import (
	enmassev1 "github.com/enmasseproject/enmasse/pkg/apis/enmasse/v1"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	labels "k8s.io/apimachinery/pkg/labels"
	schema "k8s.io/apimachinery/pkg/runtime/schema"
	types "k8s.io/apimachinery/pkg/types"
	watch "k8s.io/apimachinery/pkg/watch"
	testing "k8s.io/client-go/testing"
)

// FakeMessagingTenants implements MessagingTenantInterface
type FakeMessagingTenants struct {
	Fake *FakeEnmasseV1
	ns   string
}

var messagingtenantsResource = schema.GroupVersionResource{Group: "enmasse.io", Version: "v1", Resource: "messagingtenants"}

var messagingtenantsKind = schema.GroupVersionKind{Group: "enmasse.io", Version: "v1", Kind: "MessagingTenant"}

// Get takes name of the messagingTenant, and returns the corresponding messagingTenant object, and an error if there is any.
func (c *FakeMessagingTenants) Get(name string, options v1.GetOptions) (result *enmassev1.MessagingTenant, err error) {
	obj, err := c.Fake.
		Invokes(testing.NewGetAction(messagingtenantsResource, c.ns, name), &enmassev1.MessagingTenant{})

	if obj == nil {
		return nil, err
	}
	return obj.(*enmassev1.MessagingTenant), err
}

// List takes label and field selectors, and returns the list of MessagingTenants that match those selectors.
func (c *FakeMessagingTenants) List(opts v1.ListOptions) (result *enmassev1.MessagingTenantList, err error) {
	obj, err := c.Fake.
		Invokes(testing.NewListAction(messagingtenantsResource, messagingtenantsKind, c.ns, opts), &enmassev1.MessagingTenantList{})

	if obj == nil {
		return nil, err
	}

	label, _, _ := testing.ExtractFromListOptions(opts)
	if label == nil {
		label = labels.Everything()
	}
	list := &enmassev1.MessagingTenantList{ListMeta: obj.(*enmassev1.MessagingTenantList).ListMeta}
	for _, item := range obj.(*enmassev1.MessagingTenantList).Items {
		if label.Matches(labels.Set(item.Labels)) {
			list.Items = append(list.Items, item)
		}
	}
	return list, err
}

// Watch returns a watch.Interface that watches the requested messagingTenants.
func (c *FakeMessagingTenants) Watch(opts v1.ListOptions) (watch.Interface, error) {
	return c.Fake.
		InvokesWatch(testing.NewWatchAction(messagingtenantsResource, c.ns, opts))

}

// Create takes the representation of a messagingTenant and creates it.  Returns the server's representation of the messagingTenant, and an error, if there is any.
func (c *FakeMessagingTenants) Create(messagingTenant *enmassev1.MessagingTenant) (result *enmassev1.MessagingTenant, err error) {
	obj, err := c.Fake.
		Invokes(testing.NewCreateAction(messagingtenantsResource, c.ns, messagingTenant), &enmassev1.MessagingTenant{})

	if obj == nil {
		return nil, err
	}
	return obj.(*enmassev1.MessagingTenant), err
}

// Update takes the representation of a messagingTenant and updates it. Returns the server's representation of the messagingTenant, and an error, if there is any.
func (c *FakeMessagingTenants) Update(messagingTenant *enmassev1.MessagingTenant) (result *enmassev1.MessagingTenant, err error) {
	obj, err := c.Fake.
		Invokes(testing.NewUpdateAction(messagingtenantsResource, c.ns, messagingTenant), &enmassev1.MessagingTenant{})

	if obj == nil {
		return nil, err
	}
	return obj.(*enmassev1.MessagingTenant), err
}

// UpdateStatus was generated because the type contains a Status member.
// Add a +genclient:noStatus comment above the type to avoid generating UpdateStatus().
func (c *FakeMessagingTenants) UpdateStatus(messagingTenant *enmassev1.MessagingTenant) (*enmassev1.MessagingTenant, error) {
	obj, err := c.Fake.
		Invokes(testing.NewUpdateSubresourceAction(messagingtenantsResource, "status", c.ns, messagingTenant), &enmassev1.MessagingTenant{})

	if obj == nil {
		return nil, err
	}
	return obj.(*enmassev1.MessagingTenant), err
}

// Delete takes name of the messagingTenant and deletes it. Returns an error if one occurs.
func (c *FakeMessagingTenants) Delete(name string, options *v1.DeleteOptions) error {
	_, err := c.Fake.
		Invokes(testing.NewDeleteAction(messagingtenantsResource, c.ns, name), &enmassev1.MessagingTenant{})

	return err
}

// DeleteCollection deletes a collection of objects.
func (c *FakeMessagingTenants) DeleteCollection(options *v1.DeleteOptions, listOptions v1.ListOptions) error {
	action := testing.NewDeleteCollectionAction(messagingtenantsResource, c.ns, listOptions)

	_, err := c.Fake.Invokes(action, &enmassev1.MessagingTenantList{})
	return err
}

// Patch applies the patch and returns the patched messagingTenant.
func (c *FakeMessagingTenants) Patch(name string, pt types.PatchType, data []byte, subresources ...string) (result *enmassev1.MessagingTenant, err error) {
	obj, err := c.Fake.
		Invokes(testing.NewPatchSubresourceAction(messagingtenantsResource, c.ns, name, pt, data, subresources...), &enmassev1.MessagingTenant{})

	if obj == nil {
		return nil, err
	}
	return obj.(*enmassev1.MessagingTenant), err
}