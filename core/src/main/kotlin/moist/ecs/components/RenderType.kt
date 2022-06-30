package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameConstants.TileSize
import moist.injection.Context.inject
import moist.world.SeaManager


object GlobalDebug {
    val globalDebug = false
}

sealed class RenderType(val layer: Int) {
    class Bottom : RenderType(-1) {
        private val seaManager = inject<SeaManager>()
        private val bottomTexture by lazy { inject<Assets>().bottomTexture }
        private val camera by lazy { inject<OrthographicCamera>() }
        private val factor = 0.5f

        private val textureRegion by lazy {
            TextureRegion(bottomTexture)
        }

        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {

            val xOffset = (camera.position.x * factor).toInt()
            val yOffset = (camera.position.y * factor).toInt()
            textureRegion.regionX = xOffset % bottomTexture.getWidth()
            textureRegion.regionY = yOffset % bottomTexture.getHeight()
            textureRegion.regionWidth =  camera.viewportWidth.toInt()
            textureRegion.regionHeight = camera.viewportHeight.toInt()

            batch.draw(
                textureRegion,
                camera.position.x - camera.viewportWidth / 2,
                camera.position.y - camera.viewportHeight / 2
            )
        }

    }

    class Sea : RenderType(1) {
        private val seaManager = inject<SeaManager>()
        private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {

            for (tile in seaManager.getCurrentTiles()) {
                val x = tile.x * TileSize
                val y = tile.y * TileSize
                if (GlobalDebug.globalDebug) {
                    shapeDrawer.setColor(Color.WHITE)
                    shapeDrawer.filledCircle(tile.worldCenter, 5f)
                }
                tile.color.b = tile.depth
                tile.color.r = MathUtils.lerp(
                    tile.color.r,
                    Interpolation.exp10In.apply(
                        MathUtils.norm(
                            0f,
                            GameConstants.TileMaxFood,
                            tile.currentFood / 10f
                        )
                    ),
                    0.1f
                )
                tile.color.g = MathUtils.lerp(
                    tile.color.r, Interpolation.exp5In.apply(
                        MathUtils.norm(
                            GameConstants.MinWaterTemp,
                            GameConstants.MaxWaterTemp,
                            tile.waterTemp
                        )
                    ), 0.1f
                )
                tile.color.a = 1.0f - 1f/(tile.depth /5f)


                shapeDrawer.filledRectangle(
                    x,
                    y,
                    TileSize,
                    TileSize,
                    tile.color
                )

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