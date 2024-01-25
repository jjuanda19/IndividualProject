package com.example.remainderapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
GoogleMap.OnMapLongClickListener  {

    lateinit var txtLatitud: EditText
    lateinit var txtLongitud: EditText
    lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        txtLatitud=findViewById(R.id.textLatitud)
        txtLongitud=findViewById(R.id.txtLongitud)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap= googleMap
        this.mMap.setOnMapClickListener(this)
        this.mMap.setOnMapLongClickListener(this)

        val mexico = LatLng(19.8077463, -99.4077038)
        mMap.addMarker(MarkerOptions().position(mexico).title("México"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mexico))
    }

    override fun onMapClick(latLng: LatLng) {
        txtLatitud.setText(latLng.latitude.toString())
        txtLongitud.setText(latLng.longitude.toString())

        mMap.clear()
        val location = LatLng(latLng.latitude, latLng.longitude)
        mMap.addMarker(MarkerOptions().position(location).title(""))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

    }

    override fun onMapLongClick(latLng: LatLng) {
        txtLatitud.setText(latLng.latitude.toString())
        txtLongitud.setText(latLng.longitude.toString())

        mMap.clear()
        val location = LatLng(latLng.latitude, latLng.longitude)
        mMap.addMarker(MarkerOptions().position(location).title(""))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))


    }
}