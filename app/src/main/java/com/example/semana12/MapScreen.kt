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
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


/**
 * Función auxiliar para convertir un recurso Drawable (PNG/Vector) en un BitmapDescriptor,
 * permitiendo especificar el tamaño deseado del marcador.
 *
 * @param context Contexto de Android.
 * @param vectorResId El ID del recurso Drawable.
 * @param width El ancho deseado del marcador en píxeles.
 * @param height El alto deseado del marcador en píxeles.
 */
fun bitmapDescriptorFromVector(
    context: android.content.Context,
    @DrawableRes vectorResId: Int,
    width: Int,
    height: Int
): BitmapDescriptor? {
    // 1. Obtener el Drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    // 2. Establecer límites del Drawable al tamaño deseado
    drawable.setBounds(0, 0, width, height)

    // 3. Crear un Bitmap del tamaño deseado
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    // 4. Dibujar el Drawable en el Canvas del Bitmap
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)

    // 5. Devolver el BitmapDescriptor
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

    // Ámbito de corrutinas para la función animate
    val coroutineScope = rememberCoroutineScope()

    // --- Datos de Marcadores ---
    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994) // Zamacola
    )
    val initialLocation = locations.first()

    // --- Datos de Polígonos ---
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

    // Estado de la Cámara
    val cameraPositionState = rememberCameraPositionState {

        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(initialLocation, 9f)
    }

    // Inicialización del ícono y Animación de la Cámara
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

                // 2. Animar la cámara usando el coroutineScope
                coroutineScope.launch {
                    // Mover la cámara al centro de los polígonos (Plaza de Armas)
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

                // --- Múltiples Marcadores ---
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

                // --- Polígonos ---
                Polygon( // Plaza de Armas
                    points = plazaDeArmasPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF), // Azul semitransparente
                    strokeWidth = 5f
                )
                Polygon( // Parque Lambramani
                    points = parqueLambramaniPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF),
                    strokeWidth = 5f
                )
                Polygon( // Mall Aventura
                    points = mallAventuraPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color(0x770000FF),
                    strokeWidth = 5f
                )

            }
        }
    }
}