import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("java")
    `maven-publish`
}

group = "com.shailist"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

// Use default javadoc doclint settings (strict) so we can surface missing tags and improve docs.

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "transfer-api"
        }
    }

    repositories {
        // Publish to local maven repository by default; users can add other repositories later.
        mavenLocal()
    }
}
