package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.*
import moist.ai.UtilityAiComponent
import moist.core.Assets
import moist.core.Box2dCategories
import moist.core.GameConstants.FishMatingEnergyRequirement
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.StartFishCount
import moist.core.GameConstants.TileSize
import moist.core.GameConstants.FoodMax
import moist.core.GameConstants.FoodMin
import moist.core.GameConstants.PopulationMax
import moist.core.GameConstants.PopulationMin
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish
import moist.injection.Context.inject
import moist.world.engine
import moist.world.world

class City : Component, Poolable {
    val drag = vec2()
    val sailVector = Vector2.Y
    var population = 100f
    var food = FoodMax / 2

    val currentForce = vec2()
    val windForce = vec2()

    val potentialCatches = mutableMapOf<Entity, Float>()

    override fun reset() {
        population = 100f
        currentForce.setZero()
        windForce.setZero()

    }
}

fun city(): Entity {
    return engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set((MaxTilesPerSide / 2) * TileSize, (MaxTilesPerSide / 2) * TileSize)
//                linearDamping = 5f
                box(2f, 2f) {
                    friction = 10f //Tune
                    density = 1f //tune
//                    restitution = 0.9f
                    filter {
                        categoryBits = Box2dCategories.cities
                        maskBits = Box2dCategories.whatCitiesCollideWith
                    }
                }
                circle(radius = 60f) {
                    isSensor = true
                }
            }
        }
        with<City> {
            population = 100f
        }
        with<CameraFollow>()
        with<Renderable> {
            val sprite = inject<Assets>().citySprite
            val cityColor = Color(0.01f, 1f, 0.01f, 1f)
            val spritePos = vec2()
            renderType = RenderType.SelfRender(2) { batch, deltaTime ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                val position = this@entity.entity.body().position
                val city = this@entity.entity.city()
                val rows = 8//sqrt(city.population.toDouble()).toInt()
                val start = 0 - rows / 2
                val stop = rows / 2
                val radius = rows / 2 * sprite.width

                for (x in start until stop)
                    for (y in start until stop) {
                        spritePos.set(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.setOriginBasedPosition(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.draw(batch)
                    }

                val healthBarStart = position - vec2(rows / 2 * sprite.width - sprite.width, 0f + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f,Color.BLACK, 3f)

                val normalizedFood = MathUtils.norm(FoodMin, FoodMax, city.food)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedFood, Color.GREEN, 3f)

                healthBarStart.set(healthBarStart.x, healthBarStart.y + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f,Color.BLACK, 3f)
                val normalizedPop = MathUtils.norm(PopulationMin, PopulationMax, city.population)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedPop, Color.RED, 3f)


                shapeDrawer.line(position, position + city.sailVector * 50, Color.BLACK, 2f)
                shapeDrawer.line(position, position + city.currentForce, Color.BLUE, 2f)
                shapeDrawer.line(position, position + city.windForce, Color.WHITE, 2f)
                shapeDrawer.line(position, position + city.drag, Color.RED, 2f)



                shapeDrawer.circle(position.x, position.y, 60f)

//                cityColor.g = MathUtils.norm(foodMin, foodMax, city.food)
//                shapeDrawer.filledCircle(
//                    ,
//                    this@entity.entity.city().population / 100f,
//                    cityColor
//                )
            }
        }
    }
}

fun fish(fishPos: Vector2) {
    engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(fishPos)
                box(.5f, .5f) {
                    density = 1f
                    filter {
                        categoryBits = Box2dCategories.fish
                        maskBits = Box2dCategories.whatFishCollideWith
                    }
                }
            }
        }
        with<Fish>()
        with<UtilityAiComponent>()
        with<Renderable> {
            renderType = RenderType.SelfRender(0) { batch, deltaTime ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                val fish = this@entity.entity.fish()
                fish.fishColor.g = MathUtils.norm(0f, FishMaxEnergy, fish.energy)
                fish.fishColor.r = if(fish.energy > FishMatingEnergyRequirement) 1.0f else 0f
                shapeDrawer.filledCircle(
                    this@entity.entity.body().position,
                    1.0f,
                    fish.fishColor
                )
            }
        }
    }
}

fun fishes() {
    val min = 0 + TileSize
    val max = MaxTilesPerSide * TileSize - TileSize
    val range = min..max
    val shoalStartPoint = vec2(range.random(), range.random())
    (0 until StartFishCount).forEach {
        val fishPos = vec2(range.random(), range.random())
        fish(fishPos)
    }
}

fun sea() {
    engine().entity {
        with<Renderable> {
            renderType = RenderType.Sea()
        }
    }
}

fun hud() {

    // We project screen coordinates to world coordinates, simple as that! Maybe?
    val screenCoordinates = vec3(100f, 100f, 0f)
    val camera = inject<OrthographicCamera>()

    engine().entity {

    }

}

class CompassActor: Actor() {
    private val playerFamily = allOf(City::class).get()
    private val allCities get() = engine().getEntitiesFor(playerFamily)
    private val currentVector = Vector2.X
    private val windVector = Vector2.X

    private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
    override fun draw(batch: Batch, parentAlpha: Float) {
        for(cityEntity in allCities) {
            val body = cityEntity.body()
            val currentTile = body.currentTile()
            currentVector.lerp(currentTile.current, 0.1f)
            windVector.lerp(currentTile.wind, 0.1f)

            shapeDrawer.line(x, y, x + currentVector.x, y + currentVector.y, Color.BLUE)
            shapeDrawer.line(x, y, x + windVector.x, y + windVector.y, Color.WHITE)
        }

    }

}