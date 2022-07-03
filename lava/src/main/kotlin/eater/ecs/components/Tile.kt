package eater.ecs.components

interface TileManager {

}

interface Tile {
    val neighbours: MutableList<Tile>
    val x: Int
    val y: Int
}