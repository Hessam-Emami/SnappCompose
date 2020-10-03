package com.emami.snappcompose.ui

import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.emami.snappcompose.*
import com.emami.snappcompose.R
import com.emami.snappcompose.util.circlePaddingProp
import com.emami.snappcompose.util.circleSizeProp
import com.emami.snappcompose.util.pointerPaddingProp
import com.google.android.libraries.maps.model.LatLng


@Composable
@Preview
fun RideDetailWidgetPreview() {
    SnappComposeTheme() {
        Surface {
            RideDetailWidget()
        }
    }
}


@Composable
@Preview
fun RideTypeWidgetPreview() {
    SnappComposeTheme() {
        Surface {
            RideTypeWidget("Test item")
        }
    }
}

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

@Composable
@Preview
fun PriceCalculatorWidget(
    modifier: Modifier = Modifier,
    price: State<Int> = mutableStateOf(15000)
) {
    Box(
        shape = CircleShape,
        backgroundColor = Color.White,
        modifier = modifier.height(56.dp).width(120.dp), gravity = Alignment.Center,
    ) {
        Row {
            Text(
                text = "ریال",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.gravity(Alignment.CenterVertically),
                color = Color.Black.copy(alpha = .7f), fontFamily = fontFamily(
                    font(R.font.box_iran_sans_mobile_light_fa)
                )
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = price.value.toString(),
                style = MaterialTheme.typography.h6
            )
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
fun RideDetailWidget() {
    val list = listOf("اسنپ! (به صرفه)", "اسنب! بانوان", "اسنپ! باکس", "اسنپ! بایک")
    Card {
        Column {
            LazyRowFor(items = list) {
                RideTypeWidget(it)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row() {
                OutlinedButton(
                    shape = CircleShape.copy(all = CornerSize(0.dp)),
                    modifier = Modifier.fillMaxWidth(.5f),
                    onClick = {}) {
                    Text(
                        text = "گزینه های سفر",
                        color = colorResource(R.color.box_colorAccent),
                        fontFamily = fontFamily(
                            font(R.font.box_iran_sans_mobile_bold_fa)
                        )
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        tint = colorResource(id = R.color.box_colorAccent),
                        asset = imageResource(id = R.drawable.ic_ride_options_enabled),
                        modifier = Modifier.size(16.dp)
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape.copy(all = CornerSize(0.dp)),
                    onClick = {}) {
                    Text(
                        text = "کد تخفیف",
                        color = colorResource(R.color.box_colorAccent),
                        fontFamily = fontFamily(
                            font(R.font.box_iran_sans_mobile_bold_fa)
                        )
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        tint = colorResource(id = R.color.box_colorAccent),
                        asset = imageResource(id = R.drawable.ic_ride_voucher_enabled),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RideTypeWidget(title: String) {
    Column(
        horizontalGravity = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Image(
            modifier = Modifier.size(68.dp),
            asset = imageResource(id = R.drawable.ic_ride_for_friend_service)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            modifier = Modifier.width(100.dp), softWrap = true, textAlign = TextAlign.Center,
            text = title,
            style = MaterialTheme.typography.caption,
            maxLines = 2,
            color = colorResource(R.color.box_snapp_services_header_titles_text)
        )
    }
}


