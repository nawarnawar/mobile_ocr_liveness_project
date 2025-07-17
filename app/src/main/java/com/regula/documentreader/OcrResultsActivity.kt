package com.regula.documentreader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.regula.documentreader.api.results.DocumentReaderResults

class OcrResultsActivity : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    lateinit var documentResults: DocumentReaderResults // Rendre public pour les fragments
    
    companion object {
        const val EXTRA_DOCUMENT_RESULTS = "document_results"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_results)
        
        // Récupérer les résultats du document
        documentResults = intent.getSerializableExtra(EXTRA_DOCUMENT_RESULTS) as DocumentReaderResults
        
        initViews()
        setupViewPager()
        setupTabLayout()
        populateDocumentHeader()
    }
    
    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
    
    private fun setupViewPager() {
        val adapter = ResultsPagerAdapter(this)
        viewPager.adapter = adapter
    }
    
    private fun setupTabLayout() {
        val tabTitles = arrayOf(
            "RÉSULTAT FINAL",
            "DONNÉES TEXTE", 
            "IMAGES",
            "CONTRÔLES DE SÉCURITÉ",
            "VÉRIFICATIONS"
        )
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    private fun populateDocumentHeader() {
        // Extraire le nom complet du document
        val fullName = OcrResultsHelper.getFullName(documentResults)
        findViewById<android.widget.TextView>(R.id.tvDocumentName).text = fullName
        
        // Définir les icônes de statut basées sur les résultats
        updateStatusIcons()
        
        // Charger la photo du document si disponible
        loadDocumentPhoto()
    }

    private fun updateStatusIcons() {
        val status1 = findViewById<android.widget.ImageView>(R.id.ivStatus1)
        val status2 = findViewById<android.widget.ImageView>(R.id.ivStatus2)
        val status3 = findViewById<android.widget.ImageView>(R.id.ivStatus3)

        // Get the overall status as Int
        val overallStatus = documentResults.status.overallStatus

        // Compare with the actual enum value using .value property
        val iconRes = when (overallStatus) {
            com.regula.documentreader.api.enums.eCheckResult.CH_CHECK_OK.value -> R.drawable.ic_check_circle
            else -> R.drawable.ic_error_circle
        }

        status1.setImageResource(iconRes)
        status2.setImageResource(iconRes)
        status3.setImageResource(iconRes)
    }
    
    private fun loadDocumentPhoto() {
        val documentPhoto = findViewById<android.widget.ImageView>(R.id.ivDocumentPhoto)
        
        // Rechercher l'image du portrait dans les résultats graphiques
        documentResults.graphicResult?.fields?.forEach { field ->
            if (field.fieldType == com.regula.documentreader.api.enums.eGraphicFieldType.GF_PORTRAIT) {
                field.bitmap?.let { bitmap ->
                    documentPhoto.setImageBitmap(bitmap)
                    return
                }
            }
        }
    }
}

class ResultsPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FinalResultFragment()
            1 -> TextDataFragment()
            2 -> ImagesFragment()
            3 -> SecurityChecksFragment()
            4 -> VerificationsFragment()
            else -> FinalResultFragment()
        }
    }
}

