package kr.hahaha98757.fingerprintmacro.features

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import kr.hahaha98757.fingerprintmacro.Setting
import kr.hahaha98757.fingerprintmacro.printDebug
import kr.hahaha98757.fingerprintmacro.printErr
import kotlin.system.exitProcess

object InputHandler {
    private val lookup = arrayOfNulls<List<Key>>(256)

    fun run(result: BooleanArray, test: Boolean = false) {
        var mask = 0
        for (i in result.indices) if (result[i]) mask = mask or (1 shl i)
        val keyList = lookup[mask]!!
        if (Setting.debug) printDebug("입력할 키: $keyList")
        val start = System.nanoTime()
        if (!test) for (key in keyList) inputKey(key)
        val elapsedTime = (System.nanoTime() - start) / 1_000_000.0
        printDebug("1회 입력 평균 소요 시간: ${"%.2f".format(elapsedTime / keyList.size)}ms")
    }

    private fun inputKey(key: Key) {
        if (Setting.pressingTimes <= 0) sendBatch(key.keyCode)
        else {
            send(key.keyCode, false)
            Thread.sleep(Setting.pressingTimes)
            send(key.keyCode, true)
        }
        if (Setting.inputDelays > 0) Thread.sleep(Setting.inputDelays)
    }

    @Suppress("UNCHECKED_CAST")
    private val inputs = WinUser.INPUT().toArray(2) as Array<WinUser.INPUT>

    private fun send(keyCode: Int, keyUp: Boolean) {
        val input = inputs[0]
        input.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
        input.input.setType("ki")
        input.input.ki.wVk = WinDef.WORD(keyCode.toLong())
        input.input.ki.dwFlags = WinDef.DWORD(if (keyUp) 0x0002L else 0L)

        User32.INSTANCE.SendInput(WinDef.DWORD(1), inputs, input.size())
    }

    private fun sendBatch(keyCode: Int) {
        val down = inputs[0]
        val up = inputs[1]

        down.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
        down.input.setType("ki")
        down.input.ki.wVk = WinDef.WORD(keyCode.toLong())
        down.input.ki.dwFlags = WinDef.DWORD(0L)

        up.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
        up.input.setType("ki")
        up.input.ki.wVk = WinDef.WORD(keyCode.toLong())
        up.input.ki.dwFlags = WinDef.DWORD(0x0002L)

        User32.INSTANCE.SendInput(WinDef.DWORD(2), inputs, down.size())
    }

    fun init() {
        for (mask in 0..<256) {
            if (Integer.bitCount(mask) != 4) continue
            lookup[mask] = createShortestPath(mask)
        }
        try {
            User32.INSTANCE.GetForegroundWindow()
        } catch (_: Throwable) {}
    }

    private fun createShortestPath(targetMask: Int): List<Key> {
        val start = State(0, 0)

        val queue = ArrayDeque<State>()
        val visited = mutableSetOf<State>()

        val parent = mutableMapOf<State, State>()
        val action = mutableMapOf<State, Key>()

        queue += start
        visited += start

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()

            if (state.selected == targetMask) {
                val result = mutableListOf<Key>()

                var cur = state
                while (cur != start) {
                    result += action[cur]!!
                    cur = parent[cur]!!
                }

                result.reverse()
                result += Key.TAB
                return result
            }

            fun visit(next: State, key: Key) {
                if (visited.add(next)) {
                    parent[next] = state
                    action[next] = key
                    queue += next
                }
            }

            visit(State((state.cursor + 6) % 8, state.selected), Key.UP)
            visit(State((state.cursor + 2) % 8, state.selected), Key.DOWN)
            visit(State((state.cursor + 7) % 8, state.selected), Key.LEFT)
            visit(State((state.cursor + 1) % 8, state.selected), Key.RIGHT)
            visit(State(state.cursor, state.selected xor (1 shl state.cursor)), Key.ENTER)
        }

        printErr("패턴 '${Integer.toBinaryString(targetMask).padStart(8, '0')}'에 대한 최단 경로를 찾을 수 없습니다.")
        exitProcess(1)
    }
}

private data class State(
    val cursor: Int,
    val selected: Int
)

private enum class Key(val keyCode: Int, val str: String) {
    LEFT(0x25, "Left"),
    UP(0x26, "Up"),
    RIGHT(0x27, "Right"),
    DOWN(0x28, "Down"),
    ENTER(0x0D, "Enter"),
    TAB(0x09, "Tab");

    override fun toString() = str
}