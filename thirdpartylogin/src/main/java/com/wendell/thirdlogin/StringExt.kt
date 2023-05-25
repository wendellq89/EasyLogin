/**
 *
 * @author Wendell
 * 2023/5/24 11:27
 */
fun String.getFirstAndLastName(): Pair<String, String> {
    val nameArray = this.trim().split(" ")
    if (nameArray.size > 1) {
        return Pair(nameArray[0], nameArray[1])
    } else {
        return Pair("", this)
    }
}