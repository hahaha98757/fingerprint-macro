package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.inputKey
import java.awt.event.KeyEvent

object InputHandler {
    private val lookup = arrayOfNulls<List<Int>>(256)

    fun run(result: BooleanArray, test: Boolean = false) {
        var mask = 0
        for (i in result.indices) if (result[i]) mask = mask or (1 shl i)
        val keyList = lookup[mask] ?: return
        keyList.forEach { inputKey(it, test) }
    }

    fun init() {
        println("최단 경로 계산 중...")
        for (mask in 0 until 256) {
            if (Integer.bitCount(mask) != 4) continue
            lookup[mask] = createShortestPath(mask)
        }
        println("최단 경로 계산 완료")
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

        error("No solution")
    }
}

private data class State(
    val cursor: Int,
    val selected: Int
)