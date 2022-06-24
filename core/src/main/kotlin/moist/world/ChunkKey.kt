package moist.world

import moist.core.GameConstants

data class ChunkKey(val chunkX: Int, val chunkY: Int) {
    companion object {
        fun keyForTileCoords(worldX: Int, worldY: Int): ChunkKey {
            val chunkX = Math.floorDiv(worldX - GameConstants.MaxTilesPerSide, GameConstants.MaxTilesPerSide) + 1
            val chunkY = Math.floorDiv(worldY - GameConstants.MaxTilesPerSide, GameConstants.MaxTilesPerSide) + 1

            return ChunkKey(chunkX, chunkY)
        }
    }
}