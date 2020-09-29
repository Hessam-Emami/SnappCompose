package com.emami.snappcompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.emami.snappcompose.ui.SnappComposeTheme
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnappComposeTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val map = rememberMapViewWithLifecycle()
    val buttonState = remember { mutableStateOf(MapPointerState.DRAGGING) }

    //Location of Tehran- Iran
    val position = LatLng("35.6892".toDouble(), "51.3890".toDouble())
    Stack {
        AndroidView({ map }) { mapView ->
            mapView.getMapAsync {
                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        position, 15.0f
                    )
                )
                //Though its strange, i had to swap..
                it.setOnCameraMoveStartedListener {
                    buttonState.value = MapPointerState.IDLE
                }
                it.setOnCameraIdleListener {
                    buttonState.value = MapPointerState.DRAGGING
                }
            }
        }
        //TODO add marker animation
        AnimatedMapPointer(
            modifier = Modifier.gravity(Alignment.Center).padding(bottom = 52.dp), buttonState
        ) {
            map.getMapAsync {
                val target = it.cameraPosition.target
                it.addMarker(
                    MarkerOptions().position(target)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_start))
                )

            }
        }
    }
}

@Composable
fun MapPointer(
    modifier: Modifier = Modifier,
    transitionState: TransitionState?,
    onClick: () -> Unit
) {
    Stack(modifier.wrapContentWidth()) {
        Box(
            shape = CircleShape, backgroundColor = Color.Gray.copy(alpha = .3f),
            modifier = Modifier.padding(top = transitionState!![circlePaddingProp])
                .size(transitionState!![circleSizeProp])
                .gravity(Alignment.BottomCenter)

        )
        Image(
            asset = imageResource(id = R.drawable.ic_location_start_pointer),
            modifier = Modifier
                .padding(bottom = transitionState!![pointerPaddingProp]).clickable(
                    onClick =
                    onClick, indication = null
                )
                .size(32.dp, 64.dp),
        )
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SnappComposeTheme {
        HomeScreen()
    }
}

enum class MapPointerState {
    IDLE, DRAGGING
}

val circleSizeProp = DpPropKey()
val circlePaddingProp = DpPropKey()
val pointerPaddingProp = DpPropKey()

@Preview
@Composable
fun AnimatedMapPointer(
    modifier: Modifier = Modifier,
    buttonState: State<MapPointerState> = mutableStateOf(MapPointerState.IDLE),
    onClick: () -> Unit = {}
) {
    val transitionDefinition = transitionDefinition<MapPointerState> {
        state(MapPointerState.IDLE) {
            this[circleSizeProp] = 16.dp
            this[circlePaddingProp] = 0.dp
            this[pointerPaddingProp] = 0.dp
        }
        state(MapPointerState.DRAGGING) {
            this[circleSizeProp] = 24.dp
            this[circlePaddingProp] = 4.dp
            this[pointerPaddingProp] = 16.dp
        }
        transition(fromState = MapPointerState.IDLE, toState = MapPointerState.DRAGGING) {
            circleSizeProp using tween(durationMillis = 100)
            circlePaddingProp using tween(durationMillis = 100)
            pointerPaddingProp using tween(durationMillis = 100)
        }

        transition(MapPointerState.DRAGGING to MapPointerState.IDLE) {
            circleSizeProp using tween(durationMillis = 100)
            circlePaddingProp using tween(durationMillis = 100)
            pointerPaddingProp using tween(durationMillis = 100)
        }
    }
    val toState = if (buttonState.value == MapPointerState.IDLE) {
        MapPointerState.DRAGGING
    } else {
        MapPointerState.IDLE
    }

    val state = transition(
        definition = transitionDefinition,
        initState = buttonState.value,
        toState = toState
    )

    MapPointer(modifier = modifier, transitionState = state) {
        onClick()

    }
}