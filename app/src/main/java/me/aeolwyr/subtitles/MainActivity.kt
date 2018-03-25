package me.aeolwyr.subtitles

import android.app.Activity
import android.os.Bundle
import android.view.View
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import kotlinx.android.synthetic.main.activity_main.text as textView

class MainActivity: Activity() {
    private var threadTCP: ReadThreadTCP? = null
    private var threadUDP: ReadThreadUDP? = null
    /** Required for the TCP thread. */
    private val stringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // on pause, thread will quit with an exception
        // this is not an emergency for this application
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->  }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        threadTCP = ReadThreadTCP().apply { start() }
        threadUDP = ReadThreadUDP().apply { start() }
    }

    override fun onPause() {
        super.onPause()
        threadTCP?.serverSocket?.close()
        threadTCP?.socket?.close()
        threadUDP?.socket?.close()
    }

    /**
     * TCP thread, streams until a null byte is received, and then prints the downloaded subtitle.
     */
    private inner class ReadThreadTCP: Thread() {
        val serverSocket = ServerSocket(17827)
        var socket: Socket? = null
        override fun run() {
            while (true) {
                socket = serverSocket.accept()
                val input = socket?.getInputStream() ?: continue
                while (true) {
                    val value = input.read()
                    if (value < 0) {
                        // connection is lost, clear the screen
                        val text = ""
                        runOnUiThread { textView.text = text }
                        stringBuilder.setLength(0)
                        break
                    } else if (value == 0) {
                        // end of the current subtitle
                        val text = stringBuilder.toString()
                        runOnUiThread { textView.text = text }
                        stringBuilder.setLength(0)
                    } else {
                        // still downloading the current subtitle
                        stringBuilder.append(value.toChar())
                    }
                }
            }
        }
    }

    /**
     * UDP thread, directly prints the received subtitle packet.
     */
    private inner class ReadThreadUDP: Thread() {
        val socket = DatagramSocket(17827)
        override fun run() {
            val packet = DatagramPacket(ByteArray(1024), 1024)
            while (true) {
                socket.receive(packet)
                val text = String(packet.data, 0, packet.length)
                runOnUiThread { textView.text = text }
            }
        }
    }
}
