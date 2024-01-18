package com.example.remainderapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCoarseLocationPermissionGranted = false
    private var isFineLocationPermissionGranted = false
    private var isBackgroundLocationPermissionGranted = false
    private var isInternetPermissionGranted = false
    private var isWakeLockPermissionGranted = false
    private var isReadExternalStoragePermissionGranted = false
    private var isWriteExternalStoragePermissionGranted = false
    private var isManageExternalStoragePermissionGranted = false
    private var isNetworkPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isCoarseLocationPermissionGranted =permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isCoarseLocationPermissionGranted
            isFineLocationPermissionGranted =permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isFineLocationPermissionGranted
            isBackgroundLocationPermissionGranted =permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: isBackgroundLocationPermissionGranted
            isInternetPermissionGranted =permissions[Manifest.permission.INTERNET] ?: isInternetPermissionGranted
            isWakeLockPermissionGranted =permissions[Manifest.permission.WAKE_LOCK] ?: isWakeLockPermissionGranted
            isReadExternalStoragePermissionGranted =permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadExternalStoragePermissionGranted
            isWriteExternalStoragePermissionGranted =permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteExternalStoragePermissionGranted
            isManageExternalStoragePermissionGranted =permissions[Manifest.permission.MANAGE_EXTERNAL_STORAGE] ?: isManageExternalStoragePermissionGranted
            isNetworkPermissionGranted =permissions[Manifest.permission.ACCESS_NETWORK_STATE] ?: isNetworkPermissionGranted

        }
        requestPermission()

        val buttonAddReminder = findViewById<Button>(R.id.buttonAddReminder)
        buttonAddReminder.setOnClickListener {
            val intent = Intent(this, HubActivity::class.java)
            startActivity(intent)
        }

    }

    private fun requestPermission() {
        isCoarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isFineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isBackgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isInternetPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        isWakeLockPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WAKE_LOCK
        ) == PackageManager.PERMISSION_GRANTED

        isReadExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteExternalStoragePermissionGranted= ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isManageExternalStoragePermissionGranted= ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isNetworkPermissionGranted= ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest : MutableList<String> = ArrayList()

        if(!isCoarseLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }
        if(!isFineLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(!isBackgroundLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if(!isInternetPermissionGranted) {
            permissionRequest.add(Manifest.permission.INTERNET)
        }
        if(!isWakeLockPermissionGranted) {
            permissionRequest.add(Manifest.permission.WAKE_LOCK)
        }
        if(!isReadExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!isWriteExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!isManageExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
        if(!isNetworkPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }
}