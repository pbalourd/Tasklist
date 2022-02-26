package tasklist

val taskList = mutableListOf<String>()

fun main() {

    do {
        println("Input an action (add, print, end):")
        val action = readLine()!!
        when (action) {
            "add" -> add()
            "print" -> print()
            "end" -> println("Tasklist exiting!")
            else -> println("The input action is invalid")
        }
    } while (action != "end")

}

fun add() {
    println("Input a new task (enter a blank line to end):")
    val builder = StringBuilder()
    do {
        val line = readLine()!!.trim()
        if (line != "") builder.appendLine(line)
    } while (line != "")
    if (builder.toString().trim() == "") println("The task is blank")
    else taskList.add(builder.toString())
}

fun print() {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return
    }
    for ((num, task) in taskList.withIndex()) {
        val taskLines = task.split("\n").filter { it != "" }
        taskLines.forEachIndexed { index, s ->
            val prefix = if (index == 0)
                if (num < 9) "${num + 1}  "
                else "${num + 1} "
            else "   "
            println("$prefix$s")
        }
        println()
    }
}


