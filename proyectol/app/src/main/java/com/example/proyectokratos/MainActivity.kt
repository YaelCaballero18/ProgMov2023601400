package com.example.proyecto

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null


    private val umbralHacha = 15.0f
    private val umbralPunetazoMin = 10.0f
    private val umbralPunetazoMax = 14.0f
    private val umbralIraKratos = 12.0f


    private val umbralMinimo = 2.0f


    private val tiempoMinimoEntreAcciones = 500L


    private var imagenActual by mutableIntStateOf(R.drawable.espadas1)


    private var ultimaAccionTiempo = 0L


    private var accionSeleccionada by mutableStateOf("")


    private var botonPresionado by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Define la interfaz de usuario con Jetpack Compose
        setContent {
            AppUI(
                imagenActual = imagenActual,
                accionSeleccionada = accionSeleccionada,
                onSeleccionarAccion = ::seleccionarAccion,
                imagenArriba = R.drawable.triangulo,
                imagenAbajo = R.drawable.cruz,
                imagenIzquierda = R.drawable.cuadrado,
                imagenDerecha = R.drawable.circulo
            )
        }
    }

    override fun onResume() {
        super.onResume()

        acelerometro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val aceleracionX = it.values[0]
            val aceleracionY = it.values[1]
            val aceleracionZ = it.values[2]


            val velocidad = sqrt(
                (aceleracionX * aceleracionX + aceleracionY * aceleracionY + aceleracionZ * aceleracionZ).toDouble()
            ).toFloat()


            if (velocidad > umbralMinimo) {

                val tiempoActual = System.currentTimeMillis()

                if (tiempoActual - ultimaAccionTiempo > tiempoMinimoEntreAcciones) {

                    if (botonPresionado) {
                        when (accionSeleccionada) {
                            "Hacha" -> if (velocidad > umbralHacha) lanzarHacha()
                            "Puñetazo" -> if (velocidad > umbralPunetazoMin && velocidad < umbralPunetazoMax) lanzarPunetazo()
                            "Espada" -> if (velocidad > umbralHacha) lanzarEspada()
                            "Ira de Kratos" -> if (esMovimientoDiagonal(aceleracionX, aceleracionY, aceleracionZ)) lanzarIraKratos()
                        }
                    }


                    ultimaAccionTiempo = tiempoActual
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun lanzarHacha() {
        val sonidoHacha = listOf(R.raw.hacha1, R.raw.hacha2).random()
        val imagenHacha = when (sonidoHacha) {
            R.raw.hacha1 -> R.drawable.hacha1
            R.raw.hacha2 -> R.drawable.hacha2
            else -> R.drawable.hacha1
        }
        reproducirSonido(sonidoHacha)
        imagenActual = imagenHacha
        botonPresionado = false
    }

    private fun lanzarPunetazo() {

        val sonidoPunetazo = R.raw.golpe
        val imagenPunetazo = R.drawable.golpe
        reproducirSonido(sonidoPunetazo)
        imagenActual = imagenPunetazo // Actualiza la imagen en la UI
        botonPresionado = false // Reinicia el estado del botón
    }

    private fun lanzarEspada() {

        val sonidoEspada = R.raw.espadas1
        val imagenEspada = R.drawable.espadas1
        reproducirSonido(sonidoEspada)
        imagenActual = imagenEspada
        botonPresionado = false
    }

    private fun lanzarIraKratos() {

        val sonidoIraKratos = R.raw.ira
        val imagenIraKratos = R.drawable.ira1
        reproducirSonido(sonidoIraKratos)
        imagenActual = imagenIraKratos
        botonPresionado = false
    }

    private fun reproducirSonido(sonido: Int) {
        MediaPlayer.create(this, sonido).apply {
            setOnCompletionListener {
                it.release()
            }
            start()
        }
    }

    private fun seleccionarAccion(accion: String) {
        accionSeleccionada = accion
        botonPresionado = true
    }

    private fun esMovimientoDiagonal(aceleracionX: Float, aceleracionY: Float, aceleracionZ: Float): Boolean {

        return (aceleracionY < -umbralIraKratos && abs(aceleracionX) > umbralIraKratos / 2 && abs(aceleracionZ) > umbralIraKratos / 2)
    }
}

@Composable
fun AppUI(
    imagenActual: Int,
    accionSeleccionada: String,
    onSeleccionarAccion: (String) -> Unit,
    imagenArriba: Int,
    imagenAbajo: Int,
    imagenIzquierda: Int,
    imagenDerecha: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CrucetaUI(
            onSeleccionarAccion = onSeleccionarAccion,
            imagenArriba = imagenArriba,
            imagenAbajo = imagenAbajo,
            imagenIzquierda = imagenIzquierda,
            imagenDerecha = imagenDerecha
        )

        Spacer(modifier = Modifier.height(32.dp))


        Image(
            painter = painterResource(id = imagenActual),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun CrucetaUI(
    onSeleccionarAccion: (String) -> Unit,
    imagenArriba: Int,
    imagenAbajo: Int,
    imagenIzquierda: Int,
    imagenDerecha: Int
) {
    val cornerRadius = 30.dp

    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón de arriba (Triángulo)
        Button(
            onClick = { onSeleccionarAccion("Hacha") },
            modifier = Modifier.size(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Image(
                painter = painterResource(id = imagenArriba),
                contentDescription = "Arriba",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }

        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = { onSeleccionarAccion("Espada") },
                modifier = Modifier.size(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(4.dp)
            ) {
                Image(
                    painter = painterResource(id = imagenIzquierda),
                    contentDescription = "Izquierda",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                )
            }

            Spacer(modifier = Modifier.width(60.dp))


            Button(
                onClick = { onSeleccionarAccion("Puñetazo") },
                modifier = Modifier.size(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(4.dp)
            ) {
                Image(
                    painter = painterResource(id = imagenDerecha),
                    contentDescription = "Derecha",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                )
            }
        }


        Button(
            onClick = { onSeleccionarAccion("Ira de Kratos") },
            modifier = Modifier.size(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Image(
                painter = painterResource(id = imagenAbajo),
                contentDescription = "Abajo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
    }
}