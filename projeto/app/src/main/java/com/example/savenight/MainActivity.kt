package com.example.savenight

import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.savenight.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {


    private lateinit var  binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Maps())
        //clean local storage
        var sharedPref = getSharedPreferences("locations", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.map -> {
                    //get the last fragment in the backstack
                    val index = supportFragmentManager.backStackEntryCount - 1
                    Log.d("index", index.toString())
                    if (index >= 0) {
                        val lastFragment = supportFragmentManager.getBackStackEntryAt(index)


                        if (lastFragment.name== "lobby") {
                            val transaction = supportFragmentManager.beginTransaction()
                            //hide the lobby fragment
                            transaction.hide(supportFragmentManager.findFragmentByTag("lobby")!!)
                            transaction.add(R.id.frameLayout, Maps())
                            transaction.addToBackStack("map")

                            transaction.commit()

                        }
                        else {
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.hide(supportFragmentManager.fragments.last())
                            transaction.add(R.id.frameLayout, Maps())
                            transaction.addToBackStack("map")
                            transaction.commit()


                        }

                    }else{
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frameLayout, Maps())
                        transaction.addToBackStack("map")
                        //hide actual fragment
                        transaction.hide(supportFragmentManager.fragments.last())
                        transaction.commit()
                    }
                    true
                }
                R.id.feed -> {
                    //get the last fragment in the backstack
                    val index = supportFragmentManager.backStackEntryCount - 1
                    Log.d("index", index.toString())
                    if (index >= 0) {
                        val lastFragment = supportFragmentManager.getBackStackEntryAt(index)


                        if (lastFragment.name == "lobby") {
                            val transaction = supportFragmentManager.beginTransaction()
                            //hide the lobby fragment
                            transaction.hide(supportFragmentManager.findFragmentByTag("lobby")!!)
                            transaction.add(R.id.frameLayout, Feed())
                            transaction.addToBackStack("feed")

                            transaction.commit()
                            return@setOnItemSelectedListener true
                        } else {
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.add(R.id.frameLayout, Feed())
                            transaction.addToBackStack("feed")
                            //hide actual fragment
                            transaction.hide(supportFragmentManager.fragments.last())
                            transaction.commit()

                            return@setOnItemSelectedListener true
                        }

                    }else{
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frameLayout, Feed())
                        transaction.addToBackStack("feed")
                        transaction.commit()
                    }
                    true
                }
                R.id.drunk -> {
                    //get the last fragment in the backstack
                    val index = supportFragmentManager.backStackEntryCount - 1
                    Log.d("index", index.toString())
                    if (index >= 0) {
                        val lastFragment = supportFragmentManager.getBackStackEntryAt(index)


                        if (lastFragment.name== "lobby") {
                            val transaction = supportFragmentManager.beginTransaction()
                            //hide the lobby fragment
                            transaction.hide(supportFragmentManager.findFragmentByTag("lobby")!!)
                            transaction.add(R.id.frameLayout, DrunkTest())
                            transaction.addToBackStack("drunk")

                            transaction.commit()
                            return@setOnItemSelectedListener true
                        }
                        else {
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.add(R.id.frameLayout, DrunkTest())
                            transaction.addToBackStack("drunktest")
                            //hide actual fragment
                            transaction.hide(supportFragmentManager.fragments.last())
                            transaction.commit()

                            return@setOnItemSelectedListener true
                        }

                    }else{
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frameLayout, DrunkTest())
                        transaction.addToBackStack("drunktest")
                        transaction.commit()
                    }
                    true
                }
                R.id.camera -> {
                    //get the last fragment in the backstack
                    val index = supportFragmentManager.backStackEntryCount - 1
                    Log.d("index", index.toString())
                    if (index >= 0) {
                        val lastFragment = supportFragmentManager.getBackStackEntryAt(index)


                        if (lastFragment.name== "lobby") {
                            val transaction = supportFragmentManager.beginTransaction()
                            //hide the lobby fragment
                            transaction.hide(supportFragmentManager.findFragmentByTag("lobby")!!)
                            transaction.addToBackStack("camera")
                            transaction.add(R.id.frameLayout, Camera())

                            transaction.commit()
                            return@setOnItemSelectedListener true
                        }
                        else {
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.add(R.id.frameLayout, Camera())
                            //hide actual fragment
                            transaction.hide(supportFragmentManager.fragments.last())


                            transaction.addToBackStack("camera")
                            transaction.commit()

                            return@setOnItemSelectedListener true
                        }

                    }else{
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frameLayout, Camera())
                        transaction.addToBackStack("camera")
                        transaction.commit()
                    }
                    true
                }
                R.id.group -> {
                    // see if whe have other fragment in the backstack
                    val fragment = supportFragmentManager.findFragmentByTag("lobby")
                    Log.d("TAG", "onCreate: $fragment")
                    if (fragment != null) {
                        Log.d("Lobby found", "onCreate: $fragment")
                        // show the lobby fragment
                        val transaction = supportFragmentManager.beginTransaction()
                        //hide actual fragment
                        transaction.hide(supportFragmentManager.fragments.last())
                        transaction.show(fragment)
                        transaction.addToBackStack("lobby")

                        transaction.commit()
                    } else {
                        replaceFragment(Groups())
                    }
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