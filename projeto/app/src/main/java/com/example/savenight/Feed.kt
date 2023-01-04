package com.example.savenight

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Feed : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imagesList: ArrayList<UserImage>
    private lateinit var databaseReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // navigate to the login activity
            val intent = Intent(activity, SignInActivity::class.java)
            startActivity(intent)

            // show a message to the user
            Toast.makeText(activity, "You have been logged out", Toast.LENGTH_SHORT).show()
        }


        recyclerView = view.findViewById(R.id.imageRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        imagesList = arrayListOf()
        //create adapter
        val adapter = ImageAdapter(imagesList, requireContext())
        recyclerView.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        databaseReference = FirebaseDatabase.getInstance().
        getReference(uid)

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(imageSnapshot in snapshot.children){

                        val image = UserImage(imageSnapshot.value.toString())
                        imagesList.add(image!!)
                    }
                    adapter.notifyDataSetChanged()


                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), LENGTH_SHORT).show()
            }

        })


    }
}