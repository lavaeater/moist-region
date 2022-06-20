package moist.world

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.physics.box2d.World
import com.sudoplay.joise.mapping.*
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain
import ktx.ashley.entity
import ktx.ashley.with
import moist.core.GameConstants.MaxTiles
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable
import moist.ecs.components.Tile
import moist.injection.Context.inject


class SeaManager {
    /**
     * How do you simulate wind and sea currents?
     *
     * Well, you create heatmap over the sea and sky.
     *
     * In our example, the temperature will be higher at the equator, but then wary
     * with the depth of the sea, for the sea.
     *
     * So, the sea is defined as a perlin heat-map where deeper seas give colder water (and more fish)
     *  Shallow water gives you warmer water.
     *
     *  Where the water is warm, air goes towards it, because it heats up the air.
     *  However, water moves towards colder water.
     *
     *  The heatmap can be illustrated by having colors of pixels be darker blue with temperature
     *  And more green with depth.
     *
     */
    companion object {

        fun map() {

        }

        fun generate() {
            val basis = ModuleBasisFunction()
            basis.setType(BasisType.SIMPLEX)
            basis.seed = 42

            val correct = ModuleAutoCorrect()
            correct.setSource(basis)
            correct.calculateAll()

            val scaleDomain = ModuleScaleDomain()
            scaleDomain.setSource(correct)
            scaleDomain.setScaleX(4.0)
            scaleDomain.setScaleY(4.0)

            Mapping.map2DNoZ(
                MappingMode.SEAMLESS_XY,
                MaxTiles,
                MaxTiles,
                scaleDomain,
                MappingRange.DEFAULT,
                IMapping2DWriter { x, y, value ->
                    engine().entity {
                        with<Renderable> { renderType = RenderType.Sea }
                        with<Tile> {
                            this.x = x - MaxTiles / 2
                            this.y = y - MaxTiles / 2
                            depth = value.toFloat()
                            originalDepth = depth
                        }
                    }
                },
                IMappingUpdateListener.NULL_LISTENER)
        }
    }
}

fun world(): World {
    return inject()
}

fun engine(): Engine {
    return inject()
}