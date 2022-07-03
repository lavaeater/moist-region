package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import eater.injection.InjectionContext.Companion.inject
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameConstants.TileSize
import moist.world.SeaManager

object GlobalDebug {
    var globalDebug = false
}

sealed class RenderType(val layer: Int) {
    class Sea : RenderType(1) {
        private val seaManager = inject<SeaManager>()
        private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            for (tile in if(GlobalDebug.globalDebug) seaManager.allTiles else seaManager.getCurrentTiles()) {
                tile.color.b = tile.depth
                tile.color.r = MathUtils.lerp(
                    tile.color.r,
                    Interpolation.pow2In.apply(
                        MathUtils.norm(
                            0f,
                            GameConstants.TileMaxFood,
                            tile.currentFood / 5f
                        )
                    ),
                    0.1f
                )
                tile.color.g = MathUtils.lerp(
                    tile.color.r, Interpolation.pow2In.apply(
                        MathUtils.norm(
                            GameConstants.MinWaterTemp,
                            GameConstants.MaxWaterTemp,
                            tile.waterTemp
                        )
                    ), 0.1f
                )
                val x = tile.x * TileSize
                val y = tile.y * TileSize
                shapeDrawer.filledRectangle(
                    x,
                    y,
                    TileSize,
                    TileSize,
                    tile.color
                )
//                if (GlobalDebug.globalDebug) {
//                    shapeDrawer.setColor(Color.WHITE)
//                    shapeDrawer.filledCircle(tile.worldCenter, 5f)
//                }
            }
        }
    }

    object Cloud : RenderType(3)
    object NotReallyRelevant : RenderType(2)
    class RenderAnimation(layer: Int, val animation: Animation<Sprite>, var time: Float = 0f) : RenderType(layer)
    class SelfRender(layer: Int, private val renderFunc: (batch: PolygonSpriteBatch, deltaTime: Float) -> Unit) :
        RenderType(layer) {
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            renderFunc(batch, deltaTime)
        }
    }

}