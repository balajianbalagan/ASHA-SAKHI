package com.littleb01s.ashasakhichat.presentation.screens

import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.model.LocationType
import com.littleb01s.ashasakhichat.data.model.MapLocation
import com.littleb01s.ashasakhichat.data.model.RiskLevel
import com.littleb01s.ashasakhichat.data.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for the RegionalMapScreen
 */
@HiltViewModel
class RegionalMapViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {
    
    /**
     * Get all locations
     */
    val locations: Flow<List<MapLocation>> = locationService.locations
    
    /**
     * Get locations by type
     */
    fun getLocationsByType(type: LocationType): List<MapLocation> {
        return locationService.getLocationsByType(type)
    }
    
    /**
     * Get locations by risk level
     */
    fun getLocationsByRiskLevel(riskLevel: RiskLevel): List<MapLocation> {
        return locationService.getLocationsByRiskLevel(riskLevel)
    }
    
    /**
     * Add a new location
     */
    fun addLocation(location: MapLocation) {
        locationService.addLocation(location)
    }
    
    /**
     * Update an existing location
     */
    fun updateLocation(location: MapLocation) {
        locationService.updateLocation(location)
    }
    
    /**
     * Remove a location
     */
    fun removeLocation(id: String) {
        locationService.removeLocation(id)
    }
    
    /**
     * Get a location by ID
     */
    fun getLocationById(id: String): MapLocation? {
        return locationService.getLocationById(id)
    }
} 