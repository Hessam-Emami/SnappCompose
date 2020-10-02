package com.emami.snappcompose.util

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.emami.snappcompose.R
import com.google.android.libraries.maps.MapView

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = ContextAmbient.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LifecycleOwnerAmbient.current.lifecycle
    onCommit(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
private fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }
    }

//fun GoogleMap.setZoom(
//    @FloatRange(from = MinZoom.toDouble(), to = MaxZoom.toDouble()) zoom: Float
//) {
//    resetMinMaxZoomPreference()
//    setMinZoomPreference(zoom)
//    setMaxZoomPreference(zoom)
//}