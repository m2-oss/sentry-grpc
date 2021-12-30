import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("nebula.release") version "16.0.0"
    id("org.jetbrains.dokka") version ("1.6.10")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "ru.m2"

    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.dokka")

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
        withType<DokkaTask> {
            dokkaSourceSets {
                create("main") {
                    sourceRoots.from(file("src"))
                    includeNonPublic.set(true)
                }
            }
        }
        named<Jar>("javadocJar") {
            from(project.tasks.getByName<DokkaTask>("dokkaJavadoc").outputDirectory)
            dependsOn("dokkaJavadoc")
        }
    }

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>(name) {
                    from(components["java"])
                    pom {
                        name.set("Sentry gRPC integration")
                        description.set("Sends gRPC calls to sentry")
                        url.set("https://github.com/m2-oss")
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
                        developers {
                            developer {
                                name.set("Andrew Perepelkin")
                                email.set("perepelkin.work@gmail.com")
                                organization.set("M2")
                                organizationUrl.set("https://m2.ru")
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
            if (signingKey != null && signingPassword != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            }
            sign(extensions.getByType<PublishingExtension>().publications[name])
        }
    }
}