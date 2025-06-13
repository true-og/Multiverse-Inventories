import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("eclipse")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.6"
}

version = System.getenv("GITHUB_VERSION") ?: "1.19.4"
group = "com.onarandombox.multiverseinventories"
description = "Multiverse-Inventories"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.onarandombox.com/content/groups/public") { name = "onarandombox" }
    maven("https://papermc.io/repo/repository/maven-public/") { name = "papermc" }
    maven("https://jitpack.io/") { name = "jitpack.io" }
}

dependencies {
    implementation("org.bukkit:bukkit:1.14.4-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    implementation("com.onarandombox.multiversecore:Multiverse-Core:4.2.2") {
        exclude("me.main__.util", "SerializationConfig")
    }
    api("com.dumptruckman.minecraft:JsonConfiguration:1.1")
    api("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude("junit", "junit")
    }
    api("io.papermc:paperlib:1.0.7")
    api("com.dumptruckman.minecraft:Logging:1.1.1") {
        exclude("junit", "junit")
    }
    implementation("uk.co:MultiInv:3.0.6") { exclude("*", "*") }
    implementation("me.drayshak:WorldInventories:1.0.2") { exclude("*", "*") }
    implementation("com.onarandombox.multiverseadventure:Multiverse-Adventure:2.5.0-SNAPSHOT") { exclude("*", "*") }
    testImplementation("com.github.MilkBowl:VaultAPI:1.7.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.11.2")
}

tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
tasks.withType<Javadoc>().configureEach { options.encoding = "UTF-8" }

val shadowApi = configurations.create("shadowApi") {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.getByName("api"))
}

configurations {
    named("apiElements") {
        outgoing.artifacts.removeIf { it.file.name.endsWith(".jar") }
        outgoing.artifact(tasks.named("shadowJar"))
    }
    named("runtimeElements") {
        outgoing.artifacts.removeIf { it.file.name.endsWith(".jar") }
        outgoing.artifact(tasks.named("shadowJar"))
    }
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(props) }
    outputs.upToDateWhen { false }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowApi)
    relocate("com.dumptruckman.minecraft.util.Logging", "com.onarandombox.multiverseinventories.utils.InvLogging")
    relocate("com.dumptruckman.minecraft.util.DebugLog", "com.onarandombox.multiverseinventories.utils.DebugFileLogger")
    relocate("com.dumptruckman.bukkit.configuration", "com.onarandombox.multiverseinventories.utils.configuration")
    relocate("io.papermc.lib", "com.onarandombox.multiverseinventories.utils.paperlib")
    relocate("net.minidev.json", "com.onarandombox.multiverseinventories.utils.json")
    archiveFileName.set("${archiveBaseName.get()}-${project.version}.${archiveExtension.get()}")
}

tasks.named<Jar>("jar") { enabled = false }
tasks.named("build") { dependsOn("shadowJar") }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Multiverse/Multiverse-Inventories")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

