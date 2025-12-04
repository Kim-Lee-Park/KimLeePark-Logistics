FROM prom/prometheus:v2.54.0

COPY prometheus.yaml /etc/prometheus/prometheus.yml

ENTRYPOINT ["/bin/prometheus", "--config.file=/etc/prometheus/prometheus.yml"]
