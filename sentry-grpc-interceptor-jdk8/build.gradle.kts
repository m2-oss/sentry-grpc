dependencies {
    implementation(
        project(":sentry-grpc-interceptor")
    )
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
