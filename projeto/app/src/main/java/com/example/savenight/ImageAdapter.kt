package com.example.savenight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context


class ImageAdapter(
    private val imagesList:ArrayList<UserImage>,
    private val context: Context

):
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(){

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.imageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context).load(imagesList[position].imageUrl).into(holder.image)
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }


}