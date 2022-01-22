package vio.utils

import java.util.*

object DbUtils {

    fun prepareFullTextQuery(query: String): String {
        return query
            .split("\\s".toRegex())
            .filter { it.matches("\\w+".toRegex()) }
            .joinToString(" & ")
            { "${it.trim()}:*" }
    }

    fun getPartitionKey(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal[Calendar.YEAR] * 100 + cal[Calendar.MONTH]
    }
}
