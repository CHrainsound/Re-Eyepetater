package com.example.ept.person

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.core.common.FragmentInterface
import com.example.ept.person.adapter.PersonAdapter
import com.example.ept.person.fragment.AlbumFragment
import com.example.ept.person.fragment.IndexFragment
import com.example.ept.person.fragment.WorkFragment
import com.example.ept.person.viewmodel.CreatorState
import com.example.ept.person.viewmodel.CreatorViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.core.model.NavPaths
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route

@Route(path = NavPaths.PERSON, params = ["uid", "000000000"])
class PersonActivity : AppCompatActivity() {
    private lateinit var aplCreatorDefault: AppBarLayout
    private lateinit var ivCreatorBack: ImageView
    private lateinit var ivCreatorBackground: ImageView
    private lateinit var ivCreatorProfile: ShapeableImageView
    private lateinit var tvCreatorName: TextView
    private lateinit var tvCreatorDesc: TextView
    private lateinit var tvCreatorUnfold: TextView
    private lateinit var tvCreatorFans: TextView
    private lateinit var tvCreatorFollow: TextView
    private lateinit var tvCreatorBadge: TextView
    private lateinit var tvCreatorPopular: TextView
    private lateinit var btnCreatorAdd: Button
    private lateinit var srlCreatorDefault: SwipeRefreshLayout
    private lateinit var tlCreatorDefault: TabLayout
    private lateinit var creatorViewModel: CreatorViewModel
    private var fragmentList = mutableListOf<FragmentInterface>()

    @Autowired
    lateinit var uid: String
    private lateinit var vp2CreatorDefault: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_person)
        aplCreatorDefault = findViewById(R.id.apl_creator_default)
        ivCreatorBack = findViewById(R.id.iv_creator_back)
        ivCreatorBackground = findViewById(R.id.iv_creator_background)
        ivCreatorProfile = findViewById(R.id.iv_creator_profile)
        tvCreatorName = findViewById(R.id.tv_creator_name)
        tvCreatorDesc = findViewById(R.id.tv_creator_desc)
        tvCreatorUnfold = findViewById(R.id.tv_creator_unfold)
        tvCreatorFans = findViewById(R.id.tv_creator_fans)
        tvCreatorFollow = findViewById(R.id.tv_creator_follow)
        tvCreatorBadge = findViewById(R.id.tv_creator_badge)
        tvCreatorPopular = findViewById(R.id.tv_creator_popular)
        btnCreatorAdd = findViewById(R.id.btn_creator_add)
        srlCreatorDefault = findViewById(R.id.srl_creator_default)
        vp2CreatorDefault = findViewById(R.id.vp2_creator_default)
        tlCreatorDefault = findViewById(R.id.tl_creator_default)
        creatorViewModel = ViewModelProvider(this)[CreatorViewModel::class.java]
        // 审查修复：@Autowired 字段原本只声明未 inject，靠 intent.getStringExtra 兜底；
        // 现在显式调用 TheRouter.inject 完成路由参数注入，未注入时回退默认值。
        TheRouter.inject(this)
        if (!this::uid.isInitialized) uid = "000000000"
        creatorViewModel.initCreator(uid)

        ivCreatorBack.setOnClickListener {
            finish()
        }
        initViewModel()
        initRefresh()
    }

    fun initRefresh() {
        aplCreatorDefault.addOnOffsetChangedListener { layout, i ->
            val isExpanded = i == 0
            srlCreatorDefault.isEnabled = isExpanded
        }
        srlCreatorDefault.setOnRefreshListener {
            srlCreatorDefault.isRefreshing = false
            creatorViewModel.initRefresh(uid)
        }
    }

    fun initViewModel() {
        creatorViewModel.liveData.observe(this) { state ->
            when (state) {
                is CreatorState.FailedState -> {
                    Toast.makeText(this, state.msg, Toast.LENGTH_SHORT).show()
                }

                is CreatorState.InitState -> {
                    initVP2(state.length)
                    initTabs(state.length)
                    Glide.with(ivCreatorBackground.context)
                        .load(state.userInfo.cover)
                        .error(R.drawable.eyepetater)
                        .placeholder(R.drawable.eyepetater)
                        .into(ivCreatorBackground)
                    Glide.with(ivCreatorProfile.context)
                        .load(state.userInfo.avatar?.url)
                        .error(R.drawable.eyepetater)
                        .placeholder(R.drawable.eyepetater)
                        .into(ivCreatorProfile)
                    tvCreatorName.text = state.userInfo.nick
                    tvCreatorDesc.text = state.userInfo.description
                    tvCreatorFans.text = "${state.userInfo.fansCount}粉丝"
                    tvCreatorFollow.text = "${state.userInfo.followCount}关注"
                    tvCreatorBadge.text = "${state.userInfo.medalCount}勋章"
                    tvCreatorPopular.text =
                        "被收藏${state.userInfo.collectedCount}次，被分享${state.userInfo.sharedCount}次"
                    srlCreatorDefault.isRefreshing = false
                }

                is CreatorState.RefreshState -> {
                    Glide.with(ivCreatorBackground.context)
                        .load(state.userInfo.cover)
                        .error(R.drawable.eyepetater)
                        .placeholder(R.drawable.eyepetater)
                        .into(ivCreatorBackground)
                    Glide.with(ivCreatorProfile.context)
                        .load(state.userInfo.avatar?.url)
                        .error(R.drawable.eyepetater)
                        .placeholder(R.drawable.eyepetater)
                        .into(ivCreatorProfile)
                    tvCreatorName.text = state.userInfo.nick
                    tvCreatorDesc.text = state.userInfo.description
                    tvCreatorFans.text = "${state.userInfo.fansCount}粉丝"
                    tvCreatorFollow.text = "${state.userInfo.followCount}关注"
                    tvCreatorBadge.text = "${state.userInfo.medalCount}勋章"
                    tvCreatorPopular.text =
                        "被收藏${state.userInfo.collectedCount}次，被分享${state.userInfo.sharedCount}次"
                    srlCreatorDefault.isRefreshing = false
                }
            }
        }
    }

    fun initVP2(length: Int) {
        fragmentList.clear()
        when (length) {
            0 -> {

            }

            1 -> {
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return WorkFragment()
                    }
                })
            }

            2 -> {
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return IndexFragment()
                    }
                })
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return WorkFragment()
                    }
                })
            }

            3 -> {
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return IndexFragment()
                    }
                })
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return WorkFragment()
                    }
                })
                fragmentList.add(object : FragmentInterface {
                    override fun back(): Fragment {
                        return AlbumFragment()
                    }
                })
            }

        }
        val adapter = PersonAdapter(fragmentList, this)
        vp2CreatorDefault.adapter = adapter
    }

    fun initTabs(length: Int) {
        TabLayoutMediator(tlCreatorDefault, vp2CreatorDefault) { p0, p1 ->
            when (length) {
                0 -> {}
                1 -> {
                    when (p1) {
                        0 -> {
                            p0.text = "作品"
                        }
                    }
                }

                2 -> {
                    when (p1) {
                        0 -> {
                            p0.text = "主页"
                        }

                        1 -> {
                            p0.text = "作品"
                        }
                    }
                }

                3 -> {
                    when (p1) {
                        0 -> {
                            p0.text = "主页"
                        }

                        1 -> {
                            p0.text = "作品"
                        }

                        2 -> {
                            p0.text = "专辑"
                        }
                    }
                }
            }
        }.attach()
    }
}