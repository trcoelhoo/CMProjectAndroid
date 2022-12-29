package com.example.savenight

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.PermissionChecker.checkSelfPermission


class Groups : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)






    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //on click listener for create group button to go to create group fragment
        val createGroupButton = view.findViewById<View>(R.id.create)
        createGroupButton?.setOnClickListener {
            Log.d("Groups", "Create group button clicked")
            val createGroupFragment = Create()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, createGroupFragment)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        //on click listener for join group button to go to join group fragment
        val joinGroupButton = view.findViewById<View>(R.id.join)
        joinGroupButton?.setOnClickListener {
            val joinGroupFragment = Join_group()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, joinGroupFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }



}