package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.ecs.components.City
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable
import moist.ecs.components.Tile
import moist.world.SeaManager

fun Entity.body(): Body {
    return AshleyMappers.box.get(this).body
}

fun Entity.city(): City {
    return AshleyMappers.city.get(this)
}

fun Entity.renderable(): Renderable {
    return AshleyMappers.renderable.get(this)
}

fun Entity.renderType(): RenderType {
    return this.renderable().renderType
}

fun Entity.layer(): Int {
    return this.renderType().layer
}


class RenderSystem(private val batch: PolygonSpriteBatch, assets: Assets) : SortedIteratingSystem(
    allOf(Renderable::class).get(),
    Comparator<Entity> { p0, p1 ->
        val layer0 = p0.layer()
        val layer1 = p1.layer()
        layer0.compareTo(layer1)
    }) {


    private val seaColor = Color(1f, 1f, 0.1f, 1f)
    private val shapeDrawer by lazy { assets.shapeDrawer }

    override fun update(deltaTime: Float) {
        batch.use {
            renderSea(deltaTime)

            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        when (val renderType = entity.renderType()) {
            RenderType.Sprite -> renderSprite(entity, deltaTime)
            is RenderType.SelfRender -> renderType.render(batch, deltaTime)
        }
    }

    private fun renderSprite(entity: Entity, deltaTime: Float) {
        TODO("Not yet implemented")
    }

    private fun renderSea(deltaTime: Float) {
        for(column in SeaManager.tiles) {
            for(tile in column) {
                seaColor.b = tile.depth
                seaColor.r = tile.depth / 10f
                seaColor.g = MathUtils.norm(MinWaterTemp, MaxWaterTemp, tile.waterTemp)
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