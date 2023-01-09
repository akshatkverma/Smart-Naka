package ethos.lifetime.smartnaka.dao

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import ethos.lifetime.smartnaka.models.User
import ethos.lifetime.smartnaka.models.VehicleLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.tasks.await

class VehiclesDao {

    private val dataBase = FirebaseFirestore.getInstance()
    private val vehicleCollection = dataBase.collection("stolenVehicles")
    private val logs=dataBase.collection("logs")
    private val userCollection = dataBase.collection("users")

    fun addUser(user: User) {
        user.let {
            GlobalScope.launch(Dispatchers.IO) {
                userCollection.document(user.uid).set(it)
            }
        }
    }

    fun getUser(uid:String, callback:(User) -> Unit) {
        val docRef = userCollection.document(uid)
        var user: User
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
                    user = document.toObject(User::class.java)!!
                    callback(user)
                } else {
                    Log.d("Firebase", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "get failed with ", exception)
            }
    }

    public fun addLog(uid: String, log: VehicleLog){
        val docRef = userCollection.document(uid)
        var user = User()
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
                    user = document.toObject(User::class.java)!!
                    user.logs.add(log)
                    addUser(user)
                } else {
                    Log.d("Firebase", "No such document")
                }
            }

    }

    suspend fun checkVehicle(
        registrationNumber: String,
        chassisNumber: String,
        engineNumber: String
    ): Boolean {
        var currentVal = false
        Log.w("firebase", "Function checkVehicle Called")
        // Checking if registration Number matches
        vehicleCollection
            .whereEqualTo("registrationNumber", registrationNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase", "Reached flag 1")
        if (currentVal)
            return currentVal

        // Checking if Chassis Number matches
        vehicleCollection
            .whereEqualTo("chassisNumber", chassisNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase", "Reached flag 2")
        if (currentVal)
            return currentVal


        // Checking if Engine Number matches
        vehicleCollection
            .whereEqualTo("engineNumber", engineNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0)
                    currentVal = true
                Log.w("Firebase", "${documents.size()}")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: $exception")
            }
            .await()

        Log.w("Firebase", "Reached flag 3")
        return currentVal
    }


}