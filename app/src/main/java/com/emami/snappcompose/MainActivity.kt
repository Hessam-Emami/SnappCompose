package com.emami.snappcompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.emami.snappcompose.ui.SnappComposeTheme
import com.google.android.libraries.maps.CameraUpdateFactory
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

            }
        }
        MapPointer(Modifier.gravity(Alignment.Center)) {
            map.getMapAsync {
                val target = it.cameraPosition.target
                it.addMarker(MarkerOptions().position(target))
            }
        }
    }
}

@Composable
fun MapPointer(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(
        asset = imageResource(id = R.drawable.ic_location_start_pointer),
        modifier = modifier
            .clickable(onClick = onClick).padding(16.dp), contentScale = FixedScale(.8f)

    )

}

@Preview
@Composable
fun MapPointerPreview() {
    SnappComposeTheme {
        MapPointer {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SnappComposeTheme {
//        HomeScreen()
    }
}