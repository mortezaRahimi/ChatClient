package com.example.chatclient.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatclient.Greeting
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    val client by lazy {
        HttpClient {
            install(WebSockets)
        }
    }

    var msgFromServer by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    GreetingView(actionSend = ::sendMessage,messageFrom = msgFromServer )

                }
            }
        }
    }


    private fun sendMessage(messageToSend: String) {

        CoroutineScope(Dispatchers.Main).launch {
            client.webSocket(
                method = HttpMethod.Get,
                host = "192.168.126.96",
                port = 8080,
                path = "/chat"
            ) {
                send(messageToSend)
                for (message in incoming) {
                    message as? Frame.Text ?: continue
                    println(message.readText())
                    msgFromServer = message.readText()
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


suspend fun DefaultClientWebSocketSession.inputMessages() {

    while (true) {
        val message = readLine() ?: ""
        if (message.equals("exit", true)) return
        try {
            send("Hi morteza")
        } catch (e: Exception) {
            println("Error while sending: " + e.localizedMessage)
            return
        }
    }
}


fun DefaultClientWebSocketSession.outputMessages() {

    CoroutineScope(Dispatchers.Main).launch {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                println(message.readText())
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }
}

@Composable
fun GreetingView(actionSend: (text: String) -> Unit, messageFrom: String? = null) {
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

        Button(onClick = { actionSend(messageToSend) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Send")
        }


        Text(text = messageFrom ?: "no reply")

    }

}



