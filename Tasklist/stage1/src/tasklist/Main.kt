package tasklist

val taskList = mutableListOf<String>()

fun main() {

    println("Input the tasks (enter a blank line to end):")
    do {
        val task = readLine()!!.trim()
        if (task != "") taskList.add(task)
    } while (task != "")

    if (taskList.isEmpty()) println("No tasks have been input")
    else {
        taskList.forEachIndexed { index, s ->
            val spaces = if (index < 9) "  " else " "
            println("${index + 1}$spaces$s")
        }
    }
}


