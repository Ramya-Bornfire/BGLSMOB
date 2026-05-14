import java.text.SimpleDateFormat
import java.util.Locale

fun main() {
    val dateStr = "2024-08-07T00:00:00.000+00:00"
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    try {
        val date = inputFormat.parse(dateStr)
        println(outputFormat.format(date))
    } catch(e: Exception) {
        println("Error: ${e.message}")
    }
}
