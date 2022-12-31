package com.example.savenight

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8


class Create: Fragment() {
    /**
     * Strategy for telling the Nearby Connections API how we want to discover and connect to
     * other nearby devices. A star shaped strategy means we want to discover multiple devices but
     * only connect to and communicate with one at a time.
     */
    private val STRATEGY = Strategy.P2P_STAR
    private var mListView: ListView? = null
    /**
     * Our handle to the [Nearby Connections API][ConnectionsClient].
     */
    private lateinit var connectionsClient: ConnectionsClient
    /**
     * The request code for verifying our call to [requestPermissions]. Recall that calling
     * [requestPermissions] leads to a callback to [onRequestPermissionsResult]
     */
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    // List of endpoints connected to us
    private val connectedEndpoints = ArrayList<String>()
    private var connectedNames =Array<String>(0){""}
    // Dict to save endpoint id and device name
    private val endpointDict = HashMap<String, String>()

    // Device name
    private val deviceName = android.os.Build.MODEL;

    private var advertising=false

    private var mMessageAdapter: ArrayAdapter<String>? = null


    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val receivedMessage = String(it, StandardCharsets.UTF_8)
                if (receivedMessage.contains("location")){
                Log.d("payload", "location received: $receivedMessage")
                //get location
                var location = receivedMessage.split("location")[1]
                //remove , from location
                val locationList = location.split(",")
                location=locationList[0]+";"+locationList[1]

                //save location in local storage
                val sharedPref = requireActivity().getSharedPreferences("locations", Context.MODE_PRIVATE)
                Log.d("sharedPref", sharedPref.toString())
                //get endpoint name
                val name= connectedNames[connectedEndpoints.indexOf(endpointId)]
                val store= name+ " : " + location
                with(sharedPref.edit()) {
                    putString("location", store)
                    commit()
                }
                Log.d("sharedPref", sharedPref.toString())


            } else {
                    Toast.makeText(
                        context,
                        "Received message: $receivedMessage",
                        Toast.LENGTH_LONG
                    ).show()
                    //get lobby fragment function
                    val lobbyFragment =
                        requireActivity().supportFragmentManager.findFragmentByTag("lobby") as Lobby
                    Log.d("lobbyFragment", lobbyFragment.toString())
                    val endpointname = connectedNames[connectedEndpoints.indexOf(endpointId)]
                    Log.d("endpointname", endpointname)
                    //add message to list
                    lobbyFragment.messageReceived(receivedMessage, endpointname)

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

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {

            //Alert dialog to accept or reject connection
            AlertDialog.Builder(context)
                .setTitle("Accept connection to " + info.endpointName)
                .setMessage("Confirm the code matches on both devices: " + info.authenticationDigits)
                .setPositiveButton(
                    "Accept"
                ) { dialog: DialogInterface?, which: Int ->  // The user confirmed, so we can accept the connection.
                    Nearby.getConnectionsClient(context!!)
                        .acceptConnection(endpointId, payloadCallback)
                    endpointDict[endpointId] = info.endpointName

                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog: DialogInterface?, which: Int ->  // The user canceled, so we should reject the connection.
                    Nearby.getConnectionsClient(context!!).rejectConnection(endpointId)
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                //on touch outside dialog box, dialog box will not close

                .show()
        }


        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                // add the endpoint to the list of connected endpoints

                connectedEndpoints.add(endpointId)
                Toast.makeText(
                    context,
                    "Connected to endpoint: $endpointId",
                    Toast.LENGTH_LONG
                ).show()
                // add to listview that we are connected to this endpoint
                val message = "Connected to endpoint: $endpointId"
                mMessageAdapter?.add(message)
                // send connected endpoints list to all connected endpoints
                //get names from endpointDict
                Log.d("endpointDict", endpointDict.toString())
                Log.d("connectedEndpoints", connectedEndpoints.toString())
                connectedNames = endpointDict.values.toTypedArray()
                Log.d("connectedNames", connectedNames.toString())
                val connectedEndpointsMessage= "connectedendpoints " + connectedNames.joinToString(",")
                val idsMessage = "endpointsIDS " + connectedEndpoints.joinToString(",")
                connectedEndpoints.forEach {
                    connectionsClient.sendPayload(it, Payload.fromBytes(connectedEndpointsMessage.toByteArray()))
                    connectionsClient.sendPayload(it, Payload.fromBytes(idsMessage.toByteArray()))
                }
                Log.d("connectedendpoints", connectedEndpointsMessage)


            } else {
                Toast.makeText(
                    context,
                    "Connection failed. Result code: " + result.status.statusCode,
                    Toast.LENGTH_LONG
                ).show()
                // add to listview that we are connected to this endpoint
                val message = "Connection failed. Result code: " + result.status.statusCode
                mMessageAdapter?.add(message)
                endpointDict.remove(endpointId)

            }
        }

        override fun onDisconnected(endpointId: String) {
            // remove the endpoint from the list of connected endpoints
            connectedEndpoints.remove(endpointId)
            Toast.makeText(
                context,
                "Disconnected from endpoint: $endpointId",
                Toast.LENGTH_LONG
            ).show()
            // add to listview that we are connected to this endpoint
            val message = "Disconnected from endpoint: $endpointId"
            mMessageAdapter?.add(message)
            endpointDict.remove(endpointId)
            connectedNames = endpointDict.values.toTypedArray()
            val connectedEndpointsMessage= "connectedendpoints " + connectedNames.joinToString(",")
            val idsMessage = "endpointsIDS " + connectedEndpoints.joinToString(",")
            // send connected endpoints list to all connected endpoints
            connectedEndpoints.forEach {
                connectionsClient.sendPayload(it, Payload.fromBytes(idsMessage.toByteArray()))
                connectionsClient.sendPayload(it, Payload.fromBytes(connectedEndpointsMessage.toByteArray()))
            }
            //get lobby fragment function
            val lobbyFragment = requireActivity().supportFragmentManager.findFragmentByTag("lobby") as Lobby
            Log.d("lobbyFragment", lobbyFragment.toString())
            val endpointname= ""
            Log.d("endpointname", endpointname)
            //add message to list
            val receivedMessage = "Disconnected from endpoint: $endpointId"
            lobbyFragment.messageReceived(receivedMessage, endpointname)


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        endpointDict[deviceName] = deviceName
        Log.d("endpointDictoncreate", endpointDict.toString())




    }

    private fun startAdvertising(){
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            deviceName,
            requireActivity().packageName,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            Toast.makeText(
                context,
                "Advertising started",
                Toast.LENGTH_LONG

            ).show()
            advertising=true
            Log.d("ADVERTISING FUNCTION", advertising.toString())
        }.addOnFailureListener { e ->
            Toast.makeText(
                context,
                "Advertising failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            advertising=false

        }


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        endpointDict[deviceName]=deviceName
        mListView = view.findViewById(R.id.listView)
        //initialize mListView
        mMessageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        mListView?.adapter = mMessageAdapter

        //change group name to device name
        val groupName = view.findViewById<TextView>(R.id.groupName)
        val name= "$deviceName's Group"
        groupName?.text = name
        //on click listener for start advertising button to start advertising
        val startAdvertisingButton = view.findViewById<View>(R.id.btnAdvertising)
        startAdvertisingButton?.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_REQUIRED_PERMISSIONS)
            } else {
                connectionsClient = Nearby.getConnectionsClient(requireContext())
                if (advertising==false){
                    connectedEndpoints.clear()
                    endpointDict.clear()
                    endpointDict[deviceName]=deviceName
                    startAdvertising()
                    advertising=true
                }
                else{
                    //close all endpoints
                    connectionsClient.stopAllEndpoints()
                    // clear connected endpoints
                    connectedEndpoints.clear()
                    endpointDict.clear()
                    endpointDict[deviceName]=deviceName
                    // clear listview
                    mListView?.adapter = null

                    connectionsClient.stopAdvertising()
                    Toast.makeText(
                        context,
                        "Advertising stopped",
                        Toast.LENGTH_LONG
                    ).show()
                    advertising=false
                }

                //wait for advertising to start

                Log.d("advertising",advertising.toString())

                if (advertising && startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text=="Start Advertising"){

                    // turn progress bar on
                    // get progress bar
                    val progressBar = view.findViewById<View>(R.id.progressBar)
                    progressBar?.visibility = View.VISIBLE
                    // turn visible start lobby button
                    val startLobbyButton = view.findViewById<View>(R.id.lobby)
                    startLobbyButton?.visibility = View.VISIBLE


                    //change button text to stop advertising
                    startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text = "Stop Advertising"

                }else if (!advertising && startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text=="Stop Advertising"){
                    //remove progressDialog
                    // get progress bar
                    val progressBar = view.findViewById<View>(R.id.progressBar)
                    progressBar?.visibility = View.INVISIBLE
                    // turn invisible start lobby button
                    val startLobbyButton = view.findViewById<View>(R.id.lobby)
                    startLobbyButton?.visibility = View.INVISIBLE
                    startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text = "Start Advertising"
                    //close all endpoints
                    connectionsClient.stopAllEndpoints()
                    // clear connected endpoints
                    connectedEndpoints.clear()
                    endpointDict.clear()
                    endpointDict[deviceName]=deviceName


                }else if (!advertising && startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text=="Start Advertising"){
                    //remove progressDialog
                    // get progress bar
                    val progressBar = view.findViewById<View>(R.id.progressBar)
                    progressBar?.visibility = View.INVISIBLE
                    // turn invisible start lobby button
                    val startLobbyButton = view.findViewById<View>(R.id.lobby)
                    startLobbyButton?.visibility = View.INVISIBLE
                    startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text = "Start Advertising"
                    //close all endpoints
                    connectionsClient.stopAllEndpoints()
                    // clear connected endpoints
                    connectedEndpoints.clear()
                    endpointDict.clear()
                    endpointDict[deviceName]=deviceName


                }else if (advertising && startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text=="Stop Advertising"){
                    //remove progressDialog
                    // get progress bar
                    val progressBar = view.findViewById<View>(R.id.progressBar)
                    progressBar?.visibility = View.VISIBLE
                    // turn visible start lobby button
                    val startLobbyButton = view.findViewById<View>(R.id.lobby)
                    startLobbyButton?.visibility = View.VISIBLE
                    startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text = "Stop Advertising"

                }else{
                    //remove progressDialog
                    // get progress bar
                    val progressBar = view.findViewById<View>(R.id.progressBar)
                    progressBar?.visibility = View.INVISIBLE
                    // turn invisible start lobby button
                    val startLobbyButton = view.findViewById<View>(R.id.lobby)
                    startLobbyButton?.visibility = View.INVISIBLE
                    startAdvertisingButton.findViewById<Button>(R.id.btnAdvertising)?.text = "Start Advertising"
                    //close all endpoints
                    connectionsClient.stopAllEndpoints()
                    // clear connected endpoints
                    connectedEndpoints.clear()
                    endpointDict.clear()
                    endpointDict[deviceName]=deviceName



                }
            }

        }
        //on click listener for start lobby button to start lobby
        val startLobbyButton = view.findViewById<View>(R.id.lobby)
        startLobbyButton?.setOnClickListener {

            advertising = false
            //start lobby fragment and pass connected endpoints and device name
            val lobbyFragment = Lobby()

            val bundle = Bundle()

            bundle.putString("host", deviceName)
            bundle.putStringArrayList("endpointsIDS", connectedEndpoints)

            bundle.putStringArray("connectedEndpoints", connectedNames)
            //convert connectionsClient so can be passed
            val connectionsClientString = connectionsClient.toString()
            bundle.putString("connectionsClient", connectionsClientString)
            lobbyFragment.arguments = bundle
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            transaction.add(R.id.frameLayout, lobbyFragment,"lobby")
            //remove advertising fragment
            transaction.hide(this)
            transaction.addToBackStack("lobby")
            transaction.commit()
        }


    }



}
