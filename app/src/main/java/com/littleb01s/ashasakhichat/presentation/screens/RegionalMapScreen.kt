package com.littleb01s.ashasakhichat.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.littleb01s.R
import com.littleb01s.ashasakhichat.data.model.LocationType
import com.littleb01s.ashasakhichat.data.model.MapLocation
import com.littleb01s.ashasakhichat.data.model.RiskLevel
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegionalMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegionalMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mapInitialized by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationText by remember { mutableStateOf("Location: Not available") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var mapLocations by remember { mutableStateOf<List<MapLocation>>(emptyList()) }
    
    // Collect locations from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.locations.collectLatest { locations ->
            mapLocations = locations
            // Update map with new locations if map is initialized
            if (mapInitialized && webView != null) {
                updateMapWithLocations(webView, mapLocations, userLocation)
            }
        }
    }
    
    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            // Get user location when permission is granted
            userLocation = getUserLocation(context)
            updateLocationText(userLocation, locationText) { newText ->
                locationText = newText
            }
        }
    }
    
    // Check for location permission on start
    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            userLocation = getUserLocation(context)
            updateLocationText(userLocation, locationText) { newText ->
                locationText = newText
            }
        } else {
            // Request permission
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    // Initialize map after a short delay to ensure container is ready
    LaunchedEffect(Unit) {
        delay(500) // Give the WebView time to render
        mapInitialized = true
    }
    
    // Rerender map when user location changes
    LaunchedEffect(userLocation) {
        if (mapInitialized && webView != null && userLocation != null) {
            val locationScript = "updateMapLocation(${userLocation?.longitude}, ${userLocation?.latitude});"
            webView?.evaluateJavascript(
                "if (typeof updateMapLocation === 'function') { $locationScript }",
                null
            )
        }
    }
    
    // Set up location updates
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && locationPermissionGranted) {
                // Update location when app resumes
                userLocation = getUserLocation(context)
                updateLocationText(userLocation, locationText) { newText ->
                    locationText = newText
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    DetailScaffold(
        title = stringResource(R.string.regional_map),
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Map overview card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Regional Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View and manage your assigned geographic area",
                        fontSize = 14.sp
                    )
                }
            }

            // Location information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = locationText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bhuvan Map WebView
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        // Reinitialize map when page is loaded
                                        if (mapInitialized) {
                                            // Pass user location to the map if available
                                            val locationScript = if (userLocation != null) {
                                                "initMap(${userLocation?.longitude}, ${userLocation?.latitude});"
                                            } else {
                                                "initMap();"
                                            }
                                            evaluateJavascript(
                                                "if (typeof initMap === 'function') { $locationScript }",
                                                null
                                            )
                                            
                                            // Add locations to the map
                                            updateMapWithLocations(this@apply, mapLocations, userLocation)
                                        }
                                    }
                                }
                                
                                // Handle geolocation permissions in WebView
                                webChromeClient = object : WebChromeClient() {
                                    override fun onGeolocationPermissionsShowPrompt(
                                        origin: String?,
                                        callback: GeolocationPermissions.Callback?
                                    ) {
                                        callback?.invoke(origin, true, false)
                                    }
                                }
                                
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    setGeolocationEnabled(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    setSupportZoom(true)
                                    setSupportMultipleWindows(true)
                                }
                                
                                // Load the HTML content with OpenLayers and Bhuvan WMS
                                loadDataWithBaseURL(
                                    null,
                                    getMapHtml(),
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                
                                // Store reference to WebView
                                webView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Back to Center Button
                    FloatingActionButton(
                        onClick = {
                            if (userLocation != null && webView != null) {
                                val script = "updateMapLocation(${userLocation?.longitude}, ${userLocation?.latitude});"
                                webView?.evaluateJavascript(
                                    "if (typeof updateMapLocation === 'function') { $script }",
                                    null
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Center"
                        )
                    }
                }
            }

            // Statistics cards
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Area Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatisticItem("Total Patients", mapLocations.count { it.type == LocationType.PATIENT }.toString())
                        StatisticItem("High Risk", mapLocations.count { it.type == LocationType.PATIENT && it.riskLevel == RiskLevel.HIGH }.toString())
                        StatisticItem("Due This Month", "12")
                    }
                }
            }
        }
    }
}

// Helper function to update map with locations
private fun updateMapWithLocations(webView: WebView?, locations: List<MapLocation>, userLocation: Location?) {
    if (webView == null) return

    val locationsJson = locations.map { location ->
        val type = location.type?.name ?: "PATIENT"
        """
        {
            \"id\": \"${location.id}\",
            \"name\": \"${location.name}\",
            \"latitude\": ${location.latitude},
            \"longitude\": ${location.longitude},
            \"type\": \"$type\",
            \"description\": \"${location.description}\",
            \"riskLevel\": \"${location.riskLevel}\"
        }
        """.trimIndent()
    }.joinToString(",")

    val userLocationJson = userLocation?.let {
        """
        {
            \"id\": \"user_location\",
            \"name\": \"You\",
            \"latitude\": ${it.latitude},
            \"longitude\": ${it.longitude},
            \"type\": \"USER\",
            \"description\": \"Current Location\",
            \"riskLevel\": \"\"
        }
        """.trimIndent()
    }

    val allLocationsJson = if (userLocationJson != null) {
        "[$locationsJson,$userLocationJson]"
    } else {
        "[$locationsJson]"
    }

    val script = "addLocations($allLocationsJson);"
    webView.evaluateJavascript(
        "if (typeof addLocations === 'function') { $script }",
        null
    )
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp
        )
    }
}

// Get user location from LocationManager
private fun getUserLocation(context: Context): Location? {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Check if GPS is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Get last known location
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("RegionalMapScreen", "GPS Location: ${location?.latitude}, ${location?.longitude}")
            return location
        }
        
        // If GPS is not available, try network provider
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            Log.d("RegionalMapScreen", "Network Location: ${location?.latitude}, ${location?.longitude}")
            return location
        }
    } catch (e: SecurityException) {
        Log.e("RegionalMapScreen", "Error getting location: ${e.message}")
        e.printStackTrace()
    }
    
    return null
}

// Update location text
private fun updateLocationText(location: Location?, currentText: String, updateText: (String) -> Unit) {
    if (location != null) {
        val newText = "Location: ${location.latitude}, ${location.longitude}"
        Log.d("RegionalMapScreen", newText)
        updateText(newText)
    } else {
        Log.d("RegionalMapScreen", "Location not available")
        updateText("Location: Not available")
    }
}

private fun getMapHtml(): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Bhuvan Map</title>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.15.1/css/ol.css">
            <script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.15.1/build/ol.js"></script>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    overflow: hidden;
                    touch-action: none;
                }
                .map {
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    top: 0;
                    left: 0;
                }
                .ol-popup {
                    position: absolute;
                    background-color: white;
                    box-shadow: 0 0 10px rgba(0,0,0,0.5);
                    padding: 8px;
                    border-radius: 4px;
                    font-size: 14px;
                    z-index: 1000;
                    min-width: 120px;
                }
                .popup-content {
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
                .ol-overlay-container {
                    z-index: 1000;
                }
                .ol-layer {
                    z-index: 1;
                }
                .ol-control {
                    z-index: 1000;
                }
                .location-button {
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    z-index: 1000;
                    background: white;
                    border: none;
                    border-radius: 4px;
                    padding: 8px;
                    cursor: pointer;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                }
                .location-button:hover {
                    background: #f0f0f0;
                }
                .map-legend {
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    background: black;
                    border: 1px solid #ccc;
                    padding: 10px;
                    font-size: 14px;
                    border-radius: 4px;
                    box-shadow: 0 0 5px rgba(0,0,0,0.2);
                    color: white;
                    z-index: 2000;
                }
                .legend-dot {
                    display: inline-block;
                    width: 12px;
                    height: 12px;
                    border-radius: 50%;
                    margin-right: 6px;
                    vertical-align: middle;
                }
            </style>
        </head>
        <body>
            <div class="map-legend">
              <div><span class="legend-dot" style="background-color: red;"></span> Hospital</div>
              <div><span class="legend-dot" style="background-color: blue;"></span> Patient</div>
              <div><span class="legend-dot" style="background-color: purple;"></span> College</div>
              <div><span class="legend-dot" style="background-color: gray;"></span> Other</div>
              <div><span class="legend-dot" style="background-color: green;"></span> You</div>
            </div>
            <div id="map" class="map"></div>
            <script>
                // Global map variable
                let map;
                let userLocationLayer;
                let pulseLayer;
                let locationOverlay;
                let markerLayer; // New layer for all location markers
                let locationMarkers = [];
                let currentUserLocation = null;
                
                // Function to initialize the map
                function initMap(userLng, userLat) {
                    // Check if map container has dimensions
                    const mapElement = document.getElementById('map');
                    if (mapElement.offsetWidth === 0 || mapElement.offsetHeight === 0) {
                        console.error('Map container has no dimensions');
                        return;
                    }
                    
                    // Default center (Chennai)
                    let center = [80.16, 13.11];
                    let zoom = 12; // Default zoom level is now 12
                    
                    // Use user location if provided
                    if (userLng !== undefined && userLat !== undefined) {
                        center = [userLng, userLat];
                        currentUserLocation = [userLng, userLat];
                        console.log('Using user location:', userLng, userLat);
                    }
                    
                    // Create the map
                    map = new ol.Map({
                        target: 'map',
                        layers: [
                            // Base layer - OpenStreetMap
                            new ol.layer.Tile({
                                source: new ol.source.OSM()
                            }),
                            // Bhuvan WMS layer
                            new ol.layer.Image({
                                source: new ol.source.ImageWMS({
                                    url: 'https://bhuvan-vec1.nrsc.gov.in/bhuvan/gwc/service/wms',
                                    params: {
                                        'LAYERS': 'india3',
                                        'FORMAT': 'image/jpeg',
                                        'VERSION': '1.1.1',
                                        'SERVICE': 'WMS',
                                        'REQUEST': 'GetMap',
                                        'SRS': 'EPSG:4326'
                                    },
                                    ratio: 1,
                                    serverType: 'geoserver'
                                })
                            })
                        ],
                        view: new ol.View({
                            projection: 'EPSG:4326',
                            center: center,
                            zoom: zoom
                        }),
                        controls: ol.control.defaults().extend([
                            new ol.control.ZoomSlider(),
                            new ol.control.ScaleLine()
                        ])
                    });
                    
                    // Add user location marker if coordinates are provided
                    if (userLng !== undefined && userLat !== undefined) {
                        addUserLocationMarker(userLng, userLat);
                    }
                    
                    // Force a resize event to ensure the map renders correctly
                    setTimeout(function() {
                        window.dispatchEvent(new Event('resize'));
                    }, 100);
                    
                    // Center on home location after a short delay
                    setTimeout(function() {
                        updateMapLocation(80.16, 13.11);
                    }, 500);
                }
                
                // Function to update map location
                function updateMapLocation(userLng, userLat) {
                    if (!map) {
                        console.error('Map not initialized');
                        return;
                    }
                    
                    console.log('Updating map location:', userLng, userLat);
                    currentUserLocation = [userLng, userLat];
                    
                    // Update map center with animation
                    map.getView().animate({
                        center: [userLng, userLat],
                        duration: 1000,
                        zoom: 12
                    });
                    
                    // Update or add user location marker
                    if (userLocationLayer) {
                        map.removeLayer(userLocationLayer);
                    }
                    
                    if (pulseLayer) {
                        map.removeLayer(pulseLayer);
                    }
                    
                    if (locationOverlay) {
                        map.removeOverlay(locationOverlay);
                    }
                    
                    addUserLocationMarker(userLng, userLat);
                }
                
                // Function to add user location marker
                function addUserLocationMarker(userLng, userLat) {0
                    // Create a vector layer for the marker
                    userLocationLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: [
                                new ol.Feature({
                                    geometry: new ol.geom.Point([userLng, userLat])
                                })
                            ]
                        }),
                        style: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 8,
                                fill: new ol.style.Fill({
                                    color: 'green' // Changed to green for user location
                                }),
                                stroke: new ol.style.Stroke({
                                    color: '#ffffff',
                                    width: 2
                                })
                            })
                        })
                    });
                    
                    // Add the layer to the map
                    map.addLayer(userLocationLayer);
                    
                    // Add a pulsing effect to the marker
                    const pulseFeature = new ol.Feature({
                        geometry: new ol.geom.Point([userLng, userLat])
                    });
                    
                    pulseLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: [pulseFeature]
                        }),
                        style: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 12,
                                fill: new ol.style.Fill({
                                    color: 'rgba(66, 133, 244, 0.3)'
                                }),
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(66, 133, 244, 0.5)',
                                    width: 1
                                })
                            })
                        })
                    });
                    
                    map.addLayer(pulseLayer);
                    
                    // Animate the pulse effect
                    let radius = 12;
                    let growing = true;
                    
                    setInterval(function() {
                        if (growing) {
                            radius += 0.5;
                            if (radius >= 20) growing = false;
                        } else {
                            radius -= 0.5;
                            if (radius <= 12) growing = true;
                        }
                        
                        pulseLayer.setStyle(new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: radius,
                                fill: new ol.style.Fill({
                                    color: 'rgba(66, 133, 244, 0.3)'
                                }),
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(66, 133, 244, 0.5)',
                                    width: 1
                                })
                            })
                        }));
                    }, 50);
                    
                    // Add a popup with location info
                    const popup = document.createElement('div');
                    popup.className = 'ol-popup';
                    popup.innerHTML = '<div class="popup-content">' + '👤 ' + 'You' + '<br>' + 'Current Location' + '</div>';
                    document.body.appendChild(popup);
                    
                    locationOverlay = new ol.Overlay({
                        element: popup,
                        position: [userLng, userLat],
                        positioning: 'bottom-center',
                        offset: [0, -10]
                    });
                    
                    map.addOverlay(locationOverlay);
                }
                
                // Function to add locations to the map
                function addLocations(locations) {
                    if (!map) {
                        console.error('Map not initialized');
                        return;
                    }
                    
                    console.log('Adding locations:', locations);
                    
                    // Remove existing location markers
                    locationMarkers.forEach(marker => {
                        if (marker.overlay) {
                            map.removeOverlay(marker.overlay);
                        }
                    });
                    
                    // Remove existing marker layer if it exists
                    if (markerLayer) {
                        map.removeLayer(markerLayer);
                    }
                    
                    locationMarkers = [];
                    
                    // Create features for all locations
                    const features = locations.map(location => {
                        // Create marker feature
                        return new ol.Feature({
                            geometry: new ol.geom.Point([location.longitude, location.latitude]),
                            name: location.name,
                            description: location.description,
                            type: location.type,
                            riskLevel: location.riskLevel
                        });
                    });
                    
                    // Create a single vector source for all markers
                    const vectorSource = new ol.source.Vector({
                        features: features
                    });
                    
                    // Create marker layer with style function
                    markerLayer = new ol.layer.Vector({
                        source: vectorSource,
                        style: function(feature) {
                            const type = feature.get('type');
                            const riskLevel = feature.get('riskLevel');
                            
                            // Create base marker style
                            const baseStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 8,
                                    fill: new ol.style.Fill({
                                        color: getColorForType(type, riskLevel)
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: '#ffffff',
                                        width: 2
                                    })
                                })
                            });
                            
                            // Create pulsing effect style
                            const pulseStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 12,
                                    fill: new ol.style.Fill({
                                        color: 'rgba(255, 82, 82, 0.3)' // Red with transparency
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: 'rgba(255, 82, 82, 0.5)', // Red with transparency
                                        width: 1
                                    })
                                })
                            });
                            
                            return [pulseStyle, baseStyle];
                        },
                        zIndex: 2
                    });
                    
                    // Add the marker layer to the map
                    map.addLayer(markerLayer);
                    
                    // Animate the pulse effect for all markers
                    let radius = 12;
                    let growing = true;
                    
                    setInterval(function() {
                        if (growing) {
                            radius += 0.5;
                            if (radius >= 20) growing = false;
                        } else {
                            radius -= 0.5;
                            if (radius <= 12) growing = true;
                        }
                        
                        // Update style for all features
                        markerLayer.getSource().getFeatures().forEach(feature => {
                            const type = feature.get('type');
                            const riskLevel = feature.get('riskLevel');
                            
                            const baseStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 8,
                                    fill: new ol.style.Fill({
                                        color: getColorForType(type, riskLevel)
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: '#ffffff',
                                        width: 2
                                    })
                                })
                            });
                            
                            const pulseStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: radius,
                                    fill: new ol.style.Fill({
                                        color: 'rgba(255, 82, 82, 0.3)'
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: 'rgba(255, 82, 82, 0.5)',
                                        width: 1
                                    })
                                })
                            });
                            
                            feature.setStyle([pulseStyle, baseStyle]);
                        });
                    }, 50);
                    
                    // Add popups for each location
                    locations.forEach(location => {
                        const popup = document.createElement('div');
                        popup.className = 'ol-popup';
                        popup.innerHTML = '<div class="popup-content">' + getIconForType(location.type, location.riskLevel) + ' ' + location.name + '<br>' + location.description + '</div>';
                        document.body.appendChild(popup);
                        
                        const overlay = new ol.Overlay({
                            element: popup,
                            position: [location.longitude, location.latitude],
                            positioning: 'bottom-center',
                            offset: [0, -10],
                            stopEvent: true
                        });
                        
                        map.addOverlay(overlay);
                        locationMarkers.push({ overlay });
                    });
                }
                
                // Helper function to get icon for location type
                function getIconForType(type, riskLevel) {
                    switch(type) {
                        case 'PATIENT':
                            return '👤';
                        case 'HEALTH_CENTER':
                            return '🏥';
                        case 'PHARMACY':
                            return '💊';
                        case 'OTHER':
                            return '📍';
                        default:
                            return '📍';
                    }
                }
                
                // Helper function to get color for location type
                function getColorForType(type, riskLevel) {
                    switch(type) {
                        case 'PATIENT':
                            // Determine color based on risk level
                            switch(riskLevel) {
                                case 'HIGH':
                                    return '#FF5252'; // Red
                                case 'MEDIUM':
                                    return '#FFC107'; // Amber
                                case 'LOW':
                                    return '#4CAF50'; // Green
                                default:
                                    return '#9E9E9E'; // Grey
                            }
                        case 'HEALTH_CENTER':
                            return '#2196F3'; // Blue
                        case 'PHARMACY':
                            return '#9C27B0'; // Purple
                        case 'OTHER':
                            return '#9E9E9E'; // Grey
                        default:
                            return '#9E9E9E'; // Grey
                    }
                }
                
                // Initialize map when DOM is loaded
                document.addEventListener('DOMContentLoaded', function() {
                    // Wait a bit to ensure container has dimensions
                    setTimeout(function() {
                        initMap();
                    }, 100);
                });
            </script>
        </body>
        </html>
    """.trimIndent()
} 