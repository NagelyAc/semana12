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
import com.google.maps.android.compose.Polyline // Importación para Polilínea
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.PatternItem


/**
 * Función auxiliar para convertir un recurso Drawable (PNG/Vector) en un BitmapDescriptor.
 */
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

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current

    val markerSizeDp = 40.dp
    val markerWidthPx = with(density) { markerSizeDp.roundToPx() }
    val markerHeightPx = with(density) { markerSizeDp.roundToPx() }

    var customIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // --- 1. Datos de Marcadores ---
    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994) // Zamacola
    )
    val initialLocation = locations.first()

    // --- 2. Datos de Polígonos ---
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )
    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )
    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    // --- 3. Datos de Polilíneas ---
    val rutaPrincipal = listOf(
        LatLng(-16.3980, -71.5369), // Plaza de Armas
        LatLng(-16.3930, -71.5390), // Puente Grau
        LatLng(-16.3850, -71.5450), // Av. Ejército
        LatLng(-16.3680, -71.5500)  // Cercano al Aeropuerto
    )
    val rutaAlterna = listOf(
        LatLng(-16.3980, -71.5369), // Plaza de Armas
        LatLng(-16.3970, -71.5450), // Av. La Marina
        LatLng(-16.3890, -71.5510)  // Tramo de línea simulada
    )

    // Estado de la Cámara
    val cameraPositionState = rememberCameraPositionState {
        // Zoom inicial bajo para que la animación sea visible
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(initialLocation, 9f)
    }

    // Inicialización del ícono y Animación de la Cámara (ejecutado de forma segura)
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST, object : OnMapsSdkInitializedCallback {
            override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
                // 1. Inicializa el ícono
                customIcon = bitmapDescriptorFromVector(
                    context,
                    R.drawable.iconmaps,
                    markerWidthPx,
                    markerHeightPx
                )

                // 2. Animar la cámara al área de los Polígonos
                coroutineScope.launch {
                    val plazaDeArmasCenter = LatLng(-16.3989, -71.5365)
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(plazaDeArmasCenter, 15f),
                        durationMs = 3000
                    )
                }
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (customIcon != null) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {

                // --- A. Marcadores ---
                locations.forEachIndexed { index, location ->
                    Marker(
                        state = rememberMarkerState(position = location),
                        title = "Ubicación ${index + 1}",
                        snippet = when(index) {
                            0 -> "José Luis Bustamante y Rivero"
                            1 -> "Paucarpata"
                            2 -> "Zamacola"
                            else -> "Punto de interés"
                        },
                        icon = customIcon
                    )
                }

                // --- B. Polígonos ---
                Polygon(
                    points = plazaDeArmasPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF), // Azul semitransparente
                    strokeWidth = 5f
                )
                Polygon(
                    points = parqueLambramaniPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF),
                    strokeWidth = 5f
                )
                Polygon(
                    points = mallAventuraPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF),
                    strokeWidth = 5f
                )

                // Polilíneas

                // 1. Ruta Principal (Sólida)
                Polyline(
                    points = rutaPrincipal,
                    color = Color(0xFF006400), // Verde oscuro
                    width = 15f,
                    zIndex = 1f
                )

                // 2. Ruta Alterna (Patrón de Guiones)
                Polyline(
                    points = rutaAlterna,
                    color = Color.Blue,
                    width = 10f,
                    pattern = listOf(
                        Dash(20f),
                        Gap(10f)
                    )
                )

            }
        }
    }
}