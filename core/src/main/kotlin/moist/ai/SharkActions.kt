package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import moist.injection.Context.inject
import moist.world.SeaManager

class SharkMating: Component, Poolable {
    override fun reset() {

    }

}

class SharkHunting: Component, Poolable {
    override fun reset() {
    }

}

object SharkActions {
    val seaManager by lazy { inject<SeaManager>() }

    val huntingAction = GenericActionWithState("Shark Hunting", {
        0.0
                                                                }, {

    }, { entity, state, deltaTime ->

    }, SharkHunting::class.java)
    val matingAction = GenericAction("Shark Mating", {0.0}, {

    }, { entity,deltaTime ->

    })

}