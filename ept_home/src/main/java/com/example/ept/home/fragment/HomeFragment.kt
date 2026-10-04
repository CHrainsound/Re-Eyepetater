package com.example.ept.home.fragment

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
import com.example.ept.home.R
import com.example.ept.home.adapter.HomeAdapter
import com.example.ept.home.viewmodel.HomeState
import com.example.ept.home.viewmodel.HomeViewModel
import com.therouter.TheRouter

class HomeFragment : Fragment() {
    private lateinit var ivHomeNotify: ImageView
    private lateinit var rvHomeDefault: RecyclerView
    private lateinit var srlHomeDefault: SwipeRefreshLayout
    private lateinit var viewModel: HomeViewModel
    private lateinit var view: View
    private lateinit var adapter: HomeAdapter
    private var isLoading = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        ivHomeNotify = view.findViewById(R.id.iv_home_notify)
        rvHomeDefault = view.findViewById(R.id.rv_home_default)
        srlHomeDefault = view.findViewById(R.id.srl_home_default)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        initEvent()
        return view
    }

    fun initEvent() {
        adapter = HomeAdapter { video ->
            video.videoId?.let { VideoPlayerActivity.start(requireContext(), it) }
        }
        rvHomeDefault.layoutManager = LinearLayoutManager(requireContext())
        rvHomeDefault.adapter = adapter
        ivHomeNotify.setOnClickListener {
            TheRouter.build(NavPaths.NOTIFY)
                .navigation(view.context)
        }
        viewModel.liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeState.RefreshState -> {
                    adapter.submitList(state.videoList)
                    srlHomeDefault.isRefreshing = false
                }

                is HomeState.LoadingMoreState -> {
                    isLoading = false
                    adapter.submitList(state.newVideoList)
                }

                is HomeState.ErrorState -> {
                    Toast.makeText(requireContext(), "错误！" + state.errorMsg, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        srlHomeDefault.setOnRefreshListener {
            viewModel.refreshLiveData()
        }
        rvHomeDefault.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
}
