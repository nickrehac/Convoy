package edu.temple.convoy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
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
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.maps.android.compose.GoogleMap
import edu.temple.convoy.ui.theme.ConvoyTheme
import kotlin.getValue


sealed class UIState: ViewModel() {
    data class Login(
        val username: MutableState<String> = mutableStateOf(""),
        val password: MutableState<String> = mutableStateOf(""),

        val usernameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val passwordInvalid: MutableState<Boolean> = mutableStateOf(false),
    ) : UIState()
    data class Register(
        val fullname: MutableState<String> = mutableStateOf(""),
        val username: MutableState<String> = mutableStateOf(""),
        val password: MutableState<String> = mutableStateOf(""),
        val confirmPassword: MutableState<String> = mutableStateOf(""),

        val fullnameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val usernameInvalid: MutableState<Boolean> = mutableStateOf(false),
        val passwordInvalid: MutableState<Boolean> = mutableStateOf(false),
        val confirmPasswordInvalid: MutableState<Boolean> = mutableStateOf(false),
    ) : UIState()
    data class Map(
        val webtoken: String? = null,
    ) : UIState()
}

class MainActivityViewModel: ViewModel() {
    var ui: MutableState<UIState> = mutableStateOf(UIState.Login())
}

class MainActivity : ComponentActivity() {
    val viewmodel: MainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                { vm.ui.value = UIState.Map() },
                { vm.ui.value = UIState.Register() }
            )
            is UIState.Register -> RegisterPage(
                currentUI,
                {vm.ui.value = UIState.Login()},
                {vm.ui.value = UIState.Map()}
            )
            is UIState.Map -> MapPage(currentUI)
        }
    }
}

@Composable
fun LoginPage(state: UIState.Login, onTokenReceipt: (String) -> Unit, onSwitchToRegister: () -> Unit) {
    val ctx = LocalContext.current
    fun submit() {
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
            onTokenReceipt("boing boing")
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
                    Button({ submit() }) {
                        Text("Log In")
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterPage(state: UIState.Register, onSwitchToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    fun submit() {
        var valid = true

        state.fullnameInvalid.value = if (
            state.fullname.value.length > 1
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
            onRegisterSuccess()
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
                    state.fullname.value,
                    {
                        state.fullname.value = it
                    },
                    singleLine = true,
                    label = { Text("Full Name") },
                    keyboardOptions = KeyboardOptions(
                        KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
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
                    )
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
                    isError = state.passwordInvalid.value
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
                    )
                )
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
                    Button({ submit() }) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

@Composable
fun MapPage(state: UIState.Map) {
    GoogleMap { }
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
fun GreetingPreview() {
    val viewmodel = MainActivityViewModel()
    MainContent(viewmodel)
}