package com.example.savenight

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


        recyclerView = view.findViewById(R.id.imageRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        imagesList = arrayListOf()
        //create adapter
        val adapter = ImageAdapter(imagesList, requireContext())
        recyclerView.adapter = adapter


        databaseReference = FirebaseDatabase.getInstance().getReference("images")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(dataSnapShot in snapshot.children){
                        val image = dataSnapShot.getValue(UserImage::class.java)
                        imagesList.add(image!!)
                    }
                    recyclerView.adapter = ImageAdapter(imagesList,this@Feed)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), LENGTH_SHORT).show()
            }

        })


    }
}