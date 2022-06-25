package moist.core

import kotlin.experimental.or

object GameConstants {
    const val TileSize = 25f
    const val GameWidth = 96f
    const val GameHeight = 64f
    const val MaxTilesPerSide = 50
    const val StartFishCount = 500
    const val FishMatingEnergyRequirement = 0.7f
    const val MaxWaterTemp = 35f
    const val MinWaterTemp = 1f
    const val CurrentsMagnitude = 50f
    const val ControlMagnitude = 25f
    const val FishMagnitude = 1f
    const val FishMaxVelocity = 50f
    const val populationMin = 1f
    const val populationMax = 1000f
    const val foodMin = 1f
    const val foodMax = 1000f
    const val FishMaxEnergy = 100f
    const val FishEnergyExpenditurePerSecond = 0.1f
    const val TileStartFood = 2f
    const val TileMaxFood = 100f
    const val FoodTempMin = 5f
    const val FoodTempMax = 10f
    const val FoodTempDeath = 25f
    val FoodTempRange = FoodTempMin..FoodTempMax
    const val FishEatingPace = 5f
    const val WindMagnitude = 75f
}

object Box2dCategories {
    const val none: Short = 0
    const val cities: Short = 1
    const val fish: Short = 2

    val whatFishCollideWith = cities
    val whatCitiesCollideWith = cities or fish
}