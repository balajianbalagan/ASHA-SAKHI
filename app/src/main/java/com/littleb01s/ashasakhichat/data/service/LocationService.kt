package com.littleb01s.ashasakhichat.data.service

import com.littleb01s.ashasakhichat.data.model.LocationType
import com.littleb01s.ashasakhichat.data.model.MapLocation
import com.littleb01s.ashasakhichat.data.model.RiskLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing locations on the map
 */
@Singleton
class LocationService @Inject constructor() {
    
    // StateFlow to hold the list of locations
    private val _locations = MutableStateFlow<List<MapLocation>>(emptyList())
    val locations: Flow<List<MapLocation>> = _locations.asStateFlow()
    
    init {
        // Initialize with static data
        loadStaticData()
    }
    
    /**
     * Load static data for testing
     */
    private fun loadStaticData() {
        val staticLocations = listOf(
            // Home location
            MapLocation(
                id = "home",
                name = "Home",
                latitude = 13.11,
                longitude = 80.16,
                type = LocationType.OTHER,
                description = "Your home location"
            ),
            
            // Nearby Patients
            MapLocation(
                id = "p1",
                name = "Priya Sharma",
                latitude = 13.112,
                longitude = 80.162,
                type = LocationType.PATIENT,
                description = "Pregnant woman, 28 weeks",
                riskLevel = RiskLevel.HIGH
            ),
            MapLocation(
                id = "p2",
                name = "Anjali Patel",
                latitude = 13.108,
                longitude = 80.158,
                type = LocationType.PATIENT,
                description = "New mother, 2 weeks postpartum",
                riskLevel = RiskLevel.MEDIUM
            ),
            MapLocation(
                id = "p3",
                name = "Meera Reddy",
                latitude = 13.115,
                longitude = 80.165,
                type = LocationType.PATIENT,
                description = "Pregnant woman, 12 weeks",
                riskLevel = RiskLevel.LOW
            ),
            MapLocation(
                id = "p4",
                name = "Lakshmi Devi",
                latitude = 13.105,
                longitude = 80.155,
                type = LocationType.PATIENT,
                description = "High-risk pregnancy, 32 weeks",
                riskLevel = RiskLevel.HIGH
            ),
            MapLocation(
                id = "p5",
                name = "Sita Kumari",
                latitude = 13.118,
                longitude = 80.168,
                type = LocationType.PATIENT,
                description = "Postpartum care, 1 week",
                riskLevel = RiskLevel.MEDIUM
            ),
            
            // Nearby Health Centers
            MapLocation(
                id = "h1",
                name = "City Health Center",
                latitude = 13.113,
                longitude = 80.163,
                type = LocationType.HEALTH_CENTER,
                description = "Primary health center with maternity ward"
            ),
            MapLocation(
                id = "h2",
                name = "Community Clinic",
                latitude = 13.107,
                longitude = 80.157,
                type = LocationType.HEALTH_CENTER,
                description = "24/7 emergency care"
            ),
            
            // Nearby Pharmacies
            MapLocation(
                id = "ph1",
                name = "Community Pharmacy",
                latitude = 13.109,
                longitude = 80.159,
                type = LocationType.PHARMACY,
                description = "24/7 pharmacy with maternal health supplies"
            ),
            MapLocation(
                id = "ph2",
                name = "MedPlus",
                latitude = 13.116,
                longitude = 80.166,
                type = LocationType.PHARMACY,
                description = "General pharmacy with basic supplies"
            )
        )
        
        _locations.value = staticLocations
    }
    
    /**
     * Add a new location to the list
     */
    fun addLocation(location: MapLocation) {
        val currentLocations = _locations.value.toMutableList()
        currentLocations.add(location)
        _locations.value = currentLocations
    }
    
    /**
     * Update an existing location
     */
    fun updateLocation(location: MapLocation) {
        val currentLocations = _locations.value.toMutableList()
        val index = currentLocations.indexOfFirst { it.id == location.id }
        if (index != -1) {
            currentLocations[index] = location
            _locations.value = currentLocations
        }
    }
    
    /**
     * Remove a location by ID
     */
    fun removeLocation(id: String) {
        val currentLocations = _locations.value.toMutableList()
        currentLocations.removeAll { it.id == id }
        _locations.value = currentLocations
    }
    
    /**
     * Get locations by type
     */
    fun getLocationsByType(type: LocationType): List<MapLocation> {
        return _locations.value.filter { it.type == type }
    }
    
    /**
     * Get locations by risk level
     */
    fun getLocationsByRiskLevel(riskLevel: RiskLevel): List<MapLocation> {
        return _locations.value.filter { it.riskLevel == riskLevel }
    }
    
    /**
     * Get a location by ID
     */
    fun getLocationById(id: String): MapLocation? {
        return _locations.value.find { it.id == id }
    }
} 