package com.example.aulapamfirebase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aulapamfirebase.ui.theme.AulaPAMFireBaseTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AulaPAMFireBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(db)
                }
            }
        }
    }
}

@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf(listOf<HashMap<String, String>>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Box(
            modifier = Modifier
                .size(150.dp)  // Define o tamanho do círculo
                .clip(CircleShape) // Aplica o formato circular
                .align(Alignment.CenterHorizontally) // Centraliza a imagem
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_aplicativo),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxSize(), // Preenche o tamanho da Box
                contentScale = ContentScale.Crop // Corta a imagem para se ajustar ao círculo
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "App Firebase Firestore.",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henrique Porto de Sousa 3°DS A Manhã",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Campos de entrada
        InputField(label = "Nome:", value = nome) { nome = it }
        InputField(label = "Telefone:", value = telefone) { telefone = it }
        InputField(label = "Cidade:", value = cidade) { cidade = it }
        InputField(label = "Bairro:", value = bairro) { bairro = it }
        InputField(label = "Cep:", value = cep) { cep = it }


        // Lista de clientes
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Nome:", modifier = Modifier.weight(0.2f))
                    Text(text = "Telefone:", modifier = Modifier.weight(0.2f))
                    Text(text = "Cidade:", modifier = Modifier.weight(0.2f))
                    Text(text = "Bairro:", modifier = Modifier.weight(0.2f))
                    Text(text = "Cep:", modifier = Modifier.weight(0.2f))
                }
            }
            items(clientes) { cliente ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = cliente["nome"] ?: "--", modifier = Modifier.weight(0.2f))
                    Text(text = cliente["telefone"] ?: "--", modifier = Modifier.weight(0.2f))
                    Text(text = cliente["cidade"] ?: "--", modifier = Modifier.weight(0.2f))
                    Text(text = cliente["bairro"] ?: "--", modifier = Modifier.weight(0.2f))
                    Text(text = cliente["cep"] ?: "--", modifier = Modifier.weight(0.2f))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botão para adicionar cliente
        Button(
            onClick = {
                val clientData = hashMapOf<String, Any>(
                    "nome" to nome,
                    "telefone" to telefone,
                    "cidade" to cidade,
                    "bairro" to bairro,
                    "cep" to cep
                )
                db.collection("Clientes").add(clientData)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Cliente cadastrado com sucesso.")
                        BuscarClientes(db) { BuscarClientes ->
                            clientes = BuscarClientes
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Erro ao cadastrar cliente", e)
                    }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Cadastrar")
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(0.3f))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(0.7f)
        )
    }
}

// Função para buscar clientes do Firestore
private fun BuscarClientes(db: FirebaseFirestore, onFetch: (List<HashMap<String, String>>) -> Unit) {
    db.collection("Clientes").get()
        .addOnSuccessListener { documents ->
            val clientsList = mutableListOf<HashMap<String, String>>()
            for (document in documents) {
                val clientData = hashMapOf<String, String>(
                    "nome" to (document.getString("nome") ?: "--"),
                    "telefone" to (document.getString("telefone") ?: "--"),
                    "cidade" to (document.getString("cidade") ?: "--"),
                    "bairro" to (document.getString("bairro") ?: "--"),
                    "cep" to (document.getString("cep") ?: "--")
                )
                clientsList.add(clientData)
                Log.d("Firestore", "${document.id} => ${document.data}")
            }
            onFetch(clientsList)
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Erro ao obter documentos: ", exception)
        }
}
