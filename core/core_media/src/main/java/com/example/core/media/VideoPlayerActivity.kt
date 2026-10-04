package com.example.core.media

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.core.network.RetrofitClient
import com.example.core.network.api.UniversalApi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * description ： 视频播放页 Activity，通过 videoId 从 API 获取所有数据
 * email : 3014386984@qq.com
 * date : 2026/7/22
 */
class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        private const val EXTRA_RESOURCE_TYPE = "resource_type"
        const val EXTRA_VIDEO_URL = "video_url"
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_VIDEO_COVER = "video_cover"
        const val EXTRA_AUTHOR_NAME = "author_name"
        const val EXTRA_AUTHOR_ICON = "author_icon"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_DESCRIPTION = "description"

        fun start(context: Context, videoId: String, resourceType: String = "pgc_video") {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_RESOURCE_TYPE, resourceType)
            }
            context.startActivity(intent)
        }

        /**
         * 相关推荐等场景的统一入口（审查修复）：
         * 页面数据由 onCreate 通过 API 重新拉取，此处只传 videoId，
         * 不再手工拼接 10 个从未被读取的 extra。
         */
        fun start(context: Context, item: RelatedVideoAdapter.RelatedVideoItem) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, item.id.toString())
                putExtra(EXTRA_RESOURCE_TYPE, "pgc_video")
            }
            context.startActivity(intent)
        }
    }

    private val api = RetrofitClient.create<UniversalApi>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        setupStatusBar()
        setContentView(R.layout.activity_video_player)

        val root = findViewById<View>(R.id.fragment_container)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: ""
        val resourceType = intent.getStringExtra(EXTRA_RESOURCE_TYPE) ?: "pgc_video"

        if (videoId.isEmpty()) {
            Toast.makeText(this, "视频ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        api.getItemDetail(videoId, resourceType).execute()
                    }
                    if (!response.isSuccessful) {
                        Log.e("VideoPlayer", "HTTP ${response.code()}")
                        Toast.makeText(this@VideoPlayerActivity, "网络错误", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    val rawBody = response.body()?.string() ?: ""

                    if (rawBody.isEmpty()) {
                        Toast.makeText(this@VideoPlayerActivity, "响应体为空", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    // 手动解析 JSON
                    val topLevel = try {
                        gson.fromJson(rawBody, Map::class.java) as? Map<String, Any>
                    } catch (e: Exception) {
                        Log.e("VideoPlayer", "JSON parse error", e)
                        null
                    }

                    if (topLevel == null) {
                        Toast.makeText(this@VideoPlayerActivity, "JSON解析失败", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    val code = (topLevel["code"] as? Number)?.toInt() ?: -1
                    if (code != 0) {
                        Toast.makeText(this@VideoPlayerActivity, "接口错误: code=$code", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    @Suppress("UNCHECKED_CAST")
                    val result = topLevel["result"] as? Map<String, Any>
                    if (result == null) {
                        Toast.makeText(this@VideoPlayerActivity, "无结果数据", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    // API 直接返回 result 作为视频数据（无 data 包裹）
                    @Suppress("UNCHECKED_CAST")
                    val dataMap = result["data"] as? Map<String, Any> ?: result
                    if (dataMap == null) {
                        Log.e("VideoPlayer", "result.data is null, result keys=${result.keys}")
                        Toast.makeText(this@VideoPlayerActivity, "视频数据为空", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    val videoInfo = dataMap["video"] as? Map<String, Any>
                    val authorInfo = dataMap["author"] as? Map<String, Any>
                    val consumptionInfo = dataMap["consumption"] as? Map<String, Any>
                    val authorUid = authorInfo?.get("uid")?.toString() ?: ""

                    val rawPlayUrl = videoInfo?.get("play_url") as? String ?: ""
                    val cleanPlayUrl = rawPlayUrl
                        .replace("\\u003d", "=")
                        .replace("\\u0026", "&")

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            VideoPlayerFragment.newInstance(
                                videoId = videoId.toLongOrNull() ?: 0L,
                                videoUrl = cleanPlayUrl,
                                videoTitle = videoInfo?.get("title") as? String ?: "",
                                videoCover = (videoInfo?.get("cover") as? Map<String, Any>)?.get("url") as? String ?: "",
                                authorName = authorInfo?.get("nick") as? String ?: "",
                                authorIcon = (authorInfo?.get("avatar") as? Map<String, Any>)?.get("url") as? String ?: "",
                                authorUid = authorUid,
                                category = (dataMap["tags"] as? List<*>)?.firstOrNull()?.let {
                                    (it as? Map<String, Any>)?.get("title") as? String
                                } ?: "",
                                description = dataMap["text"] as? String ?: "",
                                collectionCount = (consumptionInfo?.get("collection_count") as? Number)?.toInt() ?: 0,
                                replyCount = (consumptionInfo?.get("comment_count") as? Number)?.toInt() ?: 0,
                                playUrl = ""
                            )
                        )
                        .commit()
                } catch (e: Exception) {
                    Log.e("VideoPlayer", "加载失败", e)
                    Toast.makeText(this@VideoPlayerActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupStatusBar() {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkMode
    }
}
