package com.example.savenight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.savenight.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Maps())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.map -> {
                    replaceFragment(Maps())
                    true
                }
                R.id.feed -> {
                    replaceFragment(Feed())
                    true
                }
                R.id.drunk -> {
                    replaceFragment(DrunkTest())
                    true
                }
                R.id.camera -> {
                    replaceFragment(Camera())
                    true
                }
                R.id.group -> {
                    replaceFragment(Groups())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}