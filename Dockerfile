FROM gcr.io/distroless/java17@sha256:2578479b0d22bdf9dba8320de62969793b32e3226c9327b1f5e1c9f2bd3f1021
USER nonroot
EXPOSE 9501

COPY target/ms-robotics-drs-controller-*.jar /ms-robotics-drs-controller.jar
COPY src/main/resources/config.yml /config.yml

ENTRYPOINT [ "java", "-jar", "/ms-robotics-drs-controller.jar", "server", "/config.yml" ]
