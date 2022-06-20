package moist.input

sealed class KeyPress {
    object Up : KeyPress()
    object Down : KeyPress()
}