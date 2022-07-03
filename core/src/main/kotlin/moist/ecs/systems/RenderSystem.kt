package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture
import eater.ai.UtilityAiComponent
import eater.ecs.components.CameraFollow
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import moist.core.Assets
import moist.core.GameConstants
import moist.ecs.components.*
import moist.world.currentTile

fun Fixture.isCreature(): Boolean {
    return this.body.userData is Entity && (this.body.userData as Entity).isCreature()
}

fun Fixture.isCity(): Boolean {
    return this.body.userData is Entity && (this.body.userData as Entity).isCity()
}

fun Entity.isShark(): Boolean {
    return Shark.has(this)
}

fun Entity.isFish(): Boolean {
    return Fish.has(this)
}

fun Entity.isCreature(): Boolean {
    return AshleyMappers.creature.has(this)
}

fun Entity.hasAi(): Boolean
{
    return AshleyMappers.ai.has(this)
}
fun Entity.ai(): UtilityAiComponent {
    return AshleyMappers.ai.get(this)
}


fun Entity.cloud(): Cloud {
    return AshleyMappers.cloud.get(this)
}

fun Entity.isCity(): Boolean {
    return AshleyMappers.city.has(this)
}

fun Entity.creature(): CreatureStats {
    return AshleyMappers.creature.get(this)
}

fun Entity.hasBody():Boolean {
    return AshleyMappers.box.has(this)
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

    val shapeDrawer by lazy { assets.shapeDrawer }
    override fun update(deltaTime: Float) {
        batch.use {
            super.update(deltaTime)
        }
    }

    val cameraFollowMapper = mapperFor<CameraFollow>()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (GlobalDebug.globalDebug && cameraFollowMapper.has(entity) && entity.isCreature()) {
            val fish = entity.creature()
            val body = entity.body()
            var x = 0f
            var y = 0f
            val targetTile = fish.targetTile
            if (targetTile != null) {
                x = targetTile.x * GameConstants.TileSize
                y = targetTile.y * GameConstants.TileSize
                shapeDrawer.filledRectangle(
                    x,
                    y,
                    GameConstants.TileSize,
                    GameConstants.TileSize, Color.GREEN
                )
            }

            val currentTile = body.currentTile()
            x = currentTile.x * GameConstants.TileSize
            y = currentTile.y * GameConstants.TileSize
            shapeDrawer.filledRectangle(
                x,
                y,
                GameConstants.TileSize,
                GameConstants.TileSize, Color.RED
            )
        }
        when (val renderType = entity.renderType()) {
            is RenderType.NotReallyRelevant -> renderSprite(entity, deltaTime)
            is RenderType.SelfRender -> renderType.render(batch, deltaTime)
            is RenderType.Sea -> renderType.render(batch, deltaTime)
            is RenderType.RenderAnimation -> renderAnimation(batch, entity, deltaTime, renderType)
            RenderType.Cloud -> renderCloud(entity)
        }


    }

    private val shadowColor = Color(0.1f, 0.1f, 0.1f, 0.8f)
    private val cloudExtraColor = Color(0.6f, 0.6f, 0.9f, 0.4f)
    private fun renderCloud(entity: Entity) {
        val cloud = entity.cloud()
        val position = entity.body().position
        for (cloudPuff in cloud.cloudPuffs) {
            shapeDrawer.filledCircle(position.x + cloudPuff.x, position.y + cloudPuff.y, cloudPuff.radius, shadowColor)
        }
        for (cloudPuff in cloud.cloudPuffs) {

            shapeDrawer.filledCircle(
                position.x + cloudPuff.x + 50f,
                position.y + cloudPuff.y + 50f,
                cloudPuff.radius,
                Color.WHITE
            )
            shapeDrawer.filledCircle(
                position.x + cloudPuff.x + 50f,
                position.y + cloudPuff.y + 50f,
                cloudPuff.radius,
                cloudExtraColor
            )
            shapeDrawer.filledCircle(
                position.x + cloudPuff.x + 60f,
                position.y + cloudPuff.y + 60f,
                cloudPuff.radius,
                Color.WHITE
            )

        }
    }

    private fun renderAnimation(
        batch: PolygonSpriteBatch,
        entity: Entity,
        deltaTime: Float,
        renderType: RenderType.RenderAnimation
    ) {
        val body = entity.body()
        var angle = body.angle * MathUtils.radiansToDegrees

        renderType.time += deltaTime
        val keyFrame = renderType.animation.getKeyFrame(renderType.time)
        keyFrame.setOriginBasedPosition(body.position.x, body.position.y)
        if (entity.isCreature()) {
            val creature = entity.creature()
            angle = creature.direction.cpy().rotate90(-1).angleDeg()
            keyFrame.setScale(creature.size)
        }

        keyFrame.rotation = angle
        keyFrame.draw(batch)
//        if(GlobalDebug.globalDebug) {
//            shapeDrawer.setColor(Color.RED)
//            shapeDrawer.filledCircle(body.position, 5f)
//        }
        if(GlobalDebug.globalDebug && entity.isCreature()) {
            val creature = entity.creature()
            val healthBarStart = body.position
            shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f, Color.BLACK, 3f)
            val normalizedHealth = MathUtils.norm(0f, GameConstants.FishMaxEnergy, creature.energy)
            shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedHealth, Color.GREEN, 3f)
        }
    }

    private fun renderSprite(entity: Entity, deltaTime: Float) {
        TODO("Not yet implemented")
    }
}