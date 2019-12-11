import com.jfrog.bintray.gradle.BintrayExtension
import org.javamodularity.moduleplugin.tasks.TestModuleOptions

plugins {
    // determine version from git tags, follow semver spec
    id("org.ajoberstar.reckon").version("0.12.0")
    // differentiate transient from implementation dependencies
    `java-library`
    // manage JavaFx modules
    id("org.openjfx.javafxplugin").version("0.0.8")
    // publishing plugins
    `maven-publish`
    id("com.jfrog.bintray").version("1.8.4")
    id("com.jfrog.artifactory").version("4.11.0")
}

group = "io.reactivex.rxjava2"
description = "RxJava extensions for JavaFX"

reckon {
    scopeFromProp()
    stageFromProp("beta", "rc", "final")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    // tasks and publication artifacts for sources and javadoc
    withJavadocJar()
    withSourcesJar()
}

javafx {
    modules = listOf("javafx.controls", "javafx.graphics")
    version = "11"
    configuration = "compileOnly"
}

configurations {
    testImplementation.extendsFrom(compileOnly.get())
}

repositories {
    jcenter()
}

dependencies {
    api("io.reactivex.rxjava2:rxjava:2.2.4")
    val junit4Version = "4.12"
    val junitBomVersion = "5.5.2"
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("junit:junit:$junit4Version")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("org.mockito:mockito-core:3.2.0")
}

tasks {
    test {
        useJUnitPlatform {
            includeEngines("junit-vintage")
        }
        extensions.configure(TestModuleOptions::class) {
            runOnClasspath = true
        }
    }
    javadoc {
        source = sourceSets.main.get().allJava
        classpath = configurations.compileClasspath.get()
        options(closureOf<StandardJavadocDocletOptions> {
            memberLevel = JavadocMemberLevel.PUBLIC
            setAuthor(true)
            links("http://docs.oracle.com/javase/7/docs/api/")
        })
    }
    bintrayUpload {
        onlyIf {
            if (project.hasProperty("bintray_user") && project.hasProperty("bintray_api_key")) {
                true
                } else {
                    println("Sorry, Bintray credentials are not available. Interrupting upload")
                    false
                }
        }
    }
}

val pkg_name: String by project
val github_org: String by project
val github_repo: String by project
val vcsUrl = "https://github.com/$github_org/$github_repo"
val homepage = vcsUrl
publishing {
    publications {
        register("rxjavafx", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set(pkg_name)
                description.set(project.description)
                url.set(homepage)
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("thomasnield")
                        name.set("Thomas Nield")
                        email.set("thomasnield@live.com")
                        organization.set("ReactiveX")
                        organizationUrl.set("http://reactivex.io/")
                    }
                    }
                    scm {
                        url.set(vcsUrl)
                        connection.set("scm:git:git://github.com/$github_org/$github_repo.git")
                        developerConnection.set("scm:git:ssh://github.com/$github_org/$github_repo.git")
                        }
                        issueManagement {
                            system.set("github")
                            url.set("$vcsUrl/issues")
                            }
                            }
                            }
                        }
}

bintray {
    user = project.findProperty("bintray_user") as String?
    key = project.findProperty("bintray_api_key") as String?
    setPublications("rxjavafx")
    publish = true
    pkg(closureOf<BintrayExtension.PackageConfig> {
        userOrg = project.findProperty("bintray_org") as String?
        repo = project.findProperty("bintray_repo") as String?
        name = pkg_name
        desc = project.description
        websiteUrl = homepage
        vcsUrl = vcsUrl
        issueTrackerUrl = "$vcsUrl/issues"
        setLabels("rxjava", "reactivex")
        setLicenses("Apache-2.0")
        version(closureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"
            vcsTag = "${project.version}"
            /*
            gpg(closureOf<BintrayExtension.GpgConfig> {
                sign = true
            })
            mavenCentralSync(closureOf<BintrayExtension.MavenCentralSyncConfig> {
                sync = true
                user = project.findProperty("sonatypeUsername") as String?
                password = project.findProperty("sonatypePassword") as String?
                close = "1"
                })
            */
        })
    })
}
