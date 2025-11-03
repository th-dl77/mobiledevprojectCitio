package edu.ap.citioios.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.models.Location
import com.google.firebase.auth.ktx.auth
import edu.ap.citioios.models.User


object FirebaseRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth


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

    // Registration and login functions 
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: ""
            )
        } else {
            null
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

}