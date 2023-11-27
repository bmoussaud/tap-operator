#!/bin/bash
echo "Copy package from ${IMGPKG_REGISTRY_HOSTNAME_0}  to ${IMGPKG_REGISTRY_HOSTNAME_1}"
echo "Package ${PACKAGE}"
echo "Version ${VERSION}"
imgpkg version
echo "Copy:"
set -x
# --tty maybe...
imgpkg copy -b ${IMGPKG_REGISTRY_HOSTNAME_0}/${PACKAGE}:${VERSION} --to-repo ${IMGPKG_REGISTRY_HOSTNAME_1}/${PACKAGE}
COPY_EXIT_CODE=$?
set +x
if [ $COPY_EXIT_CODE -ne 0 ]; then
    echo "Copy Error ${COPY_EXIT_CODE}"
    exit ${COPY_EXIT_CODE}
fi
echo "List: ${IMGPKG_REGISTRY_HOSTNAME_1}/${PACKAGE}"
set -x
imgpkg tag list -i ${IMGPKG_REGISTRY_HOSTNAME_1}/${PACKAGE}
set +x
echo "Describe ${IMGPKG_REGISTRY_HOSTNAME_1}/${PACKAGE}:${VERSION} :"
set -x
imgpkg describe -b ${IMGPKG_REGISTRY_HOSTNAME_1}/${PACKAGE}:${VERSION} -o yaml
