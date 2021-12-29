plugins {
    id("nebula.release") version "16.0.0"
}

subprojects {
    group = "ru.m2"

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
        extensions.configure<PublishingExtension> {
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
                            license {
                                name.set("MIT")
                                url.set("https://www.opensource.org/licenses/mit-license")
                            }
                        }
                    }
                }
            }

            repositories {
                val mavenRepoUrl: String? by project
                val mavenRepoUsername: String? by project
                val mavenRepoPassword: String? by project

                if (mavenRepoUrl != null) {
                    maven {
                        url = uri(mavenRepoUrl!!)
                        if (mavenRepoPassword != null && mavenRepoUsername != null)
                            credentials {
                                username = mavenRepoUsername
                                password = mavenRepoPassword
                            }
                    }
                }
            }
        }

        extensions.configure<SigningExtension> {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications[name])
        }
    }
}