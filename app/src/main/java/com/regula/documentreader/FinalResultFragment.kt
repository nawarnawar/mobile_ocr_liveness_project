package com.regula.documentreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.regula.documentreader.api.enums.eVisualFieldType
import com.regula.documentreader.api.enums.eGraphicFieldType


class FinalResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_final_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Accéder aux résultats via l'activité parente
        val documentResults = (activity as? OcrResultsActivity)?.documentResults

        documentResults?.let {
            populateDocumentInfo(view, it)
            loadDocumentImage(view, it)
        }
    }

    private fun populateDocumentInfo(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val tvBirthDate = view.findViewById<TextView>(R.id.tvBirthDate)
        val tvDocumentNumber = view.findViewById<TextView>(R.id.tvDocumentNumber)
        val tvCountry = view.findViewById<TextView>(R.id.tvCountry)
        val tvDocumentType = view.findViewById<TextView>(R.id.tvDocumentType)

        documentResults.textResult?.fields?.forEach { field ->
            when (field.fieldType) {
                eVisualFieldType.FT_DATE_OF_BIRTH -> {
                    tvBirthDate.text = field.value
                }
                eVisualFieldType.FT_DOCUMENT_NUMBER -> {
                    tvDocumentNumber.text = field.value
                }
                eVisualFieldType.FT_ISSUING_STATE_NAME -> {
                    tvCountry.text = field.value
                }
                eVisualFieldType.FT_DOCUMENT_CLASS_NAME -> {
                    tvDocumentType.text = field.value
                }
            }

        }

        // Si le type de document n'est pas trouvé, utiliser une valeur par défaut
        if (tvDocumentType.text.isEmpty()) {
            tvDocumentType.text = "Document d'identité"
        }
    }

    private fun loadDocumentImage(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val ivDocumentImage = view.findViewById<ImageView>(R.id.ivDocumentImage)

        // Rechercher l'image complète du document
        documentResults.graphicResult?.fields?.forEach { field ->
            if (field.fieldType == eGraphicFieldType.GF_DOCUMENT_IMAGE) {
                field.bitmap?.let { bitmap ->
                    ivDocumentImage.setImageBitmap(bitmap)
                    return
                }
            }
        }

        // Si pas d'image complète, utiliser la première image disponible
        documentResults.graphicResult?.fields?.firstOrNull()?.bitmap?.let { bitmap ->
            ivDocumentImage.setImageBitmap(bitmap)
        }
    }
}

