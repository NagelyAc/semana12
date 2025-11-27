package com.example.semana12

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Marker
import androidx.compose.runtime.rememberCoroutineScope // ¡NUEVA IMPORTACIÓN!
import kotlinx.coroutines.launch // ¡NUEVA IMPORTACIÓN!
// ... otras importaciones
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.MapsInitializer


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
    width: Int, // <-- Nuevo
    height: Int // <-- Nuevo
): BitmapDescriptor? {
    // 1. Obtener el Drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    // 2. Establecer límites del Drawable al tamaño deseado
    drawable.setBounds(0, 0, width, height)

    // 3. Crear un Bitmap del tamaño deseado
    val bitmap = Bitmap.createBitmap(
        width, // <-- Usamos el ancho deseado
        height, // <-- Usamos el alto deseado
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

    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994) // Zamacola
    )

    val initialLocation = locations.first()

    val cameraPositionState = rememberCameraPositionState {
        // Posición inicial (antes de la animación)
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(initialLocation, 11f)
    }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST, object : OnMapsSdkInitializedCallback {
            override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {

                // 1. Inicializar el ícono
                customIcon = bitmapDescriptorFromVector(
                    context,
                    R.drawable.iconmaps,
                    markerWidthPx,
                    markerHeightPx
                )

                // 2. Animar la cámara, usando el ámbito de corrutinas
                coroutineScope.launch {
                    val yuraLocation = LatLng(-16.2520984, -71.6836503)
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(yuraLocation, 12f),
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

                // Marcadores
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

            }
        }
    }
}