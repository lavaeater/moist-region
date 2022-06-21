package moist.world

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.sudoplay.joise.mapping.*
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain
import ktx.ashley.entity
import ktx.ashley.with
import moist.core.GameConstants.MaxTiles
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
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

        val tiles = Array(MaxTiles) { x ->
            Array(MaxTiles) { y ->
                Tile(x, y)
            }
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
                    tiles[x][y].apply {
                        depth = value.toFloat()
                        originalDepth = depth
                        waterTemp = MathUtils.map(0f, 1f, MinWaterTemp, MaxWaterTemp, depth)
                    }
                },
                IMappingUpdateListener.NULL_LISTENER
            )

            for (tile in tiles.flatten()) {
                for (offsetX in -1..1) {
                    for (offsetY in -1..1) {
                        val x = tile.x + offsetX
                        val y = tile.y + offsetY
                        if ((x > 0 && x < tiles.lastIndex) && (y > 0 && y < tiles.lastIndex)) {
                            val n = tiles[x][y]
                            if (n != tile)
                                tile.neighbours.add(n)
                        }
                    }
                }
            }

            for (tile in tiles.flatten()) {
                val target = tile.neighbours.minByOrNull { it.waterTemp }!!
                if (target.waterTemp < tile.waterTemp) {
                    /*
                    Now we create a force vector pointing towards the target, and
                    also, the magnitude depends on the difference, maybe
                     */
                    tile.currentForce.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat())
                } else {
                    tile.currentForce.setZero()
                }
            }
            engine().entity {
                with<Renderable> {
                    renderType = RenderType.Sea()
                }
            }
        }
    }
}

fun world(): World {
    return inject()
}

fun engine(): Engine {
    return inject()
}