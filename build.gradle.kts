plugins {
    kotlin("js") version "1.4.32"
}

group = "com.xemantic.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://dl.bintray.com/openrndr/openrndr") {
        metadataSources {
            gradleMetadata()
        }
    }
}

dependencies {
    testImplementation(kotlin("test-js"))
    testImplementation("io.kotest:kotest-assertions-core:4.4.3")
    implementation("org.openrndr:openrndr-math:0.3.47-rc.6")
}

kotlin {
    js {
        browser {
            @Suppress("EXPERIMENTAL_API_USAGE")
            distribution {
                directory = file("$projectDir/docs/")
            }
            binaries.executable()
        }
    }
}
