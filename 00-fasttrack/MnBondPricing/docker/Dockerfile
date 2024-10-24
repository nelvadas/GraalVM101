# We are using the Java 11 version of GraalVM CE 21.0.0
FROM ghcr.io/graalvm/graalvm-ce:java11-22.3.0 as graalvm
# Install the native image tool
RUN gu install native-image
# Set the build directory
WORKDIR /home/build
# Copy the shaded application jar file to the build directory
COPY target/grpc-cat-api-*.jar /home/build/
# Create the native image executable
RUN native-image --no-server -cp grpc-cat-api-*.jar -H:Name=grpc-cat-api -H:Class=nl.sybrenbolandit.grpc.cat.api.Application -H:Name=grpc-cat-api -H:+StaticExecutableWithDynamicLibC && mv grpc-cat-api api
# Create Docker image from the Distroless base image
FROM gcr.io/distroless/base-debian10
EXPOSE 50051
COPY --from=graalvm /home/build/api /app/api
WORKDIR /app/
ENTRYPOINT ["/app/api"]