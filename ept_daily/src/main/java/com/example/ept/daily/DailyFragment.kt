package com.example.ept.daily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.core.media.VideoPlayerActivity
import com.example.core.model.NavPaths
import com.therouter.TheRouter

class DailyFragment : Fragment() {
    private lateinit var view: View
    private lateinit var ivDailyNotify: ImageView
    private lateinit var ivDailySearch: ImageView
    private lateinit var rvDailyDefault: RecyclerView
    private lateinit var srlDailyDefault: SwipeRefreshLayout
    private lateinit var viewModel: DailyViewModel
    private lateinit var adapter: DailyAdapter
    private var isLoading=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_daily, container, false)
        ivDailyNotify = view.findViewById(R.id.iv_daily_notify)
        ivDailySearch = view.findViewById(R.id.iv_daily_search)
        rvDailyDefault = view.findViewById(R.id.rv_daily_default)
        srlDailyDefault=view.findViewById(R.id.srl_daily_default)
        viewModel = ViewModelProvider(this)[DailyViewModel::class.java]
        initEvent()
        return view
    }

    fun initEvent() {
        adapter = DailyAdapter { video ->
            video.videoId?.let { VideoPlayerActivity.start(requireContext(), it) }
        }
        rvDailyDefault.adapter = adapter
        rvDailyDefault.layoutManager = LinearLayoutManager(requireContext())
        ivDailyNotify.setOnClickListener {
            initNotify()
        }
        ivDailySearch.setOnClickListener {
            initSearch()
        }
        srlDailyDefault.setOnRefreshListener {
            viewModel.refreshLiveData()
        }
        viewModel.liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DailyState.RefreshState -> {
                    adapter.submitList(state.videoList)
                    srlDailyDefault.isRefreshing = false
                }
                is DailyState.LoadingMoreState -> {
                    isLoading = false
                    adapter.submitList(state.videoList)
                }
                is DailyState.ErrorState -> {
                    Toast.makeText(
                        requireContext(),
                        "错误！" + state.errorMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        rvDailyDefault.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isLoading) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalCount = layoutManager.itemCount
                if (totalCount <= 0) return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val preloadThreshold = 6
                val remainingItems = totalCount - lastVisible - 1
                if (remainingItems <= preloadThreshold) {
                    isLoading = true
                    viewModel.loadingMore()
                }
//                val lastVisible=layoutManager.findLastVisibleItemPosition()
//                if (!isLoading&&lastVisible>=layoutManager.itemCount-6){
//                    isLoading=true
//                    viewModel.loadingMore()
//                }
            }
        })


    }

    fun initNotify() {
        TheRouter.build(NavPaths.NOTIFY).navigation(view.context)
    }
    fun initSearch(){
        TheRouter.build(NavPaths.SEARCH).navigation(view.context)
    }
}