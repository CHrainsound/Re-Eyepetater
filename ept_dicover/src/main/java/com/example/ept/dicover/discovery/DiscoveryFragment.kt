/**
 * description ： 发现页 Fragment，展示分类网格、主题播单和话题广场
 * email : 3014386984@qq.com
 * date : 2026/7/22
 */
package com.example.ept.dicover.discovery

import com.example.core.model.TopicItem
import android.os.Bundle
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ept.category.CategoryDetailActivity
import com.example.ept.dicover.R
import com.example.ept.dicover.lightTopic.LightTopicListActivity
import com.example.ept.dicover.lightTopic.LightTopicsActivity
import com.example.ept.dicover.topicdetail.TopicDetailActivity
import com.example.core.network.RetrofitClient
import com.example.core.model.ApiRequest
import com.example.ept.dicover.topicsquare.TopicSquareListActivity
import com.example.core.model.NavPaths
import com.therouter.TheRouter
/**
 * description ： 发现页 Fragment，
 * email : 3014386984@qq.com
 * date : 2026/7/21 11:39
 */
class DiscoveryFragment : Fragment() {


    private val viewModel: DiscoveryViewModel by viewModels()

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var cardStackView: CardStackView

    private lateinit var tvIndicator: TextView

    private val squareItems = mutableListOf<TopicItem>()

    private var currentSquareIndex = 0

    private var isSquareInitialized = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_discovery, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipe_refresh)

        // 通知和搜索图标
        val ivNotify = view.findViewById<ImageView>(R.id.iv_daily_notify)
        val ivSearch = view.findViewById<ImageView>(R.id.iv_daily_search)

        ivNotify.setOnClickListener {
            TheRouter.build(NavPaths.NOTIFY).navigation(requireContext())
        }
        ivSearch.setOnClickListener {
            TheRouter.build(NavPaths.SEARCH).navigation(requireContext())
        }

        // 分类
        val rvCategory = view.findViewById<RecyclerView>(R.id.rv_discovery_category)
        rvCategory.layoutManager = GridLayoutManager(requireContext(), 3)
        val categoryAdapter = CategoryAdapter { category ->
            CategoryDetailActivity.start(requireContext(), category.pageLabel, category.name)
        }
        rvCategory.adapter = categoryAdapter

        // 主题播单
        view.findViewById<ImageView>(R.id.iv_discovery_topicMore).setOnClickListener {
            TopicSquareListActivity.start(requireContext())
        }
        // 主题播单更多
        view.findViewById<ImageView>(R.id.iv_discovery_lightTopicMore).setOnClickListener {
            LightTopicListActivity.start(requireContext())
        }
        // 横向卡片展示rv
        val rvTopic = view.findViewById<RecyclerView>(R.id.rv_discovery_topic)
        rvTopic.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val topicAdapter = TopicAdapter { topic ->
            if (topic.id > 0) {
                LightTopicsActivity.start(requireContext(), topic.id.toInt(), topic.title)
            }
        }
        rvTopic.adapter = topicAdapter

        // 话题广场
        cardStackView = view.findViewById(R.id.card_stack_view)
        tvIndicator = view.findViewById(R.id.tv_square_indicator)

        // 滑动更新指示器
        cardStackView.onCardSwiped = { _ ->
            currentSquareIndex++
            updateIndicator()
        }

        // 为回收到底部的卡片重新绑定数据
        cardStackView.onCardRecycled = { recycledView ->
            val nextIndex = (currentSquareIndex + 2) % squareItems.size
            bindCardData(recycledView, squareItems[nextIndex])
        }

        // 卡片提供者：创建卡片 View（带点击监听）
        cardStackView.cardProvider = {
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_topic_square_card, cardStackView, false)
            cardView.setOnClickListener {
                val topic = it.tag as? TopicItem ?: return@setOnClickListener
                if (topic.id > 0) {
                    val pageLabel = try {
                        val uri = Uri.parse(topic.actionUrl)
                        val raw = uri.getQueryParameter("api_request") ?: ""
                        RetrofitClient.gson.fromJson(raw, ApiRequest::class.java)
                            ?.params?.get("page_label") as? String ?: ""
                    } catch (e: Exception) { "" }
                    if (pageLabel.isNotEmpty()) {
                        TopicDetailActivity.start(requireContext(), pageLabel, topic.title)
                    }
                }
            }
            cardView
        }

        //观察数据
        viewModel.liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DiscoveryState.RefreshState -> {
                    categoryAdapter.submitList(state.categories)
                    topicAdapter.submitList(state.topics)

                    squareItems.clear()
                    squareItems.addAll(state.squareItems)

                    for (i in 0 until minOf(5, squareItems.size)) {
                        Glide.with(this)
                            .load(squareItems[i].icon)
                            .transform(CenterCrop(), RoundedCorners(16))
                            .preload()
                    }

                    if (!isSquareInitialized && squareItems.isNotEmpty()) {
                        isSquareInitialized = true
                        currentSquareIndex = 0
                        cardStackView.start()
                        for (i in 0 until minOf(cardStackView.childCount, squareItems.size)) {
                            bindCardData(cardStackView.getChildAt(i), squareItems[i])
                        }
                    }
                    updateIndicator()

                    swipeRefresh.isRefreshing = false
                }
                is DiscoveryState.ErrorState -> {
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "错误！" + state.errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        if (!viewModel.loaded) viewModel.refresh()
    }

    private fun bindCardData(view: View, topic: TopicItem) {
        val ivCover = view.findViewById<ImageView>(R.id.iv_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = topic.title
        Glide.with(this)
            .load(topic.icon)
            .transform(CenterCrop(), RoundedCorners(16))
            .into(ivCover)
        view.tag = topic
    }

    private fun updateIndicator() {
        val total = squareItems.size
        if (total > 0) {
            tvIndicator.text = "${(currentSquareIndex % total) + 1}/$total"
        }
    }
}