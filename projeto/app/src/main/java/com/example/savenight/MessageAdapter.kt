package com.example.savenight

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            val view : View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceivedViewHolder(view)
        } else {
            val view : View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            SentViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if(holder.javaClass == SentViewHolder::class.java){

            val sentMessageHolder = holder as SentViewHolder
            sentMessageHolder.sentMessage.text = currentMessage.message

        }else{

            val receivedMessageHolder = holder as ReceivedViewHolder
            receivedMessageHolder.receivedMessage.text = currentMessage.message

        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(currentMessage.isSent==true){
            ITEM_SEND
        }else{
            ITEM_RECEIVE
        }
    }
    override fun getItemCount(): Int {
        return messageList.size

    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)

    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val receivedMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
    }

}