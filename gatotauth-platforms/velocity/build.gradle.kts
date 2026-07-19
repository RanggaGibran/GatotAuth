dependencies {
    implementation(project(":gatotauth-api"))
    implementation(project(":gatotauth-core"))
    implementation(project(":gatotauth-protocol"))
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}
