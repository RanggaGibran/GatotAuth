dependencies {
    implementation(project(":gatotauth-platforms:paper"))
    implementation(project(":gatotauth-platforms:proxy-agent"))
    compileOnly(libs.paper.api)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
