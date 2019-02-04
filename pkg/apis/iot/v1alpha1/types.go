/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package v1alpha1

import (
    metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// +genclient
// +k8s:openapi-gen=true
// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type IoTProject struct {
    metav1.TypeMeta   `json:",inline"`
    metav1.ObjectMeta `json:"metadata,omitempty"`

    Spec   IoTProjectSpec   `json:"spec,omitempty"`
    Status IoTProjectStatus `json:"status,omitempty"`
}

type IoTProjectSpec struct {
    DownstreamStrategy DownstreamStrategy `json:"downstreamStrategy"`
}

type IoTProjectStatus struct {
    IsReady            bool                      `json:"isReady"`
    DownstreamEndpoint *DownstreamEndpointStatus `json:"downstreamEndpoint"`
}

type DownstreamEndpointStatus struct {
    Information ExternalDownstreamStrategy `json:",inline"`
}

type DownstreamStrategy struct {
    ExternalDownstreamStrategy *ExternalDownstreamStrategy `json:"externalStrategy"`
    ProvidedDownstreamStrategy *ProvidedDownstreamStrategy `json:"providedStrategy"`
}

type ProvidedDownstreamStrategy struct {
    Namespace        string `json:"namespace"`
    AddressSpaceName string `json:"addressSpaceName"`

    EndpointMode EndpointMode `json:"endpointMode"`
    EndpointName string       `json:"endpointName"`
    PortName     string       `json:"portName"`

    Credentials `json:",inline"`
}

type ExternalDownstreamStrategy struct {
    Host string `json:"host"`
    Port uint16 `json:"port"`

    Credentials `json:",inline"`

    TLS         bool   `json:"tls"`
    Certificate []byte `json:"certificate,omitempty"`
}

type Credentials struct {
    Username string `json:"username"`
    Password string `json:"password"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type IoTProjectList struct {
    metav1.TypeMeta `json:",inline"`
    metav1.ListMeta `json:"metadata,omitempty"'`

    Items []IoTProject `json:"items"`
}
