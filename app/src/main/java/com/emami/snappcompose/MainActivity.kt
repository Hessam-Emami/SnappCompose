package com.emami.snappcompose

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.TabConstants.defaultTabIndicatorOffset
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.emami.snappcompose.PointerState.*
import com.emami.snappcompose.ui.SnappComposeTheme
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val pointerState: MutableState<PointerState> =
        mutableStateOf(ORIGIN(LatLng("35.6892".toDouble(), "51.3890".toDouble())))

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
            is ORIGIN -> super.onBackPressed()
            is DESTINATION, is PICKED -> pointerState.value =
                CLEAR(pointerState.value.initialLocation)
            is CLEAR -> {
                //Do nothing for now
            }
        }
    }
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    asset: ImageAsset,
    shape: Shape = CircleShape,
    onClick: () -> Unit
) {
    Surface(
        elevation = 4.dp,
        modifier = modifier.size(56.dp).clickable(
            onClick = onClick,
            indication = RippleIndication(bounded = false, radius = 28.dp)
        ),
        shape = shape
    ) {
        Box(
            backgroundColor = colorResource(id = R.color.white),
            gravity = Alignment.Center,
        ) {

            Icon(
                tint = colorResource(id = R.color.box_snapp_services_header_titles_text),
                asset = asset,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MyIndicator(
    modifier: Modifier = Modifier,
    height: Dp = TabConstants.DefaultIndicatorHeight,
    color: Color = contentColor()
) {
    Box(
        shape = CircleShape, modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = color)
    )
}

@Composable
fun HomeScreen(pointerState: MutableState<PointerState>) {
    val map = rememberMapViewWithLifecycle()
    val buttonState = remember { mutableStateOf(MapPointerMovingState.DRAGGING) }
    val zoomLevel = remember { 17f }
    val context = ContextAmbient.current
    //Location of Tehran- Iran
    Stack {

        AndroidView({ map }) { mapView ->
            mapView.getMapAsync {
                if (pointerState.value !is PICKED) {

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
        }
        IconButton(
            Modifier.padding(top = 16.dp, start = 16.dp).gravity(Alignment.TopStart),
            imageResource(id = R.drawable.ic_flight_user)
        ) {
            Toast.makeText(context, "Not implemented yet, Create a PR! ;)", Toast.LENGTH_LONG)
                .show()
        }
        IconButton(
            Modifier.padding(top = 16.dp, end = 16.dp).gravity(Alignment.TopEnd),
            imageResource(id = R.drawable.ic_arrow_forward)
        ) {
            Toast.makeText(context, "Not implemented yet, Create a PR! ;)", Toast.LENGTH_LONG)
                .show()
        }

        if (pointerState.value !is PICKED) {
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
                            .icon(BitmapDescriptorFactory.fromResource(if (pointerState.value is ORIGIN) R.drawable.ic_location_marker_origin else R.drawable.ic_location_marker_destination))
                    )
                    pointerState.value =
                        when (pointerState.value) {
                            is ORIGIN -> DESTINATION(
                                (pointerState.value as ORIGIN).initialLocation,
                                marker
                            )
                            is DESTINATION -> PICKED(
                                pointerState.value.initialLocation,
                                (pointerState.value as DESTINATION).originSelectedMarker,
                                marker
                            )
                            else -> ORIGIN(pointerState.value.initialLocation)
                        }
                    if (pointerState.value !is PICKED) {
                        val rand = Random.nextBoolean()
                        val xRand = Random.nextInt(150, 300).toFloat()
                        val yRand = Random.nextInt(150, 300).toFloat()
                        it.moveCamera(
                            CameraUpdateFactory.scrollBy(
                                if (rand) xRand * 1f else xRand * -1f,
                                if (!rand) yRand * 1f else yRand * -1f
                            )
                        )
                    }
                    it.animateCamera(CameraUpdateFactory.zoomBy(-0.5f))
                }

            }
        }
    }
    if (pointerState.value is CLEAR) {
        map.getMapAsync { it.clear() }
        pointerState.value = ORIGIN(pointerState.value.initialLocation)
    }
}

/**
 * @property ORIGIN, when the user first starts to pick a location
 * @property DESTINATION, happens after Origin
 * @property PICKED, happens when the picking is finished and we go to calculation state.
 */
sealed class PointerState(var initialLocation: LatLng) {
    class ORIGIN(initialLocation: LatLng) : PointerState(initialLocation)
    class DESTINATION(initialLocation: LatLng, val originSelectedMarker: Marker) :
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
        ORIGIN(
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
            asset = imageResource(id = if (pointerState.value is ORIGIN) R.drawable.ic_location_pointer_origin else R.drawable.ic_location_pointer_destination),
            modifier = Modifier
                .padding(bottom = transitionState!![pointerPaddingProp]).clickable(
                    onClick =
                    onClick, indication = null
                )
                .size(32.dp, 64.dp),
        )
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
    pointerState: State<PointerState> = mutableStateOf(
        ORIGIN(
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