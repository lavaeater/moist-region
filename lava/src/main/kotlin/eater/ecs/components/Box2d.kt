package eater.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Pool.Poolable

class Box2d : Component, Poolable {
    private var _body: Body? = null
    var body: Body
        get() = _body!!
    set(value) {
        _body = value
    }
    override fun reset() {
        _body = null
    }
}

