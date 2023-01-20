FROM gcr.io/distroless/java11@sha256:e0835ed9898c4e486761b1b1018bf8a92bdd04a8390038cf97e3dc7dbfcf25d8
USER nonroot
EXPOSE 9501

COPY target/ms-robotics-drs-controller-*.jar /ms-robotics-drs-controller.jar
COPY src/main/resources/config.yml /config.yml

ENTRYPOINT [ "java", "-jar", "/ms-robotics-drs-controller.jar", "server", "/config.yml" ]
