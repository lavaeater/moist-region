package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.allOf
import ktx.graphics.use
import moist.core.Assets
import moist.core.GameConstants
import moist.ecs.components.City
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable
import moist.ecs.components.Tile

fun Entity.tile(): Tile {
    return AshleyMappers.tile.get(this)
}

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
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        when (val renderType = entity.renderType()) {
            RenderType.Sea -> renderSeaEntity(entity, deltaTime)
            RenderType.Sprite -> renderSprite(entity, deltaTime)
            is RenderType.SelfRender -> renderType.render(batch, deltaTime)
        }
    }

    private fun renderSprite(entity: Entity, deltaTime: Float) {
        TODO("Not yet implemented")
    }

    private fun renderSeaEntity(entity: Entity, deltaTime: Float) {
        val tile = entity.tile()
        seaColor.b = tile.depth
        seaColor.r = tile.depth / 10f
        seaColor.g = tile.depth / 2f
        shapeDrawer.filledRectangle(
            tile.x * GameConstants.TileSize,
            tile.y * GameConstants.TileSize,
            GameConstants.TileSize,
            GameConstants.TileSize,
            seaColor
        )
    }
}