package com.example.myapp

import android.Manifest
import android.os.Environment
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import android.widget.TextView
import android.location.Location
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.io.FileWriter
import android.os.Looper

class Location : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var Latitude: TextView
    private lateinit var Longitude: TextView
    private lateinit var Altitude: TextView
    private lateinit var Current_time: TextView

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        UI()
        checkLocationPermission()
    }
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
            }
            else -> {
                requestPermissions()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastLocation()
                }
                return
            }
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    private fun UI(){
        Latitude = findViewById(R.id.Latitude)
        Longitude = findViewById(R.id.Longitude)
        Altitude = findViewById(R.id.Altitude)
        Current_time = findViewById(R.id.Current_time)
    }

    private fun SaveDataAndUpdateUI(location: Location){
        Latitude.text = "Latitude: ${location.latitude}"
        Longitude.text = "Longitude: ${location.longitude}"
        Altitude.text = "Altitude: ${location.altitude} meters"



        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date(location.time))

        Current_time.text = "Current Time: $currentTime"

        val locationData = LocationData(
            output_latitude = location.latitude,
            output_longitude = location.longitude,
            output_altitude = location.altitude,
            output_Current_time = location.time
        )
        saveLocationDataToJSON(locationData)
    }

    private fun saveLocationDataToJSON(locationData: LocationData) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "location_data.json")

        if (!file.exists()) {
            file.createNewFile()
        }

        FileWriter(file, true).use { writer ->
            writer.append("${gson.toJson(locationData)}\n")
        }
    }

    private fun getLastLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    SaveDataAndUpdateUI(it)
                }
            }
    }
}

data class LocationData(
    val output_latitude: Double,
    val output_longitude: Double,
    val output_altitude: Double,
    val output_Current_time: Long
)