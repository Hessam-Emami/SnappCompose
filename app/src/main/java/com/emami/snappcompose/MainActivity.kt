package com.emami.snappcompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.setContent
import com.emami.snappcompose.PointerState.*
import com.emami.snappcompose.screen.HomeScreen
import com.emami.snappcompose.ui.SnappComposeTheme
import com.google.android.libraries.maps.model.LatLng

val defaultLocation = LatLng("35.6892".toDouble(), "51.3890".toDouble())


class MainActivity : AppCompatActivity() {
    private val pointerState: MutableState<PointerState> =
        mutableStateOf(ORIGIN(defaultLocation))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnappComposeTheme() {
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

/**
 * @property ORIGIN, when the user first starts to pick a location
 * @property DESTINATION, happens after Origin
 * @property PICKED, happens when the picking is finished and we go to calculation state.
 */
sealed class PointerState(var initialLocation: LatLng) {
    class ORIGIN(initialLocation: LatLng) : PointerState(initialLocation)
    class DESTINATION(initialLocation: LatLng) :
        PointerState(initialLocation)

    class PICKED(
        initialLocation: LatLng
    ) : PointerState(initialLocation)

    class CLEAR(initialLocation: LatLng) : PointerState(initialLocation = initialLocation)
}


enum class MapPointerMovingState {
    IDLE, DRAGGING
}


