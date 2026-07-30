package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.inputKey
import kr.hahaha98757.fingerprintmacro.printDebug
import kr.hahaha98757.fingerprintmacro.printErr
import java.awt.event.KeyEvent
import kotlin.system.exitProcess

object InputHandler {
    private val lookup = arrayOfNulls<List<Int>>(256)

    fun run(result: BooleanArray, test: Boolean = false) {
        var mask = 0
        for (i in result.indices) if (result[i]) mask = mask or (1 shl i)
        val keyList = lookup[mask]!!
        val inputtedKeys = StringBuilder("입력된 키: ")
        val start = System.nanoTime()
        for (keyCode in keyList) inputtedKeys.append(inputKey(keyCode, test))
        printDebug(inputtedKeys.toString())
        val elapsedTime = (System.nanoTime() - start) / 1_000_000.0
        printDebug("1회 입력 평균 소요 시간: ${"%.2f".format(elapsedTime / keyList.size)}ms")
    }

    fun init() {
        for (mask in 0..<256) {
            if (Integer.bitCount(mask) != 4) continue
            lookup[mask] = createShortestPath(mask)
        }
    }

    private fun createShortestPath(targetMask: Int): List<Int> {
        val start = State(0, 0)

        val queue = ArrayDeque<State>()
        val visited = mutableSetOf<State>()

        val parent = mutableMapOf<State, State>()
        val action = mutableMapOf<State, Int>()

        queue += start
        visited += start

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()

            if (state.selected == targetMask) {
                val result = mutableListOf<Int>()

                var cur = state
                while (cur != start) {
                    result += action[cur]!!
                    cur = parent[cur]!!
                }

                result.reverse()
                result += KeyEvent.VK_TAB
                return result
            }

            fun visit(next: State, key: Int) {
                if (visited.add(next)) {
                    parent[next] = state
                    action[next] = key
                    queue += next
                }
            }

            visit(State((state.cursor + 6) % 8, state.selected), KeyEvent.VK_UP)
            visit(State((state.cursor + 2) % 8, state.selected), KeyEvent.VK_DOWN)
            visit(State((state.cursor + 7) % 8, state.selected), KeyEvent.VK_LEFT)
            visit(State((state.cursor + 1) % 8, state.selected), KeyEvent.VK_RIGHT)
            visit(State(state.cursor, state.selected xor (1 shl state.cursor)), KeyEvent.VK_ENTER)
        }

        printErr("패턴 '${Integer.toBinaryString(targetMask).padStart(8, '0')}'에 대한 최단 경로를 찾을 수 없습니다.")
        exitProcess(1)
    }
}

private data class State(
    val cursor: Int,
    val selected: Int
)