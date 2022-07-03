package moist.world

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleScaleDomain
import eater.ecs.components.Tile
import ktx.log.info
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.ecs.components.SeaTile

inline fun <reified T: Tile>emptyArray():Array<T> {
    return kotlin.emptyArray()
}

abstract class AbstractTileManager<T: Tile> {
    private val chunks = mutableMapOf<ChunkKey, TileChunk>()
    var allTiles = chunks.values.map { it.tiles }.toTypedArray().flatten().toTypedArray()
    private var currentWorldX = 5000
    private var currentWorldY = 5000
    private var currentChunkKey = ChunkKey(currentWorldX, currentWorldY)
    private var currentChunks = emptyArray<TileChunk>()
    private lateinit var currentTiles: Array<T>
    private fun chunkKeyFromTileCoords(x: Int, y: Int): ChunkKey {
        return ChunkKey.keyForTileCoords(x, y)
    }

    private fun getOrCreateChunk(key: ChunkKey): TileChunk {
        if (!chunks.containsKey(key)) {
            chunks[key] = createChunk(key)
        }
        return chunks[key]!!
    }

    protected abstract fun createChunk(key: ChunkKey): TileChunk
    fun getCurrentTiles(): Array<T> {
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
            if (currentChunkKey != newChunkKey) {
                currentChunkKey = newChunkKey
                val minX = currentChunkKey.chunkX - 2
                val maxX = currentChunkKey.chunkX + 2
                val minY = currentChunkKey.chunkY - 2
                val maxY = currentChunkKey.chunkY + 2
                val keys = (minX..maxX).map { x -> (minY..maxY).map { y -> ChunkKey(x, y) } }.flatten()
                currentChunks = keys.map { getOrCreateChunk(it) }.toTypedArray()
                currentTiles = currentChunks.map { it.tiles }.toTypedArray().flatten().toTypedArray()
                allTiles = chunks.values.map { it.tiles }.toTypedArray().flatten().toTypedArray()
                info { "$newChunkKey is new Center of ${allTiles.count()} tiles" }
                fixNeighbours()
            }
        }
    }

    private fun hasNeighbours(chunkKey: ChunkKey): Boolean {
        val minX = chunkKey.chunkX - 1
        val maxX = chunkKey.chunkX + 1
        val minY = chunkKey.chunkY - 1
        val maxY = chunkKey.chunkY + 1
        val keys = (minX..maxX).map { x -> (minY..maxY).map { y -> ChunkKey(x, y) } }.flatten() - chunkKey
        return keys.all { chunks.containsKey(it) }
    }

    private fun fixNeighbours() {
        for ((key, chunk) in chunks.filterValues { !it.neighboursAreFixed }) {
            if (hasNeighbours(key)) {
                chunk.neighboursAreFixed = true
                for (x in chunk.minX..chunk.maxX)
                    for (y in chunk.minY..chunk.maxY) {
                        val tile = chunk.getTileAt(x, y)
                        if (tile.neighbours.count() < 8) {
                            tile.neighbours.clear()
                            for (offsetX in -1..1)
                                for (offsetY in -1..1) {
                                    val nX = x + offsetX
                                    val nY = y + offsetY
                                    val nTile = getTileAt(nX, nY)
                                    tile.neighbours.add(nTile)
                                }
                        }
                    }
            }
        }
    }

    fun getTileAt(worldX: Int, worldY: Int): Tile {
        val chunkKey = chunkKeyFromTileCoords(worldX, worldY)
        return getOrCreateChunk(chunkKey).getTileAt(worldX, worldY)
    }
}

class SeaManager : AbstractTileManager<SeaTile>() {
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

    override fun createChunk(key: ChunkKey): TileChunk {
        val newChunk = TileChunk(key)
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