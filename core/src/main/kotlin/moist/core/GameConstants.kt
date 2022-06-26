package moist.core

import kotlin.experimental.or

object GameConstants {
    const val TileSize = 50f
    const val GameWidth = 96f
    const val GameHeight = 64f
    const val MaxTilesPerSide = 25
    const val StartFishCount = 2
    const val MaxFishCount = 500
    const val FishMatingEnergyRequirement = 80f
    const val FishPlayingEnergyRequirement = 50f
    const val MaxWaterTemp = 35f
    const val MinWaterTemp = 1f
    const val CurrentsMagnitude = 25f
    const val ControlMagnitude = 25f
    const val FishMagnitude = 1f
    const val FishMaxVelocity = 50f
    const val PopulationMin = 1f
    const val PopulationMax = 1000f
    const val FoodMin = 1f
    const val FoodMax = 1000f
    const val FishMaxEnergy = 100f
    const val FishEnergyExpenditurePerSecond = 0.5f
    const val TileStartFood = 2f
    const val TileMaxFood = 100f
    const val FoodTempMin = 5f
    const val FoodTempMax = 20f
    const val FoodTempDeath = 35f
    val FoodTempRange = FoodTempMin..FoodTempMax
    const val FishEatingPace = 10f
    const val WindMagnitude = 75f
}

object Box2dCategories {
    const val none: Short = 0
    const val cities: Short = 1
    const val fish: Short = 2

    val whatFishCollideWith = cities
    val whatCitiesCollideWith = cities
    val whatCitiesSense = cities or fish
}