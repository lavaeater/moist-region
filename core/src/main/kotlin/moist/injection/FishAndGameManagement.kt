package moist.injection

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import moist.ecs.systems.*

class FishAndGameManagement: ContactListener {
    override fun beginContact(contact: Contact) {
        if((contact.fixtureA.isCreature() || contact.fixtureB.isCreature()) && (contact.fixtureA.isCity() || contact.fixtureB.isCity())) {

            val cityEntity = if(contact.fixtureA.isCity()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity
            val creatureEntity = if(contact.fixtureA.isCreature()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity

            if(creatureEntity.isShark()) {
                val something = "oranother"
            }

            val cityComponent = cityEntity.city()
            if(creatureEntity.creature().canDie)
                cityComponent.potentialCatches[creatureEntity] = 0.5f
        }
    }

    override fun endContact(contact: Contact) {
        val cityEntity = if(contact.fixtureA.isCity()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity
        val fishEntity = if(contact.fixtureA.isCreature()) contact.fixtureA.body.userData as Entity else contact.fixtureB.body.userData as Entity


        val cityComponent = cityEntity.city()
        cityComponent.potentialCatches.remove(fishEntity)
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }

}