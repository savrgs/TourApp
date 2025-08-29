package com.example.tourfrontend

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tourfrontend.ui.theme.TourfrontendTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import androidx.compose.material.icons.filled.CheckCircle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourfrontendTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CityExplorerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityExplorerApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "cities") {
        composable("cities") {
            CityExplorerScreen(navController)
        }
        composable(
            "places/{cityId}",
            arguments = listOf(navArgument("cityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getLong("cityId") ?: 0L
            PlacesScreen(cityId, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityExplorerScreen(navController: androidx.navigation.NavController) {
    // Provide Retrofit and dependencies
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(CityApiService::class.java) }
    val repository = remember { CityRepository(api) }
    val viewModel: CityViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CityViewModel(repository) as T
        }
    })

    val cities by viewModel.cities.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "City Explorer",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "City Explorer",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cities ?: emptyList()) { city ->
                        CityCard(
                            city = city,
                            onClick = {
                                navController.navigate("places/${city.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityCard(
    city: CityDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City Icon
            Icon(
                imageVector = Icons.Default.LocationCity,
                contentDescription = "City",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // City Information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = city.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = city.country,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Navigation Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate",
                modifier = Modifier
                    .size(20.dp)
                    .rotate(180f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(cityId: Long, navController: androidx.navigation.NavController) {
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(CityApiService::class.java) }
    val repository = remember { CityRepository(api) }
    val viewModel: PlaceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(repository) as T
        }
    })

    val places by viewModel.places.collectAsState()
    val loading by viewModel.loading.collectAsState()
    
    // Selected places state
    var selectedPlaces by remember { mutableStateOf<List<PlaceDto>>(emptyList()) }
    
    // Update selected places when places data changes
    LaunchedEffect(places) {
        selectedPlaces = places ?: emptyList()
    }

    // Location services
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> 
            locationPermissionGranted = granted
            if (granted) {
                // Get user location after permission is granted
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        userLocation = location
                    }
                }
            }
        }
    )

    // Check and request location permission
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        locationPermissionGranted = granted
        if (!granted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Get user location if permission already granted
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location
            }
        }
    }

    LaunchedEffect(cityId) {
        viewModel.fetchPlacesForCity(cityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Places") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (loading) {
                Text("Loading places...", modifier = Modifier.fillMaxSize())
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    MapViewComponent(
                        places = selectedPlaces,
                        showUserLocation = locationPermissionGranted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selection Summary
                    if (places?.isNotEmpty() == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedPlaces.size} of ${places!!.size} places selected for route",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Plan Tour Route Button
                    if (places?.isNotEmpty() == true) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    if (userLocation != null) {
                                        if (selectedPlaces.size >= 2) {
                                            val sortedPlaces = selectedPlaces.sortedBy { place ->
                                                val placeLocation = android.location.Location("").apply {
                                                    latitude = place.latitude
                                                    longitude = place.longitude
                                                }
                                                userLocation!!.distanceTo(placeLocation)
                                            }
                                            
                                            val origin = "${userLocation!!.latitude},${userLocation!!.longitude}"
                                            val destination = "${sortedPlaces.last().latitude},${sortedPlaces.last().longitude}"
                                            val waypoints = sortedPlaces.dropLast(1).joinToString("|") { 
                                                "${it.latitude},${it.longitude}" 
                                            }
                                            
                                            val uri = Uri.parse(
                                                "https://www.google.com/maps/dir/?api=1" +
                                                "&origin=$origin" +
                                                "&destination=$destination" +
                                                "&waypoints=$waypoints" +
                                                "&travelmode=walking"
                                            )
                                            
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.setPackage("com.google.android.apps.maps")
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Fallback to browser if Google Maps app is not installed
                                                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                                context.startActivity(browserIntent)
                                            }
                                        } else {
                                            Toast.makeText(context, "Please select at least two places to plan a route", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue color
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Plan Tour Route",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plan Tour Route",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(places ?: emptyList()) { place ->
                            PlaceCard(
                                place = place,
                                userLocation = userLocation,
                                isSelected = selectedPlaces.contains(place),
                                onSelectionChanged = { isSelected ->
                                    selectedPlaces = if (isSelected) {
                                        selectedPlaces + place
                                    } else {
                                        selectedPlaces - place
                                    }
                                },
                                onNavigate = { destinationLat, destinationLon ->
                                    if (userLocation != null) {
                                        val uri = Uri.parse(
                                            "https://www.google.com/maps/dir/?api=1" +
                                            "&origin=${userLocation!!.latitude},${userLocation!!.longitude}" +
                                            "&destination=$destinationLat,$destinationLon" +
                                            "&travelmode=walking"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.setPackage("com.google.android.apps.maps")
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback to browser if Google Maps app is not installed
                                            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(browserIntent)
                                        }
                                    } else {
                                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    place: PlaceDto,
    userLocation: android.location.Location?,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onNavigate: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    // Info Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = place.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = place.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Photo display
                    if (place.photoUrl != null) {
                        AsyncImage(
                            model = place.photoUrl,
                            contentDescription = "Photo of ${place.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop,
                            /*error = {
                                // Fallback image when photo fails to load
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📷",
                                        fontSize = 48.sp
                                    )
                                }
                            }*/
                        )
                    } else {
                        // Default image when no photo is available
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 48.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Place information with icon and selection checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Place type icon
                Text(
                    text = getPlaceTypeIcon(place.type),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                // Place details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = place.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${place.type} • ${if (place.isFree) "Free" else "Paid"}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = place.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        maxLines = 5
                    )
                }
                
                // Info icon
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "More info about ${place.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Selection checkbox
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigate button
            Button(
                onClick = { onNavigate(place.latitude, place.longitude) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navigate to ${place.name}",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Navigate",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun getPlaceTypeIcon(placeType: String): String {
    return when (placeType.lowercase()) {
        "museum" -> "🏛️"
        "church", "cathedral", "temple" -> "⛪"
        "monument", "statue" -> "🗽"
        "park", "garden" -> "🌳"
        "restaurant", "cafe" -> "🍽️"
        "hotel", "accommodation" -> "🏨"
        "shopping", "mall", "market" -> "🛍️"
        "theater", "cinema" -> "🎭"
        "library" -> "📚"
        "hospital", "clinic" -> "🏥"
        "school", "university" -> "🎓"
        "airport" -> "✈️"
        "train station", "bus station" -> "🚉"
        "beach" -> "🏖️"
        "mountain", "hill" -> "⛰️"
        "lake", "river" -> "🏞️"
        "castle", "palace" -> "🏰"
        "bridge" -> "🌉"
        "tower" -> "🗼"
        else -> "📍"
    }
}

@Composable
fun MapViewComponent(
    places: List<PlaceDto>,
    showUserLocation: Boolean
) {
    val singapore = LatLng(1.3521, 103.8198) // fallback location
    val firstPlaceLatLng = places.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            firstPlaceLatLng ?: singapore,
            12f,
            0f,
            0f
        )
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        try {
            val result = MapsInitializer.initialize(context)
            Log.d("MapViewComponent", "MapsInitializer.initialize result: $result")
        } catch (e: Exception) {
            Log.e("MapViewComponent", "MapsInitializer.initialize failed", e)
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = showUserLocation),
        uiSettings = MapUiSettings(zoomControlsEnabled = true),
        onMapLoaded = {
            Log.d("MapViewComponent", "GoogleMap loaded successfully")
        }
    ) {
        for (place in places) {
            Marker(
                state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                title = place.name,
                snippet = place.type
            )
        }
    }
}