plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "met.justlime"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven { url = uri("https://jitpack.io") }
    maven("https://repo.extendedclip.com/releases/") { name = "extendedclip" }

}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.ItzYashvardhan:RedeemCodeX-API:1.0.0")
    implementation("net.dv8tion:JDA:5.6.1")
}

val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)
}
tasks.shadowJar {
    minimize()
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
// Task to copy the jar to the server plugins folder
tasks.register<Copy>("copyToServerPlugins") {
    dependsOn("shadowJar")  // Ensure shadowJar completes before copying
    from(layout.buildDirectory.file("libs/${project.name}-${project.version}-all.jar"))  // Correct output file path
    into("E:/Minecraft/servers/Plugin-Maker/plugins")  // Destination folder
}

// Combined task to build and copy
tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServerPlugins")
}
