package edu.temple.convoy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

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
                Method.POST,
                loginUrl,
                {
                    val json = JSONObject(it)
                    val status = json.getString("status")
                    if(status == "SUCCESS") {
                        onTokenReceipt(state.username.value, json.getString("session_key"))
                    } else {
                        state.errorMessage.value = json.getString("message")
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

    Scaffold { innerPadding->
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