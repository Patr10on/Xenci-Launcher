pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.protobuf") {
                useModule("com.google.protobuf:protobuf-gradle-plugin:0.8.19")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven("../../../prebuilts/sdk/current/androidx/m2repository")
        maven("../../../prebuilts/fullsdk-darwin/extras/android/m2repository")
        maven("../../../prebuilts/fullsdk-linux/extras/android/m2repository")
        maven(url = "$rootDir/prebuilts/m2")
        maven(url = "https://jitpack.io")
    }
}
include(":iconloaderlib")
project(":iconloaderlib").projectDir =
    File(rootDir, "platform_frameworks_libs_systemui/iconloaderlib")

include(":searchuilib")
project(":searchuilib").projectDir = File(rootDir, "platform_frameworks_libs_systemui/searchuilib")

include(":SystemUIShared")

include(":CompatLib")
include(":CompatLibVR")
include(":CompatLibVS")
rootProject.name = "Xenci Launcher"