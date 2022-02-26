package tasklist

import kotlinx.datetime.*

data class TaskData(val priority: String, val dateTime: LocalDateTime, val task: String)

val taskList = mutableListOf<TaskData>()

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
    var priority = ""
    do {
        println("Input the task priority (C, H, N, L):")
        priority = readLine()!!.lowercase()
    } while (priority !in listOf("c", "h", "n", "l"))

    lateinit var date: LocalDate
    do {
        var validDate = true
        println("Input the date (yyyy-mm-dd):")
        val dateStr = readLine()!!
        val dateComponents = dateStr.trim().split("-")
        if (dateComponents.size != 3) {
            validDate = false
            println("The input date is invalid")
            continue
        }
        val year = dateComponents[0].toIntOrNull()
        val month = dateComponents[1].toIntOrNull()
        val day = dateComponents[2].toIntOrNull()
        if (year == null || month == null || day == null) {
            validDate = false
            println("The input date is invalid")
            continue
        }
        try {
            date = LocalDate(year, month, day)
        } catch (e: Exception) {
            // IllegalArgumentException
            validDate = false
            println("The input date is invalid")
        }
    } while (!validDate)

    var hour = -1
    var minutes = -1
    do {
        var validTime = true
        println("Input the time (hh:mm):")
        val timeStr = readLine()!!
        val timeComponents = timeStr.trim().split(":")
        if (timeComponents.size != 2) {
            validTime = false
            println("The input time is invalid")
            continue
        }
        hour = timeComponents[0].toIntOrNull() ?: -1
        minutes = timeComponents[1].toIntOrNull() ?: -1
        if (hour !in 0..23 || minutes !in 0..59) {
            validTime = false
            println("The input time is invalid")
            continue
        }
    } while (!validTime)

    println("Input a new task (enter a blank line to end):")
    val builder = StringBuilder()
    do {
        val line = readLine()!!.trim()
        if (line != "") builder.appendLine(line)
    } while (line != "")
    if (builder.toString().trim() == "") println("The task is blank")
    else {
        val addTask = TaskData(
            priority.uppercase(),
            LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minutes),
            builder.toString()
        )
        taskList.add(addTask)
    }
}

fun print() {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return
    }
    for ((num, task) in taskList.withIndex()) {
        val taskLines = task.task.split("\n").filter { it != "" }
        val prefix = if (num < 9) "${num + 1}  "
        else "${num + 1} "
        val dt = task.dateTime.toString().split("T")
        println("$prefix${dt[0]} ${dt[1]} ${task.priority}")
        taskLines.forEachIndexed { index, s ->
            println("   $s")
        }
        println()
    }
}


