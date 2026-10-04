# AGENTS.md

Eyepetater（豆眼）—— 开眼（Eyepetizer）Android 客户端克隆。Kotlin + MVVM(LiveData) + XML 视图，多模块 Gradle 工程。

## 构建与校验命令

- 统一用 wrapper：Windows `.\gradlew.bat`，其他平台 `./gradlew`。不要用本机 gradle。
- **必须 JDK 21**：`gradle/gradle-daemon-jvm.properties` 锁定 `toolchainVersion=21`，CI 用 Temurin 21。
- 构建：`./gradlew :app:assembleDebug`（日常）/ `./gradlew :app:assembleRelease`（CI 的真实门禁，等价于 R8 + 资源压缩）。
- 单模块：`./gradlew :ept_home:assembleDebug`。
- **没有 lint / typecheck / 格式化任务**，也没有 ktlint / detekt / spotless。别去找 `checkAll` 之类。
- 测试形同虚设：全仓库只有 AS 模板生成的 `ExampleUnitTest` / `ExampleInstrumentedTest`，CI 根本不跑测试。`./gradlew test` 能过，但没有价值。

### CI 现状

`.github/workflows/android-ci.yml`，触发条件是 push 到 `main` 或 `fix/**`，以及所有 PR。

- `:app:dependencyUpdates` 这一步**必然失败**：全仓库没有 apply `com.github.ben-manes.versions` 插件，任务不存在。这是既有问题，不是你引入的回归。
- 构建门禁看 `:app:assembleRelease` 是否通过。

## 模块拓扑

`settings.gradle.kts` 注册 14 个模块：

- `:app` —— 唯一 application，`namespace = com.example.eyepetater`，`applicationId = com.scdomm.eyepetater`（不一致是有意为之，见该文件注释）
- `:core:core_model` —— Gson 数据模型 + `NavPaths`（全仓路由常量唯一来源）
- `:core:core_network` —— `RetrofitClient`、三个 API 接口、`Call<T>.await()`
- `:core:core_common` —— `parseXxxUtils` 解析函数、SharedPreferences 工具
- `:core:core_media` —— `VideoPlayerActivity/Fragment`、GSY 播放器、相关推荐
- 功能模块：`ept_home`、`ept_daily`、`ept_dicover`、`ept_category`、`ept_search`、`ept_hot`、`ept_notify`、`ept_person`

**坑点：**

- `core/` 目录本身**不是** Gradle 模块。它有残留的 `build.gradle.kts` + 模板 `src/`，但 `settings.gradle.kts` 没有 `include(":core")`，所以 `./gradlew :core:assemble` 会失败。`.idea/gradle.xml` 里也还列着它（陈旧 IDE 状态）。这是死代码，不要"修复"它。
- `ept_dicover` / `com.example.ept.dicover` 是拼写错误（应为 discover），已被全仓引用，重命名成本极高，别动。
- 模块间依赖一律用 `implementation` 而非 `api`，**类型不传递**。用了哪个 core 模块的类型，就必须在当前模块 `build.gradle.kts` 里显式声明（`app` 因此显式声明了 `ept_category`、`ept_person`、`ept_hot`）。
- 依赖方向：`ept_daily → ept_search → {ept_person, ept_hot}`、`ept_home → ept_notify`、`ept_dicover → ept_category`。新功能先想清楚该落在哪个已有模块里，通常不需要新建模块。

## 工具链怪癖（改构建脚本前必读）

- **没有 apply `org.jetbrains.kotlin.android`**：AGP 9 内置 Kotlin 负责编译，只有 `:app` apply 了 `kotlin.compose`（Compose 编译器插件）。不要新增 Kotlin Android 插件，会和内置支持冲突。
- `compileSdk` 用的是 AGP 9 新语法：`compileSdk { version = release(36) { minorApiLevel = 1 } }`。照抄这个写法。
- Java/Kotlin 目标版本 **11**（各模块 `compileOptions`）。全仓库没有 `jvmTarget` / `kotlinOptions` / `jvmToolchain` 块，别随手加。
- `RepositoriesMode.FAIL_ON_PROJECT_REPOS`：**禁止**在任何模块的 `build.gradle.kts` 里写 `repositories {}`，只能改 `settings.gradle.kts`。
- 仓库源已收敛为 `google()` + `mavenCentral()`，jitpack 和 aliyun 镜像被**故意移除**（见 `settings.gradle.kts` 注释）。确实需要 jitpack 时按 group 收敛添加。
- 版本只在 `gradle/libs.versions.toml` 里改，模块中一律 `libs.xxx` 引用，不要写直接坐标。
- **TheRouter（KSP）**：新增 `@Route` 的模块必须同时具备 `implementation(libs.therouter.api)` + `ksp(libs.therouter.compiler)` + `ksp { arg("therouter.moduleName", ":模块名") }`。漏掉 compiler 时 `@Route` **静默不注册**，运行时才崩。CI 有 shell 检查兜底，但本地不会报错。
- Glide 注解处理器统一走 KSP（`ksp(libs.glide.compiler)`），已弃用 `annotationProcessor`。
- `org.gradle.configuration-cache=true` + `org.gradle.parallel=true` 已开启。新写的构建脚本逻辑必须兼容 configuration cache（不要在配置期做 IO / 读环境状态）。

## 密钥与签名

- `secrets.properties`（已 gitignore，模板 `secrets.properties.example`）由 `core_network/build.gradle.kts` 读取，注入成 `com.example.core.network.BuildConfig` 字段 → `NetworkConfig` 读取 → `RetrofitClient` 的 OkHttp 拦截器附加为请求头。
- **值留空则对应请求头被省略，开眼 API 全部失败**，页面会是空的。全新 clone 必须先填这个文件才能看到数据。调试时先确认它存在。
- 历史上凭据曾提交到公开仓库，已泄露；不要把任何真实值写回源码或文档。
- `keystore.properties` 可选：没有则 release 未签名（普通构建不受影响），有则挂上 `signingConfig release`。`.jks` 同样 gitignore。
- `HttpLoggingInterceptor` 仅 debug 为 `BODY`，release 固定 `NONE`；新增网络代码不要绕过这个。
- `network_security_config.xml` 全局禁明文，只对 `img/ali-img/cdn.kaiyanapp.com` 开了窄口子。新增明文域名需求前先想清楚。
- API 参数来自对开眼客户端的逆向抓包，属非官方 API。两个 README 都写明：**分发/上架前必须取得授权或替换为自有/公开 API**。改动这块时不要帮忙"洗白"来源。

## 架构与编码约定

MVVM + LiveData + 每个 ViewModel 同文件底部的 sealed state：

```kotlin
private var _liveData = MutableLiveData<XxxState>()
val liveData: LiveData<XxxState> get() = _liveData
// 文件底部：
sealed class XxxState {
    data class RefreshState(...) : XxxState()
    data class LoadingMoreState(...) : XxxState()
    data class ErrorState(val errorMsg: String) : XxxState()
}
```

全仓约 26 处这种写法（`HomeState` / `DailyState` / `FeedState` / `TopicDetailState` …）。新页面照此办理。

- ViewModel 需要 Context 时继承 `AndroidViewModel(application)`，否则用 `ViewModel`；View 侧统一 `ViewModelProvider(this)[XxxViewModel::class.java]`。
- **视图层是 XML + `findViewById`，没有 ViewBinding / DataBinding**（全仓未启用）。`:app` 虽然 `buildFeatures { compose = true }` 且有 `ui/theme/{Theme,Color}.kt`，但**仓库里没有任何 Compose UI**，那只是模板残留。不要新增 Compose 代码。
- 分页约定：`RecyclerView.OnScrollListener` 里 `itemCount - lastVisible - 1 <= 6` 触发 `loadingMore()`，配合 `isLoading` 防重入。

### 两套并存的接口/解析风格（新增接口前必须选对）

1. **强类型**：接口直接返回 `ApiResponse<PageResult>` 等，模型是 camelCase + `@SerializedName`；再用 `:core:core_common` 的 `parseVideosFromCardList` / `parseWorkListFromCardList` / `parseSearch` / `parseUserItemFromCard` / `parseAlbumCards` 解析。`ept_home`、`ept_person`、`ept_hot` 走这条。
2. **原始 body**：`SpecficApi` 部分接口返回 `okhttp3.ResponseBody`，ViewModel 里 `RetrofitClient.gson.fromJson(rawBody, GetPageResponse::class.java)` 手工拆，模型是**下划线命名的 Kotlin 字段名、不用注解**（见 `GetPageResponse.kt`）。`ept_dicover` 走这条。

两种风格在同一 model 文件里都存在（`ApiResponseModels.kt` 两种都有）。**改模型前先确认目标屏幕属于哪一套**，别把下划线字段加到 camelCase 模型上，反之亦然。`core_model/utils/ParamsUtils.kt` 的 `safeString` 用于 map 取值兜底。

### 跨模块跳转只有两条路

- 三个页面走 TheRouter，路径常量必须在 `core_model/NavPaths.kt` 登记（`NOTIFY` / `SEARCH` / `PERSON`），调用 `TheRouter.build(NavPaths.X).navigation(...)`。**禁止散落魔法字符串。**
- 视频播放**不走路由**：`VideoPlayerActivity.start(context, videoId)` 或 `start(context, videoId, resourceType)`，来自 `:core:core_media`。
- 其余跨模块页面直接 Intent，且必须在某个 manifest 里注册。

### Manifest 注册位置不统一（两种都有效，靠 merger 合并）

- 库模块自己的 manifest：`SearchActivity`(ept_search)、`PersonActivity`(ept_person)、`NotifyActivity`(ept_notify)、`VideoPlayerActivity`(core_media)
- `app/src/main/AndroidManifest.xml`：`CategoryDetailActivity`、`HotActivity`、以及 `ept_dicover` 的 4 个 Activity（都带 `@style/Theme.Eyepetater`）

新增 Activity 时挑一种、**不要在两处重复声明**。

权限同理：`INTERNET` / `ACCESS_NETWORK_STATE` 声明在 `ept_home`、`ept_daily`、`ept_search`、`ept_person`、`ept_notify` 的 manifest 里，`app` 自己的 manifest **一个权限都没声明**，全靠 merger。新模块需要权限时按现有模块的写法加。

### 深色模式

每个模块各自维护 `res/values/colors.xml` + `res/values-night/colors.xml`，主题是 `Theme.Eyepetater`（`Theme.MaterialComponents.DayNight.NoActionBar`）。新增颜色资源要成对添加，否则夜间模式回退成亮色。

## R8 / release

- release 开启 `isMinifyEnabled` + `isShrinkResources`，规则在 `app/proguard-rules.pro`。
- 已刻意收窄 keep 规则（Gson 模型只 keep `com.example.core.model.**`、Retrofit API、Glide 入口、TheRouter 注解与生成类、GSY/IJK 保守 keep）。**新增 Gson 模型只要在 `com.example.core.model` 包下就自动被 keep**；放到别处必须补规则。
- 改完混淆相关的东西必须跑 `./gradlew :app:assembleRelease` 并冒烟核心链路（首页/日报/播放/搜索/个人页），`proguard-rules.pro` 末尾有同样的告诫。

## 其他

- 提交信息沿用最近的 Conventional Commits 风格：`fix(build): 摘要` / `refactor(deps): 摘要` / `harden(build): 摘要`。更早的历史用 gitmoji（`:art:`、`:bug:`），不要再用了。
- 代码注释、README、构建脚本里的说明文字都是中文；新写的注释跟随所在文件语言。
- `README-CHrainsound.md` 和 `README-SCDOMM.md` 是两位原作者的功能/结构文档，**内容已部分过时**（例如 SCDOMM 那份仍写着把凭据放进 `NetworkConfig`，实际已改为 `secrets.properties` 注入）。当历史资料读，冲突时以代码为准。
- 所有 build 脚本和源码注释里散布着 `审查修复 P1-x` / `P2-x` 标记，指向一次外部安全/构建审查。改动相关区域前先读那些注释，它们说明了为什么现在是这个写法。
