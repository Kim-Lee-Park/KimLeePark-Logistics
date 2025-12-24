FROM grafana/tempo:2.9.0

COPY tempo.yaml /etc/tempo.yaml

ENTRYPOINT ["/tempo", "-config.file=/etc/tempo.yaml"]
