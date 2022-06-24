package moist.world

import moist.core.GameConstants
import moist.ecs.components.Tile

data class TileChunk(val key: ChunkKey) {
    constructor(x: Int, y: Int) : this(ChunkKey(x, y))

    val chunkX = key.chunkX
    val chunkY = key.chunkY
    val minX = chunkX * GameConstants.MaxTilesPerSide
    val maxX = minX + GameConstants.MaxTilesPerSide - 1
    val minY = chunkY * GameConstants.MaxTilesPerSide
    val maxY = minY + GameConstants.MaxTilesPerSide - 1
    val tiles = Array(GameConstants.MaxTilesPerSide * GameConstants.MaxTilesPerSide) { i ->
        val x = (i % GameConstants.MaxTilesPerSide) + chunkX * GameConstants.MaxTilesPerSide
        val y = (i / GameConstants.MaxTilesPerSide) + chunkY * GameConstants.MaxTilesPerSide
        Tile(x, y)
    }

    fun localX(worldX: Int): Int {
        return worldX - (GameConstants.MaxTilesPerSide * chunkX)
    }

    fun localY(worldY: Int): Int {
        return worldY - (GameConstants.MaxTilesPerSide * chunkY)
    }

    fun getIndex(localX: Int, localY: Int): Int {
        return localX + GameConstants.MaxTilesPerSide * (localY)
    }

    fun getTileAt(worldX: Int, worldY: Int): Tile {
        return tiles[getIndex(localX(worldX), localY(worldY))]
    }
}