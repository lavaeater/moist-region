package moist.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.box2d.createWorld
import ktx.inject.Context
import ktx.inject.register
import moist.ecs.systems.UtilityAiSystem
import moist.core.Assets
import moist.core.GameConstants.GameHeight
import moist.core.GameConstants.GameWidth
import moist.ecs.systems.*
import moist.world.SeaManager

sealed class ContactType {
    class FishAndCity(val fish:Entity, val city: Entity): ContactType()
}

class FishAndGameManagement: ContactListener {
    override fun beginContact(contact: Contact) {
        if((contact.fixtureA.isFish() || contact.fixtureB.isFish()) && (contact.fixtureA.isCity() || contact.fixtureB.isCity())) {

            val cityEntity = if(contact.fixtureA.isCity()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity
            val fishEntity = if(contact.fixtureA.isFish()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity


            val cityComponent = cityEntity.city()
            cityComponent.potentialCatches[fishEntity] = 1f
        }
    }

    override fun endContact(contact: Contact) {
        val cityEntity = if(contact.fixtureA.isCity()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity
        val fishEntity = if(contact.fixtureA.isFish()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity


        val cityComponent = cityEntity.city()
        cityComponent.potentialCatches.remove(fishEntity)
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }

}

object Context {
    val context = Context()

    init {
        buildContext()
    }

    inline fun <reified T> inject(): T {
        return context.inject()
    }

    private fun buildContext() {
        context.register {
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    GameWidth,
                    GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(SeaManager())
            bindSingleton(createWorld().apply {
                setContactListener(FishAndGameManagement())
            })
            bindSingleton(Assets(AssetManager()))
            bindSingleton(getEngine())
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(CurrentChunkSystem(inject()))
            //addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(RenderSystem(inject(), inject()))
            addSystem(SeaCurrentSystem(inject()))
            addSystem(WindSystem(inject()))
            addSystem(ForcesOnCitySystem(inject()))
            addSystem(FishMovementSystem())
            addSystem(FishDeathSystem())
            addSystem(TileFoodSystem(inject()))
            addSystem(UtilityAiSystem())
            addSystem(FisherySystem())
            addSystem(CityHungerSystem())
//            addSystem(SeaWavesSystem())
        }
    }
}