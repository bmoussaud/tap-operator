# TAP Operator

Tanzu Application Platformm (TAP) operator manages the following task:

* Relocating Tanzu packages to turn your TAP installation independant.
    * copy _TAP Essential_ packages from tanzu registry to a local registry (Carvel Operators)
    * copy TAP packages from tanzu registry to a local registry
    * copy PostgresSQL from tanzu registry to a local registry
* Deploy _TAP Essential_ in the cluster (kapp-control, secret-gen-control) (thanks
  @alexandreroman [tanzu-cluster-essentials-bootstrap](https://github.com/alexandreroman/tanzu-cluster-essentials-bootstrap) )
* Deploy an `App` to trigger the TAP Gitops installation and all the necessary configuration
* Generate a `ConfigMap` (inject as value in the previous App) containing the version managed by the resource.

## Installation

Prepare a secret [registry-secrets.yaml](registry-secrets.yaml.template) following this template

```
apiVersion: v1
kind: Secret
metadata:
  name: tap-operator-registry-credentials
  namespace: tap-operator
type: Opaque
stringData:
  FROM_REGISTRY_HOSTNAME: registry.tanzu.vmware.com
  FROM_REGISTRY_USERNAME: #@ data.values.TANZU_USERNAME
  FROM_REGISTRY_PASSWORD: #@ data.values.TANZU_PASSWORD
  TO_REGISTRY_HOSTNAME: #@ data.values.HOSTNAME
  TO_REGISTRY_USERNAME: #@ data.values.USERNAME
  TO_REGISTRY_PASSWORD: #@ data.values.PASSWORD
  AGE_SECRET_KEY: AGE-SECRET-KEY-123skjldjflkqsjlkdsjlkjdslkjdlkfjskldjflksjfjs  
  GIT_SSH_PRIVATEKEY: #@ data.values.PRIVATE_KEY
  GIT_SSH_KNOWNHOSTS: //Computed for a GitRepository managed on public GitHub.com
```

```
git clone git@github.com:bmoussaud/tap-operator.git
vi registry-secrets.yaml
kubectl apply -f config  
```

```
export SOPS_AGE_KEY=
export INSTALL_REGISTRY_HOSTNAME=akseutap7registry.azurecr.io
export INSTALL_REGISTRY_USERNAME=scott
export INSTALL_REGISTRY_PASSWORD=tiger
export REGISTRY_TANZU_HOSTNAME=registry.tanzu.vmware.com
export REGISTRY_TANZU_USERNAME=bmoussaud@vmware.com
export REGISTRY_TANZU_PASSWORD=bisousbious
ytt -f ytt/registry-secrets.template.yaml --data-value-file SSH_PRIVATEKEY=<path_to/ssh/id_rsa>  --data-values-env INSTALL_REGISTRY --data-values-env REGISTRY  --data-values-env SOPS --data-values-env GIT> config/registry-secrets.yaml
kubectl apply -f config/*.yaml
```

## Usage

### Minimal configuration

``````
apiVersion: org.moussaud.tanzu/v1
kind: TapResource
metadata:
  name: mytap
  namespace: tap-operator
spec:
  version: 1.7.0  
  url: git@github.com:bmoussaud/tap-gitops.git
  subPath: clusters/empty/cluster-config
  
``````

### Full configuration

``````
apiVersion: org.moussaud.tanzu/v1
kind: TapResource
metadata:
  name: mytap
  namespace: tap-operator
spec:
  version: 1.7.0
  clusterEssentialsBundleVersion: 1.7.0
  postgresVersion: 1.12.1
  secret: my-tap-operator-registry-credentials
  url: git@github.com:bmoussaud/tap-gitops.git
  subPath: clusters/empty/cluster-config
``````

if not provided the `clusterEssentialsBundleVersion` property is computed to get latest value for a given version
the `secret` property contains the name of secret holding the image registry property.

The generated config map

```
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    app.kubernetes.io/component: tap-version
    app.kubernetes.io/managed-by: tap-operator
    app.kubernetes.io/name: mytap
  name: mytap-tap-version
  namespace: tap-operator
apiVersion: v1
data:
  values.yaml: |
    ---
    tap_operator:
      tap_version: "1.8.0-build.68"
```

The deployed TAP configuration can use ` #@ data.values.tap_install.values.tap_version` to manage the version