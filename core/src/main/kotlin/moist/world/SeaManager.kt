package moist.world

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain
import eater.injection.InjectionContext.Companion.inject
import eater.world.*
import ktx.log.info
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.core.GameConstants.TileSize
import moist.ecs.components.SeaTile


fun Body.currentTile() : SeaTile {
    return inject<SeaManager>().getTileAt(this.tileX(TileSize), this.tileY(TileSize))
}

fun Body.tileX(): Int {
    return this.tileX(TileSize)
}

fun Body.tileY():Int {
    return this.tileY(TileSize)
}


class SeaManager : AbstractTileManager<SeaTile>(MaxTilesPerSide) {
    private val basis = ModuleBasisFunction()
    private val correct = ModuleAutoCorrect()
    private val scaleDomain = ModuleScaleDomain()

    init {
        setupGenerator()
    }


    private fun setupGenerator() {
        basis.setType(BasisType.SIMPLEX)
        basis.seed = (1..1000).random().toLong()

        correct.setSource(basis)
        correct.calculateAll()

        scaleDomain.setSource(correct)
        scaleDomain.setScaleX(MaxTilesPerSide / 100.0)
        scaleDomain.setScaleY(MaxTilesPerSide / 100.0)
    }

    override fun createChunk(key: ChunkKey): TileChunk<SeaTile> {
        val newChunk = TileChunk(key, MaxTilesPerSide) { x, y -> SeaTile(x, y) }
        info { "Created Chunk: $key" }
        for (tile in newChunk.tiles) {
            val d = scaleDomain.get(tile.x.toDouble() / 16, tile.y.toDouble() / 16)
            tile.apply {
                depth = d.toFloat()
                originalDepth = d.toFloat()
                waterTemp = MathUtils.map(0f, 1f, MinWaterTemp, MaxWaterTemp, d.toFloat())
            }
        }
        for (tile in newChunk.tiles) {
            for (offsetX in -1..1) {
                for (offsetY in -1..1) {
                    val x = tile.x + offsetX
                    val y = tile.y + offsetY
                    if ((x > newChunk.minX && x < newChunk.maxX) && (y > newChunk.minY && y < newChunk.maxY)) {
                        val n = newChunk.getTileAt(x, y)
                        if (n != tile)
                            tile.neighbours.add(n)
                    }
                }
            }
        }

        for (tile in newChunk.tiles) {
            val target = tile.neighbours.minByOrNull { (it as SeaTile ).waterTemp } as SeaTile
            if (target != null && target.waterTemp < tile.waterTemp) {
                /*
                Now we create a force vector pointing towards the target, and
                also, the magnitude depends on the difference, maybe
                 */
                tile.current.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat()).nor()
            } else {
                tile.current.setZero()
            }
        }

        return newChunk
    }


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
}