package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.Setting
import kr.hahaha98757.fingerprintmacro.robot
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.image.BufferedImage

object Capture {
    private lateinit var bounds: Rectangle

    fun screenshot(): BufferedImage {
        return robot.createScreenCapture(bounds)
    }

    fun loadDisplay() {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        val index = if (Setting.display > device.size) 0 else Setting.display - 1
        bounds = device[index].defaultConfiguration.bounds
    }
}