package com.example.myapplication

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var firebaseInstanceAuth = FirebaseAuth.getInstance()
    private var currentUser = firebaseInstanceAuth.currentUser


    override fun onStart() {
        super.onStart()
        FirebaseApp.initializeApp(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_bar_main)
        val bottomNavMenu: Menu = bottomNavView.menu

        bottomNavMenu.findItem(R.id.review)?.isEnabled = currentUser?.isAnonymous != true
        bottomNavMenu.findItem(R.id.collections)?.isEnabled = currentUser?.isAnonymous != true

        replaceFragment(HomepageFragment())

        binding.bottomNavBarMain.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.home -> replaceFragment(HomepageFragment())
                R.id.review ->
                    if (currentUser?.isAnonymous == true) {
                        Toast.makeText(this, "Invalid Permissions", Toast.LENGTH_LONG).show()
                    } else {
                        replaceFragment(ReviewsFragment())
                    }

                R.id.restaurants -> replaceFragment(RestaurantsFragment())
                R.id.collections ->
                    if (currentUser?.isAnonymous == true) {
                        Toast.makeText(this, "Invalid Permissions", Toast.LENGTH_LONG).show()
                    } else {
                        replaceFragment(CollectionsFragment())
                    }
            }
            true
        }

        binding.topAppBar.setNavigationOnClickListener {

        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile_icon -> replaceFragment(UserAccount())
            }
            true
        }

    }

    /**
     * changes fragments accordingly
     *
     * @param fragment
     */
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frame_layout, fragment)
        fragmentTransaction.commit()
    }
}