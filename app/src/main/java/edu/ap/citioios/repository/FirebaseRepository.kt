package edu.ap.citioios.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.models.Location

object FirebaseRepository {
    private val db = Firebase.firestore

    fun saveNewLocationToFirestore(
        location: Location,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("locations")
            .add(location)
            .addOnSuccessListener {
                Log.d("Firestore", "Nieuwe locatie succesvol opgeslagen")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Fout bij het opslaan van nieuwe locatie", e)
                onError(e)
            }
    }
}