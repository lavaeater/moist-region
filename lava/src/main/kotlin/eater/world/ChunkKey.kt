package eater.world

data class ChunkKey(val chunkX: Int, val chunkY: Int) {
    companion object {
        fun keyForTileCoords(worldX: Int, worldY: Int, maxTilesPerSide:Int): ChunkKey {
            val chunkX = Math.floorDiv(worldX - maxTilesPerSide, maxTilesPerSide) + 1
            val chunkY = Math.floorDiv(worldY - maxTilesPerSide, maxTilesPerSide) + 1

            return ChunkKey(chunkX, chunkY)
        }
    }
}