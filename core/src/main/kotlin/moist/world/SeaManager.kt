package moist.world

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.ecs.components.Tile
import moist.injection.Context.inject

class SeaManager {
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

    private val chunks = mutableMapOf<ChunkKey, TileChunk>()
    var allTiles = chunks.values.map { it.tiles }.toTypedArray().flatten().toTypedArray()

    private fun chunkKeyFromTileCoords(x: Int, y: Int): ChunkKey {
        return ChunkKey.keyForTileCoords(x, y)
    }

    private fun getOrCreateChunk(key: ChunkKey): TileChunk {
        if (!chunks.containsKey(key)) {
            chunks[key] = createChunk(key)
        }
        return chunks[key]!!
    }

    private fun createChunk(key: ChunkKey): TileChunk {
        val newChunk = TileChunk(key)
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
            val target = tile.neighbours.minByOrNull { it.waterTemp }
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

    private var currentWorldX = 0
    private var currentWorldY = 0
    private var currentChunkKey = ChunkKey(-10, -10)
    private var currentChunks = emptyArray<TileChunk>()
    private var currentTiles = emptyArray<Tile>()

    fun getCurrentTiles(): Array<Tile> {
        return currentTiles
    }

    fun getCurrentChunks(): Array<TileChunk> {
        return currentChunks
    }

    /**
     * Returns the chunk that x and y belongs to
     * and all neigbouring chunks
     */
    fun updateCurrentChunks(tileX: Int, tileY: Int) {
        if (tileX != currentWorldX && tileY != currentWorldY) {
            currentWorldX = tileX
            currentWorldY = tileY
            val newChunkKey = chunkKeyFromTileCoords(tileX, tileY)
            if(currentChunkKey != newChunkKey) {
                currentChunkKey = newChunkKey
                val minX = currentChunkKey.chunkX - 1
                val maxX = currentChunkKey.chunkX + 1
                val minY = currentChunkKey.chunkY - 1
                val maxY = currentChunkKey.chunkY + 1
                val keys = (minX..maxX).map { x -> (minY..maxY).map { y -> ChunkKey(x, y) } }.flatten()
                currentChunks = keys.map { getOrCreateChunk(it) }.toTypedArray()
                currentTiles = currentChunks.map { it.tiles }.toTypedArray().flatten().toTypedArray()
                allTiles = chunks.values.map { it.tiles }.toTypedArray().flatten().toTypedArray()
                fixNeighbours()
            }
        }
    }

    private fun fixNeighbours() {
        val minX = currentChunks.minOf { it.minX } + 1
        val maxX = currentChunks.maxOf { it.maxX } - 1
        val minY = currentChunks.maxOf { it.minY } - 1
        val maxY = currentChunks.maxOf { it.maxY } + 1
        for(x in minX..maxX)
            for(y in minY..maxY) {
                val tile = getTileAt(x, y)
                if(tile.neighbours.count() < 8) {
                    tile.neighbours.clear()
                    for(offsetX in -1..1)
                        for(offsetY in -1..1) {
                            val nX = x + offsetX
                            val nY = y + offsetY
                            val nTile = getTileAt(nX, nY)
                            tile.neighbours.add(nTile)
                        }
                }
            }

    }

    fun getTileAt(worldX: Int, worldY: Int): Tile {
        val chunkKey = chunkKeyFromTileCoords(worldX, worldY)
        return getOrCreateChunk(chunkKey).getTileAt(worldX, worldY)
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

fun world(): World {
    return inject()
}

fun engine(): Engine {
    return inject()
}