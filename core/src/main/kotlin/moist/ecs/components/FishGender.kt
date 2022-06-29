package moist.ecs.components

sealed class FishGender(val name: String) {
    object Blork: FishGender("Blork")
    object Spork: FishGender("Spork")
    companion object {
        val genders = listOf(Blork, Spork)
    }
}