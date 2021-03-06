@file:JvmName("Lwjgl3Launcher")

package moist.core.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import moist.core.MoistGame

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(MoistGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("moist-region")
        setWindowedMode(1280, 960)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
