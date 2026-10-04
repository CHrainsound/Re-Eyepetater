package com.example.core.network

/**
 * description ： 网络配置
 *
 * 凭据不再硬编码：
 * - X_API_KEY / X_THEFAIR_* / CLIENT_UA 的真实值由根目录 secrets.properties
 *   （已被 .gitignore，禁止提交）经 BuildConfig 注入，参见 secrets.properties.example；
 * - 曾经提交到公开仓库的旧凭据必须视为已泄露：先联系数据方吊销，再用新值填充本地文件；
 * - 未提供 secrets.properties 时字段为空字符串，拦截器不会发送对应请求头。
 */
object NetworkConfig {
    const val BASE_URL = "https://api.eyepetizer.net/"

    val X_API_KEY: String get() = com.example.core.network.BuildConfig.X_API_KEY
    val X_THEFAIR_APPID: String get() = com.example.core.network.BuildConfig.X_THEFAIR_APPID
    val X_THEFAIR_AUTH: String get() = com.example.core.network.BuildConfig.X_THEFAIR_AUTH
    val X_THEFAIR_CID: String get() = com.example.core.network.BuildConfig.X_THEFAIR_CID
    val CLIENT_UA: String get() = com.example.core.network.BuildConfig.CLIENT_UA

    /**
     * 逆向取得的客户端请求参数。
     *
     * 注意（审查报告 P1-5）：API 数据来源合法性尚未解决，分发/上架前必须
     * 取得授权或替换为自有/公开 API；这些参数不是密钥，但属于对原始客户端的模仿。
     */
    object DeviceProfile {
        const val UDID = "275d0202c9e74b93a4d5c417f131cf8c"
        const val VC = "7090000"
        const val VN = "7.9.0"
        const val DEVICE_MODEL = "25019PNF3C"
        const val FIRST_CHANNEL = "huawei"
        const val SCREEN_SIZE = "1440X2971"
        const val SYSTEM_VERSION_CODE = "36"
    }
}
