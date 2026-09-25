package com.aavishkar.pace1.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.model.LocationPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

private const val MAP_STYLE_DEMOTILES = "https://demotiles.maplibre.org/style.json"

@Composable
fun LiveMapView(
    lastLocationPoint: LocationPoint?,
    routePoints: List<LocationPoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    val mapView = remember { MapView(context) }
    var mapLibreMapState by remember { mutableStateOf<MapLibreMap?>(null) }

    DisposableEffect(mapView) {
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { map ->
            map.setStyle(MAP_STYLE_DEMOTILES) {
                mapLibreMapState = map
            }
        }
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(lastLocationPoint, routePoints, mapLibreMapState) {
        val map = mapLibreMapState ?: return@LaunchedEffect

        map.clear()

        val latLngs = routePoints.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .map { LatLng(it.latitude, it.longitude) }

        if (latLngs.size >= 2) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(latLngs)
                    .color(Color.BLUE)
                    .width(5f)
            )
        }

        lastLocationPoint?.let { point ->
            val currentLatLng = LatLng(point.latitude, point.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(currentLatLng)
                    .title("Current Location")
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun PostRideMapView(
    points: List<LocationPointEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    val mapView = remember { MapView(context) }
    var mapLibreMapState by remember { mutableStateOf<MapLibreMap?>(null) }

    DisposableEffect(mapView) {
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { map ->
            map.setStyle(MAP_STYLE_DEMOTILES) {
                mapLibreMapState = map
            }
        }
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(points, mapLibreMapState) {
        val map = mapLibreMapState ?: return@LaunchedEffect

        map.clear()

        val acceptedPoints = points.filter { it.isAccepted }
        val latLngs = acceptedPoints.map { LatLng(it.latitude, it.longitude) }

        if (latLngs.size >= 2) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(latLngs)
                    .color(Color.RED)
                    .width(6f)
            )

            val startPoint = latLngs.first()
            val endPoint = latLngs.last()

            map.addMarker(MarkerOptions().position(startPoint).title("Start"))
            map.addMarker(MarkerOptions().position(endPoint).title("Finish"))

            val boundsBuilder = LatLngBounds.Builder()
            latLngs.forEach { boundsBuilder.include(it) }
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        } else if (latLngs.size == 1) {
            val point = latLngs.first()
            map.addMarker(MarkerOptions().position(point).title("Ride Start/Finish"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}
