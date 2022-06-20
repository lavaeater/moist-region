package moist.ecs.systems

import ktx.ashley.mapperFor
import moist.ecs.components.Box
import moist.ecs.components.City
import moist.ecs.components.Renderable
import moist.ecs.components.Tile

object AshleyMappers {
    val renderable = mapperFor<Renderable>()
    val tile = mapperFor<Tile>()
    val box = mapperFor<Box>()
    val city = mapperFor<City>()
}