import com.google.protobuf.gradle.id

plugins {
	`java-library`
	alias(libs.plugins.protobuf)
    alias(libs.plugins.maven.publish)
}

group = project.property("GROUP").toString()
version = project.property("VERSION_NAME").toString()
val artifactId = project.property("POM_ARTIFACT_ID").toString()

base {
    archivesName.set(artifactId)
}

repositories {
	mavenCentral()
}

dependencies {
	api(libs.protobuf.java)

	compileOnly(libs.jspecify)
}

protobuf {
	// https://github.com/google/protobuf-gradle-plugin/blob/master/examples/exampleKotlinDslProject/build.gradle.kts
	protoc {
		artifact = libs.protoc.artifact.get().toString()
	}
	plugins {
		id("doc") {
			artifact = libs.protoc.gen.doc.artifact.get().toString()
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				id("doc") {
					option("markdown,docs/api/proto.md")
				}
			}
		}
	}
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(25)
	withJavadocJar()
	withSourcesJar()
}

tasks.withType<Jar> {
    // Exclude proto files.
    exclude("**/*.proto")
    includeEmptyDirs = false
}

tasks.matching { it.name == "plainJavadocJar" }.configureEach {
    enabled = false
}

tasks.withType<Javadoc> {
    options {
        val options = this as StandardJavadocDocletOptions
        options.addStringOption("Xdoclint:none", "-quiet") // Protobuf generates DocLint warnings.
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), artifactId, version.toString())
}
