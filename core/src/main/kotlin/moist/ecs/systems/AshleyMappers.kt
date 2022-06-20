package moist.ecs.systems

import ktx.ashley.mapperFor
import moist.ecs.components.Renderable
import moist.ecs.components.Tile

object AshleyMappers {
    val renderable = mapperFor<Renderable>()
    val tile = mapperFor<Tile>()
}