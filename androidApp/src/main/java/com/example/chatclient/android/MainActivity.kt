package com.example.chatclient.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chatclient.android.notif.NotifUser
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

class MainActivity : ComponentActivity() {

    val notifUser = NotifUser(this)
    var msgSent = ""
    val client by lazy {
        HttpClient {
            install(WebSockets)
        }
    }


    var msgFromServer by mutableStateOf("")
    lateinit var defaultClientWebSocketSession: DefaultClientWebSocketSession

    init {
        CoroutineScope(Dispatchers.Main).launch {
            client.webSocket(
                method = io.ktor.http.HttpMethod.Get,
                host = "192.168.104.167",
                port = 8080,
                path = "/chat"
            ) {
                defaultClientWebSocketSession = this

                for (message in incoming) {
                    message as? Frame.Text ?: continue
                    println(message.readText())
                    msgFromServer = msgFromServer + "\n" + message.readText()

//                    if (!(message.readText() as String).contains(msgSent))

                        notifUser.showNotif(this@MainActivity, message.readText())
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {

                val postNotificationPermission =
                    rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

                LaunchedEffect(key1 = true) {
                    if (!postNotificationPermission.status.isGranted) {
                        postNotificationPermission.launchPermissionRequest()
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    GreetingView(
                        actionSend = ::defaultClientWebSocketSession,
                        messageFrom = msgFromServer,
                        saveMsg = { msgSent = it }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
        println("Connection closed. Goodbye!")
    }
}

@Composable
fun GreetingView(
    actionSend: KMutableProperty0<DefaultClientWebSocketSession>,
    saveMsg: (msg: String) -> Unit,
    messageFrom: String? = null
) {
    var messageToSend by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        TextField(
            onValueChange = { messageToSend = it },
            modifier = Modifier.fillMaxWidth(),
            value = messageToSend,
            maxLines = 1
        )

        Button(
            onClick = {
                if(messageToSend.isNotEmpty())
                CoroutineScope(Dispatchers.Main).launch {
                    actionSend.get().send(messageToSend)
                    saveMsg(messageToSend)
                    messageToSend = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Send")
        }


        Text(text = messageFrom ?: "no reply")

    }
}



