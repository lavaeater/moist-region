package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import moist.core.Assets
import moist.core.GameConstants
import moist.injection.Context.inject
import moist.world.SeaManager
import space.earlygrey.shapedrawer.ShapeDrawer

sealed class RenderType(val layer: Int) {
    class Sea: RenderType(1) {
        private val seaColor = Color(1f, 1f, 1f, 0.7f)
        private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            for(column in SeaManager.tiles) {
                for(tile in column) {
                    seaColor.b = tile.depth
                    seaColor.r = tile.depth / 10f
                    seaColor.g = MathUtils.norm(GameConstants.MinWaterTemp, GameConstants.MaxWaterTemp, tile.waterTemp)
                    shapeDrawer.filledRectangle(
                        tile.x * GameConstants.TileSize - GameConstants.TileSize / 2,
                        tile.y * GameConstants.TileSize - GameConstants.TileSize / 2,
                        GameConstants.TileSize,
                        GameConstants.TileSize,
                        seaColor
                    )
                }
            }
        }
    }
    object Sprite: RenderType(2)
    class SelfRender(layer: Int, private val renderFunc: (batch: PolygonSpriteBatch, deltaTime: Float) -> Unit): RenderType(layer) {
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            renderFunc(batch, deltaTime)
        }
    }

}