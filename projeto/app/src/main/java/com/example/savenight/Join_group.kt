package com.example.savenight

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.nio.charset.StandardCharsets


class Join_group : Fragment() {
    /**
     * Strategy for telling the Nearby Connections API how we want to discover and connect to
     * other nearby devices. A star shaped strategy means we want to discover multiple devices but
     * only connect to and communicate with one at a time.
     */
    private val STRATEGY = Strategy.P2P_STAR

    /**
     * Our handle to the [Nearby Connections API][ConnectionsClient].
     */
    private lateinit var connectionsClient: ConnectionsClient
    /**
     * The request code for verifying our call to [requestPermissions]. Recall that calling
     * [requestPermissions] leads to a callback to [onRequestPermissionsResult]
     */
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    // Device name
    private val deviceName = android.os.Build.MODEL;

    //Recycler view
    private lateinit var discoversRecyclerView: RecyclerView
    private lateinit var discoversList: ArrayList<Host>
    private lateinit var discoversAdapter: HostAdapter
    private lateinit var discoverButton: Button
    private lateinit var joinButton: Button

    private var discovering=false

    //endpoint name received
    private var endpointName: String? = null

    // List of endpoints connected to us
    private val connectedEndpoints = ArrayList<String>()


    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // An endpoint was found. We request a connection to it.
                val host = Host(info.endpointName, endpointId)
                discoversList.add(host)
                discoversAdapter.notifyDataSetChanged()

            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
                //remove from list
                discoversList.removeIf { it.endpointId == endpointId }
                discoversAdapter.notifyDataSetChanged()


            }
        }
    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val receivedMessage = String(it, StandardCharsets.UTF_8)
                Log.d("payload", "Message received: $receivedMessage")
                //see if received connectedendpoints
                if(receivedMessage.contains("connectedendpoints")){


                    val connectedendpoints = receivedMessage.split("connectedendpoints")[1]
                    val connectedendpointsList = connectedendpoints.split(",")
                    //clear list
                    connectedEndpoints.clear()
                    connectedendpointsList.forEach {
                        if(!connectedEndpoints.contains(it)){
                            val endpoint = it.replace("[", "")
                            val endpoint2 = endpoint.replace("]", "")
                            connectedEndpoints.add(endpoint2)
                        }
                    }
                    Log.d("payload", "connectedendpoints: $connectedendpoints")
                }
                else{
                    //toast message
                    Toast.makeText(context, receivedMessage, Toast.LENGTH_SHORT).show()

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

    // Callbacks for connections to other devices

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                AlertDialog.Builder(context)
                    .setTitle("Accept connection to " + connectionInfo.endpointName)
                    .setMessage("Confirm the code matches on both devices: " + connectionInfo.authenticationDigits)
                    .setPositiveButton(
                        "Accept"
                    ) { dialog: DialogInterface?, which: Int ->  // The user confirmed, so we can accept the connection.
                        Nearby.getConnectionsClient(context!!)
                            .acceptConnection(endpointId, payloadCallback)
                        endpointName = connectionInfo.endpointName

                    }
                    .setNegativeButton(
                        android.R.string.cancel
                    ) { dialog: DialogInterface?, which: Int ->  // The user canceled, so we should reject the connection.
                        Nearby.getConnectionsClient(context!!).rejectConnection(endpointId)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show()
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.
                        Toast.makeText(
                            context,
                            "Connected to " + endpointId,
                            Toast.LENGTH_LONG
                        ).show()
                        //send to lobby
                        val lobbyFragment = Lobby()

                        val bundle = Bundle()

                        bundle.putString("host", endpointName)

                        bundle.putStringArrayList("connectedEndpoints", connectedEndpoints)
                        //convert connectionsClient so can be passed
                        val connectionsClientString = connectionsClient.toString()
                        bundle.putString("connectionsClient", connectionsClientString)

                        Log.d("bundle", bundle.toString())
                        lobbyFragment.arguments = bundle
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()

                        transaction.add(R.id.frameLayout, lobbyFragment,"Lobby")
                        //remove advertising fragment
                        transaction.hide(this@Join_group)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // The connection was rejected by one or both sides.
                        Toast.makeText(
                            context,
                            "Connection rejected by " + endpointName,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        // The connection broke before it was able to be accepted.
                        Toast.makeText(
                            context,
                            "Connection broke before it was able to be accepted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
                Toast.makeText(
                    context,
                    "Disconnected from $endpointName",
                    Toast.LENGTH_LONG
                ).show()

            }
        }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(requireContext())
            .startDiscovery(requireActivity().packageName, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { Toast.makeText(context, "Discovery started", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(context, "Discovery failed", Toast.LENGTH_SHORT).show() }
    }


    @CallSuper
    override fun onStart() {
        super.onStart()

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
    }
    override fun onStop() {
        super.onStop()
        connectionsClient.stopAllEndpoints()

    }

    private fun checkSelfPermission(permission: String): Int {
        return PermissionChecker.checkSelfPermission(requireContext(), permission)


    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
                    ActivityCompat.recreate(requireActivity())
                    return
                }
            }
            ActivityCompat.recreate(requireActivity())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        discoversList = ArrayList()
        discoversAdapter = HostAdapter(requireContext(), discoversList)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //change title
        val name= view.findViewById<TextView>(R.id.name)
        name.text=deviceName
        val progressBar = view.findViewById<View>(R.id.progressBar)

        discoversRecyclerView = view.findViewById(R.id.discovers)
        discoversRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        discoversRecyclerView.adapter = discoversAdapter
        discoverButton = view.findViewById(R.id.btnDiscovering)
        discoverButton.setOnClickListener {
            //check permissions
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_REQUIRED_PERMISSIONS
                )
            } else {
                connectionsClient = Nearby.getConnectionsClient(requireContext())
                if (discovering) {
                    // turn off discovering
                    progressBar.visibility = View.INVISIBLE
                    connectionsClient.stopDiscovery()

                    discoverButton.text = "Start Discovering"
                    discovering = false
                } else {
                    //turn progress bar on
                    progressBar.visibility = View.VISIBLE
                    startDiscovery()
                    discoverButton.text = "Stop Discovering"
                    discovering = true
                }

            }

        }

        //join button click
        discoversAdapter.setOnItemClickListener(object : HostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TAG", "onItemClick: clicked.")
                Log.d("item", discoversList[position].toString())
                val endpointId = discoversList[position].endpointId
                if (endpointId != null) {
                    connectionsClient.requestConnection(
                        "Host",
                        endpointId,
                        connectionLifecycleCallback
                    )
                        .addOnSuccessListener { unused: Void? -> }
                        .addOnFailureListener { e: java.lang.Exception? -> }
                }
            }
        })


    }

}


