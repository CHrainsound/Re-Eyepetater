pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 审查修复 P1-2：移除 jitpack 与 aliyun 镜像——当前依赖（含 GSYVideoPlayer）
        // 均已验证存在于 Maven Central / Google Maven，无需按需编译源与第三方镜像。
        // 若未来确有依赖仅在 jitpack 发布，请按 group 收敛添加，例如：
        // maven { url = uri("https://jitpack.io"); content { includeGroup("com.example.only.jitpack") } }
    }
}
plugins {

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 同 P1-2：不再开放 jitpack（依赖来源收敛为 google + mavenCentral）。
        // 需要时按上面的注释按 group 收敛恢复。
    }
}

rootProject.name = "Eyepetater"
include(":app")
include(":ept_home")
include(":ept_daily")
include(":ept_hot")
include(":ept_dicover")
include(":core")
include(":core:core_model")
include(":core:core_network")
include(":core:core_common")
include(":core:core_media")
include(":ept_search")
include(":ept_category")
include(":ept_notify")
include(":ept_person")
