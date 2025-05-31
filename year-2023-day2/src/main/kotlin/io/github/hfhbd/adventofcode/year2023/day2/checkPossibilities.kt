package io.github.hfhbd.adventofcode.year2023.day2

fun checkPossibilities(input: String, red: Int, green: Int, blue: Int): Sequence<Int> = sequence {
    games@for (game in input.lineSequence()) {
        val gameIndex = game.indexOf(':')
        val id = game.substring(4, gameIndex).trim().toInt()

        for (set in game.subSequence(gameIndex + 1, game.length).split(";")) {
            for (cubes in set.split(",")) {
                when {
                    "red" in cubes -> {
                        val cube = cubes.replace("red", "").trim().toInt()
                        if (cube > red) {
                            continue@games
                        }
                    }
                    "green" in cubes -> {
                        val cube = cubes.replace("green", "").trim().toInt()
                        if (cube > green) {
                            continue@games
                        }
                    }
                    "blue" in cubes -> {
                        val cube = cubes.replace("blue", "").trim().toInt()
                        if (cube > blue) {
                            continue@games
                        }
                    }
                }
            }
        }
        yield(id)
    }
}
