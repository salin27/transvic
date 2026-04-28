package au.id.transvic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import au.id.transvic.ui.theme.TransvicTheme
import androidx.compose.material.icons.filled.Search
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransvicTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TransvicApp()
                }
            }
        }
    }
}

@Composable
fun TransvicApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Train, contentDescription = "Vehicles") },
                    label = { Text("Vehicles") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                    label = { Text("Map") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> NavHost(navController, startDestination = "search") {
                    composable("search") {
                        StopSearchScreen(onStopSelected = { stop ->
                            navController.navigate("departures/${stop.stopId}/${stop.stopName}")
                        })
                    }
                    composable("departures/{stopId}/{stopName}") { back ->
                        // We'll build this screen in Phase 6
                        Text("Departures coming in Phase 6!",
                            modifier = Modifier.padding(16.dp))
                    }
                }
                1 -> VehicleListScreen()
                2 -> MapScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// VehicleListScreen stays exactly the same as Phase 3 — paste it below here

@Composable
fun VehicleListScreen() {
    // vehicles holds our list — empty to start
    var vehicles by remember { mutableStateOf(listOf<VehiclePosition>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("transvic", style = MaterialTheme.typography.headlineMedium)
        Text("Live vehicles", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                loading = true
                error = ""
                scope.launch {
                    // withContext(Dispatchers.IO) = run on network thread, not UI thread
                    val result = withContext(Dispatchers.IO) {
                        try {
                            // Fetch metro train positions from GTFS-R
                            val feed = GtfsRService.getMetroTrainPositions()

                            // Convert each entity in the feed to our VehiclePosition data class
                            feed.entityList
                                .filter { it.hasVehicle() && it.vehicle.hasPosition() }
                                .map { entity ->
                                    val v = entity.vehicle
                                    VehiclePosition(
                                        id = entity.id,
                                        routeId = v.trip.routeId,
                                        latitude = v.position.latitude.toDouble(),
                                        longitude = v.position.longitude.toDouble(),
                                        bearing = v.position.bearing,
                                        type = VehicleType.TRAIN
                                    )
                                }
                        } catch (e: Exception) {
                            null // return null if anything goes wrong
                        }
                    }
                    loading = false
                    if (result == null) {
                        error = "Failed to load — check your API key and internet"
                    } else {
                        vehicles = result
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch live train positions")
        }

        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        if (vehicles.isNotEmpty()) {
            Text("${vehicles.size} trains found",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn {
            items(vehicles) { vehicle ->
                VehicleRow(vehicle)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun VehicleRow(vehicle: VehiclePosition) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                // Show route ID — we'll replace this with real route names in Phase 5
                text = if (vehicle.routeId.isNotEmpty()) "Route ${vehicle.routeId}"
                else "Unknown route",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "%.4f, %.4f".format(vehicle.latitude, vehicle.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Train",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}