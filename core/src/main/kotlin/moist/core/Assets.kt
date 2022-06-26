package moist.core

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import ktx.assets.*
import ktx.collections.GdxArray
import moist.injection.Context
import space.earlygrey.shapedrawer.ShapeDrawer

class Assets(assetManager: AssetManager): DisposableRegistry by DisposableContainer() {
    init {
        assetManager.alsoRegister()
    }

    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap).alsoRegister() //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    val shapeDrawer by lazy {
        ShapeDrawer(Context.inject<PolygonSpriteBatch>() as Batch, shapeDrawerRegion)
    }

    val cityTexture by assetManager.loadOnDemand<Texture>("textures/city.png")
    val sound by assetManager.loadOnDemand<Sound>("audio/seasound.wav")
    val citySprite by lazy { Sprite(cityTexture).apply { setOriginCenter() } }

    val fishTexture by assetManager.loadOnDemand<Texture>("fish/fish.png")
    val fishRegions by lazy {
        GdxArray(
        Array(4) {
            val x = 32 * it
            val y = 0
            Sprite(TextureRegion(fishTexture, x, y, 32,32))
        })
    }
    val fishAnim by lazy { Animation(0.1f, fishRegions, Animation.PlayMode.LOOP_PINGPONG) }
}