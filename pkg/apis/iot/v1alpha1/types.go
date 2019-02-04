/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package v1alpha1

import (
    metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// +genclient
// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type IoTProject struct {
    metav1.TypeMeta   `json:",inline"`
    metav1.ObjectMeta `json:"metadata,omitempty"'`

    Spec IoTProjectSpec `json:"spec"`
}

type IoTProjectSpec struct {
    DownstreamStrategy DownstreamStrategy `json:"downstreamStrategy"`
}

type DownstreamStrategy struct {
    ExternalDownstreamStrategy *ExternalDownstreamStrategy `json:"externalStrategy"`
}

type ExternalDownstreamStrategy struct {
    Host string `json:"host"`
    Port uint16 `json:"port"`

    Username string `json:"username"`
    Password string `json:"password"`

    TLS         bool   `json:"tls"`
    Certificate []byte `json:"certificate,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type IoTProjectList struct {
    metav1.TypeMeta `json:",inline"`
    metav1.ListMeta `json:"metadata,omitempty"'`

    Items []IoTProject `json:"items"`
}
