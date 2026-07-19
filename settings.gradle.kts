rootProject.name = "gatotauth"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(
    "gatotauth-api",
    "gatotauth-core",
    "gatotauth-common",
    "gatotauth-protocol",
    "gatotauth-platforms:velocity",
    "gatotauth-platforms:paper",
    "gatotauth-platforms:proxy-agent",
    "gatotauth-loaders:velocity-loader",
    "gatotauth-loaders:paper-loader"
)
