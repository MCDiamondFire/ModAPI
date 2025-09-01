import com.google.protobuf.gradle.id

plugins {
	id("java")
	id("com.google.protobuf") version "0.9.5"
	id("maven-publish")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.protobuf:protobuf-java:4.32.0")
	implementation("com.google.protobuf:protobuf-java-util:4.32.0")
	implementation("com.google.code.gson:gson:2.9.1")

	implementation("org.jspecify:jspecify:1.0.0")
}

protobuf {
	// https://github.com/google/protobuf-gradle-plugin/blob/master/examples/exampleKotlinDslProject/build.gradle.kts
	protoc {
		artifact = "com.google.protobuf:protoc:4.32.0"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.74.0"
		}
		id("doc") {
			artifact = "io.github.pseudomuto:protoc-gen-doc:1.5.1"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				id("grpc") { }
				id("doc") {
					option("markdown,docs/api/proto.md")
				}
			}
		}
	}
}

java {
	withJavadocJar()
	withSourcesJar()
}

publishing {
	publications {
		create<MavenPublication>("proto") {
			from(components["java"])
			groupId = "com.mcdiamondfire"
			artifactId = "proto"
			version = "1.0.0"
		}
	}
}
