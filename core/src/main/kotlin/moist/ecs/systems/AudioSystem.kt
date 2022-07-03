package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import moist.core.Assets
import moist.ecs.components.City

class AudioSystem:IteratingSystem(allOf(Box2d::class, City::class).get()) {
    var minSpeed = 0f
    var maxSpeed = 2.5f
    var currentSpeed = 0f
    val seaSound = inject<Assets>().sound
    var isPlaying = false
    var soundId:Long = 0
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        currentSpeed = body.linearVelocity.len()
        if(currentSpeed > maxSpeed)
            maxSpeed = currentSpeed
        val newVolume = MathUtils.norm(minSpeed, maxSpeed, currentSpeed)
        if(!isPlaying) {
            isPlaying = true
            soundId = seaSound.loop(newVolume)
        }
        seaSound.setVolume(soundId, newVolume)
    }

}