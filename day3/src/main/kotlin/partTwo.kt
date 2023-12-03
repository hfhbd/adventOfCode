fun getParts2(input: String) = sequence {
    val lines = input.lines()
    for ((lineNumber, line) in lines.withIndex()) {

        var currentIndex = 0

        while (true) {
            val char = line.getOrNull(currentIndex) ?: break
            when {
                char == '*' -> {
                    val left = line.getOrNull(currentIndex - 1)
                    val leftNumber = if (left != null && left.isDigit()) {
                        previousNumber(currentIndex - 1, line).toInt()
                    } else null

                    val right = line.getOrNull(currentIndex + 1)
                    val rightNumber = if (right != null && right.isDigit()) {
                        nextNumber(currentIndex + 1, line).toInt()
                    } else null


                    val upper = lines.getOrNull(lineNumber - 1)
                    val down = lines.getOrNull(lineNumber + 1)

                    var upperNumber: Int? = null
                    var upperNumber2: Int? = null
                    if (upper != null) {
                        val (left, middle, right) = upper.substring2(currentIndex)

                        // 123
                        if (left != null && middle != null && right != null) {
                            val previous = previousNumber(currentIndex, upper)
                            val next = nextNumber(currentIndex + 1, upper)

                            upperNumber = (previous + next).toInt()
                        }
                        // __1
                        else if (left == null && middle == null) {
                            upperNumber = nextNumber(currentIndex + 1, upper).toIntOrNull()
                        }
                        // _XX
                        else if (left == null) {
                            upperNumber = nextNumber(currentIndex, upper).toIntOrNull()
                        }
                        // 1_X
                        else if (middle == null) {
                            upperNumber = previousNumber(currentIndex - 1, upper).toIntOrNull()
                            upperNumber2 = nextNumber(currentIndex + 1, upper).toIntOrNull()
                        }
                        // 11_
                        else {
                            upperNumber = previousNumber(currentIndex, upper).toIntOrNull()
                        }
                    }

                    var downNumber: Int? = null
                    var downNumber2: Int? = null
                    if (down != null) {
                        val (left, middle, right) = down.substring2(currentIndex)

                        // 123
                        if (left != null && middle != null && right != null) {
                            val previous = previousNumber(currentIndex, down)
                            val next = nextNumber(currentIndex + 1, down)

                            downNumber = (previous + next).toInt()
                        }
                        // __1
                        else if (left == null && middle == null) {
                            downNumber = nextNumber(currentIndex + 1, down).toIntOrNull()
                        }
                        // _XX
                        else if (left == null) {
                            downNumber = nextNumber(currentIndex, down).toIntOrNull()
                        }
                        // 1_X
                        else if (middle == null) {
                            downNumber = previousNumber(currentIndex - 1, down).toIntOrNull()
                            downNumber2 = nextNumber(currentIndex + 1, down).toIntOrNull()
                        }
                        // 11_
                        else {
                            downNumber = previousNumber(currentIndex, down).toIntOrNull()
                        }
                    }

                    val partNumbers =
                        listOfNotNull(rightNumber, leftNumber, upperNumber, upperNumber2, downNumber, downNumber2)
                    if (partNumbers.size == 2) {
                        val (f, l) = partNumbers
                        yield(f to l)
                    }
                }
            }
            currentIndex++
        }
    }
}

private fun String.substring2(currentIndex: Int): List<Int?> = buildList(3) {
    add(this@substring2.getOrNull(currentIndex - 1)?.digitToIntOrNull())
    add(this@substring2[currentIndex].digitToIntOrNull())
    add(this@substring2.getOrNull(currentIndex + 1)?.digitToIntOrNull())
}

fun previousNumber(startIndex: Int, line: String): String {
    var index = startIndex
    var currentNumber = line[startIndex].toString()

    // consume the number
    while (true) {
        val nextChar = line.getOrNull(index - 1) ?: break
        when {
            nextChar.isDigit() -> currentNumber = nextChar + currentNumber
            else -> return currentNumber.trim()
        }
        index -= 1
    }
    return currentNumber.trim()
}

fun nextNumber(startIndex: Int, line: String): String {
    var index = startIndex
    var currentNumber = line[startIndex].toString()

    // consume the number
    while (true) {
        val nextChar = line.getOrNull(index + 1) ?: break
        when {
            nextChar.isDigit() -> currentNumber += nextChar
            else -> return currentNumber.trim()
        }
        index += 1
    }
    return currentNumber.trim()
}
