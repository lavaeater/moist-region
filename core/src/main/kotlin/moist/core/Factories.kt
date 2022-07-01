package moist.core

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.*
import ktx.math.*
import moist.ai.UtilityAiActions
import moist.ai.UtilityAiComponent
import moist.core.GameConstants.SharkMaxVelocity
import moist.ecs.components.*
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.injection.Context
import moist.world.SeaManager
import moist.world.engine
import moist.world.world

fun city(cameraFollow: Boolean = true): Entity {
    return engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set((GameConstants.MaxTilesPerSide / 2) * GameConstants.TileSize, (GameConstants.MaxTilesPerSide / 2) * GameConstants.TileSize)
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
        if(cameraFollow)
            with<CameraFollow>()
        with<Renderable> {
            val sprite = Context.inject<Assets>().citySprite
            val cityColor = Color(0f, 0f, 0f, .3f)
            val spritePos = vec2()
            renderType = RenderType.SelfRender(2) { batch, _ ->
                val shapeDrawer = Context.inject<Assets>().shapeDrawer
                val body = this@entity.entity.body()
                val position = body.position
                val city = this@entity.entity.city()
                val rows = 8
                val start = 0 - rows / 2
                val stop = rows / 2
                shapeDrawer.setColor(cityColor)
                shapeDrawer.filledCircle(position.x, position.y, 60f)
                shapeDrawer.setColor(Color.WHITE)

                for (x in start until stop)
                    for (y in start until stop) {
                        spritePos.set(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.setOriginBasedPosition(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.draw(batch)
                    }

                val healthBarStart = position - vec2(rows / 2 * sprite.width - sprite.width, 0f + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f, Color.BLACK, 3f)

                val normalizedFood = MathUtils.norm(GameConstants.FoodMin, GameConstants.FoodMax, city.food)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedFood, Color.GREEN, 3f)

                healthBarStart.set(healthBarStart.x, healthBarStart.y + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f, Color.BLACK, 3f)
                val normalizedPop = MathUtils.norm(GameConstants.PopulationMin, GameConstants.PopulationMax, city.population)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedPop, Color.RED, 3f)

                val sailStart = position - (city.sailVector * 50).rotateDeg(45f)
                val sailStop = position + (city.sailVector * 50).rotateDeg(45f)
                val sailMast = position - (city.sailVector * 25) + (city.sailVector * 50)//.rotateDeg(45f)
                val sailArray = listOf(sailStart.x, sailStart.y, sailMast.x, sailMast.y, sailStop.x, sailStop.y)
                shapeDrawer.setColor(Color.WHITE)
                shapeDrawer.filledPolygon(sailArray.toFloatArray())

                shapeDrawer.line(sailStart, sailStop, Color.BLACK, 3f)

            }
        }
    }
}

fun cloud(cloudPos: Vector2) {
    engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(cloudPos)
                circle(1f) {
                    filter {
                        categoryBits = Box2dCategories.cloud
                        maskBits = Box2dCategories.whatCloudsCollideWith
                    }
                }
            }
        }
        with<Renderable> {
            renderType = RenderType.Cloud
        }
        with<Cloud> {
            val v = Vector2.X.cpy()
            val range = 0f..75f
            val sizeRange = 15f..50f
            val angleRange = 0f..359f
            for(i in 1 until (4..7).random()) {
                val randomSpot = (v * range.random()).rotateDeg(angleRange.random())
                cloudPuffs.add(Circle(randomSpot.x, randomSpot.y, sizeRange.random()))
            }
        }
    }
}

fun fish(fishPos: Vector2, cameraFollow: Boolean = false) {
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
        with<CreatureStats> {
            canDie = !cameraFollow
        }
        if(cameraFollow)
            with<CameraFollow>()
        with<UtilityAiComponent> {
            actions.addAll(UtilityAiActions.fishActions)
        }
        with<Renderable> {
            renderType = RenderType.RenderAnimation(0, Context.inject<Assets>().fishAnim)
        }
    }
}

fun shark(sharkPos: Vector2, cameraFollow: Boolean = false) {
    engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(sharkPos)
                box(.5f, .5f) {
                    density = 1f
                    filter {
                        categoryBits = Box2dCategories.shark
                        maskBits = Box2dCategories.whatSharksCollideWith
                    }
                }
            }
        }
        with<Shark> ()
        with<CreatureStats> {
            fishMaxVelocity = SharkMaxVelocity
            size = (1.25f..1.75f).random()
            canDie = !cameraFollow
        }
        if(cameraFollow)
            with<CameraFollow>()
        with<UtilityAiComponent> {
            actions.addAll(UtilityAiActions.sharkActions)
        }
        with<Renderable> {
            renderType = RenderType.RenderAnimation(0, Context.inject<Assets>().sharkAnim)
        }
    }
}

fun randomShark() {
    val seaManager = Context.inject<SeaManager>()
    val minX = seaManager.getCurrentTiles().minByOrNull { it.x }!!.x * GameConstants.TileSize
    val maxX = seaManager.getCurrentTiles().maxByOrNull { it.x }!!.x * GameConstants.TileSize
    val minY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * GameConstants.TileSize
    val maxY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * GameConstants.TileSize
    val xRange = minX..maxX
    val yRange = minY..maxY
    shark(vec2(xRange.random(), yRange.random()))
}

fun randomFish() {
    val seaManager = Context.inject<SeaManager>()
    val minX = seaManager.getCurrentTiles().minByOrNull { it.x }!!.x * GameConstants.TileSize
    val maxX = seaManager.getCurrentTiles().maxByOrNull { it.x }!!.x * GameConstants.TileSize
    val minY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * GameConstants.TileSize
    val maxY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * GameConstants.TileSize
    val xRange = minX..maxX
    val yRange = minY..maxY
    fish(vec2(xRange.random(), yRange.random()))
}

fun sharks(cameraFollow: Boolean = false) {
    val min = 0 + GameConstants.TileSize
    val max = GameConstants.MaxTilesPerSide * GameConstants.TileSize - GameConstants.TileSize
    val range = min..max
    (1..5).forEach {
        shark(vec2(range.random(), range.random()), it == 1 && cameraFollow)
    }
}

fun fishes() {
    val min = 0 + GameConstants.TileSize
    val max = GameConstants.MaxTilesPerSide * GameConstants.TileSize - GameConstants.TileSize
    val range = min..max
    (0 until GameConstants.StartFishCount).forEach { _ ->
        val fishPos = vec2(range.random(), range.random())
        fish(fishPos)//, it == 0)
    }
}

fun sea() {
    engine().entity {
        with<Renderable> {
            renderType = RenderType.Sea()
        }
    }
}