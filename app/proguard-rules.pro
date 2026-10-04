# ==================== 通用属性 ====================
-keepattributes Signature
-keepattributes *Annotation*

# ==================== Gson 序列化模型（精确到包，不要扩大到整个 App） ====================
-keep class com.example.core.model.** { *; }

# ==================== Retrofit 接口 ====================
-keep class com.example.core.network.api.** { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.Unit
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp / Okio（无需 keep 类，官方仅需 dontwarn） ====================
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn okio.**

# ==================== Glide（入口规则；其余由 AAR 内 consumer rules 提供） ====================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ==================== TheRouter（库体积小，保留注解与生成类，避免误伤路由） ====================
-keep @com.therouter.router.Route class * { *; }
-keep class com.therouter.** { *; }

# ==================== GSYVideoPlayer / IJK（播放器内部反射较多，保守保留） ====================
-keep class com.shuyu.gsyvideoplayer.** { *; }
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

# ==================== 协程 ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ============================================================
# 审查修复（P2-9）：删除以下原"大包大揽"规则——它们让整个 App 代码与
# 多个库免于混淆/裁剪，R8 的 isMinifyEnabled + isShrinkResources 收益被完全抵消：
#   -keep class com.example.ept.dicover.** { *; }（及各 ept_*、core.network 共 8 条）
#   -keep class okhttp3.** { *; } / -keep interface okhttp3.** { *; }
#   -keep class com.bumptech.glide.** { *; }
#   -keep class * implements com.therouter.router.Route { *; }（Route 是注解，原规则无效）
# ============================================================
# 注意：修复后必须执行 ./gradlew :app:assembleRelease 并做核心链路冒烟
# （首页/日报/播放/搜索/个人页），如出现 NoSuchMethod/ClassNotFound 再按需精确补规则。
