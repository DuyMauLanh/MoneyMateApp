package com.example.moneymate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.moneymate.databinding.ActivityMainBinding
import com.example.moneymate.fragment.CalendarFragment
import com.example.moneymate.fragment.MoreFragment
import com.example.moneymate.fragment.ReportFragment
import com.example.moneymate.fragment.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TransactionFragment())
                .commit()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_input -> {
                    replaceFragment(TransactionFragment())
                    true
                }
                R.id.navigation_report -> {
                    replaceFragment(ReportFragment())
                    true
                }
                R.id.navigation_calendar -> {
                    replaceFragment(CalendarFragment())
                    true
                }
                R.id.navigation_more -> {
                    replaceFragment(MoreFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}