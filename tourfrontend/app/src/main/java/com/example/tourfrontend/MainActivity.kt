package com.example.tourfrontend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tourfrontend.ui.theme.TourfrontendTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.util.Log
import com.google.android.gms.maps.MapsInitializer

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

    // Location permission state
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> locationPermissionGranted = granted }
    )
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        locationPermissionGranted = granted
        if (!granted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                        places = places ?: emptyList(),
                        showUserLocation = locationPermissionGranted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(places ?: emptyList()) { place ->
                            ListItem(
                                headlineContent = { Text(place.name) },
                                supportingContent = { 
                                    Text("${place.type} - ${if (place.isFree) "Free" else "Paid"}")
                                }
                            )
                        }
                    }
                }
            }
        }
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