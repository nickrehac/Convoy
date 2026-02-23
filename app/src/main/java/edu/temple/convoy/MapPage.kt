package edu.temple.convoy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPage(
    state: UIState.Map,
    onLogOut: () -> Unit,
    onConvoyStart: () -> Unit,
    onConvoyEnd: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        val latlng: LatLng = state.location.value?.let{LatLng(it.latitude, it.longitude)} ?: LatLng(0.0,0.0)
        position = CameraPosition.fromLatLngZoom(latlng, 10f)
    }

    state.location.value?.let {
        state.locationListener?.onLocationChanged(it)
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 10f)
    }

    Scaffold(
        floatingActionButton = {
            Column {
                AnimatedVisibility(
                    !state.FABOpen.value,
                    enter = expandVertically(clip = true) + fadeIn(),
                    exit = shrinkVertically(clip = true) + fadeOut()
                ) {
                    FloatingActionButton({
                        state.FABOpen.value = true
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                }
                AnimatedVisibility(
                    state.FABOpen.value,
                    enter = expandVertically(clip = true) + fadeIn(),
                    exit = shrinkVertically(clip = true) + fadeOut(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (state.convoyID.value != null) {
                            if(state.isConvoyHost.value) {
                                FloatingActionButton({
                                    state.convoyID.value = null
                                }) {
                                    Column {
                                        Icon(Icons.Default.GroupRemove, "End Group")
                                        Text("End")
                                    }
                                }
                            } else {
                                FloatingActionButton({
                                    state.convoyID.value = null
                                }) {
                                    Column {
                                        Icon(Icons.Default.GroupOff, "Leave Group")
                                        Text("Leave")
                                    }
                                }
                            }

                        } else {
                            FloatingActionButton({
                                state.convoyID.value = ""
                                state.isConvoyHost.value = true
                            }) {
                                Column {
                                    Icon(Icons.Default.GroupAdd, "New Group")
                                    Text("New")
                                }
                            }
                            FloatingActionButton({
                                state.convoyID.value = ""
                                state.isConvoyHost.value = false
                            }) {
                                Column {
                                    Icon(Icons.Default.Group, "Join Group")
                                    Text("Join")
                                }
                            }
                        }
                        FloatingActionButton({
                            state.FABOpen.value = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "close")
                        }
                    }

                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            TopAppBar(
                {
                    if(state.convoyID.value != null) {
                        Text("Convoy: Group ${state.convoyID.value}")
                    } else {
                        Text("Convoy")
                    }
                },
                actions = {
                    IconButton({ onLogOut() }) {
                        Icon(Icons.AutoMirrored.Default.Logout, "Log Out")
                    }
                }
            )
        }
    ) {innerPadding ->
        Box(Modifier.padding(innerPadding)) {

        }
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            cameraPositionState = cameraPositionState,
            locationSource = object: LocationSource {
                override fun activate(p0: LocationSource.OnLocationChangedListener) {
                    state.locationListener = p0
                }

                override fun deactivate() {
                    state.locationListener = null
                }
            }
        ) {
            //
        }
    }
}