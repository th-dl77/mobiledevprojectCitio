package edu.ap.citioios.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.models.Location
import com.google.firebase.auth.ktx.auth
import edu.ap.citioios.models.User
import edu.ap.citioios.models.City
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.Query
import edu.ap.citioios.models.Review
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

//TODO: Split repo in different repo's per subject, example authrepo, locationrepo, cityrepo

object FirebaseRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage


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

    fun checkLocationExists(
        locationName: String,
        cityId: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("locations")
            .whereEqualTo("name", locationName.trim())
            .whereEqualTo("cityId", cityId)
            .get()
            .addOnSuccessListener { result ->
                val exists = !result.isEmpty
                Log.d("Firestore", "Location existence check for '$locationName' in city '$cityId': $exists")
                onResult(exists)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error checking location existence for '$locationName'", e)
                onError(e)
            }
    }

    // City operations
    fun fetchCities(
        onSuccess: (List<City>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("cities")
            .orderBy("name")
            .get()
            .addOnSuccessListener { result ->
                val cities = result.documents.mapNotNull { document ->
                    document.toObject(City::class.java)
                }
                Log.d("Firestore", "Cities fetched successfully: ${cities.size}")
                onSuccess(cities)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching cities", e)
                onError(e)
            }
    }

    fun fetchLocationsByCityId(
        cityId: String,
        onSuccess: (List<Location>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("locations")
            .whereEqualTo("cityId", cityId)
            .get()
            .addOnSuccessListener { result ->
                val locations = result.documents.mapNotNull { document ->
                    document.toObject(Location::class.java)
                }
                Log.d("Firestore", "Locations for cityId $cityId fetched successfully: ${locations.size}")
                onSuccess(locations)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching locations for cityId $cityId", e)
                onError(e)
            }
    }

    fun checkCityExists(
        cityName: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("cities")
            .whereEqualTo("name", cityName.trim())
            .get()
            .addOnSuccessListener { result ->
                val exists = !result.isEmpty
                Log.d("Firestore", "City existence check for '$cityName': $exists")
                onResult(exists)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error checking city existence for '$cityName'", e)
                onError(e)
            }
    }

    fun saveCityToFirestore(
        city: City,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("cities")
            .add(city)
            .addOnSuccessListener {
                Log.d("Firestore", "New city successfully saved")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error saving new city", e)
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

    fun registerUser(
        email: String,
        password: String,
        displayName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update display name
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d("FirebaseAuth", "User profile updated")
                                onSuccess()
                            } else {
                                Log.w("FirebaseAuth", "Failed to update profile", profileTask.exception)
                                onSuccess()
                            }
                        }
                } else {
                    Log.w("FirebaseAuth", "Registration failed", task.exception)
                    task.exception?.let { onError(it) }
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Login successful")
                    onSuccess()
                } else {
                    Log.w("FirebaseAuth", "Login failed", task.exception)
                    task.exception?.let { onError(it) }
                }
            }
    }
    fun fetchReviewsForLocation(locationId: String, onSuccess: (List<Review>) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("locationId", locationId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    println("Fout bij het ophalen van reviews: $e")
                    return@addSnapshotListener
                }

                val reviews = querySnapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)
                } ?: emptyList()
                
                onSuccess(reviews)
            }
    }

    // Upload location image
    suspend fun uploadLocationImage(imageUri: Uri, locationId: String): String {
        try {
            val storageRef = storage.reference
            val imageRef = storageRef.child("locations/images/$locationId.jpg")
            
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            
            Log.d("FirebaseStorage", "Image uploaded successfully: $downloadUrl")
            return downloadUrl.toString()
        } catch (e: Exception) {
            Log.w("FirebaseStorage", "Error uploading image", e)
            throw e
        }
    }

}
