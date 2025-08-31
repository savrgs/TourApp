package com.example.tourfrontend
import com.example.tourfrontend.optimizeRoute
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Headers
import retrofit2.Response
import androidx.compose.ui.text.style.TextDecoration
import com.example.tourfrontend.RatingViewModel
import androidx.compose.ui.graphics.Color


// Utility function for place type icon
fun getPlaceTypeIcon(type: String): String {
    return when (type.lowercase()) {
        "museum" -> "🏛️"
        "park" -> "🌳"
        "restaurant" -> "🍽️"
        "monument" -> "🗿"
        "church" -> "⛪"
        "gallery" -> "🖼️"
        else -> "📍"
    }
}

// Minimal MapViewComponent composable
@Composable
fun MapViewComponent(
    places: List<PlaceDto>,
    showUserLocation: Boolean,
    cityLat: Double,
    cityLon: Double,
    userLocation: android.location.Location? = null,
    trafficEnabled: Boolean = false,
    modifier: Modifier = Modifier // Accept modifier
) {
    val initialCenter = if (showUserLocation && userLocation != null) {
        LatLng(userLocation.latitude, userLocation.longitude)
    } else {
        LatLng(cityLat, cityLon)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(initialCenter, 12f, 0f, 0f)
    }

    // Animate camera to user location if available
    LaunchedEffect(userLocation) {
        if (showUserLocation && userLocation != null) {
            cameraPositionState.position = CameraPosition(LatLng(userLocation.latitude, userLocation.longitude), 12f, 0f, 0f)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = showUserLocation,
            isTrafficEnabled = trafficEnabled
        ),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        places.forEach { place ->
            Marker(
                state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                title = place.name,
                snippet = place.type
            )
        }
        // Draw polyline from user location to all selected places
        if (places.isNotEmpty()) {
            val polylinePoints = mutableListOf<LatLng>()
            if (showUserLocation && userLocation != null) {
                polylinePoints.add(LatLng(userLocation.latitude, userLocation.longitude))
            }
            polylinePoints.addAll(places.map { LatLng(it.latitude, it.longitude) })
            if (polylinePoints.size >= 2) {
                Polyline(
                    points = polylinePoints,
                    color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                    width = 6f
                )
            }
        }
    }
}

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

data class LoginRequest(val usernameOrEmail: String, val password: String)
data class User(val id: Long, val username: String, val email: String)
data class RegisterRequest(val username: String, val email: String, val password: String)

interface AuthApiService {
    @POST("/api/users/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("/api/users/register")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body request: RegisterRequest): Response<User>
}

@Composable
fun RegisterScreen(onRegisterSuccess: (User) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApi = remember { retrofit.create(AuthApiService::class.java) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                loading = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        val response = authApi.register(RegisterRequest(username, email, password))
                        if (response.isSuccessful && response.body() != null) {
                            // Instead of logging in, go back to login screen
                            onBack()
                        } else {
                            errorMessage = "Registration failed"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Registration failed: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Registering..." else "Register")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Back to Login",
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onBack() }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit, onRegisterClick: () -> Unit) {
    val context = LocalContext.current
    var usernameOrEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApi = remember { retrofit.create(AuthApiService::class.java) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = usernameOrEmail,
            onValueChange = { usernameOrEmail = it },
            label = { Text("Username or Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                loading = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        val response = authApi.login(LoginRequest(usernameOrEmail, password))
                        if (response.isSuccessful && response.body() != null) {
                            onLoginSuccess(response.body()!!)
                        } else {
                            errorMessage = "Password incorrect"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Login failed: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Logging in..." else "Login")
        }
        if (errorMessage == "Password incorrect") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        } else if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Register",
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onRegisterClick() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityExplorerApp() {
        val appViewModel: AppViewModel = viewModel()
        val loggedInUser = appViewModel.loggedInUser.value
        val showRegister = appViewModel.showRegister.value
        if (loggedInUser == null) {
            if (showRegister) {
                RegisterScreen(
                    onRegisterSuccess = { /* unused, handled in RegisterScreen */ },
                    onBack = { appViewModel.showRegister.value = false }
                )
            } else {
                LoginScreen(
                    onLoginSuccess = { user -> appViewModel.loggedInUser.value = user },
                    onRegisterClick = { appViewModel.showRegister.value = true }
                )
            }
        } else {
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
            composable(
                "weatherDetail/{cityName}",
                arguments = listOf(navArgument("cityName") { type = NavType.StringType })
            ) { backStackEntry ->
                val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
                val weatherApi = Retrofit.Builder()
                    .baseUrl("https://api.weatherapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WeatherApiService::class.java)
                val forecastViewModel: WeatherForecastViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WeatherForecastViewModel(weatherApi, "bdfdb5adccc243d192a154654253008") as T
                    }
                })
                val forecast by forecastViewModel.forecastDays.collectAsState()
                val loading by forecastViewModel.loading.collectAsState()
                val error by forecastViewModel.error.collectAsState()
                LaunchedEffect(cityName) {
                    forecastViewModel.fetchForecast(cityName, days = 7)
                }
                ForecastScreen(
                    cityName = cityName,
                    forecast = forecast,
                    loading = loading,
                    error = error,
                    onBack = { navController.popBackStack() }
                )
            }
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

    // Weather API setup
    val weatherApi = remember {
        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchCities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "City Explorer",
                                modifier = Modifier.size(32.dp),
                                tint = Color.White // White icon on blue background
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "City Explorer",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3), // Blue background
                    titleContentColor = Color.White // White text on blue background
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Discovering cities...",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                val cityList = cities ?: emptyList()
                if (cityList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "No cities",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No cities available yet",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Check back later for new destinations",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Instructional header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD) // Light blue background
                                ),
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationCity,
                                        contentDescription = "Select City",
                                        modifier = Modifier.size(32.dp),
                                        tint = androidx.compose.ui.graphics.Color(0xFF1976D2)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Select Your Destination",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = androidx.compose.ui.graphics.Color(0xFF1976D2)
                                        )
                                        Text(
                                            text = "Tap on a city to explore its attractions and plan your visit",
                                            fontSize = 14.sp,
                                            color = androidx.compose.ui.graphics.Color(0xFF1976D2).copy(alpha = 0.8f),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        items(cityList) { city ->
                            Column {
                                CityCard(
                                    city = city,
                                    onClick = {
                                        navController.navigate("places/${city.id}")
                                    }
                                )
                                val cityWeatherViewModel: WeatherViewModel = viewModel(
                                    key = "weather_${city.name}",
                                    factory = object : ViewModelProvider.Factory {
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            @Suppress("UNCHECKED_CAST")
                                            return WeatherViewModel(weatherApi, "bdfdb5adccc243d192a154654253008") as T
                                        }
                                    }
                                )
                                CityWeatherCard(
                                    weatherViewModel = cityWeatherViewModel,
                                    cityName = city.name,
                                    navController = navController
                                )
                            }
                        }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced City Icon with background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFF2196F3), // Blue background
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "City",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White // White icon on blue background
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Enhanced City Information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = city.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Country",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = city.country,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Enhanced Navigation Arrow with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFF64B5F6), // Lighter blue background
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Explore",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(180f),
                    tint = Color.White // White arrow on blue background
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(cityId: Long, navController: androidx.navigation.NavController) {

    val selectedTypesState = remember { mutableStateOf<Set<String>>(setOf()) }
    var selectedTypes: Set<String> by selectedTypesState

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
    var selectedPlaces by remember { mutableStateOf<Set<Long>>(setOf()) } // Use place.id for selection

    val allTypes: List<String> = places?.map { it.type }?.distinct()?.sorted() ?: emptyList()

    val filteredPlaces: List<PlaceDto> =
    if (selectedTypes.isEmpty()) places ?: emptyList()
    else places?.filter { selectedTypes.contains(it.type) } ?: emptyList()

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
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        userLocation = location
                    }
                }
            }
        }
    )
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        locationPermissionGranted = granted
        if (!granted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location
            }
        }
    }
    LaunchedEffect(cityId) { viewModel.fetchPlacesForCity(cityId) }

    // Get city coordinates from places (if available)
    val cityLat = places?.firstOrNull()?.latitude ?: 0.0
    val cityLon = places?.firstOrNull()?.longitude ?: 0.0

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
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Traffic toggle above filter checkboxes
            var trafficEnabled by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Traffic", modifier = Modifier.weight(1f))
                Switch(
                    checked = trafficEnabled,
                    onCheckedChange = { trafficEnabled = it }
                )
            }
            // Place type filter bar
            if (allTypes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Checkbox(
                                checked = selectedTypes.contains(type),
                                onCheckedChange = { checked ->
                                    selectedTypes = if (checked) selectedTypes + type else selectedTypes - type
                                }
                            )
                            Text(type, fontSize = 14.sp)
                        }
                    }
                }
            }
            // Map at top, fixed height
            val selectedPlaceObjects: List<PlaceDto> = places?.filter { selectedPlaces.contains(it.id) } ?: emptyList()
            val optimizedPlaces: List<PlaceDto> = optimizeRoute(userLocation, selectedPlaceObjects)
            MapViewComponent(
                places = optimizedPlaces,
                showUserLocation = locationPermissionGranted,
                cityLat = cityLat,
                cityLon = cityLon,
                userLocation = userLocation,
                trafficEnabled = trafficEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Fixed height for map
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Selection Summary
            if (filteredPlaces.isNotEmpty()) {
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
                        val selectedCount = filteredPlaces.count { selectedPlaces.contains(it.id) }
                        Text(
                            text = "$selectedCount of ${filteredPlaces.size} places selected for route",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Plan Tour Route Button
            if (filteredPlaces.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (userLocation != null) {
                                if (selectedPlaceObjects.size >= 2) {
                                    val optimizedPlaces: List<PlaceDto> = optimizeRoute(userLocation, selectedPlaceObjects)
                                    val origin = "${userLocation!!.latitude},${userLocation!!.longitude}"
                                    val destination = "${optimizedPlaces.last().latitude},${optimizedPlaces.last().longitude}"
                                    val waypoints = optimizedPlaces.dropLast(1).joinToString("|") { place ->
                                        "${place.latitude},${place.longitude}"
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
                            containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2) // Darker blue for button
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
            // Scrollable list of filtered places
            if (filteredPlaces.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f), // Use weight for empty state too
                    contentAlignment = Alignment.Center
                ) {
                    Text("No places available for this city.", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredPlaces) { place ->
                        PlaceCard(
                            place = place,
                            cityId = cityId,
                            userLocation = userLocation,
                            isSelected = selectedPlaces.contains(place.id),
                            onSelectionChanged = { isSelected ->
                                selectedPlaces = if (isSelected) selectedPlaces + place.id else selectedPlaces - place.id
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
                                        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(browserIntent)
                                    }
                                } else {
                                    Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRatingSubmitted = {
                                viewModel.fetchPlacesForCity(cityId)
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
fun PlaceCard(
    place: PlaceDto,
    cityId: Long,
    userLocation: android.location.Location?,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onNavigate: (Double, Double) -> Unit,
    onRatingSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { 
                // Reset all states when opening dialog for a new place
                showDialog = true 
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use icon for place type instead of photo
            Text(
                getPlaceTypeIcon(place.type),
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(place.type, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Text(place.description, fontSize = 12.sp, maxLines = 2)
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
        }
    }

    if (showDialog) {
        var ratingInput by remember(place.id) { mutableStateOf(1) }
        var submitting by remember(place.id) { mutableStateOf(false) }
        var error by remember(place.id) { mutableStateOf<String?>(null) }
        
        val ratingViewModel: RatingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RatingViewModel(onRatingSubmitted = { 
                    onRatingSubmitted() // Use the callback to refresh places
                    showDialog = false // Close the dialog after successful rating
                    Toast.makeText(context, "Rating submitted successfully!", Toast.LENGTH_SHORT).show()
                }) as T
            }
        })

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (place.photoUrl != null) {
                        AsyncImage(
                            model = place.photoUrl,
                            contentDescription = "Photo of ${place.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = place.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = place.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Current Rating: ${"%.1f".format(place.rating)}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Dropdown for rating selection
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Select Rating: $ratingInput")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            (1..5).forEach { value ->
                                DropdownMenuItem(onClick = {
                                    ratingInput = value
                                    expanded = false
                            }, text = { Text("$value") })
                        }
                    }
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            submitting = true
                            ratingViewModel.submitRating(place.id, ratingInput.toDouble())
                        },
                        enabled = !submitting,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (submitting) "Submitting..." else "Submit Rating", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    ratingViewModel.errorMessage?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }
}

