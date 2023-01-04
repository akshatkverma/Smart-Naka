package ethos.lifetime.smartnaka

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VehiclesDao {

    private val dataBase = FirebaseFirestore.getInstance()
    private val vehicleCollection = dataBase.collection("stolenVehicles")

    suspend fun checkVehicle(registrationNumber: String, chassisNumber: String, engineNumber: String) : Boolean{
        var currentVal = false
        Log.w("firebase", "Function checkVehicle Called")
        // Checking if registration Number matches
        vehicleCollection
            .whereEqualTo("registrationNumber", registrationNumber)
            .get()
            .addOnSuccessListener { documents->
                if(documents.size()>0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase","Reached flag 1")
        if(currentVal)
            return currentVal

        // Checking if Chassis Number matches
        vehicleCollection
            .whereEqualTo("chassisNumber", chassisNumber)
            .get()
            .addOnSuccessListener { documents->
                if(documents.size()>0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase","Reached flag 2")
        if(currentVal)
            return currentVal


        // Checking if Engine Number matches
        vehicleCollection
            .whereEqualTo("engineNumber", engineNumber)
            .get()
            .addOnSuccessListener { documents->
                if(documents.size()>0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase","Reached flag 3")
        return currentVal
    }
}