plugins {
    kotlin("jvm") version Version.kotlin
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":sentry-grpc-interceptor"))

    api(platform("io.sentry:sentry-bom:${Version.sentry}"))
    api(platform("org.springframework.boot:spring-boot-dependencies:${Version.springBoot}"))

    api("io.github.lognet:grpc-spring-boot-starter:${Version.lognetGrpcSpring}")
    api("org.springframework.boot:spring-boot-starter")
    api("io.sentry:sentry-logback")
}
