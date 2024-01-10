# TAP Operator

Tanzu Application Platformm (TAP) operator manages the following task:

* Relocating Tanzu packages to turn your TAP installation independant.
    * copy _TAP Essential_ packages from tanzu registry to a local registry (Carvel Operators)
    * copy TAP packages from tanzu registry to a local registry
    * copy PostgresSQL from tanzu registry to a local registry
* Deploy _TAP Essential_ in the cluster (kapp-control, secret-gen-control) (thanks
  @alexandreroman [tanzu-cluster-essentials-bootstrap](https://github.com/alexandreroman/tanzu-cluster-essentials-bootstrap) )
* Deploy tap-sync to trigger the TAP Gitops installation

## Installation

Prepare a secret [registry-secrets.yaml](config/registry-secrets.yaml) following this template

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
  # either (GIT_SSH_PRIVATEKEY+GIT_KNOWN_HOST) or (GIT_USERNAME + GIT_PASSWORD)
  GIT_SSH_PRIVATEKEY: #@ data.values.PRIVATE_KEY
  GIT_SSH_KNOWNHOSTS: //Computed for a GitRepository managed on public GitHub.com
```

```
git clone git@github.com:bmoussaud/tap-operator.git
kubectl apply -f config -f target/classes/META-INF/fabric8/tapresources.org.moussaud.tanzu-v1.yml -f registry-secrets.yaml.
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