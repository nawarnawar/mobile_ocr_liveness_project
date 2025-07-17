package com.regula.documentreader.api.enums

/**
 * Énumération pour les résultats de vérification des documents
 */
enum class eCheckResult(val value: Int) {
    CH_CHECK_OK(0),
    CH_CHECK_ERROR(1),
    CH_CHECK_WAS_NOT_DONE(2);

    companion object {
        /**
         * Convertit une valeur entière en eCheckResult
         * @param value La valeur entière à convertir
         * @return L'énumération correspondante ou CH_CHECK_WAS_NOT_DONE par défaut
         */
        @JvmStatic
        fun fromValue(value: Int): eCheckResult {
            return values().find { it.value == value } ?: CH_CHECK_WAS_NOT_DONE
        }

        /**
         * Méthode alternative pour la compatibilité
         */
        @JvmStatic
        fun valueOf(value: Int): eCheckResult {
            return fromValue(value)
        }
    }

    /**
     * Méthodes utilitaires pour vérifier l'état
     */
    fun isOk(): Boolean = this == CH_CHECK_OK
    fun isError(): Boolean = this == CH_CHECK_ERROR
    fun wasNotDone(): Boolean = this == CH_CHECK_WAS_NOT_DONE

    override fun toString(): String {
        return when (this) {
            CH_CHECK_OK -> "Check OK"
            CH_CHECK_ERROR -> "Check Error"
            CH_CHECK_WAS_NOT_DONE -> "Check Was Not Done"
        }
    }
}