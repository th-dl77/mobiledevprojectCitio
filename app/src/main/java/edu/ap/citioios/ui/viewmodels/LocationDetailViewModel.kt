package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import edu.ap.citioios.models.Review
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationDetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    fun submitReview(locationId: String, rating: Int, comment: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            println("Fout: Gebruiker niet ingelogd")
            return
        }

        val newReview = Review(
            locationId = locationId,
            userId = currentUserId,
            rating = rating,
            comment = comment
        )

        viewModelScope.launch {
            try {
                val reviewRef = firestore.collection("reviews").add(newReview).await()
                println("Review succesvol opgeslagen! Document ID: ${reviewRef.id}")

                updateLocationRating(locationId, rating)

            } catch (e: Exception) {
                println("Fout bij het opslaan/updaten van de review/locatie: $e")
            }
        }
    }

    private suspend fun updateLocationRating(locationId: String, newRating: Int) {
        val locationRef = firestore.collection("locations").document(locationId)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(locationRef)

                val oldReviewCount = snapshot.getLong("reviewCount") ?: 0
                val oldAverageRating = snapshot.getDouble("averageRating") ?: 0.0

                val oldTotalRatingSum = oldAverageRating * oldReviewCount
                val newTotalRatingSum = oldTotalRatingSum + newRating

                val newReviewCount = oldReviewCount + 1
                val newAverageRating = newTotalRatingSum / newReviewCount.toDouble()

                transaction.update(
                    locationRef,
                    mapOf(
                        "reviewCount" to newReviewCount,
                        "averageRating" to newAverageRating
                    )
                )

                newAverageRating
            }.await()

            println("Locatie $locationId succesvol bijgewerkt met nieuwe rating.")

        } catch (e: Exception) {
            println("Fout bij Transactie: $e")
        }
    }
}