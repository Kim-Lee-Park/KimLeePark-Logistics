FROM otel/opentelemetry-collector-contrib:0.142.0

COPY otel-collector-config.yaml /etc/otel-config.yaml

ENTRYPOINT ["/otelcol-contrib", "--config=/etc/otel-config.yaml"]
