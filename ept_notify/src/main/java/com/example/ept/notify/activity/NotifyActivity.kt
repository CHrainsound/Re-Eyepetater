package com.example.ept.notify.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.ept.notify.R
import com.example.ept.notify.adapter.NotifyAdapter
import com.example.core.common.FragmentInterface
import com.example.ept.notify.fragment.InfoFragment
import com.example.ept.notify.fragment.InteractFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.core.model.NavPaths
import com.therouter.router.Route

@Route(path = NavPaths.NOTIFY)
class NotifyActivity : AppCompatActivity() {
    private lateinit var vp2NotifyDefault: ViewPager2
    private lateinit var tlNotifyDefault: TabLayout
    private lateinit var ivNotifyBack: ImageView
    private var fragmentList = mutableListOf<FragmentInterface>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_notify)

        val root = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        vp2NotifyDefault = findViewById(R.id.vp2_notify_default)
        tlNotifyDefault = findViewById(R.id.tl_notify_default)
        ivNotifyBack=findViewById(R.id.iv_notify_back)
        initEvent()
        initTabs()
    }

    fun initEvent() {
        ivNotifyBack.setOnClickListener {
            finish()
        }

        fragmentList.add(object : FragmentInterface {
            override fun back(): Fragment {
                return InteractFragment()
            }
        })
        fragmentList.add(object : FragmentInterface {
            override fun back(): Fragment {
                return InfoFragment()
            }
        })
        val adapter = NotifyAdapter(fragmentList, this)
        vp2NotifyDefault.adapter = adapter
    }

    fun initTabs() {
        TabLayoutMediator(tlNotifyDefault, vp2NotifyDefault) { p0, p1 ->
            if (p1 == 0) {
                p0.text = "互动"
            } else if (p1 == 1) {
                p0.text = "消息"
            }
        }.attach()
    }
}