plugins {
    kotlin("jvm") version Version.kotlin
}

dependencies {
    implementation(kotlin("stdlib"))

    api(platform("io.sentry:sentry-bom:${Version.sentry}"))
    api(platform("io.grpc:grpc-bom:${Version.grpc}"))

    api("io.sentry:sentry")
    api("com.google.guava:guava:31.0.1-jre")

    api("io.grpc:grpc-api")
    api("io.grpc:grpc-core")
    api("org.slf4j:slf4j-api:1.7.32")

}