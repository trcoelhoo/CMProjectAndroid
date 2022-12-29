package com.example.savenight

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.nio.charset.StandardCharsets


class Lobby : Fragment() {

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var leaveButton: Button
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private var connectedEndpoints = ArrayList<String>()
    private lateinit var groupName : String
    var receiverRoom: String? = null
    var senderRoom: String? = null
    private val receiverName = android.os.Build.MODEL;
    private lateinit var connectionsClient: ConnectionsClient

    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val receivedMessage = String(it, StandardCharsets.UTF_8)
                Log.d("Lobby", "Message received: $receivedMessage")
                if(receivedMessage == "disconnect") {
                    val message = "User $endpointId has left the group"
                    val newMessage = Message(message, endpointId, false)
                    messageList.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    connectedEndpoints.remove(endpointId)
                } else if (receivedMessage.contains("connectedendpoints")){

                        val connectedendpoints = receivedMessage.split("connectedendpoints")[1]
                        val connectedendpointsList = connectedendpoints.split(",")
                        //clear list
                        connectedEndpoints.clear()
                        connectedendpointsList.forEach {
                            if(!connectedEndpoints.contains(it)){
                                //remove "[" and "]"
                                val endpoint = it.replace("[", "")
                                val endpoint2 = endpoint.replace("]", "")
                                connectedEndpoints.add(endpoint2)

                            }
                        }
                        Log.d("connectedEndpoints", connectedEndpoints.toString())




                } else {
                    val message= "$endpointId: $receivedMessage"
                    val newMessage = Message(message, endpointId, false)
                    messageList.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                }

            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Toast.makeText(
                    context,
                    "Payload transfer complete",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(requireContext(), messageList)

        //get connectedendpoints from bundle
        connectedEndpoints = arguments?.getStringArrayList("connectedEndpoints") as ArrayList<String>
        Log.d("connectedEndpoints ARGUMENT", connectedEndpoints.toString())


        //get host name from bundle
        val host = arguments?.getString("host")
        groupName= "$host's Group"
        senderRoom= receiverName + host
        receiverRoom= host + receiverName

        //get connectionsClient from bundle

        val connectionsClientString = arguments?.getString("connectionsClient")
        connectionsClient= Nearby.getConnectionsClient(requireContext())

        Log.d("connectionsClient", connectionsClient.toString())


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lobby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageRecyclerView = view.findViewById(R.id.messages)
        messageBox = view.findViewById(R.id.messageBox)
        sendButton = view.findViewById(R.id.sendButton)
        leaveButton = view.findViewById(R.id.leaveGroup)

        //set group name
        val groupNameView = view.findViewById<TextView>(R.id.groupName)
        groupNameView.setText(groupName)

        //show members endpoint names to listview
        val membersList = view.findViewById<ListView>(R.id.memberList)
        //add every connected endpoint to listview

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, connectedEndpoints)
        membersList.adapter = adapter


        adapter.notifyDataSetChanged()

        Log.d("Lobby", "Connected endpoints: $connectedEndpoints")

        //set adapter for messages
        messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        messageRecyclerView.adapter = messageAdapter


        //send message
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val sender = receiverName
            val isSent = true
            val newMessage = Message(message, sender, isSent)
            messageList.add(newMessage)
            messageAdapter.notifyDataSetChanged()
            messageRecyclerView.scrollToPosition(messageList.size - 1)
            messageBox.setText("")
        }

        leaveButton.setOnClickListener {
            //remove this fragment from backstack
            requireActivity().supportFragmentManager.popBackStack()
            //clear connected endpoints
            connectedEndpoints.clear()
            //send disconnect message to all connected endpoints
            for (endpoint in connectedEndpoints) {
                connectionsClient.sendPayload(endpoint, Payload.fromBytes("disconnect".toByteArray()))
            }
            //disconnect from all endpoints
            connectionsClient.stopAllEndpoints()
            val groupsFragment = Groups()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, groupsFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }





    }


}