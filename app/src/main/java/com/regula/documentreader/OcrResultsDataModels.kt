package com.regula.documentreader

import android.graphics.Bitmap
import com.regula.documentreader.api.enums.eCheckResult
import java.io.Serializable

/**
 * Modèle de données pour un élément de données texte
 */
data class TextDataItem(
    val fieldName: String,
    val fieldValue: String,
    val status: String,
    val sourceType: Int? = null,
    val pageIndex: Int? = null,
    val lcid: Int? = null
) : Serializable

/**
 * Modèle de données pour les informations du document
 */
data class DocumentInfo(
    val fullName: String,
    val documentNumber: String,
    val birthDate: String,
    val country: String,
    val documentType: String,
    val portraitImage: Bitmap? = null,
    val documentImage: Bitmap? = null
) : Serializable

/**
 * Modèle de données pour les vérifications de sécurité
 */
data class SecurityChecks(
    val faceComparisonStatus: eCheckResult,
    val visualCameraStatus: eCheckResult,
    val similarityPercentage: Int,
    val capturedImage: Bitmap? = null,
    val referenceImage: Bitmap? = null
) : Serializable

/**
 * Modèle de données pour les vérifications biométriques
 */
data class BiometricVerification(
    val faceComparisonStatus: eCheckResult,
    val livenessTestStatus: eCheckResult
) : Serializable

/**
 * Modèle de données pour les vérifications du document
 */
data class DocumentVerification(
    val documentTypeStatus: eCheckResult,
    val textFieldsStatus: eCheckResult,
    val imageQualityStatus: eCheckResult,
    val authenticityStatus: eCheckResult
) : Serializable

/**
 * Modèle de données pour une image du document
 */
data class DocumentImageItem(
    val title: String,
    val image: Bitmap? = null,
    val fieldType: Int
) : Serializable

/**
 * Modèle de données pour les résultats de comparaison
 */
data class ComparisonResult(
    val fieldName: String,
    val sourceType1: Int,
    val sourceType2: Int,
    val comparisonStatus: eCheckResult,
    val value1: String? = null,
    val value2: String? = null
) : Serializable

/**
 * Modèle de données pour les données RFID
 */
data class RfidDataItem(
    val dataGroupName: String,
    val status: eCheckResult,
    val data: String? = null
) : Serializable

/**
 * Modèle de données consolidé pour tous les résultats OCR
 */
data class OcrResultsData(
    val documentInfo: DocumentInfo,
    val textDataItems: List<TextDataItem>,
    val securityChecks: SecurityChecks,
    val biometricVerification: BiometricVerification,
    val documentVerification: DocumentVerification,
    val documentImages: List<DocumentImageItem>,
    val comparisonResults: List<ComparisonResult>,
    val rfidData: List<RfidDataItem>,
    val overallStatus: eCheckResult,
    val confidenceScore: Int
) : Serializable

/**
 * Énumération pour les types d'onglets
 */
enum class TabType {
    FINAL_RESULT,
    TEXT_DATA,
    IMAGES,
    SECURITY_CHECKS,
    VERIFICATIONS
}

/**
 * Modèle de données pour les statuts avec icônes
 */
data class StatusItem(
    val label: String,
    val status: eCheckResult,
    val iconRes: Int? = null,
    val value: String? = null
) : Serializable

/**
 * Extension pour obtenir une représentation textuelle du statut
 */
fun eCheckResult.toDisplayString(): String {
    return when (this) {
        eCheckResult.CH_CHECK_OK -> "✓"
        eCheckResult.CH_CHECK_ERROR -> "✗"
        eCheckResult.CH_CHECK_WAS_NOT_DONE -> "—"
        else -> "?"
    }
}

/**
 * Extension pour obtenir une couleur basée sur le statut
 */
fun eCheckResult.getColorRes(): Int {
    return when (this) {
        eCheckResult.CH_CHECK_OK -> android.R.color.holo_green_dark
        eCheckResult.CH_CHECK_ERROR -> android.R.color.holo_red_dark
        else -> android.R.color.darker_gray
    }
}

/**
 * Extension pour vérifier si le statut est positif
 */
fun eCheckResult.isSuccess(): Boolean {
    return this == eCheckResult.CH_CHECK_OK
}

/**
 * Fonction d’aide pour convertir un Int en eCheckResult de façon sûre
 */
fun eCheckResult.Companion.fromInt(value: Int): eCheckResult {
    return try {
        eCheckResult.fromValue(value)
    } catch (ex: Exception) {
        eCheckResult.CH_CHECK_WAS_NOT_DONE
    }
}