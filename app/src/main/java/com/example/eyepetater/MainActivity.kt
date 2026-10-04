package com.example.eyepetater

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.ept.daily.DailyFragment
import com.example.ept.dicover.discovery.DiscoveryFragment
import com.example.ept.home.fragment.HomeFragment
import com.example.ept.person.fragment.MyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
/**
 * description ： MainActivity
 * email : 3014386984@qq.com
 * date : 2026/7/23 16:53
 */
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupViewPager()
        setupBottomNavigation()

        // 避开状态栏
        val root = findViewById<android.view.View>(R.id.fragment_container)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.isUserInputEnabled = false

        // 审查修复：删除原 setCurrentItem(2, false)（与下方 selectedItemId = nav_home 矛盾，
        // 造成初始位置多余的两次跳动）；默认从 position 0 开始，由底部导航统一切换。
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.setCurrentItem(0, false)
                    true
                }
                R.id.nav_daily -> {
                    viewPager.setCurrentItem(1, false)
                    true
                }
                R.id.nav_discovery -> {
                    viewPager.setCurrentItem(2, false)
                    true
                }
                R.id.nav_community -> {
                    viewPager.setCurrentItem(3, false)
                    true
                }
                else -> false
            }
        }

        // 设置初始界面
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> DailyFragment()
                2 -> DiscoveryFragment()
                3 -> MyFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
