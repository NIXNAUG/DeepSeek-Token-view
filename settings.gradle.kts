pluginManagement {
    repositories {
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google/")
        }
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugin/")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google/")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "DeepSeekBalance"
include(":app")