package moist.world

import com.sudoplay.joise.mapping.*
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain


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

        fun generate(): Array<Array<Tile>> {
            val tiles = Array(100) { x ->
                Array(100) {y ->
                    Tile(x, y)
                }
            }

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
                100,
                100,
                scaleDomain,
                MappingRange.DEFAULT,
                IMapping2DWriter { x, y, value ->
                    tiles[x][y].depth = value.toFloat()
                },
                IMappingUpdateListener.NULL_LISTENER)

            return tiles
        }
    }
}

data class Tile(val x: Int, val y: Int, val tileSize: Float = 10f, var depth: Float = 0f, var waterTemp: Float = 10f, var airTemp:Float = 15f)