import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

plugins {
    id("java")
    id("maven-publish")
    id("com.jfrog.artifactory") version "6.0.0+"
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
    }
}


configure<ArtifactoryPluginConvention> {
    val hytaleModdingArtifactoryContextUrl = "https://maven.hytalemodding.guide/artifactory"
    // Artifactory publishing configuration
    val hytaleModdingArtifactoryUser: String? =
        providers.gradleProperty("hytaleModdingArtifactoryUsername").getOrNull()
            ?: System.getenv("HYTALE_MODDING_ARTIFACTORY_USERNAME")
    val hytaleModdingArtifactoryPassword: String? =
        providers.gradleProperty("hytaleModdingArtifactoryPassword").getOrNull()
            ?: System.getenv("HYTALE_MODDING_ARTIFACTORY_PASSWORD")

    val hytaleModdingPublishRepoKey = if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
    
    setContextUrl(hytaleModdingArtifactoryContextUrl)

    publish {
        repository {
            
            setRepoKey(hytaleModdingPublishRepoKey)

            // Use username/password authentication only (token support removed)
            setUsername(hytaleModdingArtifactoryUser)
            setPassword(hytaleModdingArtifactoryPassword)

            // Use Maven layout (no need to call setMavenCompatible, handled by default)
        }

        defaults {
            publications("mavenJava")
            setPublishArtifacts(true)
            setPublishPom(true)
            // Do not publish Ivy descriptors
            setPublishIvy(false)
            // Disable build info publishing to avoid 403 permission errors
            setPublishBuildInfo(false)
        }
    }
}

// Ensure the artifactoryPublish task picks up the Gradle publication
tasks.named<ArtifactoryTask>("artifactoryPublish") {
    publications(publishing.publications["mavenJava"])
}
