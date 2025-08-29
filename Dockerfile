FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/install/*/lib /app/lib
ENTRYPOINT ["java", "-cp", "/app/lib/*", "fakedings.ApplicationKt"]
