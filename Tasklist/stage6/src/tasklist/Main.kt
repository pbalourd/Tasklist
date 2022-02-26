package tasklist

import com.squareup.moshi.*
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File

data class TaskData(var priority: String, var dateTime: LocalDateTime, var task: String)

val taskList = mutableListOf<TaskData>()

fun main() {
    loadData(taskList)

    do {
        println("Input an action (add, print, edit, delete, end):")
        val action = readLine()!!
        when (action) {
            "add" -> add()
            "print" -> print()
            "edit" -> edit()
            "delete" -> delete()
            "end" -> {
                saveData(taskList)
                println("Tasklist exiting!")
            }
            else -> println("The input action is invalid")
        }
    } while (action != "end")
}

data class JsonData(val priority: String, val date: String, val time: String, val task: String)
fun loadData(taskList: MutableList<TaskData>) {
    val jsonFile = File("tasklist.json")
    if (!jsonFile.exists()) return

    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type = Types.newParameterizedType(List::class.java, JsonData::class.java)
    val taskListAdapter = moshi.adapter<List<JsonData?>>(type)
    val jsonStr = jsonFile.readText()
    if (jsonStr.isBlank()) return
    val jsonList = taskListAdapter.fromJson(jsonStr)
    if (jsonList == null || jsonList.isEmpty()) return
    jsonList.forEach {
        val (y, m, d) = it?.date!!.split("-").map { it.toInt() }
        val (h, min) = it?.time!!.split(":").map { it.toInt() }
        taskList.add(TaskData(it.priority, LocalDateTime(y, m, d, h, min), it.task))
    }
}

fun saveData(taskList: MutableList<TaskData>) {
    val jsonList = mutableListOf<JsonData>()

    taskList.forEach {
        val (d, t) = it.dateTime.toString().split("T")
        jsonList.add(JsonData(it.priority, d, t, it.task)) }
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type = Types.newParameterizedType(List::class.java, JsonData::class.java)
    val taskListAdapter = moshi.adapter<List<JsonData?>>(type).indent("    ")
    val jsonFile = File("tasklist.json")
    jsonFile.writeText(taskListAdapter.toJson(jsonList))
}

fun edit() {
    val taskNumber = getTaskNumber()
    if (taskNumber == 0) return
    val fields = listOf("priority", "date", "time", "task")
    var field = ""
    do {
        println("Input a field to edit (priority, date, time, task):")
        field = readLine()!!
        when (field) {
            "priority" -> {
                val pr = getPriority()
                taskList[taskNumber - 1].priority = pr.uppercase()
            }
            "date" -> {
                val d = getDate()
                taskList[taskNumber - 1].dateTime = LocalDateTime(
                    d.year,
                    d.monthNumber,
                    d.dayOfMonth,
                    taskList[taskNumber - 1].dateTime.hour,
                    taskList[taskNumber - 1].dateTime.minute
                )
            }
            "time" -> {
                val t = getTime()
                taskList[taskNumber - 1].dateTime = LocalDateTime(
                    taskList[taskNumber - 1].dateTime.year,
                    taskList[taskNumber - 1].dateTime.monthNumber,
                    taskList[taskNumber - 1].dateTime.dayOfMonth,
                    t.first,
                    t.second
                )
            }
            "task" -> {
                val tsk = getTask()
                taskList[taskNumber - 1].task = tsk
            }
            else -> println("Invalid field")
        }
    } while (field !in fields)
    println("The task is changed")
}

fun delete() {
    val taskNumber = getTaskNumber()
    if (taskNumber == 0) return
    taskList.removeAt(taskNumber - 1)
    println("The task is deleted")
}

fun add() {
    val priority = getPriority()
    val date: LocalDate = getDate()
    val (hour, minutes) = getTime()
    val taskString = getTask()
    if (taskString == "") println("The task is blank")
    else {
        val addTask = TaskData(
            priority.uppercase(),
            LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minutes),
            taskString
        )
        taskList.add(addTask)
    }
}

private fun getTask(): String {
    println("Input a new task (enter a blank line to end):")
    val builder = StringBuilder()
    do {
        val line = readLine()!!.trim()
        if (line != "") builder.appendLine(line)
    } while (line != "")
    val taskString = builder.toString().trim()
    return taskString
}

fun print() {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return
    }
    val lineStr   = "+----+------------+-------+---+---+--------------------------------------------+"
    val header    = "| N  |    Date    | Time  | P | D |                   Task                     |"
    val multiLine = "|    |            |       |   |   |"
    println(lineStr)
    println(header)
    println(lineStr)
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+2")).date
    for ((num, task) in taskList.withIndex()) {
        val taskLines = task.task.split("\n").filter { it != "" }.map { it.chunked(44) }.flatten()
        val taskDate = task.dateTime.date
        val daysUntil = currentDate.daysUntil(taskDate)
        val dueTag = when {
            daysUntil > 0 -> "\u001B[102m \u001B[0m"
            daysUntil < 0 -> "\u001B[101m \u001B[0m"
            else -> "\u001B[103m \u001B[0m"
        }
        val p = when(task.priority) {
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            else -> "\u001B[104m \u001B[0m"
        }

        val dt = task.dateTime.toString().split("T")

        val firstLine = StringBuilder()
            .append("| ")
            .append(String.format("%-3d",num + 1))
            .append("| ")
            .append(dt[0])
            .append(" | ")
            .append(dt[1])
            .append(" | ")
            .append(p)
            .append(" | ")
            .append(dueTag)
            .append(" |")
            .append(String.format("%-44s", taskLines[0]))
            .append("|")
            .toString()
        println(firstLine)
        for (i in 1..taskLines.lastIndex) {
            print(multiLine)
            print(String.format("%-44s", taskLines[i]))
            println("|")
        }
        println(lineStr)
    }
}

fun getTaskNumber(): Int {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return 0
    }
    print()
    var taskNumber: Int? = 0
    do {
        println("Input the task number (1-${taskList.size}):")
        taskNumber = readLine()!!.lowercase().toIntOrNull()
        if (taskNumber == null || taskNumber !in 1..taskList.size)
            println("Invalid task number")
    } while (taskNumber !in 1..taskList.size)
    return taskNumber!!
}

fun getPriority(): String {
    var priority = ""
    do {
        println("Input the task priority (C, H, N, L):")
        priority = readLine()!!.lowercase()
    } while (priority !in listOf("c", "h", "n", "l"))
    return priority
}

private fun getDate(): LocalDate {
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
    return date
}

private fun getTime(): Pair<Int, Int> {
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
    return Pair(hour, minutes)
}


