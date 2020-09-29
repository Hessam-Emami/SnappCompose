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
import com.google.android.libraries.maps.model.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    val pointerState: MutableState<PointerState> =
        mutableStateOf(PointerState.ORIGIN(LatLng("35.6892".toDouble(), "51.3890".toDouble())))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnappComposeTheme {
                HomeScreen(pointerState = pointerState)
            }

        }
    }

    override fun onBackPressed() {
        when (pointerState.value) {
            is PointerState.ORIGIN -> super.onBackPressed()
            is PointerState.DESTINATION, is PointerState.PICKED -> pointerState.value =
                PointerState.CLEAR(pointerState.value.initialLocation)
            is PointerState.CLEAR -> {
                //Do nothing for now
            }
        }
    }
}

@Composable
fun HomeScreen(pointerState: MutableState<PointerState>) {
    val map = rememberMapViewWithLifecycle()
    val buttonState = remember { mutableStateOf(MapPointerMovingState.DRAGGING) }
    val zoomLevel = remember { 17f }
    //Location of Tehran- Iran
    Stack {
        AndroidView({ map }) { mapView ->
            mapView.getMapAsync {

                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        pointerState.value.initialLocation, zoomLevel
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
            modifier = Modifier.gravity(Alignment.Center).padding(bottom = 52.dp),
            buttonState,
            pointerState = pointerState
        ) {

            map.getMapAsync {
                val target = it.cameraPosition.target
                pointerState.value.initialLocation = target
                val marker = it.addMarker(
                    MarkerOptions().position(target)
                        .icon(BitmapDescriptorFactory.fromResource(if (pointerState.value is PointerState.ORIGIN) R.drawable.ic_location_marker_origin else R.drawable.ic_location_marker_destination))
                )
                val rand = Random.nextBoolean()
                val xRand = Random.nextInt(150, 300).toFloat()
                val yRand = Random.nextInt(150, 300).toFloat()
                it.moveCamera(
                    CameraUpdateFactory.scrollBy(
                        if (rand) xRand * 1f else xRand * -1f,
                        if (!rand) yRand * 1f else yRand * -1f
                    )
                )
                it.animateCamera(CameraUpdateFactory.zoomBy(-0.5f))
                pointerState.value =
                    if (pointerState.value is PointerState.ORIGIN) PointerState.DESTINATION(
                        (pointerState.value as PointerState.ORIGIN).initialLocation,
                        marker
                    ) else PointerState.ORIGIN(LatLng("35.6892".toDouble(), "51.3890".toDouble()))
            }

        }
    }
    if (pointerState.value is PointerState.CLEAR) {
        map.getMapAsync { it.clear() }
        pointerState.value = PointerState.ORIGIN(pointerState.value.initialLocation)
    }
}

/**
 * @property ORIGIN, when the user first starts to pick a location
 * @property DESTINATION, happens after Origin
 * @property PICKED, happens when the picking is finished and we go to calculation state.
 */
sealed class PointerState(var initialLocation: LatLng) {
    class ORIGIN(initialLocation: LatLng) : PointerState(initialLocation)
    class DESTINATION(initialLocation: LatLng, originSelectedMarker: Marker) :
        PointerState(initialLocation)

    class PICKED(
        initialLocation: LatLng,
        originSelectedMarker: Marker,
        destinationSelectedMarker: Marker
    ) : PointerState(initialLocation)

    class CLEAR(initialLocation: LatLng) : PointerState(initialLocation = initialLocation)
}

@Composable
fun MapPointer(
    modifier: Modifier = Modifier,
    transitionState: TransitionState?,
    pointerState: State<PointerState> = mutableStateOf(
        PointerState.ORIGIN(
            LatLng(
                "35.6892".toDouble(),
                "51.3890".toDouble()
            )
        )
    ),
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
            asset = imageResource(id = if (pointerState.value is PointerState.ORIGIN) R.drawable.ic_location_pointer_origin else R.drawable.ic_location_pointer_destination),
            modifier = Modifier
                .padding(bottom = transitionState!![pointerPaddingProp]).clickable(
                    onClick =
                    onClick, indication = null
                )
                .size(32.dp, 64.dp),
        )
    }

}
//
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    SnappComposeTheme {
//        HomeScreen()
//    }
//}

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
    pointerState: State<PointerState> = mutableStateOf(
        PointerState.ORIGIN(
            LatLng(
                "35.6892".toDouble(),
                "51.3890".toDouble()
            )
        )
    ),
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