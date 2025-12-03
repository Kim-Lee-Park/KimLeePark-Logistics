FROM public.ecr.aws/aws-observability/aws-otel-collector:latest

COPY otel-collector-config.yaml /etc/otel-config.yaml

ENTRYPOINT ["/awscollector", "--config=/etc/otel-config.yaml"]
