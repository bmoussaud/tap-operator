#!/bin/bash
set -x

export TAP_VERSION="1.7.3-build.1"
export REMOTE_URL="https://github.com/bmoussaud/tap-gitops.git"
export CLUSTER_NAME="empty"
export AGE_KEY="AGE-SECRET-KEY-123skjldjflkqsjdflmkqjdlmkfjlmksdjfmlkqsjmlfkjqmlksfjmlkq"

#internal config
export SAVED_TAP_VERSION=${TAP_VERSION}
export TAP_VERSION=
export TAP_PKGR_REPO=${IMGPKG_REGISTRY_HOSTNAME_1}/tanzu-application-platform/tap-packages
export PIVKEY="32463e3184d34e47be6f26c06e6ade50-r"
export TMP_DIR="/tmp/benoit"

mkdir -p ${TMP_DIR}
pivnet login --api-token=${PIVKEY}
pivnet pfs --product-slug='tanzu-application-platform' --release-version=${SAVED_TAP_VERSION} --format json >out.json
jq '.[] | select(.name == "Tanzu GitOps Reference Implementation" )' out.json >item.json
cat item.json
PRODUCT_FILE_ID=$(jq -r '.id' item.json)
echo "PRODUCT_FILE_ID: ${PRODUCT_FILE_ID}"
AWS_OBJECT_KEY=$(jq -r '.aws_object_key' item.json)
echo "AWS_OBJECT_KEY: ${AWS_OBJECT_KEY}"
DOWNLOADED_FILE=$(basename ${AWS_OBJECT_KEY})
echo "DOWNLOADED_FILE: ${DOWNLOADED_FILE}"
pivnet download-product-files --product-slug='tanzu-application-platform' --release-version=${SAVED_TAP_VERSION} --product-file-id=${PRODUCT_FILE_ID}
tar zxvf ${DOWNLOADED_FILE} -C ${TMP_DIR}
cd ${TMP_DIR}
./setup-repo.sh ${CLUSTER_NAME} sops
cd ${TMP_DIR}/clusters/${CLUSTER_NAME}

touch tanzu-sync/app/values/tanzu-sync.yaml
find .
echo "
---
git:
  url: ${REMOTE_URL}
  ref: origin/main
  sub_path: clusters/${CLUSTER_NAME}/cluster-config  
tap_package_repository:
  oci_repository: ${TAP_PKGR_REPO}
" >tanzu-sync/app/values/tanzu-sync.yaml

cat tanzu-sync/app/values/tanzu-sync.yaml

echo "
---
tap_install:
  package_repository:
    oci_repository: ${TAP_PKGR_REPO}
" >cluster-config/values/tap-install-values.yaml

echo "
---
secrets:
    sops:
        age_key: |
            # created: 2023-07-03T12:44:05+02:00
            # public key: age1sfvahczpatyd232kk396plhxxz7cuu5l8mw52zljg2cg7xqwdf0qp8k9q9
            ${AGE_KEY}
        registry:
            hostname: ${IMGPKG_REGISTRY_HOSTNAME_1}
            username: ${IMGPKG_REGISTRY_USERNAME_1}
            password: ${IMGPKG_REGISTRY_PASSWORD_1}
        git:
            basic_auth:
                username: "xxx"
                password: "yyy"
" >tanzu-sync/app/sensitive-values/tanzu-sync-values.sops.yaml

cat tanzu-sync/app/sensitive-values/tanzu-sync-values.sops.yaml

echo "DEPLOY"
set -o errexit -o nounset -o pipefail
set -o xtrace

kapp deploy --yes -a tanzu-sync \
    -f <(
        ytt -f tanzu-sync/app/config \
            -f cluster-config/config/tap-install/.tanzu-managed/version.yaml \
            --data-values-file tanzu-sync/app/sensitive-values \
            --data-values-file tanzu-sync/app/values
    ) $@

echo "DONE"
