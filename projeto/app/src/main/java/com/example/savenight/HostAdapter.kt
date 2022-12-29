package com.example.savenight

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HostAdapter(val context: Context, val hostList: ArrayList<Host>): RecyclerView.Adapter<HostAdapter.HostViewHolder>() {
    //listener for click
    private var onItemClickListener: ((Host) -> Unit)? = null
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    class HostViewHolder(itemView: View, listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        val deviceName = itemView.findViewById<TextView>(R.id.deviceName)
        val endpointId = itemView.findViewById<TextView>(R.id.endpointId)
        val joinButton = itemView.findViewById<TextView>(R.id.joinButton)



        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.host, parent, false)
        return HostViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        val currentItem = hostList[position]
        holder.deviceName.text = currentItem.deviceName
        holder.endpointId.text = currentItem.endpointId
        holder.joinButton.text= "Join"


    }

    override fun getItemCount(): Int {
        return hostList.size
    }

    fun setData(host: List<Host>){
        this.hostList.clear()
        this.hostList.addAll(host)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: Any){
        onItemClickListener = listener as ((Host) -> Unit)?


    }
}