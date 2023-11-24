FROM ubuntu

# perl is needed for shasum
RUN apt-get -y update && apt-get install -y wget perl ca-certificates && update-ca-certificates && rm -rf /var/lib/apt/lists/*

RUN bash -c "set -eo pipefail; wget -O- https://carvel.dev/install.sh | bash"

RUN ytt version && kapp version && kbld version && kwt version && imgpkg version && vendir version

RUN groupadd --gid 1000 tap-operator && useradd -g 1000 -u 1000 -m tap-operator
ADD copy_package.sh /home
RUN chmod +x /home/copy_package.sh && chown tap-operator:tap-operator /home/copy_package.sh

USER tap-operator
WORKDIR /home

CMD ["/bin/bash", "-c", "/home/copy_package.sh"]