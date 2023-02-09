FROM gcr.io/distroless/java11@sha256:97386fb51202ca02f4ec0c9e9fde9a766545848b74fd309a6724f489a81b33fa
USER nonroot
EXPOSE 9501

COPY target/ms-robotics-drs-controller-*.jar /ms-robotics-drs-controller.jar
COPY src/main/resources/config.yml /config.yml

ENTRYPOINT [ "java", "-jar", "/ms-robotics-drs-controller.jar", "server", "/config.yml" ]
