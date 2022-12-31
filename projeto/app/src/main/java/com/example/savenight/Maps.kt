package com.example.savenight

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.lifecycle.ViewModelProvider
import com.example.savenight.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Maps : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionCode = 101
    private var mapReady = false



    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        mMap = googleMap
        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
        // see if we have location members on local storage
        // if we do, then we set markers for each of them
        // if we don't, then we don't do anything
        // get from local storage
        var sharedPref = requireActivity().getSharedPreferences("locations", Context.MODE_PRIVATE)
        Log.d("sharedPref", sharedPref.all.toString())
        if (sharedPref!=null) {

            // percorrer o sharedPref size
            for (i in 0 until sharedPref.all.size) {
                // get the location
                var location = sharedPref.getString("location", String.toString())
                Log.d("location", location.toString())
                if (location!=null) {
                    val name = location.split(":")[0]
                    val lat = location.split(":")[1].split(";")[0].toDouble()
                    val long = location.split(":")[1].split(";")[1].toDouble()
                    val latLng = LatLng(lat, long)
                    mMap.addMarker(MarkerOptions().position(latLng).title(name))

                }
            }



        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentLocationUser()
    }




    @SuppressLint("MissingPermission")
    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location==null){
                println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!location is null  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            }
            if (location != null) {

                currentLocation = location
                Toast.makeText(
                    requireContext(),
                    "${currentLocation.latitude} ${currentLocation.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
                val supportMapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                supportMapFragment.getMapAsync(callback)

            }

        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationUser()
                }
            }
        }
    }


}
