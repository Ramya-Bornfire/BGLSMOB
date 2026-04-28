// Extensions.kt
import java.text.SimpleDateFormat
import java.util.*

fun parseIsoTimestamp(timestamp: String?): Date? {
    if (timestamp.isNullOrEmpty()) return null
    return try {
        // Normalize +0000 to +00:00 for SimpleDateFormat
        val normalized = timestamp.replace(Regex("(\\+\\d{2})(\\d{2})$"), "$1:$2")
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        isoFormat.parse(normalized)
    } catch (e: Exception) {
        // Fallback: try older format without milliseconds or colon in offset
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            format.parse(timestamp)
        } catch (ex: Exception) { null }
    }
}

fun formatAuditDateFromIso(timestamp: String?): String {
    val date = parseIsoTimestamp(timestamp) ?: return timestamp ?: ""
    return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
}

fun formatEntryTimeFromIso(timestamp: String?): String {
    val date = parseIsoTimestamp(timestamp) ?: return timestamp ?: ""
    val calendar = Calendar.getInstance().apply { time = date }
    val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val ampm = if (hour24 in 0..11) "AM" else "PM"
    // Format hour and minute with leading zeros
    return String.format("%02d:%02d %s", hour24, minute, ampm)
}
