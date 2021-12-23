# Sentry for gRPC integration

Integration based on [grpc-java](https://github.com/grpc/grpc-java) implementation

Usage
=====

Plain interceptor
-----------------

To use plain interceptor add the following dependency to your build.gradle    

    implementation("ru.m2:sentry-grpc-interceptor:1.0.0")

Then you could use one of the standard [sentry configuration way](https://docs.sentry.io/platforms/java/configuration/#setting-the-dsn)

Interceptor just implements `io.grpc.ServerInterceptor` interface, so you could add it with `io.grpc.ServerBuilder` for example

    ServerBuilder builder = ...
    builder.intercept(new SentryServerInterceptor()).build()

Spring boot starter
-----------------

To use with Spring Boot add following dependency to your build.gradle    

    implementation("ru.m2:sentry-grpc-spring-boot-starter:1.0.0")

Then you could use one of the standard [sentry configuration way](https://docs.sentry.io/platforms/java/configuration/#setting-the-dsn)

And then - relax

Starter based on [grpc-spring-boot-starter](https://github.com/LogNet/grpc-spring-boot-starter)