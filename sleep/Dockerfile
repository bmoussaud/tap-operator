FROM ubuntu
RUN groupadd --gid 1000 tap-operator && useradd -g 1000 -u 1000 -m tap-operator
USER tap-operator
WORKDIR /home

CMD ["/bin/bash", "-c", "sleep 1"]