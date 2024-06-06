import korlibs.event.*
import korlibs.image.color.*
import korlibs.korge.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.time.*

suspend fun main() = Korge(windowSize = Size(612, 612), backgroundColor = Colors["#2b2b2b"], title = "NextDevv's Game of Life" ) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
	override suspend fun SContainer.sceneMain() {
        val size = 150
        val cells = Array(size) { Array(size) { Pair(solidRect(1.0, 1.0), true) } }
        val cellSize = 5
        val cellSpacing = 2
        val cellTotalSize = cellSize + cellSpacing
        var pause = true

        val clearCells: () -> Unit = {
            for (y in 0 until size) {
                for (x in 0 until size) {
                    val cell = solidRect(cellSize, cellSize, Colors.WHITE) {
                        position(x * cellTotalSize, y * cellTotalSize)
                        color = Colors.WHITE
                    }
                    cells[y][x] = cell to false
                }
            }
        }

        clearCells()

        mouse {
            onClick {
                val point = it.upPosLocal
                val x = (point.x / cellTotalSize).toInt()
                val y = (point.y / cellTotalSize).toInt()

                if (x in 0 until size && y in 0 until size) {
                    cells[y][x] = cells[y][x].first to !cells[y][x].second
                    cells[y][x].first.color = if (cells[y][x].second) Colors.BLACK else Colors.WHITE
                }
            }
        }

        keys {
            down {
                when(it.key) {
                    Key.SPACE -> {
                        pause = !pause
                    }
                    
                    Key.R -> {
                        clearCells()
                    }
                    else -> {}
                }
            }
        }

        while(true) {
            if(!pause) {
                val blackCells =
                    cells.map { pairs -> pairs.map { it.first } }.flatten().filter { it.color == Colors.BLACK }

                blackCells.forEach { rect ->
                    cells.getNeighbors(
                        rect.cellPos(cellTotalSize).x.toInt(),
                        rect.cellPos(cellTotalSize).y.toInt()
                    ).forEach { neighbor ->
                        val (_, isAlive) = neighbor
                        val pos = rect.cellPos(cellTotalSize)

                        val neighbors = cells.getNeighbors(pos.x.toInt(), pos.y.toInt())
                        val aliveNeighbors = neighbors.count { it.second }
                        if (aliveNeighbors == 3 || aliveNeighbors == 2) {
                            rect.color = Colors.BLACK
                            cells.set(pos.x, pos.y, rect to true)

                            val randomNeighbor = neighbors.filter { !it.second }.random()
                            randomNeighbor.first.color = Colors.BLACK
                            cells.set(randomNeighbor.first.cellPos(cellTotalSize).x, randomNeighbor.first.cellPos(cellTotalSize).y, randomNeighbor.first to true)
                        }

                        if (aliveNeighbors <= 1) {
                            rect.color = Colors.WHITE
                            cells.set(pos.x, pos.y, rect to false)
                        }

                        if (aliveNeighbors >= 4) {
                            rect.color = Colors.WHITE
                            cells.set(pos.x, pos.y, rect to false)
                        }

                        rect.color = if (isAlive) Colors.BLACK else Colors.WHITE
                        cells.set(pos.x, pos.y, rect to isAlive)
                    }
                }
            }

            delay(0.1.seconds)
        }
	}
}

fun Array<Array<Pair<SolidRect, Boolean>>>.get(x: Int, y: Int): Boolean {
    if (x < 0 || y < 0 || x >= size || y >= size) return false
    return this[y][x].second
}

fun Array<Array<Pair<SolidRect, Boolean>>>.set(x: Double, y: Double, pair: Pair<SolidRect, Boolean>) {
    if (x < 0 || y < 0 || x >= size || y >= size) return
    this[y.toInt()][x.toInt()] = pair
}

fun Array<Array<Pair<SolidRect, Boolean>>>.pair(x: Int, y: Int): Pair<SolidRect, Boolean> {
    if (x < 0 || y < 0 || x >= size || y >= size) return this[0][0]
    return this[y][x]
}

fun View.cellPos(size: Int): Point {
    return Point((x / size).toInt(), (y / size).toInt())
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getTopLeft(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x - 1, y - 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getTop(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x, y - 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getTopRight(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x + 1, y - 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getLeft(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x - 1, y)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getRight(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x + 1, y)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getBottomLeft(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x - 1, y + 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getBottom(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x, y + 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getBottomRight(x: Int, y: Int): Pair<SolidRect, Boolean> {
    return this.pair(x + 1, y + 1)
}

fun Array<Array<Pair<SolidRect, Boolean>>>.getNeighbors(x: Int, y: Int): List<Pair<SolidRect, Boolean>> {
    return listOf(
        getTopLeft(x, y),
        getTop(x, y),
        getTopRight(x, y),
        getLeft(x, y),
        getRight(x, y),
        getBottomLeft(x, y),
        getBottom(x, y),
        getBottomRight(x, y)
    )
}
