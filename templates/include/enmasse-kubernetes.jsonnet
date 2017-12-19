local templateConfig = import "template-config.jsonnet";
local addressController = import "address-controller.jsonnet";
local common = import "common.jsonnet";
local restapiRoute = import "restapi-route.jsonnet";
local messagingService = import "messaging-service.jsonnet";
local mqttService = import "mqtt-service.jsonnet";
local consoleService = import "console-service.jsonnet";
local images = import "images.jsonnet";
local roles = import "roles.jsonnet";
{
  cluster_roles::
  {
    "apiVersion": "v1",
    "kind": "List",
    "items": [
      roles.address_admin_role("rbac.authorization.k8s.io/v1"),
      roles.namespace_admin_role("rbac.authorization.k8s.io/v1"),
      roles.event_reporter_role("rbac.authorization.k8s.io/v1")
    ]
  },

  list::
  {
    "apiVersion": "v1",
    "kind": "List",
    "items": [ templateConfig.global,
               addressController.deployment(images.address_controller, "enmasse-template-config", "address-controller-cert", "development", "false", "enmasse-admin", "address-space-admin"),
               addressController.internal_service ]
  },

  external_lb::
    addressController.external_service,
}
