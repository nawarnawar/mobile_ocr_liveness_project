package com.regula.documentreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.regula.documentreader.api.enums.eGraphicFieldType

class ImagesFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val documentResults = (activity as? OcrResultsActivity)?.documentResults
        documentResults?.let {
            loadImages(view, it)
        }
    }
    
    private fun loadImages(view: View, documentResults: com.regula.documentreader.api.results.DocumentReaderResults) {
        val container = view.findViewById<LinearLayout>(R.id.imagesContainer)
        
        documentResults.graphicResult?.fields?.forEach { field ->
            when (field.fieldType) {
                eGraphicFieldType.GF_PORTRAIT -> {
                    addImageSection(container, "Portrait", field.bitmap)
                }
                eGraphicFieldType.GF_DOCUMENT_IMAGE -> {
                    addImageSection(container, "Document image", field.bitmap)
                }
                eGraphicFieldType.GF_SIGNATURE -> {
                    addImageSection(container, "Signature", field.bitmap)
                }
                else -> {
                    // Ajouter d'autres types d'images si nécessaire
                    val fieldName = getFieldTypeName(field.fieldType)
                    addImageSection(container, fieldName, field.bitmap)
                }
            }
        }
    }
    
    private fun addImageSection(container: LinearLayout, title: String, bitmap: android.graphics.Bitmap?) {
        val context = requireContext()
        
        // Créer le layout pour cette section d'image
        val sectionLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // Titre de la section
        val titleView = TextView(context).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        sectionLayout.addView(titleView)
        
        // ImageView pour afficher l'image
        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Hauteur fixe en pixels
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = context.getDrawable(android.R.color.darker_gray)
        }
        
        // Charger l'image si disponible
        bitmap?.let {
            imageView.setImageBitmap(it)
        }
        
        sectionLayout.addView(imageView)
        container.addView(sectionLayout)
    }
    
    private fun getFieldTypeName(fieldType: Int): String {
        return when (fieldType) {
            eGraphicFieldType.GF_PORTRAIT -> "Portrait"
            eGraphicFieldType.GF_DOCUMENT_IMAGE -> "Document image"
            eGraphicFieldType.GF_SIGNATURE -> "Signature"
            eGraphicFieldType.GF_FINGERPR -> "Empreinte digitale"
            else -> "Image $fieldType"
        }
    }
}

