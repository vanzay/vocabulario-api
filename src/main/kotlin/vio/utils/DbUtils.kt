package vio.utils

import java.time.ZoneId
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
        val localDate = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.year * 100 + localDate.monthValue
    }
}
