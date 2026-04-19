package com.example.riegon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.example.riegon.ui.theme.RiegonTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Done


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RiegonTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RiegoConfigScreen()
                }
            }
        }
    }
}
//https://riegon-default-rtdb.firebaseio.com/
//xwjvXoF5Qk36yL7B1HurY2ev4ZlJTh5hxH06kJrH
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiegoConfigScreen(){
    val context=LocalContext.current
    val database = Firebase.database.reference.child("sistema_riego")

    //Opciones del menu desplegable para el tiempo
    val opcionesTiempo = listOf("1 min", "5 min", "10 min", "15 min", "20 min", "30 min")

    //Estados de los sensores
    var tempActual by remember { mutableStateOf("--") }
    var humActual by remember { mutableStateOf("--")}

    //estados de configuracion (datos persistentes)
    var esModoAutomatico by remember { mutableStateOf(false) }
    var riegoManualActivo by remember { mutableStateOf(false) }
    var umbralTemp by remember { mutableStateOf("") }
    var umbralHum by remember { mutableStateOf("") }

    //Array para guardar la hora, la duraccion y sue el menú está abierto

    val horarios= remember { mutableStateListOf(*Array(6){ "" }) }
    val duraciones= remember { mutableStateListOf(*Array(6){ "5 min" }) }
    val expandedStates = remember { mutableStateListOf(*Array(6){ false }) }

    ////Escuchar firebase al iniciar

    DisposableEffect(Unit) {
        val listener= object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Sensores
                tempActual = snapshot.child("monitoreo/temperatura").getValue().toString()
                humActual = snapshot.child("monitoreo/humedad").getValue().toString()

                //Modos
                esModoAutomatico = (snapshot.child("modo").getValue(String ::class.java) == "automatico")
                riegoManualActivo = (snapshot.child("control/manual_trigger").getValue(Int::class.java) == 1)

                //Rellenar cajones si existen datos
                umbralTemp=snapshot.child("configuracion/umbral_temp").getValue(String::class.java) ?: ""
                umbralHum=snapshot.child("configuracion/umbral_hum").getValue(String::class.java) ?: ""

                //leer los horarios y tiempos
                for (i in 0..5){
                    val horaNube=snapshot.child("horarios/t${i+1}/hora").getValue(String::class.java)
                    val duracionNube=snapshot.child("horarios/t${i+1}/duracion").getValue(String::class.java)

                    if (horaNube ==null){
                        val valorAntiguo=snapshot.child("horarios/t${i+1}").getValue(String::class.java)
                        horarios[i]= if (valorAntiguo != null && valorAntiguo != "null" && !valorAntiguo.contains("{")) valorAntiguo else ""

                    }else{
                        horarios[i]= if(horaNube != "null") horaNube else ""
                    }
                    duraciones[i]= if(duracionNube != null && duracionNube != "null") duracionNube else "5 min"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)
        onDispose { database.removeEventListener(listener) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Riego I.E Carlos Ramírez París") }) }
    ) { padding->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            //Panel superior sensores
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceEvenly) {
                Card(modifier = Modifier.weight(1f).padding(4.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Temperatura", fontSize = 12.sp)
                        Text("$tempActual °C", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                }
                Card(modifier = Modifier.weight(1f).padding(4.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text("Humedad", fontSize = 12.sp)
                        Text("$humActual %", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            //Switch de seleccion de modo

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text( if (esModoAutomatico) "CONTROL POR SENSORES" else "CONTROL POR HORARIOS", fontWeight = FontWeight.Bold)
                        Text("Mueve el switch para cambiar",fontSize = 12.sp)
                    }
                    Switch(checked = esModoAutomatico,
                        onCheckedChange = {
                            esModoAutomatico=it
                            database.child("modo").setValue(if (it) "automatico" else "horario")
                       }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            //bonton manual

            Button(
                onClick = {database.child("control/manual_trigger").setValue(if (riegoManualActivo) 0 else 1)},
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (riegoManualActivo) Color(0xFF43A047) else Color.DarkGray)
            ) {
                Icon(Icons.Default.WaterDrop, null)
                Spacer(Modifier.width(8.dp))
                Text(if (riegoManualActivo) "RIEGO MANUAL ACTIVADO" else "RIEGO MANUAL AHORA")
            }
            Spacer(Modifier.height(20.dp))

            //Configuracion opcional dinamica
            Text("Programación Opcional", fontWeight = FontWeight.Bold, color = Color.Gray)

            if (esModoAutomatico){
                OutlinedTextField(value = umbralTemp, onValueChange = { umbralTemp=it}, label = { Text("Regar si temperatura > °C") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = umbralHum, onValueChange = { umbralHum=it}, label = { Text("Regar si humedad < %") }, modifier = Modifier.fillMaxWidth())
            }else{
                LazyColumn(Modifier.weight(1f)) {
                    itemsIndexed(horarios){ index, _->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            OutlinedTextField(
                                value = horarios[index],
                                onValueChange ={horarios[index]=it},
                                label = { Text("Temp. ${index+1} (HH:MM)") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ej: 06:00") },
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                                //Spinner para duracion
                            ExposedDropdownMenuBox(
                                expanded = expandedStates[index],
                                onExpandedChange = { expandedStates[index] = !expandedStates[index] },
                                modifier = Modifier.weight(1f)

                            ){
                                OutlinedTextField(
                                    value = duraciones[index],
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Duración") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStates[index])},
                                    modifier = Modifier.menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedStates[index],
                                    onDismissRequest = { expandedStates[index] = false },
                                     ){
                                   opcionesTiempo.forEach { tiempo ->
                                       DropdownMenuItem(
                                           text = { Text(tiempo) },
                                           onClick = {
                                               duraciones[index] = tiempo
                                               expandedStates[index] = false
                                           }
                                       )
                                   }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    database.child("configuracion/umbral_temp").setValue(umbralTemp.trim())
                    database.child("configuracion/umbral_hum").setValue(umbralHum.trim())

                    for (i in 0..5){
                        database.child("horarios/t${i+1}/hora").setValue(horarios[i].trim())
                        database.child("horarios/t${i+1}/duracion").setValue(duraciones[i].trim())

                    }
                    Toast.makeText(context, "✅ Datos Actualizados", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)

            ) {
                Icon(Icons.Default.Done, null)
                Spacer(Modifier.width(8.dp))
                Text("GUARDAR EN LA NUBE")
            }
        }
    }
}















