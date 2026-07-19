dependencies {
    implementation(project(":gatotauth-api"))
    implementation(project(":gatotauth-core"))
    implementation(project(":gatotauth-protocol"))
    compileOnly(libs.paper.api)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
