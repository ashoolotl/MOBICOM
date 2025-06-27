package com.mobicom.s17.mco2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 3 // Number of tabs (pages)
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TabFragment1() // Fragment for first tab
            1 -> TabFragment2() // Fragment for second tab
            2 -> TabFragment3() // Fragment for third tab
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Tab 1"
            1 -> "Tab 2"
            2 -> "Tab 3"
            else -> null
        }
    }
}
