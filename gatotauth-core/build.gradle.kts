dependencies {
    implementation(project(":gatotauth-api"))
    implementation(libs.hikaricp)
    implementation(libs.sqlite.jdbc)
    implementation(libs.jbcrypt)
}
