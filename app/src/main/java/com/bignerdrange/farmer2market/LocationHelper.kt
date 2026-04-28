package com.bignerdrange.farmer2market

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun detectLocation(context: Context, onResult: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown Area"
                            val country = addresses[0].countryName ?: ""
                            onResult("$city, $country")
                        } else {
                            onResult("Harare, Zimbabwe") // Fallback for simulation
                        }
                    } catch (e: Exception) {
                        onResult("Harare, Zimbabwe")
                    }
                } else {
                    onResult("Harare, Zimbabwe")
                }
            }
            .addOnFailureListener {
                onResult("Harare, Zimbabwe")
            }
    }
}
