package com.example.remainderapplication


import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import java.util.Locale



class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
GoogleMap.OnMapLongClickListener  {
    // Late-initialized properties for two EditText elements and the GoogleMap.
    private lateinit var txtLatitude: TextView
    private lateinit var txtLongitude: TextView
    private lateinit var txtAddress: TextView
    private lateinit var mMap: GoogleMap

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode =101

    private lateinit var autocompleteFragment:AutocompleteSupportFragment

    // onCreate is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)


        Places.initialize(applicationContext,getString(R.string.google_maps_key))
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                Toast.makeText(this@MapActivity,"Some error in Search",Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {

                // val add = place.address
                //val idd = place.id
                val latLng = place.latLng!!
                val address = getAddressFromLatLng(latLng)
                // Sets the latitude and longitude in the EditText fields.
                txtLatitude.setText(latLng.latitude.toString())
                txtLongitude.setText(latLng.longitude.toString())
                txtAddress.setText(address)


                // Clears any existing markers and adds a new marker at the clicked location.
                mMap.clear()
                val location = LatLng(latLng.latitude, latLng.longitude)
                mMap.addMarker(MarkerOptions().position(location).title(""))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                zooOnMap(latLng)
            }

        })

        // Initializes the EditText fields by finding them in the layout.
        txtLatitude=findViewById(R.id.textLatitude)
        txtLongitude=findViewById(R.id.txtLongitude)
        txtAddress=findViewById(R.id.textAddress)

        val addressbuttonsave=findViewById<Button>(R.id.saveaddressbutton)
        addressbuttonsave.setOnClickListener {
            val data:String= txtAddress.text.toString()
            val sendDataIntent= Intent(this,HubActivity::class.java)
            sendDataIntent.putExtra("EXTRA_LATITUDE",data)
            startActivity(sendDataIntent)


        }


        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationUser()


    }

    private fun zooOnMap(latLng: LatLng)
    {
    val newLatLngZoom= CameraUpdateFactory.newLatLngZoom(latLng,17f)//Amount of zoom
        mMap.animateCamera(newLatLngZoom)
    }
    private fun getCurrentLocationUser() {
        if(ActivityCompat.checkSelfPermission(
            this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
                return
            }
        val getLocation=fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            location ->

            if(location != null){
                currentLocation=location


                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){

            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED){

                getCurrentLocationUser()
            }
        }
    }


    // onMapReady is called when the Google Map is available for use.
    override fun onMapReady(googleMap: GoogleMap) {
        mMap= googleMap


        // Sets up listeners for click and long click events on the map.
        this.mMap.setOnMapClickListener(this)
        this.mMap.setOnMapLongClickListener(this)

        // Defines a location (in this case, a location in Mexico) and moves the camera to it.
        val userLocation = LatLng(currentLocation.latitude,currentLocation.longitude)
        mMap.addMarker(MarkerOptions().position(userLocation).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
        txtLatitude.setText(currentLocation.latitude.toString())
        txtLongitude.setText(currentLocation.longitude.toString())
        // Get and set the address
        val address = getAddressFromLatLng(userLocation)
        txtAddress.setText(address)

    }

    // onMapClick is called when the user clicks on the map.
    override fun onMapClick(latLng: LatLng) {
        val address = getAddressFromLatLng(latLng)

        // Sets the latitude and longitude in the EditText fields.
        txtLatitude.setText(latLng.latitude.toString())
        txtLongitude.setText(latLng.longitude.toString())
        txtAddress.setText(address)


        // Clears any existing markers and adds a new marker at the clicked location.
        mMap.clear()
        val location = LatLng(latLng.latitude, latLng.longitude)
        mMap.addMarker(MarkerOptions().position(location).title(""))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

    }

    // onMapLongClick is called when the user long-clicks on the map.
    override fun onMapLongClick(latLng: LatLng) {
        val address = getAddressFromLatLng(latLng)
        // Similar to onMapClick, sets the latitude and longitude in the EditText fields
        // and updates the map with a new marker at the long-clicked location.
        txtLatitude.setText(latLng.latitude.toString())
        txtLongitude.setText(latLng.longitude.toString())
        txtAddress.setText(address)

        mMap.clear()
        val location = LatLng(latLng.latitude, latLng.longitude)
        mMap.addMarker(MarkerOptions().position(location).title(""))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))


    }
    // Function to perform reverse geocoding
    private fun getAddressFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            @Suppress("DEPRECATION") val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    return addresses[0].getAddressLine(0) // Return the first address line
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "No address found"
    }

}