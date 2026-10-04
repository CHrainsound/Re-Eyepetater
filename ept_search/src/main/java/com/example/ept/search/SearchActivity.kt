package com.example.ept.search

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.core.model.NavPaths
import com.example.ept.search.fragment.InitialFragment
import com.example.ept.search.fragment.PreSearchFragment
import com.example.ept.search.fragment.ResultFragment
import com.example.ept.search.viewmodel.SearchState
import com.example.ept.search.viewmodel.SearchViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@com.therouter.router.Route(path = NavPaths.SEARCH)
class SearchActivity : AppCompatActivity() {
    lateinit var viewModel: SearchViewModel
    private lateinit var svSearchTop: SearchView
    private lateinit var initialFragment: InitialFragment
    private lateinit var preSearchFragment: PreSearchFragment
    private lateinit var resultFragment: ResultFragment

    private lateinit var tvSearchCancel: TextView
    private val _queryFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var currentFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        svSearchTop = findViewById(R.id.sv_search_top)
        tvSearchCancel = findViewById(R.id.tv_search_cancel)
        initEvent()
        initSearch()
        initPreSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        svSearchTop.setOnQueryTextListener(null)
        tvSearchCancel.setOnClickListener(null)
    }


    fun initEvent() {
        tvSearchCancel.setOnClickListener {
            finish()
        }
        initialFragment = InitialFragment()
        preSearchFragment = PreSearchFragment()
        resultFragment = ResultFragment()
        //切换三大状态
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_search_container, initialFragment, "initial")
            .add(R.id.fl_search_container, preSearchFragment, "suggestion")
            .add(R.id.fl_search_container, resultFragment, "result")
            .hide(preSearchFragment)
            .hide(resultFragment)
            .show(initialFragment)
            .commit()
        currentFragment = initialFragment
        viewModel.liveData.observe(this) { value ->
            when (value) {
                is SearchState.Failed -> {
                    Toast.makeText(this, value.message, Toast.LENGTH_SHORT).show()
                }
                SearchState.Ranking -> {}
                SearchState.Recommend -> {}
                is SearchState.Result -> {}
                SearchState.PreSearch -> {}
                is SearchState.History -> {
                    svSearchTop.isIconified = false
                    svSearchTop.setQuery(value.query,true)
                }
            }
        }
    }

    fun initSearch() {
        //通过监听搜索切换状态
        svSearchTop.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(p0: String?): Boolean {
                //发射流
                _queryFlow.tryEmit(p0 ?: "")
                if (p0?.isBlank() ?: true) {
                    switchTo(initialFragment)
                } else {
                    switchTo(preSearchFragment)
                }
                return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0?.isBlank() ?: true) {
                    //清除焦点
                    svSearchTop.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(svSearchTop.windowToken, 0)
                    return true
                } else {
                    svSearchTop.clearFocus()
                    switchTo(resultFragment)
                    viewModel.search(p0)
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(svSearchTop.windowToken, 0)
                }
                return true
            }
        })
    }

    fun initPreSearch() {
        //消抖
        lifecycleScope.launch {
            _queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        viewModel.fetchPreSearch(query)
                    }
                }
        }
    }
    //切换Fragment
    private fun switchTo(target: Fragment) {
        if (target != currentFragment) {
            // 审查修复：原 currentFragment!! 强解引用在进程恢复后可能为 null，改为安全解引用
            val current = currentFragment
            if (current != null) {
                supportFragmentManager.beginTransaction()
                    .hide(current)
                    .show(target)
                    .commitNow()
                currentFragment = target
            }
        }
    }

}