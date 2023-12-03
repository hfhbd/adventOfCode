import kotlin.math.max

fun checkPossibilities2(input: String) = input.lineSequence().map { game ->
    val gameIndex = game.indexOf(':')

    var maxRed = -1
    var maxGreen = -1
    var maxBlue = -1

    for (set in game.subSequence(gameIndex + 1, game.length).split(";")) {
        for (cubes in set.split(",")) {
            when {
                "red" in cubes -> {
                    val cube = cubes.replace("red", "").trim().toInt()
                    maxRed = max(maxRed, cube)
                }
                "green" in cubes -> {
                    val cube = cubes.replace("green", "").trim().toInt()
                    maxGreen = max(maxGreen, cube)
                }
                "blue" in cubes -> {
                    val cube = cubes.replace("blue", "").trim().toInt()
                    maxBlue = max(maxBlue, cube)
                }
            }
        }
    }

    maxRed * maxGreen * maxBlue
}
