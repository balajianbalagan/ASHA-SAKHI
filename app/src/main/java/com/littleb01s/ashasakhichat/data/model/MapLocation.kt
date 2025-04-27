package com.littleb01s.ashasakhichat.data.model

/**
 * Data class representing a location on the map
 * @param id Unique identifier for the location
 * @param name Name of the location (e.g., patient name)
 * @param latitude Latitude coordinate
 * @param longitude Longitude coordinate
 * @param type Type of location (e.g., "patient", "health_center", "pharmacy")
 * @param description Additional information about the location
 * @param riskLevel Risk level associated with the location (for patients)
 */
data class MapLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType = LocationType.PATIENT,
    val description: String = "",
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN
)

/**
 * Enum representing the type of location
 */
enum class LocationType {
    PATIENT,
    HEALTH_CENTER,
    PHARMACY,
    OTHER
}

/**
 * Enum representing the risk level of a patient
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN
} 