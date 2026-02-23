package edu.temple.convoy

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import edu.temple.convoy.ui.theme.ConvoyTheme
import org.json.JSONObject
import kotlin.getValue


sealed class UIState: ViewModel() {
    data class Login(
        val username: MutableState<String> = mutableStateOf(""),
        val password: MutableState<String> = mutableStateOf(""),

        val usernameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val passwordInvalid: MutableState<Boolean> = mutableStateOf(false),

        val errorMessage: MutableState<String?> = mutableStateOf(null),

        val submitting: MutableState<Boolean> = mutableStateOf(false),
    ) : UIState()
    data class Register(
        val firstname: MutableState<String> = mutableStateOf(""),
        val lastname: MutableState<String> = mutableStateOf(""),
        val username: MutableState<String> = mutableStateOf(""),
        val password: MutableState<String> = mutableStateOf(""),
        val confirmPassword: MutableState<String> = mutableStateOf(""),

        val firstnameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val lastnameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val usernameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val passwordInvalid: MutableState<Boolean> = mutableStateOf(false),
        val confirmPasswordInvalid: MutableState<Boolean> = mutableStateOf(false),

        val errorMessage: MutableState<String?> = mutableStateOf(null),

        val submitting: MutableState<Boolean> = mutableStateOf(false),
    ) : UIState()
    data class Map(
        val username: String,
        val webtoken: String,

        val convoyID: MutableState<String?> = mutableStateOf(null),
        val isConvoyHost: MutableState<Boolean> = mutableStateOf(false),

        val FABOpen: MutableState<Boolean> = mutableStateOf(false),
        val location: MutableState<Location?> = mutableStateOf(null),
        var locationListener: LocationSource.OnLocationChangedListener? = null,
    ) : UIState()
}

class MainActivityViewModel: ViewModel() {
    var ui: MutableState<UIState> = mutableStateOf(UIState.Login())
}

class MainActivity : ComponentActivity() {
    val viewmodel: MainActivityViewModel by viewModels<MainActivityViewModel>()

    var convoyService: ConvoyService? = null

    fun verifyLocationPermissions(): Boolean {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationManager = getSystemService(LocationManager::class.java)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1.0f,
            {
                Log.d("GPS", it.toString())
                (viewmodel.ui.value as? UIState.Map)?.location?.value = it
            }, Looper.getMainLooper())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(verifyLocationPermissions()) startLocationUpdates()

        setContent {
            MainContent(
                viewmodel,
                {},
                {}
                )
        }
    }
}

@Composable
fun MainContent(vm: MainActivityViewModel, onConvoyStart: () -> Unit, onConvoyEnd: () -> Unit) {
    val currentUI = vm.ui.value
    ConvoyTheme {
        when (currentUI) {
            is UIState.Login -> LoginPage(
                currentUI,
                { vm.ui.value = UIState.Register() },
                {user, token -> vm.ui.value = UIState.Map(username = user, webtoken = token) }
            )
            is UIState.Register -> RegisterPage(
                currentUI,
                { vm.ui.value = UIState.Login() },
                {user, token -> vm.ui.value = UIState.Map(username = user, webtoken = token)}
            )
            is UIState.Map -> MapPage(
                currentUI,
                { vm.ui.value = UIState.Login() },
                onConvoyStart,
                onConvoyEnd
            )
        }
    }
}


@Preview
@Composable
fun LoginPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Login()
    MainContent(viewmodel, {}, {})
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Register()
    MainContent(viewmodel, {}, {})
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun MapPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Map("george123", "")
    MainContent(viewmodel, {}, {})
}