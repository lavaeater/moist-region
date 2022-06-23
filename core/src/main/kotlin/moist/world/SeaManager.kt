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
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable
import moist.ecs.components.Tile
import moist.injection.Context.inject

data class ChunkKey(val chunkX: Int, val chunkY: Int) {

}
data class TileChunk(val key: ChunkKey) {
    constructor(x: Int, y: Int):this(ChunkKey(x, y))
    val chunkX = key.chunkX
    val chunkY = key.chunkY
    val minX = chunkX * MaxTilesPerSide
    val maxX = minX + MaxTilesPerSide
    val minY = chunkY * MaxTilesPerSide
    val maxY = minY + MaxTilesPerSide
    val tiles = Array(MaxTilesPerSide * MaxTilesPerSide) { i ->
        val x = (i % MaxTilesPerSide) + chunkX * MaxTilesPerSide
        val y = (i / MaxTilesPerSide) + chunkY * MaxTilesPerSide
        Tile(x, y)
    }

    fun localX(worldX:Int):Int {
        return worldX 
    }
    fun localY(worldY:Int):Int {
        return worldY + minY
    }
    fun getIndex(x: Int, y: Int): Int{
        return x + MaxTilesPerSide * y
    }
}

class SeaManager {
    init {
        setupGenerator()
    }
    val basis = ModuleBasisFunction()
    val correct = ModuleAutoCorrect()
    val scaleDomain = ModuleScaleDomain()

    private fun setupGenerator() {
        basis.setType(BasisType.SIMPLEX)
        basis.seed = (1..1000).random().toLong()

        correct.setSource(basis)
        correct.calculateAll()

        scaleDomain.setSource(correct)
        scaleDomain.setScaleX(MaxTilesPerSide / 100.0)
        scaleDomain.setScaleY(MaxTilesPerSide / 100.0 )
    }

    val chunks = mutableMapOf<ChunkKey, TileChunk>()

    fun chunkKeyFromTileCoords(x:Int, y:Int): ChunkKey {
        return ChunkKey(x / MaxTilesPerSide, y / MaxTilesPerSide)
    }
    fun chunkExistsFor(x:Int, y:Int): Boolean {
        return chunks.containsKey(chunkKeyFromTileCoords(x,y))
    }

    fun getOrCreateChunk(key: ChunkKey): TileChunk {
        if(!chunks.containsKey(key)) {
            chunks[key] = createChunk(key)
        }
        return chunks[key]!!
    }

    private fun createChunk(key: ChunkKey): TileChunk {
        val newChunk = TileChunk(key)
        for (tile in newChunk.tiles) {
            val d = scaleDomain.get(tile.x.toDouble(), tile.y.toDouble())
            tile.apply {
                depth = d.toFloat()
                originalDepth = d.toFloat()
                waterTemp = MathUtils.map(0f, 1f, MinWaterTemp, MaxWaterTemp, d.toFloat())
            }
        }
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

        return newChunk
    }

    private var currentChunkKey = ChunkKey(-10,-10)
    private var currentChunks = mutableListOf<TileChunk>()
    /**
     * Returns the chunk that x and y belongs to
     * and all neigbouring chunks
     */
    fun getCurrentChunks(tileX:Int, tileY:Int) : List<TileChunk> {
        val newKey = chunkKeyFromTileCoords(tileX, tileY)
        if(newKey != currentChunkKey) {
            val minX = newKey.chunkX - 1
            val maxX = newKey.chunkX + 1
            val minY = newKey.chunkY - 1
            val maxY = newKey.chunkY + 1
            val keys = (minX..maxX).map { x -> (minY..maxY).map { y -> ChunkKey(x,y) } }.flatten()
            currentChunks.clear()
            currentChunks.addAll(keys.map { getOrCreateChunk(it) })
        }
        return currentChunks
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

    fun getTileAt(x: Int, y: Int): Tile {
        return tiles[getIndex(x,y)]
    }
}

fun world(): World {
    return inject()
}

fun engine(): Engine {
    return inject()
}