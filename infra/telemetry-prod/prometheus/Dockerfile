FROM prom/prometheus:v3.8.1

COPY prometheus.yaml /etc/prometheus/prometheus.yml

ENTRYPOINT ["/bin/prometheus", "--config.file=/etc/prometheus/prometheus.yml"]
