FROM gcr.io/distroless/java17@sha256:68e2373f7bef9486c08356bd9ffd3b40b56e6b9316c5f6885eb58b1d9093b43d
USER nonroot
EXPOSE 9501

COPY target/ms-robotics-drs-controller-*.jar /ms-robotics-drs-controller.jar
COPY src/main/resources/config.yml /config.yml

ENTRYPOINT [ "java", "-jar", "/ms-robotics-drs-controller.jar", "server", "/config.yml" ]
