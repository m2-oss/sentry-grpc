

subprojects {
    group = "ru.m2"
    version = "1.0-SNAPSHOT"

    apply(plugin = "maven-publish")

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
                }
            }

            repositories {
                maven {
                    url = uri("https://nexus.m2.ru/repository/maven-snapshots/")
                }
            }
        }
    }
}