plugins {
    id("net.fabricmc.fabric-loom")
    id("com.gradleup.shadow") version "9.3.0"
    `java-library`
    `maven-publish`
}

version = parent!!.version
group = parent!!.group

base {
    archivesName = "mod-api-fabric"
}

val bundled by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    compileOnly("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    compileOnly("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    compileOnly(libs.jspecify)

    compileOnly(parent!!)
    bundled(parent!!)
}

tasks.processResources {
    val version = version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

tasks.withType<Javadoc>().configureEach {
    exclude("**/internal/**")
    (options as StandardJavadocDocletOptions).memberLevel = JavadocMemberLevel.PUBLIC
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

tasks.shadowJar {
    configurations = listOf(bundled)
    archiveClassifier = "dev-shadow"
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

val protocolSources = parent!!.tasks.named<Jar>("sourcesJar")

tasks.named<Jar>("sourcesJar") {
    dependsOn(protocolSources)
    from(protocolSources.flatMap { it.archiveFile }.map { zipTree(it) })
}

tasks.jar {
    val projectName = project.name
    inputs.property("projectName", projectName)

    from(rootProject.file("LICENSE")) {
        rename { "${it}_$projectName" }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "mod-api-fabric"
            from(components["java"])

            pom {
                name = "DiamondFire ModAPI Fabric"
                description = "High-level Fabric client API for DiamondFire's ModAPI."
                url = "https://github.com/MCDiamondFire/ModAPI/"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/license/mit/"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "mcdiamondfire"
                        name = "MCDiamondFire"
                        url = "https://github.com/MCDiamondFire/"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/MCDiamondFire/ModAPI.git"
                    developerConnection = "scm:git:ssh://git@github.com/MCDiamondFire/ModAPI.git"
                    url = "https://github.com/MCDiamondFire/ModAPI/"
                }
            }
        }
    }

    repositories {
    }
}
