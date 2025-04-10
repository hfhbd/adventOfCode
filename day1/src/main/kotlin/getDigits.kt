fun getDigits(input: String) = input.lineSequence().map {
    val digits = it.mapNotNull {
        it.digitToIntOrNull()
    }
    (digits.first() * 10) + digits.last()
}
