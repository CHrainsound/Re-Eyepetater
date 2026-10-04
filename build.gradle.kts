// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
}

// 审查修复 P2-2：删除原 subprojects.eachDependency 强制降级块
// （它把一个没有任何模块引用的 lifecycle-runtime-compose-android 钉在 2.8.7，
//  属于反模式，且会掩盖真实版本冲突信号）。lifecycle 系列已在版本目录统一为 2.11.0。

// 依赖锁定（P1-1）：在本地构建环境执行一次生成并提交 lockfile，即可打开锁定。
// 注意：未生成 lockfile 前开启将导致构建失败，因此这里仅给出就绪片段：
//
// subprojects {
//     dependencyLocking {
//         lockAllConfigurations()
//     }
// }
//
// 生成命令（需 JDK 21 + Android SDK）：
//   ./gradlew --write-locks
//   ./gradlew --write-verification-metadata pgp,sha256 help   // 校验元数据
//   ./gradlew :app:dependencyUpdates                        // 版本滞后报告
