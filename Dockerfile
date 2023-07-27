FROM gcr.io/distroless/java17@sha256:052076466984fd56979c15a9c3b7433262b0ad9aae55bc0c53d1da8ffdd829c3
USER nonroot
EXPOSE 9501

COPY target/ms-robotics-drs-controller-*.jar /ms-robotics-drs-controller.jar
COPY src/main/resources/config.yml /config.yml

ENTRYPOINT [ "java", "-jar", "/ms-robotics-drs-controller.jar", "server", "/config.yml" ]
