package com.regula.documentreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.regula.documentreader.api.enums.eGraphicFieldType
import com.regula.documentreader.api.enums.eCheckResult

class SecurityChecksFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_security_checks, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val documentResults = (activity as? OcrResultsActivity)?.documentResults
        documentResults?.let {
            setupSecurityChecks(view, it)
            loadComparisonImages(view, it)
        }
    }
    
    private fun setupSecurityChecks(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val ivFaceComparisonStatus = view.findViewById<ImageView>(R.id.ivFaceComparisonStatus)
        val ivVisualCameraStatus = view.findViewById<ImageView>(R.id.ivVisualCameraStatus)
        val tvSimilarity = view.findViewById<TextView>(R.id.tvSimilarity)
        
        // Vérifier les résultats de comparaison faciale
        val faceComparisonResult = checkFaceComparison(documentResults)
        val visualCameraResult = checkVisualCamera(documentResults)
        val similarity = calculateSimilarity(documentResults)
        
        // Mettre à jour les icônes de statut
        ivFaceComparisonStatus.setImageResource(
            if (faceComparisonResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )
        
        ivVisualCameraStatus.setImageResource(
            if (visualCameraResult) R.drawable.ic_check_circle else R.drawable.ic_error_circle
        )
        
        // Afficher le pourcentage de similarité
        tvSimilarity.text = "${similarity}%"
    }
    
    private fun loadComparisonImages(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val ivCapturedImage = view.findViewById<ImageView>(R.id.ivCapturedImage)
        val ivReferenceImage = view.findViewById<ImageView>(R.id.ivReferenceImage)
        
        // Charger l'image capturée (portrait de la caméra)
        documentResults.graphicResult?.fields?.forEach { field ->
            when (field.fieldType) {
                eGraphicFieldType.GF_PORTRAIT -> {
                    field.bitmap?.let { bitmap ->
                        ivReferenceImage.setImageBitmap(bitmap)
                    }
                }
                // Vous pouvez ajouter d'autres types d'images selon vos besoins
            }
        }
        
        // Pour l'image capturée, vous devrez la passer depuis l'activité de capture
        // ou la stocker dans les résultats personnalisés
    }
    
    private fun checkFaceComparison(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Vérifier les résultats de comparaison faciale
        documentResults.textResult?.fields?.forEach { field ->
            field.comparisonList.forEach { comparison ->
                if (comparison.status == eCheckResult.CH_CHECK_OK.value) {
                    return true
                }
            }
        }
        return false
    }
    
    private fun checkVisualCamera(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Boolean {
        // Logique pour vérifier la comparaison visuel vs caméra
        // Cela dépend de votre implémentation spécifique
        return true // Valeur par défaut
    }
    
    private fun calculateSimilarity(documentResults: com.regula.documentreader.api.results.DocumentReaderResults): Int {
        // Calculer le pourcentage de similarité basé sur les résultats de comparaison
        // Cela dépend de votre implémentation spécifique
        
        // Pour l'exemple, retourner 98% comme dans la capture d'écran
        return 98
    }
}

