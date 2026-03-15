package vio.domain

enum class Language {
    ru, en, es;

    companion object {

        fun of(iso2: String?): Language? {
            if (iso2 == null) {
                return null
            }
            return try {
                valueOf(iso2)
            } catch (_: Exception) {
                null
            }
        }

        fun getOrDefault(iso2: String?): Language {
            return of(iso2) ?: en
        }
    }
}
