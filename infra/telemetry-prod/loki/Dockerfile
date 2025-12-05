FROM grafana/loki:3.1.0

COPY loki-config.yaml /etc/loki.yaml

ENTRYPOINT ["/usr/bin/loki", "-config.file=/etc/loki.yaml"]
