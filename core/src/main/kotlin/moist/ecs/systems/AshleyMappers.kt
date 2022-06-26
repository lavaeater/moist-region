package moist.ecs.systems

import ktx.ashley.mapperFor
import moist.ai.UtilityAiComponent
import moist.ecs.components.*

object AshleyMappers {
    val fish = mapperFor<Fish>()
    val renderable = mapperFor<Renderable>()
    val box = mapperFor<Box>()
    val city = mapperFor<City>()
    val cloud = mapperFor<Cloud>()
    val ai = mapperFor<UtilityAiComponent>()
}