package com.emami.snappcompose.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope.gravity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.loadFontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.emami.snappcompose.*
import com.emami.snappcompose.R
import com.emami.snappcompose.ui.AnimatedMapPointer
import com.emami.snappcompose.ui.IconButton
import com.emami.snappcompose.ui.PriceCalculatorWidget
import com.emami.snappcompose.ui.RideDetailWidget
import com.emami.snappcompose.util.rememberMapViewWithLifecycle
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun HomeScreen(pointerState: MutableState<PointerState>) {
    val map = rememberMapViewWithLifecycle()
    val buttonState = remember { mutableStateOf(MapPointerMovingState.DRAGGING) }
    val zoomLevel = remember { 17f }
    val context = ContextAmbient.current
    val onPointClick = {
        map.getMapAsync {
            val target = it.cameraPosition.target
            pointerState.value.initialLocation = target
            it.addMarker(
                MarkerOptions().position(target)
                    .icon(BitmapDescriptorFactory.fromResource(if (pointerState.value is PointerState.ORIGIN) R.drawable.ic_location_marker_origin else R.drawable.ic_location_marker_destination))
            )
            pointerState.value =
                when (pointerState.value) {
                    is PointerState.ORIGIN -> PointerState.DESTINATION(
                        (pointerState.value as PointerState.ORIGIN).initialLocation
                    )
                    is PointerState.DESTINATION -> PointerState.PICKED(
                        pointerState.value.initialLocation
                    )
                    else -> PointerState.ORIGIN(pointerState.value.initialLocation)
                }
            if (pointerState.value !is PointerState.PICKED) {
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

    Stack {
        //First child of the stack because content must be drawn below everything
        HomeContent(
            pointerModifier = Modifier.gravity(Alignment.Center).padding(bottom = 52.dp),
            pointerState = pointerState,
            buttonState = buttonState,
            zoomLevel = zoomLevel,
            map = map,
            onClick = onPointClick
        )
        HomeHeader(context, pointerState)
        HomeFooter(Modifier.gravity(Alignment.BottomCenter), pointerState, onPointClick)
    }

    if (pointerState.value is PointerState.CLEAR) {
        map.getMapAsync { it.clear() }
        pointerState.value = PointerState.ORIGIN(pointerState.value.initialLocation)
    }
}


@Composable
fun HomeContent(
    pointerState: State<PointerState>,
    buttonState: MutableState<MapPointerMovingState>,
    zoomLevel: Float,
    map: MapView,
    pointerModifier: Modifier,
    onClick: () -> Unit
) {
    AndroidView({ map }) { mapView ->
        mapView.getMapAsync {
            if (pointerState.value !is PointerState.PICKED) {

                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        pointerState.value.initialLocation, zoomLevel
                    )
                )
                it.setOnCameraMoveStartedListener {
                    buttonState.value = MapPointerMovingState.IDLE
                }
                it.setOnCameraIdleListener {
                    buttonState.value = MapPointerMovingState.DRAGGING
                }
            }
        }
    }
    if (pointerState.value !is PointerState.PICKED) {
        AnimatedMapPointer(
            modifier = pointerModifier,
            buttonState,
            pointerState = pointerState,
            onClick = onClick
        )
    }
}

@Composable
@Preview
fun HomeHeader(
    context: Context = ContextAmbient.current,
    pointerState: State<PointerState> = mutableStateOf(
        PointerState.PICKED(DEFAULT_LOCATION)
    )
) {
    Stack(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        IconButton(
            Modifier.padding(start = 16.dp).gravity(Alignment.TopStart),
            imageResource(id = R.drawable.ic_flight_user)
        ) {
            Toast.makeText(context, "Not implemented yet, Create a PR! ;)", Toast.LENGTH_LONG)
                .show()
        }
        IconButton(
            Modifier.padding(end = 16.dp).gravity(Alignment.TopEnd),
            imageResource(id = R.drawable.ic_arrow_forward)
        ) {
            Toast.makeText(context, "Not implemented yet, Create a PR! ;)", Toast.LENGTH_LONG)
                .show()
        }
        if (pointerState.value is PointerState.PICKED) {
            PriceCalculatorWidget(Modifier.gravity(Alignment.Center))
        }
    }
}


@Composable
fun HomeFooter(
    modifier: Modifier = Modifier,
    pointerState: State<PointerState>,
    onClick: () -> Unit
) {
    Column(
        modifier.padding(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        if (pointerState.value is PointerState.PICKED) {
            RideDetailWidget()
            Spacer(modifier = Modifier.size(16.dp))
        }
        Button(
            onClick = {
                if (pointerState.value !is PointerState.PICKED) onClick()
            }, backgroundColor = colorResource(id = R.color.box_colorAccent),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = if (pointerState.value is PointerState.ORIGIN || pointerState.value is PointerState.CLEAR) "تایید مبدا"
                else if (pointerState.value is PointerState.DESTINATION) "تایید مقصد"
                else "در خواست اسنپ",
                color = Color.White,fontFamily = fontFamily(
                    font(R.font.box_iran_sans_mobile_bold_fa)
                )
            )
        }
    }
}

