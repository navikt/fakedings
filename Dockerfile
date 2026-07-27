FROM cgr.dev/chainguard/jre

COPY build/install/*/lib /app/lib
ENTRYPOINT ["java", "-cp", "/app/lib/*", "fakedings.ApplicationKt"]
