package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameConstants.TileSize
import moist.injection.Context.inject
import moist.world.SeaManager

sealed class RenderType(val layer: Int) {
    class Sea : RenderType(1) {
        private val seaManager = inject<SeaManager>()
        private val seaColor = Color(0f, 0f, 0f, 0.8f)
        private val currentStartColor = Color(.4f, .4f, 1f, 0.5f)
        private val currentEndColor = Color(.7f, .7f, 1f, 0.5f)
        private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            var x = 0f
            var y = 0f
            for (tile in seaManager.getCurrentTiles()) {
                seaColor.b = tile.depth
                seaColor.r = MathUtils.norm(0f, GameConstants.TileMaxFood, tile.currentFood / 4f)
                seaColor.g = MathUtils.norm(GameConstants.MinWaterTemp, GameConstants.MaxWaterTemp, tile.waterTemp / 4f)
                x = tile.x * TileSize
                y = tile.y * TileSize
                shapeDrawer.filledRectangle(
                    x,
                    y,
                    TileSize,
                    TileSize,
                    seaColor
                )
                shapeDrawer.setColor(Color.WHITE)
                shapeDrawer.filledCircle(tile.worldCenter, 5f)
            }
        }
    }
    object Cloud: RenderType(3)
    object NotReallyRelevant : RenderType(2)
    class RenderAnimation(layer:Int, val animation: Animation<Sprite>, var time: Float = 0f) : RenderType(layer)
    class SelfRender(layer: Int, private val renderFunc: (batch: PolygonSpriteBatch, deltaTime: Float) -> Unit) :
        RenderType(layer) {
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            renderFunc(batch, deltaTime)
        }
    }

}