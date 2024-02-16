package com.example.chatclient.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

class MainActivity : ComponentActivity() {
    val client by lazy {
        HttpClient {
            install(WebSockets)
        }
    }

    var msgFromServer by mutableStateOf("")
    lateinit var defaultClientWebSocketSession:DefaultClientWebSocketSession

    init {
        CoroutineScope(Dispatchers.Main).launch {
            client.webSocket(
                method = io.ktor.http.HttpMethod.Get,
                host = "192.168.199.34",
                port = 8080,
                path = "/chat"
            ) {
        defaultClientWebSocketSession = this

                for (message in incoming) {
                    message as? Frame.Text ?: continue
                    println(message.readText())
                    msgFromServer = msgFromServer + "\n" +message.readText()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    GreetingView(actionSend = ::defaultClientWebSocketSession, messageFrom = msgFromServer)
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
fun GreetingView(actionSend: KMutableProperty0<DefaultClientWebSocketSession>, messageFrom: String? = null) {
    var messageToSend by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        TextField(
            onValueChange = { messageToSend = it },
            modifier = Modifier.fillMaxWidth(),
            value = messageToSend,
        )

        Button(
            onClick = { CoroutineScope(Dispatchers.Main).launch { actionSend.get().send(messageToSend) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Send")
        }


        Text(text = messageFrom ?: "no reply")

    }
}



