FROM ubuntu

# perl is needed for shasum
RUN apt-get -y update && apt-get install -y wget jq perl ca-certificates && update-ca-certificates && rm -rf /var/lib/apt/lists/*

RUN bash -c "set -eo pipefail; wget -O- https://carvel.dev/install.sh | bash"

RUN ytt version && kapp version && kbld version && kwt version && imgpkg version && vendir version

RUN wget https://github.com/pivotal-cf/pivnet-cli/releases/download/v4.1.1/pivnet-linux-amd64-4.1.1 -O /usr/local/bin/pivnet \
    && chmod +x /usr/local/bin/pivnet  

RUN groupadd --gid 1000 tap-operator && useradd -g 1000 -u 1000 -m tap-operator
ADD pivnet-download.sh /home
RUN chmod +x /home/pivnet-download.sh && chown tap-operator:tap-operator /home/pivnet-download.sh

USER tap-operator
WORKDIR /home

CMD ["/bin/bash", "-c", "/home/pivnet-download.sh"]