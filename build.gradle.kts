plugins {
    alias(libs.plugins.spotless) apply false
}

allprojects {
    group = "dev.gatotauth"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")
    apply(plugin = "com.diffplug.spotless")

    configure<org.gradle.api.plugins.JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            importOrder()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    configure<org.gradle.api.plugins.quality.CheckstyleExtension> {
        toolVersion = "10.18.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = false
        maxWarnings = 0
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        "testImplementation"(libs.findLibrary("junit-jupiter-api").get())
        "testRuntimeOnly"(libs.findLibrary("junit-jupiter-engine").get())
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "testImplementation"(libs.findLibrary("assertj-core").get())
        "testImplementation"(libs.findLibrary("mockito-core").get())
        "testImplementation"(libs.findLibrary("mockito-junit-jupiter").get())
        "testImplementation"(libs.findLibrary("testcontainers").get())
        "testImplementation"(libs.findLibrary("testcontainers-junit-jupiter").get())
    }

    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
