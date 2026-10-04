package com.example.core.media

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.core.network.RetrofitClient
import com.example.core.network.api.UniversalApi
import com.example.core.model.NavPaths
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoManager.backFromWindowFull
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * description ： 视频播放 Fragment，包含播放器、视频信息展示、相关推荐列表
 * email : 3014386984@qq.com
 * date : 2026/7/15 14:46
 */
class VideoPlayerFragment : Fragment() {

    private lateinit var videoPlayer: StandardGSYVideoPlayer
    private lateinit var scrollView: NestedScrollView
    private lateinit var tvTitle: TextView
    private lateinit var ivAuthor: ImageView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCollectionCount: TextView
    private lateinit var tvReplyCount: TextView
    private lateinit var ivShare: ImageView
    private lateinit var rvRelated: RecyclerView
    private var orientationUtils: OrientationUtils? = null

    private val api = RetrofitClient.create<UniversalApi>()
    private lateinit var relatedAdapter: RelatedVideoAdapter
    private var isPlay: Boolean = false

    private var videoId: Long = 0
    private var videoUrl: String = ""
    private var videoTitle: String = ""
    private var videoCover: String = ""
    private var authorName: String = ""
    private var authorIcon: String = ""
    private var authorUid: String = ""
    private var category: String = ""
    private var description: String = ""
    private var collectionCount: Int = 0
    private var replyCount: Int = 0
    private var playUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoId = it.getLong(ARG_VIDEO_ID, 0)
            videoUrl = it.getString(ARG_VIDEO_URL, "")
            videoTitle = it.getString(ARG_VIDEO_TITLE, "")
            videoCover = it.getString(ARG_VIDEO_COVER, "")
            authorName = it.getString(ARG_AUTHOR_NAME, "")
            authorIcon = it.getString(ARG_AUTHOR_ICON, "")
            authorUid = it.getString(ARG_AUTHOR_UID, "")
            category = it.getString(ARG_CATEGORY, "")
            description = it.getString(ARG_DESCRIPTION, "")
            collectionCount = it.getInt(ARG_COLLECTION_COUNT, 0)
            replyCount = it.getInt(ARG_REPLY_COUNT, 0)
            playUrl = it.getString(ARG_PLAY_URL, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPlayer = view.findViewById(R.id.video_player)
        tvTitle = view.findViewById(R.id.tv_title)
        ivAuthor = view.findViewById(R.id.iv_author)
        tvAuthorName = view.findViewById(R.id.tv_author_name)
        tvCategory = view.findViewById(R.id.tv_category)
        tvDescription = view.findViewById(R.id.tv_description)
        tvCollectionCount = view.findViewById(R.id.tv_collection_count)
        tvReplyCount = view.findViewById(R.id.tv_reply_count)
        ivShare = view.findViewById(R.id.iv_share)
        rvRelated = view.findViewById(R.id.rv_related)
        scrollView = view.findViewById(R.id.nsv_media_detail)

        bindData()
        initRelatedVideos()
        loadRelatedVideos()
        resolveAndPlay()
    }

    /**
     * 异步解析 playUrl 302 重定向后初始化播放器
     */
    private fun resolveAndPlay() {
        viewLifecycleOwner.lifecycleScope.launch {
            val realUrl = withContext(Dispatchers.IO) {
                RetrofitClient.resolvePlayUrl(videoUrl)
            }
            initVideoPlayer(realUrl)
        }
    }

    /**
     * 配置 GSYVideoPlayer 参数并开始播放
     */
    private fun initVideoPlayer(realUrl: String) {

        GSYVideoOptionBuilder()
            .setUrl(realUrl)
            .setVideoTitle(videoTitle)
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(false)
            .setCacheWithPlay(false)
            .setStartAfterPrepared(true)
            .setNeedOrientationUtils(false)
            .setVideoAllCallBack(object : GSYSampleCallBack() {


                override fun onPrepared(url: String, vararg objects: Any) {
                    super.onPrepared(url, *objects)
                    // 开始播放了才能旋转和全屏
                    orientationUtils?.setEnable(true)
                    isPlay = true
                }

                override fun onQuitFullscreen(url: String, vararg objects: Any) {
                    super.onQuitFullscreen(url, *objects)
                    orientationUtils?.backToProtVideo()
                }
            })
            .setLockClickListener { view, lock ->
                orientationUtils?.setEnable(!lock)
            }
            .build(videoPlayer)
        //全屏时返回键
        videoPlayer.setBackFromFullScreenListener {
            if (!isAdded) return@setBackFromFullScreenListener
            backFromWindowFull(requireActivity())
        }



        orientationUtils = OrientationUtils(requireActivity(), videoPlayer)
        videoPlayer.fullscreenButton.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            orientationUtils?.resolveByClick()
            videoPlayer.startWindowFullscreen(requireActivity(), true, true)
        }
        //非全屏返回键
        videoPlayer.backButton.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            if (!videoPlayer.isIfCurrentIsFullscreen()) {
                requireActivity().finish()
            }
        }


    }


    /**
     * 初始化相关推荐列表及点击跳转逻辑
     *
     * 审查修复：原实现绕过 VideoPlayerActivity 的 companion 工厂手工拼 10 个 extra，
     * 而 onCreate 实际只读取 video_id / resource_type（其余 extra 从未被使用）；
     * 现改走统一工厂方法，参数收敛到 videoId。
     */
    private fun initRelatedVideos() {
        relatedAdapter = RelatedVideoAdapter { item ->
            if (!isAdded) return@RelatedVideoAdapter
            VideoPlayerActivity.start(requireContext(), item)
        }
        rvRelated.layoutManager = LinearLayoutManager(requireContext())
        rvRelated.adapter = relatedAdapter
    }

    /**
     * 加载相关推荐视频数据
     */
    private fun loadRelatedVideos() {
        if (videoId == 0L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getRelatedRecommend(videoId.toString()).execute()
                }
                if (response.isSuccessful) {
                    val rawBody = response.body()?.string() ?: ""

                    val topLevel = try {
                        gson.fromJson(rawBody, Map::class.java) as? Map<String, Any>
                    } catch (e: Exception) {
                        null
                    }

                    val code = (topLevel?.get("code") as? Number)?.toInt() ?: -1
                    if (code != 0) return@launch

                    @Suppress("UNCHECKED_CAST")
                    val result = topLevel?.get("result") as? Map<String, Any>
                    @Suppress("UNCHECKED_CAST")
                    val itemList = result?.get("item_list") as? List<Map<String, Any>> ?: emptyList()

                    val videoItems = mutableListOf<RelatedVideoAdapter.RelatedVideoItem>()
                    for (itemMap in itemList) {
                        // API 返回的 item 是扁平结构，video 信息直接在 item 中
                        @Suppress("UNCHECKED_CAST")
                        val videoMap = itemMap["video"] as? Map<String, Any> ?: continue

                        val id = when (val v = videoMap["video_id"] ?: itemMap["item_id"]) {
                            is Number -> v.toLong()
                            is String -> v.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        if (id == 0L) continue

                        @Suppress("UNCHECKED_CAST")
                        val coverMap = videoMap["cover"] as? Map<String, Any>
                        @Suppress("UNCHECKED_CAST")
                        val authorMap = itemMap["author"] as? Map<String, Any>
                        val duration = videoMap["duration"]
                        val durationLong = when (duration) {
                            is Map<*, *> -> (duration["value"] as? Number)?.toLong() ?: 0L
                            is Number -> duration.toLong()
                            else -> 0L
                        }

                        videoItems.add(
                            RelatedVideoAdapter.RelatedVideoItem(
                                id = id,
                                title = videoMap["title"] as? String ?: "",
                                coverUrl = coverMap?.get("feed") as? String ?: coverMap?.get("url") as? String ?: "",
                                duration = durationLong,
                                authorName = authorMap?.get("nick") as? String ?: "",
                                authorIcon = (authorMap?.get("avatar") as? Map<String, Any>)?.get("url") as? String ?: "",
                                category = (itemMap["category"] as? Map<String, Any>)?.get("name") as? String ?: "",
                                description = itemMap["text"] as? String ?: "",
                                playUrl = videoMap["play_url"] as? String ?: ""
                            )
                        )
                    }

                    relatedAdapter.submitList(videoItems)
                }
            } catch (e: Exception) {
                Log.e("VideoPlayer", "loadRelatedVideos failed", e)
            }
        }
    }

    /**
     * 绑定视频标题、作者、分类、描述到 UI
     */
    private fun bindData() {
        tvTitle.text = videoTitle
        tvAuthorName.text = authorName
        tvCategory.text = if (category.isNotEmpty()) "#$category" else ""
        tvDescription.text = description
        tvCollectionCount.text = formatCount(collectionCount)
        tvReplyCount.text = formatCount(replyCount)

        if (authorIcon.isNotEmpty()) {
            Glide.with(this)
                .load(authorIcon)
                .transform(CircleCrop())
                .into(ivAuthor)
        }

        // 点击头像或作者名跳转作者页
        ivAuthor.setOnClickListener { navigateToAuthor() }
        tvAuthorName.setOnClickListener { navigateToAuthor() }

        // 分享按钮
        val shareUrl = playUrl.ifEmpty { videoUrl }
        ivShare.setOnClickListener {
            if (shareUrl.isNotEmpty()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                }
                startActivity(Intent.createChooser(shareIntent, "分享视频"))
            }
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> String.format("%.1f万", count / 10000.0)
            else -> count.toString()
        }
    }

    private fun navigateToAuthor() {
        if (!isAdded || authorUid.isEmpty()) return
        com.therouter.TheRouter.build(NavPaths.PERSON)
            .withString("uid", authorUid)
            .navigation()
    }


    override fun onPause() {
        videoPlayer.currentPlayer.onVideoPause()
        super.onPause()
    }

    override fun onResume() {
        videoPlayer.currentPlayer.onVideoResume(false)
        super.onResume()
    }

    override fun onDestroyView() {
        orientationUtils?.releaseListener()
        orientationUtils = null
        GSYVideoManager.releaseAllVideos()
        super.onDestroyView()
    }



    companion object {
        private val gson = com.google.gson.Gson()

        /** 视频 ID 参数键名 */
        private const val ARG_VIDEO_ID = "video_id"
        /** 视频播放地址参数键名 */
        private const val ARG_VIDEO_URL = "video_url"
        /** 视频标题参数键名 */
        private const val ARG_VIDEO_TITLE = "video_title"
        /** 视频封面图参数键名 */
        private const val ARG_VIDEO_COVER = "video_cover"
        /** 作者名称参数键名 */
        private const val ARG_AUTHOR_NAME = "author_name"
        /** 作者头像参数键名 */
        private const val ARG_AUTHOR_ICON = "author_icon"
        /** 作者 UID 参数键名 */
        private const val ARG_AUTHOR_UID = "author_uid"
        /** 视频分类参数键名 */
        private const val ARG_CATEGORY = "category"
        /** 视频描述参数键名 */
        private const val ARG_DESCRIPTION = "description"
        /** 收藏数参数键名 */
        private const val ARG_COLLECTION_COUNT = "collection_count"
        /** 回复数参数键名 */
        private const val ARG_REPLY_COUNT = "reply_count"
        /** 视频播放地址参数键名（用于分享） */
        private const val ARG_PLAY_URL = "play_url"

        /**
         * 创建 Fragment 实例并传入视频参数
         */
        @JvmStatic
        fun newInstance(
            videoId: Long,
            videoUrl: String,
            videoTitle: String,
            videoCover: String,
            authorName: String,
            authorIcon: String,
            authorUid: String = "",
            category: String,
            description: String,
            collectionCount: Int = 0,
            replyCount: Int = 0,
            playUrl: String = ""
        ) = VideoPlayerFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_VIDEO_ID, videoId)
                putString(ARG_VIDEO_URL, videoUrl)
                putString(ARG_VIDEO_TITLE, videoTitle)
                putString(ARG_VIDEO_COVER, videoCover)
                putString(ARG_AUTHOR_NAME, authorName)
                putString(ARG_AUTHOR_ICON, authorIcon)
                putString(ARG_AUTHOR_UID, authorUid)
                putString(ARG_CATEGORY, category)
                putString(ARG_DESCRIPTION, description)
                putInt(ARG_COLLECTION_COUNT, collectionCount)
                putInt(ARG_REPLY_COUNT, replyCount)
                putString(ARG_PLAY_URL, playUrl)
            }
        }
    }
}
