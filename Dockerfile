FROM ghcr.io/graalvm/native-image-community:17 AS builder

# Install Maven
WORKDIR /app
# 'native-image-community' is based on Oracle Linux, uses microdnf
RUN microdnf install -y maven

# Copy project files
COPY pom.xml .
COPY src src

# Build the native image
# We skip tests to speed up the build. 
# The output binary will be in /app/target/early-child-learn
RUN mvn native:compile -DskipTests

# Runtime stage
# We use a small glibc-based image. Alpine requires static linking (more complex).
FROM debian:stable-slim

WORKDIR /app
COPY --from=builder /app/target/early-child-learn .

EXPOSE 8080
CMD ["./early-child-learn"]
