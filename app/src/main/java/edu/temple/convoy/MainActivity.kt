package edu.temple.convoy

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import edu.temple.convoy.ui.theme.ConvoyTheme
import org.json.JSONObject
import java.security.Permission
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
            {it
                Log.d("GPS", it.toString())
                (viewmodel.ui.value as? UIState.Map)?.location?.value = it
            }, Looper.getMainLooper())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(verifyLocationPermissions()) startLocationUpdates()

        setContent {
            MainContent(viewmodel)
        }
    }
}

@Composable
fun MainContent(vm: MainActivityViewModel) {
    val currentUI = vm.ui.value
    MaterialTheme {
        when (currentUI) {
            is UIState.Login -> LoginPage(
                currentUI,
                { vm.ui.value = UIState.Register() },
                {user, token -> vm.ui.value = UIState.Map(username = user, webtoken = token) }
            )
            is UIState.Register -> RegisterPage(
                currentUI,
                {vm.ui.value = UIState.Login()},
                {user, token -> vm.ui.value = UIState.Map(username = user, webtoken = token)}
            )
            is UIState.Map -> MapPage(currentUI)
        }
    }
}

@Composable
fun LoginPage(state: UIState.Login, onSwitchToRegister: () -> Unit, onTokenReceipt: (String, String) -> Unit) {
    val ctx = LocalContext.current
    val loginUrl = "https://kamorris.com/lab/convoy/account.php"
    fun submit() {
        state.errorMessage.value = null
        var valid = true

        state.usernameInvalid.value = if (
            state.username.value.length > 3
            ) false
        else {
            valid = false
            true
        }

        state.passwordInvalid.value = if (
            state.password.value.length > 3
            ) false
        else {
            valid = false
            true
        }


        if (valid) {
            state.submitting.value = true
            Volley.newRequestQueue(ctx).add(object: StringRequest(
                Request.Method.POST,
                loginUrl,
                {
                    val json = JSONObject(it)
                    val status = json.getString("status")
                    if(status == "SUCCESS") {
                        onTokenReceipt(state.username.value, json.getString("session_key"))
                    } else {
                        state.errorMessage.value = json.getString("message");
                    }
                    state.submitting.value = false
                },
                {
                    state.errorMessage.value = "Unknown Error. Try Again"
                    state.submitting.value = false
                }
            ) {
                override fun getParams(): Map<String?, String?> {
                    return HashMap<String?, String?>().apply {
                        put("action", "LOGIN")
                        put("username", state.username.value)
                        put("password", state.password.value)
                    }
                }
            })
        }
    }

    Scaffold() { innerPadding->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Welcome to Convoy!", style = MaterialTheme.typography.titleLarge)
            Column(
                Modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Log In:")
                TextField(
                    state.username.value,
                    {
                        state.username.value = it
                    },
                    singleLine = true,
                    label = { Text("Username") },
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.None,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.usernameInvalid.value
                )
                TextField(
                    state.password.value,
                    { state.password.value = it },
                    singleLine = true,
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    isError = state.passwordInvalid.value
                )
                state.errorMessage.value?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Register",
                        Modifier.clickable { onSwitchToRegister() },
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                    Button({ submit() }, enabled = !state.submitting.value ) {
                        Text("Log In")
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterPage(state: UIState.Register, onSwitchToLogin: () -> Unit, onTokenReceipt: (String, String) -> Unit) {
    val registerUrl = "https://kamorris.com/lab/convoy/account.php"
    val ctx = LocalContext.current
    fun submit() {
        state.errorMessage.value = null
        var valid = true

        state.firstnameInvalid.value = if (
            state.firstname.value.length > 1
            ) false
        else {
            valid = false
            true
        }

        state.lastnameInvalid.value = if (
            state.lastname.value.length > 1
        ) false
        else {
            valid = false
            true
        }

        state.usernameInvalid.value = if (
            state.username.value.length > 3
            ) false
        else {
            valid = false
            true
        }

        state.passwordInvalid.value = if (
            state.password.value.length > 3
            ) false
        else {
            valid = false
            true
        }

        state.confirmPasswordInvalid.value = if (
            state.confirmPassword.value == state.password.value
            ) false
        else {
            valid = false
            true
        }

        if (valid) {
            state.submitting.value = true
            Volley.newRequestQueue(ctx).add(object: StringRequest(
                Method.POST,
                registerUrl,
                {
                    val json = JSONObject(it)
                    val status = json.getString("status")
                    if(status == "SUCCESS") {
                        onTokenReceipt(state.username.value, json.getString("session_key"))
                    } else {
                        state.errorMessage.value = json.getString("message");
                    }
                    state.submitting.value = false
                },
                {
                    state.errorMessage.value = "Unknown Error. Try Again"
                    state.submitting.value = false
                }
            ) {
                override fun getParams(): Map<String?, String?> {
                    return HashMap<String?, String?>().apply {
                        put("action", "REGISTER")
                        put("username", state.username.value)
                        put("password", state.password.value)
                        put("firstname", state.firstname.value)
                        put("lastname", state.lastname.value)
                    }
                }
            })
        }
    }

    Scaffold() { innerPadding->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Welcome to Convoy!", style = MaterialTheme.typography.titleLarge)
            Column(
                Modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Register:")
                TextField(
                    state.firstname.value,
                    {
                        state.firstname.value = it
                    },
                    singleLine = true,
                    label = { Text("First Name") },
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.firstnameInvalid.value,
                )
                TextField(
                    state.lastname.value,
                    {
                        state.lastname.value = it
                    },
                    singleLine = true,
                    label = { Text("Last Name") },
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.lastnameInvalid.value
                )
                TextField(
                    state.username.value,
                    {
                        state.username.value = it
                    },
                    singleLine = true,
                    label = { Text("Username") },
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.None,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.usernameInvalid.value,
                )
                TextField(
                    state.password.value,
                    { state.password.value = it },
                    singleLine = true,
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.passwordInvalid.value,
                )
                TextField(
                    state.confirmPassword.value,
                    { state.confirmPassword.value = it },
                    singleLine = true,
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    isError = state.confirmPasswordInvalid.value,
                )
                state.errorMessage.value?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Log In",
                        Modifier.clickable { onSwitchToLogin() },
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                    Button({ submit() }, enabled = !state.submitting.value ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

@Composable
fun MapPage(state: UIState.Map) {
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
                        FloatingActionButton({
                        }) {
                            Icon(Icons.Default.GroupAdd, "New/Join Group")
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
        floatingActionButtonPosition = FabPosition.End
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



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Login()
    MainContent(viewmodel)
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Register()
    MainContent(viewmodel)
}

@Preview(showBackground = true)
@Composable
fun MapPreview() {
    val viewmodel = MainActivityViewModel()
    viewmodel.ui.value = UIState.Map("george123", "")
    MainContent(viewmodel)
}