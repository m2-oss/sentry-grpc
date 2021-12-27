

subprojects {
    group = "ru.m2"
    version = "1.0-SNAPSHOT"

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    repositories {
        mavenCentral()
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    afterEvaluate {
        extensions.configure<PublishingExtension>() {
            publications {
                create<MavenPublication>(name) {
                    from(components["kotlin"])
                    pom {
                        name.set("Sentry gRPC integration")
                        scm {
                            connection.set("git@github.com:m2-oss/sentry-grpc.git")
                            developerConnection.set("git@github.com:m2-oss/sentry-grpc.git")
                            url.set("https://github.com/m2-oss/sentry-grpc")
                        }
                        contributors {
                            contributor {
                                name.set("Andrew Perepelkin")
                                email.set("perepelkin.work@gmail.com")
                                url.set("https://github.com/aperepelkin")
                            }
                            contributor {
                                name.set("Maxim Gorelikov")
                                email.set("GorelikovMV@m2.ru")
                                url.set("https://github.com/gorelikov")
                            }
                        }
                        licenses {
                            license{
                                name.set("MIT")
                                url.set("https://www.opensource.org/licenses/mit-license")
                            }
                        }
                    }
                }
            }

            repositories {
                maven {
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = System.getenv("OSSRH_USERNAME")
                        password = System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
        }
    }
}