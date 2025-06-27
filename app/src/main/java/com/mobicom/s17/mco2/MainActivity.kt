// MainActivity.kt
package com.mobicom.s17.mco2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var floatingActionButton: FloatingActionButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Ensure this points to the layout you provided

        // Initialize views
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabs)
        floatingActionButton = findViewById(R.id.add_entry_button)

        // Setup ViewPager with an Adapter
        val adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        // Link ViewPager with TabLayout
        tabLayout.setupWithViewPager(viewPager)

        // Handle Floating Action Button click
        floatingActionButton.setOnClickListener {
            // Perform action when FAB is clicked, like opening a new fragment or dialog
            // For example, open a new fragment or activity
            // val intent = Intent(this, SomeOtherActivity::class.java)
            // startActivity(intent)
        }
    }


}
