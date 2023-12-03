fun getDigits(input: String) = input.lineSequence().map {
    val digits = it.mapNotNull {
        if (it.isDigit()) it.digitToInt() else null
    }
    (digits.first() * 10) + digits.last()
}
