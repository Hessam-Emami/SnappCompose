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
import com.google.android.libraries.maps.model.CameraPosition
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlin.random.Random

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
    val buttonState = remember { mutableStateOf(MapPointerMovingState.DRAGGING) }
    val pointerState = remember { mutableStateOf(PointerState.ORIGIN) }
    val zoomLevel = remember { 17f }
    //Location of Tehran- Iran
    var position = LatLng("35.6892".toDouble(), "51.3890".toDouble())
    Stack {
        AndroidView({ map }) { mapView ->
            mapView.getMapAsync {
                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        position, zoomLevel
                    )
                )
                //Though its strange, i had to swap..
                it.setOnCameraMoveStartedListener {
                    buttonState.value = MapPointerMovingState.IDLE
                }
                it.setOnCameraIdleListener {
                    buttonState.value = MapPointerMovingState.DRAGGING
                }
            }
        }
        AnimatedMapPointer(
            modifier = Modifier.gravity(Alignment.Center).padding(bottom = 52.dp), buttonState,pointerState = pointerState
        ) {

            map.getMapAsync {
                val target = it.cameraPosition.target
                position = target
                it.addMarker(
                    MarkerOptions().position(target)
                        .icon(BitmapDescriptorFactory.fromResource(if (pointerState.value == PointerState.ORIGIN) R.drawable.ic_location_marker_origin else R.drawable.ic_location_marker_destination))
                )
                val rand = Random.nextBoolean()
                val xRand = Random.nextInt(150, 300).toFloat()
                val yRand = Random.nextInt(150, 300).toFloat()
                it.moveCamera(
                    CameraUpdateFactory.scrollBy(
                        if (rand) xRand*1f else xRand*-1f , if (!rand) yRand*1f else yRand*-1f
                    )
                )
                it.animateCamera(CameraUpdateFactory.zoomBy(-0.5f))
                pointerState.value =
                    if (pointerState.value == PointerState.ORIGIN) PointerState.DESTINATION else PointerState.ORIGIN
            }

        }
    }
}

enum class PointerState {
    ORIGIN, DESTINATION
}

@Composable
fun MapPointer(
    modifier: Modifier = Modifier,
    transitionState: TransitionState?,
    pointerState: State<PointerState> = mutableStateOf(PointerState.ORIGIN),
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
            asset = imageResource(id = if (pointerState.value == PointerState.ORIGIN) R.drawable.ic_location_pointer_origin else R.drawable.ic_location_pointer_destination),
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

enum class MapPointerMovingState {
    IDLE, DRAGGING
}

val circleSizeProp = DpPropKey()
val circlePaddingProp = DpPropKey()
val pointerPaddingProp = DpPropKey()

@Preview
@Composable
fun AnimatedMapPointer(
    modifier: Modifier = Modifier,
    buttonMovingState: State<MapPointerMovingState> = mutableStateOf(MapPointerMovingState.IDLE),
    pointerState: State<PointerState> = mutableStateOf(PointerState.ORIGIN),
    onClick: () -> Unit = {}
) {
    val transitionDefinition = transitionDefinition<MapPointerMovingState> {
        state(MapPointerMovingState.IDLE) {
            this[circleSizeProp] = 16.dp
            this[circlePaddingProp] = 0.dp
            this[pointerPaddingProp] = 0.dp
        }
        state(MapPointerMovingState.DRAGGING) {
            this[circleSizeProp] = 24.dp
            this[circlePaddingProp] = 4.dp
            this[pointerPaddingProp] = 16.dp
        }
        transition(
            fromState = MapPointerMovingState.IDLE,
            toState = MapPointerMovingState.DRAGGING
        ) {
            circleSizeProp using tween(durationMillis = 100)
            circlePaddingProp using tween(durationMillis = 100)
            pointerPaddingProp using tween(durationMillis = 100)
        }

        transition(MapPointerMovingState.DRAGGING to MapPointerMovingState.IDLE) {
            circleSizeProp using tween(durationMillis = 100)
            circlePaddingProp using tween(durationMillis = 100)
            pointerPaddingProp using tween(durationMillis = 100)
        }
    }
    val toState = if (buttonMovingState.value == MapPointerMovingState.IDLE) {
        MapPointerMovingState.DRAGGING
    } else {
        MapPointerMovingState.IDLE
    }

    val state = transition(
        definition = transitionDefinition,
        initState = buttonMovingState.value,
        toState = toState
    )

    MapPointer(modifier = modifier, pointerState = pointerState, transitionState = state) {
        onClick()

    }
}