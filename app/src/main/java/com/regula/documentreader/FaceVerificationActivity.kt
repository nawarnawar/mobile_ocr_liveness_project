package com.regula.documentreader

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.regula.documentreader.databinding.ActivityFaceVerificationBinding
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.LivenessConfiguration
import com.regula.facesdk.request.MatchFacesRequest
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import com.regula.facesdk.enums.LivenessStatus
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse

class FaceVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceVerificationBinding
    private var documentPhoto: Bitmap? = null
    private var livenessPhoto: Bitmap? = null

    companion object {
        const val EXTRA_DOCUMENT_PHOTO = "extra_document_photo"
        const val RESULT_VERIFICATION_SUCCESS = Activity.RESULT_OK
        const val RESULT_VERIFICATION_FAILED = Activity.RESULT_CANCELED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupère la photo du document passée via l'intent
        val documentPhotoBytes = intent.getByteArrayExtra(EXTRA_DOCUMENT_PHOTO)
        if (documentPhotoBytes != null) {
            documentPhoto = BitmapFactory.decodeByteArray(documentPhotoBytes, 0, documentPhotoBytes.size)
            binding.imageViewDocument.setImageBitmap(documentPhoto)

            // Démarre automatiquement la détection de vivacité
            startLivenessProcess()
        } else {
            Toast.makeText(this, "Photo du document manquante.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Ce bouton peut rester visible ou être masqué selon ton UI
        binding.buttonStartLiveness.setOnClickListener {
            startLivenessProcess()
        }
    }

    private fun startLivenessProcess() {
        val configuration = LivenessConfiguration.Builder().build()

        FaceSDK.Instance().startLiveness(this, configuration) { response ->
            if (response.liveness == LivenessStatus.PASSED) {
                livenessPhoto = response.bitmap
                binding.imageViewLiveness.setImageBitmap(livenessPhoto)
                Toast.makeText(this, "Détection de vivacité réussie !", Toast.LENGTH_SHORT).show()
                compareFaces()
            } else {
                Toast.makeText(this, "Détection de vivacité échouée: ${response.exception?.message}", Toast.LENGTH_LONG).show()
                Log.e("FaceVerification", "Liveness failed: ${response.exception?.message}")
                setResult(RESULT_VERIFICATION_FAILED)
                finish()
            }
        }
    }

    private fun compareFaces() {
        if (documentPhoto == null || livenessPhoto == null) {
            Toast.makeText(this, "Photos pour comparaison manquantes.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_VERIFICATION_FAILED)
            finish()
            return
        }

        val images = listOf(
            MatchFacesImage(documentPhoto!!, ImageType.PRINTED),
            MatchFacesImage(livenessPhoto!!, ImageType.LIVE)
        )

        val request = MatchFacesRequest(images)

        FaceSDK.Instance().matchFaces(this, request) { response: MatchFacesResponse? ->
            if (response != null && response.exception == null) {
                val split = MatchFacesSimilarityThresholdSplit(response.results, 0.75)
                if (split.matchedFaces.isNotEmpty()) {
                    Toast.makeText(this, "Comparaison faciale réussie !", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_VERIFICATION_SUCCESS)
                } else {
                    Toast.makeText(this, "Comparaison échouée : visages non correspondants.", Toast.LENGTH_LONG).show()
                    setResult(RESULT_VERIFICATION_FAILED)
                }
            } else {
                Toast.makeText(this, "Erreur de comparaison : ${response?.exception?.message}", Toast.LENGTH_LONG).show()
                Log.e("FaceVerification", "Erreur matchFaces : ${response?.exception?.message}")
                setResult(RESULT_VERIFICATION_FAILED)
            }
            finish()
        }

    }
}
