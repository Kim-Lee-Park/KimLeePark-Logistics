FROM grafana/loki:3.6.3

COPY loki-config.yaml /etc/loki.yaml

ENTRYPOINT ["/usr/bin/loki", "-config.file=/etc/loki.yaml"]
