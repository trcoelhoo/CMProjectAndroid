package com.example.savenight
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class Camera : Fragment() {
    private val IMAGE_CAPTURE_CODE = 1001
    private lateinit var imageView: ImageView
    private var storageReference = Firebase.storage
    private lateinit var uri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)

    }


    private fun requestPermission(){
        requestCameraPermissionIfMissin { granted ->
            if(granted)
                openCameraInterface()
            else
                Toast.makeText(context, "Please Allow the Permission", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCameraPermissionIfMissin(onResult: ((Boolean) -> Unit)) {
        if(context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED)
            onResult(true)
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(Manifest.permission.CAMERA)

    }



    private fun openCameraInterface() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, R.string.take_picture)
        values.put(MediaStore.Images.Media.DESCRIPTION, R.string.take_picture_description)
        uri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        // Create camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        // Launch intent
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Callback from camera intent
        if (resultCode == Activity.RESULT_OK){
            // Set image captured to image view
            imageView?.setImageURI(uri)
        }
        else {
            // Failed to take picture
            showAlert("Failed to take camera picture")
        }
    }



    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(activity as Context)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok_button_title, null)

        val dialog = builder.create()
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.imageview_picture)
        storageReference = FirebaseStorage.getInstance()

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
                if (it != null) {
                    uri = it
                }
            }

        )

        view.findViewById<Button>(R.id.button_take_picture).setOnClickListener {
            requestPermission()
        }

        view.findViewById<Button>(R.id.button_select_picture).setOnClickListener {
            galleryImage.launch("image/*")

        }

        view.findViewById<Button>(R.id.button_upload_picture).setOnClickListener {
            storageReference.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri)
                .addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            val uid = FirebaseAuth.getInstance().currentUser!!.uid
                            val imageMap = mapOf(
                                "url" to uri.toString()
                            )
                            val databaseReference =
                                FirebaseDatabase.getInstance().
                                getReferenceFromUrl("https://savenight-f8bc3-default-rtdb.firebaseio.com")
                            databaseReference.child(uid).setValue(imageMap)
                                .addOnSuccessListener{
                                    Toast.makeText(context, "Successful Shared", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                                }
                        }
                }

        }



        view.findViewById<Button>(R.id.button_upload_picture).setOnClickListener {
            storageReference.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri)
                .addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->

                            val databaseReference:DatabaseReference =
                                FirebaseDatabase.getInstance().getReferenceFromUrl("https://savenight-f8bc3-default-rtdb.firebaseio.com")
                                    .child("images")
                            val hashMap: HashMap<String, String> = HashMap()
                            hashMap.put("imageUrl", uri.toString())
                            databaseReference.setValue(hashMap)
                            Toast.makeText(context,"Successful Shared", Toast.LENGTH_SHORT).show()
                        }
                }

        }
    }


}