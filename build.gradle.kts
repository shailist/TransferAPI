import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("java")
    id("maven-publish")
}

group = "com.shailist.hytale"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2-1")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("com.google.guava:guava:33.5.0-jre")

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

// Register common custom Javadoc tags so we can use tags such as @apiNote and @implSpec
tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("tag", "apiNote:a:API Note:")
    (options as StandardJavadocDocletOptions).addStringOption("tag", "implNote:a:Implementation Note:")
    (options as StandardJavadocDocletOptions).addStringOption("tag", "implSpec:a:Implementation Specification:")
}

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

        // Publish to HytaleModding repository. The repository path will
        // switch between snapshots and releases depending on the project version.
        maven {
            val hytaleModdingBase = "https://maven.hytalemodding.xyz"
            val hytaleModdingRepo = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
            url = uri("$hytaleModdingBase/repository/$hytaleModdingRepo/")

            // Credentials can be provided in `~/.gradle/gradle.properties` or via
            // environment variables `HYTALEMODDING_USERNAME` and `HYTALEMODDING_PASSWORD`.
            val hytaleModdingUser: String? = findProperty("hytaleModdingUsername") as String? ?: System.getenv("HYTALEMODDING_USERNAME")
            val hytaleModdingPass: String? = findProperty("hytaleModdingPassword") as String? ?: System.getenv("HYTALEMODDING_PASSWORD")

            credentials {
                username = hytaleModdingUser
                password = hytaleModdingPass
            }
        }
    }
}
