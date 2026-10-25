package com.example.app_academia_ulbra

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_academia_ulbra.dados.CorAviso
import com.example.app_academia_ulbra.ui.theme.AmareloAtencao
import com.example.app_academia_ulbra.ui.theme.CinzaNeutro
import com.example.app_academia_ulbra.ui.theme.Espaco
import com.example.app_academia_ulbra.ui.theme.VerdeSucesso
import com.example.app_academia_ulbra.ui.theme.VermelhoPerigo

// equivalente aos toasts do site
fun mostrarToast(context: Context, mensagem: String) {
    Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
}

fun corDoAviso(cor: CorAviso): Color = when (cor) {
    CorAviso.SUCESSO -> VerdeSucesso
    CorAviso.ATENCAO -> AmareloAtencao
    CorAviso.PERIGO -> VermelhoPerigo
    CorAviso.NEUTRO -> CinzaNeutro
}

// coluna com rolagem e o padding padrão das telas (o "ion-padding" do site)
@Composable
fun ConteudoTela(conteudo: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(Espaco.medio),
        verticalArrangement = Arrangement.spacedBy(Espaco.pequeno)
    ) {
        conteudo()
    }
}

@Composable
fun BannerBoasVindas(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Espaco.pequeno),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(subtitulo, color = Color(0xFF666666), textAlign = TextAlign.Center)
    }
}

// os cards de menu das telas iniciais (home-aluno e home-professor)
@Composable
fun CardMenu(
    icone: ImageVector,
    corIcone: Color,
    titulo: String,
    descricao: String,
    novidade: Boolean = false,
    aoClicar: () -> Unit
) {
    Card(
        onClick = aoClicar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFD7D8DA)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Espaco.medio),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(Espaco.pequeno))
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(descricao, fontSize = 13.sp, color = Color(0xFF666666), textAlign = TextAlign.Center)
            }
            if (novidade) {
                Text(
                    "Novidade",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Espaco.pequeno)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// card colorido de aviso (situação do plano, plano inativo...)
@Composable
fun CardAviso(
    mensagem: String,
    cor: CorAviso,
    aoClicar: (() -> Unit)? = null,
    extra: @Composable () -> Unit = {}
) {
    val fundo = corDoAviso(cor)
    val texto = if (cor == CorAviso.ATENCAO) Color.Black else Color.White
    val modifier = Modifier.fillMaxWidth()
    val conteudo: @Composable () -> Unit = {
        Column(Modifier.padding(Espaco.medio)) {
            Text(mensagem, color = texto)
            extra()
        }
    }
    if (aoClicar != null) {
        Card(onClick = aoClicar, modifier = modifier, colors = CardDefaults.cardColors(containerColor = fundo)) { conteudo() }
    } else {
        Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = fundo)) { conteudo() }
    }
}

// card branco com título, usado nas listas de treinos e planos
@Composable
fun CardSimples(conteudo: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(Espaco.medio), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            conteudo()
        }
    }
}

@Composable
fun TituloSecao(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = Espaco.pequeno)
    )
}

@Composable
fun TextoSecundario(texto: String) {
    Text(texto, color = Color(0xFF666666), fontSize = 14.sp)
}

@Composable
fun TextoVazio(texto: String) {
    Text(
        texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Espaco.medio),
        textAlign = TextAlign.Center
    )
}

@Composable
fun Carregando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Espaco.grande),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EtiquetaStatus(confirmado: Boolean) {
    Text(
        if (confirmado) "Confirmado" else "Pendente",
        color = if (confirmado) Color.White else Color.Black,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (confirmado) VerdeSucesso else AmareloAtencao)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    placeholder: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    senha: Boolean = false,
    erro: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = erro != null,
        supportingText = erro?.let { { Text(it) } },
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}

// substitui o ion-select: um campo que abre uma lista de opções
@Composable
fun <T> Seletor(
    rotulo: String,
    opcoes: List<Pair<T, String>>,
    selecionado: T?,
    aoSelecionar: (T) -> Unit,
    placeholder: String = "Selecione",
    habilitado: Boolean = true
) {
    var aberto by remember { mutableStateOf(false) }
    val textoSelecionado = opcoes.firstOrNull { it.first == selecionado }?.second ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textoSelecionado,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(rotulo) },
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // camada transparente por cima pra abrir o menu ao tocar no campo
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = habilitado) { aberto = true }
        )
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEach { (valor, texto) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        aoSelecionar(valor)
                        aberto = false
                    }
                )
            }
        }
    }
}

// botão que abre o vídeo de demonstração do exercício (ex: YouTube)
@Composable
fun ExercicioMidia(videoUrl: String?) {
    if (videoUrl.isNullOrBlank()) return
    val uriHandler = LocalUriHandler.current
    TextButton(onClick = { uriHandler.openUri(videoUrl) }) {
        Text("Ver vídeo do exercício")
    }
}

@Composable
fun DialogoConfirmacao(
    titulo: String,
    mensagem: String,
    textoConfirmar: String,
    perigo: Boolean = false,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(titulo) },
        text = { Text(mensagem) },
        confirmButton = {
            TextButton(onClick = aoConfirmar) {
                Text(textoConfirmar, color = if (perigo) VermelhoPerigo else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}

// diálogo com opções de rádio (o alert com inputs "radio" do site)
@Composable
fun <T> DialogoEscolha(
    titulo: String,
    opcoes: List<Pair<T, String>>,
    textoConfirmar: String,
    mensagem: String? = null,
    aoConfirmar: (T) -> Unit,
    aoCancelar: () -> Unit
) {
    var escolhido by remember { mutableStateOf(opcoes.firstOrNull()?.first) }

    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(titulo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (mensagem != null) {
                    Text(mensagem, modifier = Modifier.padding(bottom = Espaco.pequeno))
                }
                opcoes.forEach { (valor, texto) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = escolhido == valor, onClick = { escolhido = valor })
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = escolhido == valor, onClick = { escolhido = valor })
                        Text(texto)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { escolhido?.let(aoConfirmar) }) { Text(textoConfirmar) }
        },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}

// diálogo com campos de texto (o alert com inputs de texto do site)
@Composable
fun DialogoFormulario(
    titulo: String,
    textoConfirmar: String,
    mensagem: String? = null,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit,
    campos: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Espaco.pequeno)
            ) {
                if (mensagem != null) Text(mensagem)
                campos()
            }
        },
        confirmButton = { TextButton(onClick = aoConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}
