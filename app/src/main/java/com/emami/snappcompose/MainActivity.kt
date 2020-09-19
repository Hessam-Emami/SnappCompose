package com.emami.snappcompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.setContent
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
    AndroidView({ map }) { mapView ->
        mapView.getMapAsync {
            val position = LatLng("40.416775".toDouble(), "-3.703790".toDouble())
            it.addMarker(
                MarkerOptions().position(position)
            )
            it.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SnappComposeTheme {
        HomeScreen()
    }
}