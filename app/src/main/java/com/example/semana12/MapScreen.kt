package com.example.semana12

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Marker
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.MapsInitializer
import androidx.compose.ui.graphics.Color
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.launch
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient

// Convierte un vector en un icono bitmap para los marcadores
fun bitmapDescriptorFromVector(
    context: android.content.Context,
    @DrawableRes vectorResId: Int,
    width: Int,
    height: Int
): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// Obtiene la última ubicación disponible del usuario
fun requestCurrentLocation(
    context: android.content.Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (LatLng) -> Unit,
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationFetched(LatLng(it.latitude, it.longitude))
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Tamaño del icono personalizado
    val markerSizeDp = 40.dp
    val markerWidthPx = with(density) { markerSizeDp.roundToPx() }
    val markerHeightPx = with(density) { markerSizeDp.roundToPx() }
    var customIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val initialCameraPosition = LatLng(-16.3989, -71.5365)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(initialCameraPosition, 15f)
    }

    // Solicitud de permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestCurrentLocation(context, fusedLocationClient) {
                userLocation = it
            }
        }
    }

    // Tipo de mapa
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    val mapProperties = MapProperties(mapType = mapType)

    // Habilita el botón de "mi ubicación"
    val mapUiSettings by remember {
        mutableStateOf(MapUiSettings(myLocationButtonEnabled = true))
    }

    // Datos estáticos
    val locations = listOf(
        LatLng(-16.433415, -71.5442652),
        LatLng(-16.4205151, -71.4945209),
        LatLng(-16.3524187, -71.5675994)
    )

    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    val rutaPrincipal = listOf(
        LatLng(-16.3980, -71.5369),
        LatLng(-16.3930, -71.5390),
        LatLng(-16.3850, -71.5450),
        LatLng(-16.3680, -71.5500)
    )

    val rutaAlterna = listOf(
        LatLng(-16.3980, -71.5369),
        LatLng(-16.3970, -71.5450),
        LatLng(-16.3890, -71.5510)
    )

    // Inicializa Google Maps y carga el icono personalizado
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, null) {
            customIcon = bitmapDescriptorFromVector(
                context, R.drawable.iconmaps, markerWidthPx, markerHeightPx
            )
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Centra la cámara al obtener la ubicación del usuario
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(location, 15f),
                    durationMs = 2000
                )
            }
        }
    }

    // Mapa + FAB de cambio de tipo
    Box(modifier = Modifier.fillMaxSize()) {

        if (customIcon != null) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings
            ) {

                // Marcadores estáticos
                locations.forEachIndexed { index, location ->
                    Marker(
                        state = rememberMarkerState(position = location),
                        title = "Ubicación ${index + 1}",
                        snippet = when (index) {
                            0 -> "José Luis Bustamante y Rivero"
                            1 -> "Paucarpata"
                            2 -> "Zamacola"
                            else -> "Punto de interés"
                        },
                        icon = customIcon
                    )
                }

                // Marcador manual de ubicación del usuario
                userLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Mi Ubicación Actual",
                        snippet = "Tú estás aquí",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Polígonos
                Polygon(
                    points = plazaDeArmasPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF),
                    strokeWidth = 5f
                )

                // Rutas
                Polyline(points = rutaPrincipal, color = Color(0xFF006400), width = 15f)
                Polyline(
                    points = rutaAlterna,
                    color = Color.Blue,
                    width = 10f,
                    pattern = listOf(Dash(20f), Gap(10f))
                )
            }
        }

        // Cambiar tipo de mapa
        FloatingActionButton(
            onClick = {
                mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Map, contentDescription = "Cambiar Tipo de Mapa")
        }
    }
}
