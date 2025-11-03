package edu.ap.citioios.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.models.Location
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    fun registerUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = User(
                    uid = result.user?.uid ?: "",
                    email = result.user?.email ?: ""
                )
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = User(
                    uid = result.user?.uid ?: "",
                    email = result.user?.email ?: ""
                )
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: ""
            )
        } else {
            null
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

}