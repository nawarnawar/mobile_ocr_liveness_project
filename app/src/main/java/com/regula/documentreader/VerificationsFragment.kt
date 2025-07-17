package com.regula.documentreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.regula.documentreader.api.enums.eCheckResult

class VerificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val documentResults = (activity as? OcrResultsActivity)?.documentResults
        documentResults?.let {
            setupBiometricVerification(view, it)
            setupDocumentVerification(view, it)
        }
    }

    private fun setupBiometricVerification(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val ivBiometricFaceComparison = view.findViewById<ImageView>(R.id.ivBiometricFaceComparison)
        val ivLivenessTest = view.findViewById<ImageView>(R.id.ivLivenessTest)

        // Vérification de la comparaison faciale biométrique
        val faceComparisonResult = checkBiometricFaceComparison(documentResults)
        ivBiometricFaceComparison.setImageResource(
            if (faceComparisonResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )

        // Test de vitalité
        val livenessResult = checkLivenessTest(documentResults)
        ivLivenessTest.setImageResource(
            if (livenessResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )
    }

    private fun setupDocumentVerification(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val ivDocumentType = view.findViewById<ImageView>(R.id.ivDocumentType)
        val tvTextFields = view.findViewById<TextView>(R.id.tvTextFields)
        val ivImageQuality = view.findViewById<ImageView>(R.id.ivImageQuality)
        val tvAuthenticity = view.findViewById<TextView>(R.id.tvAuthenticity)

        // Type de document reconnu
        val documentTypeResult = checkDocumentType(documentResults)
        ivDocumentType.setImageResource(
            if (documentTypeResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )

        // Champs texte
        val textFieldsResult = checkTextFields(documentResults)
        tvTextFields.text = if (textFieldsResult) "✓" else "—"

        // Qualité d'image
        val imageQualityResult = checkImageQuality(documentResults)
        ivImageQuality.setImageResource(
            if (imageQualityResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )

        // Authenticité
        val authenticityResult = checkAuthenticity(documentResults)
        tvAuthenticity.text = if (authenticityResult) "✓" else "—"
    }

    private fun checkBiometricFaceComparison(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier les résultats de comparaison faciale biométrique
        return documentResults.status.overallStatus == eCheckResult.CH_CHECK_OK.value
    }

    private fun checkLivenessTest(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier les résultats du test de vitalité
        // Cela dépend de votre implémentation de détection de vivacité
        return true // Valeur par défaut
    }

    private fun checkDocumentType(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier si le type de document a été reconnu correctement
        return documentResults.status.detailsOptical.docType == 1 // CH_CHECK_OK constant value
    }

    private fun checkTextFields(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier la validité des champs texte
        var hasValidFields = false
        documentResults.textResult?.fields?.forEach { field ->
            field.validityList.forEach { validity ->
                if (validity.status == eCheckResult.CH_CHECK_OK.value) {
                    hasValidFields = true
                }
            }
        }
        return hasValidFields
    }

    private fun checkImageQuality(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier la qualité de l'image
        return documentResults.status.detailsOptical.imageQA == 1 // CH_CHECK_OK constant value
    }

    private fun checkAuthenticity(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier l'authenticité du document
        return documentResults.status.detailsOptical.security == 1 // CH_CHECK_OK constant value
    }
}