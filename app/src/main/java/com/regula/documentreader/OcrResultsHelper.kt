package com.regula.documentreader

import android.content.Context
import com.regula.documentreader.api.enums.eVisualFieldType
import com.regula.documentreader.api.enums.eCheckResult
import com.regula.documentreader.api.results.DocumentReaderResults
import com.regula.documentreader.api.results.DocumentReaderTextField

object OcrResultsHelper {

    /**
     * Extrait la valeur d'un champ spécifique des résultats
     */
    fun getFieldValue(results: DocumentReaderResults, fieldType: Int): String? {
        results.textResult?.fields?.forEach { field ->
            if (field.fieldType == fieldType) {
                return field.values.firstOrNull()?.value ?: ""
            }
        }
        return null
    }

    /**
     * Obtient le nom complet à partir des résultats
     */
    fun getFullName(results: DocumentReaderResults): String {
        val surname = getFieldValue(results, eVisualFieldType.FT_SURNAME) ?: ""
        val givenNames = getFieldValue(results, eVisualFieldType.FT_GIVEN_NAMES) ?: ""

        return if (surname.isNotEmpty() && givenNames.isNotEmpty()) {
            "$surname $givenNames"
        } else {
            surname.ifEmpty { givenNames.ifEmpty { "Nom non disponible" } }
        }
    }

    /**
     * Obtient le statut de validité d'un champ
     */
    fun getFieldValidityStatus(field: DocumentReaderTextField, sourceType: Int?): Any {
        field.validityList.forEach { validity ->
            if (sourceType == null || validity.sourceType == sourceType) {
                return validity.status
            }
        }
        return eCheckResult.CH_CHECK_WAS_NOT_DONE
    }

    /**
     * Convertit un statut de validité en symbole d'affichage
     */
    fun getStatusSymbol(status: eCheckResult): String {
        return when (status) {
            eCheckResult.CH_CHECK_OK -> "✓"
            eCheckResult.CH_CHECK_ERROR -> "✗"
            else -> "—"
        }
    }

    /**
     * Obtient la couleur appropriée pour un statut
     */
    fun getStatusColor(context: Context, status: eCheckResult): Int {
        return when (status) {
            eCheckResult.CH_CHECK_OK -> context.getColor(android.R.color.holo_green_dark)
            eCheckResult.CH_CHECK_ERROR -> context.getColor(android.R.color.holo_red_dark)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }

    /**
     * Vérifie si les résultats contiennent des données RFID
     */
    fun hasRfidData(results: DocumentReaderResults): Boolean {
        return results.rfidSessionData != null &&
                results.rfidSessionData!!.applications.isNotEmpty()
    }

    /**
     * Vérifie si les résultats contiennent des données de comparaison
     */
    fun hasComparisonData(results: DocumentReaderResults): Boolean {
        results.textResult?.fields?.forEach { field ->
            if (field.comparisonList.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    /**
     * Obtient le type de document reconnu
     */
    fun getDocumentType(results: DocumentReaderResults): String {
        val documentClass = getFieldValue(results, eVisualFieldType.FT_DOCUMENT_CLASS_NAME)
        val issuingState = getFieldValue(results, eVisualFieldType.FT_ISSUING_STATE_NAME)

        return when {
            documentClass != null && issuingState != null -> "$issuingState - $documentClass"
            documentClass != null -> documentClass
            issuingState != null -> "$issuingState - Document d'identité"
            else -> "Document d'identité"
        }
    }

    /**
     * Calcule un score de confiance global basé sur les vérifications
     */
    fun calculateConfidenceScore(results: DocumentReaderResults): Int {
        var totalChecks = 0
        var passedChecks = 0

        // Vérifier le statut global
        if (results.status.overallStatus == eCheckResult.CH_CHECK_OK.value) {
            passedChecks += 3
        }
        totalChecks += 3

        // Vérifier les champs texte
        results.textResult?.fields?.forEach { field ->
            field.validityList.forEach { validity ->
                totalChecks++
                if (validity.status == eCheckResult.CH_CHECK_OK.value) {
                    passedChecks++
                }
            }
        }

        return if (totalChecks > 0) {
            (passedChecks * 100) / totalChecks
        } else {
            0
        }
    }

    /**
     * Formate une date selon le format local
     */
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        // Ici vous pouvez ajouter une logique de formatage de date
        // selon vos besoins spécifiques
        return dateString
    }

    /**
     * Obtient une description lisible du type de champ
     */
    fun getFieldDescription(context: Context, fieldType: Int): String {
        return when (fieldType) {
            eVisualFieldType.FT_DOCUMENT_NUMBER -> "Numéro de document"
            eVisualFieldType.FT_DATE_OF_BIRTH -> "Date de naissance"
            eVisualFieldType.FT_SURNAME -> "Nom de famille"
            eVisualFieldType.FT_GIVEN_NAMES -> "Prénom(s)"
            eVisualFieldType.FT_ADDRESS -> "Adresse"
            eVisualFieldType.FT_ISSUING_STATE_NAME -> "État de délivrance"
            eVisualFieldType.FT_NATIONALITY -> "Nationalité"
            eVisualFieldType.FT_SEX -> "Sexe"
            eVisualFieldType.FT_DATE_OF_ISSUE -> "Date de délivrance"
            eVisualFieldType.FT_DATE_OF_EXPIRY -> "Date d'expiration"
            else -> "Champ $fieldType"
        }
    }
}