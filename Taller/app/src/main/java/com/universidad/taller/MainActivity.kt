package com.universidad.taller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universidad.taller.ui.theme.TallerTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TallerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculadoraNominaApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculadoraNominaApp(modifier: Modifier = Modifier) {
    var salarioBasico by rememberSaveable { mutableStateOf("") }
    var horasDiurnas by rememberSaveable { mutableStateOf("") }
    var horasNocturnas by rememberSaveable { mutableStateOf("") }
    
    var esDominical by rememberSaveable { mutableStateOf(false) }
    var transporteEmpresa by rememberSaveable { mutableStateOf(false) }
    
    var resultado by remember { mutableStateOf<ResultadoNomina?>(null) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }
    
    val calcular = {
        val salario = salarioBasico.toDoubleOrNull()
        val hDiurnas = if (horasDiurnas.isBlank()) 0.0 else horasDiurnas.toDoubleOrNull()
        val hNocturnas = if (horasNocturnas.isBlank()) 0.0 else horasNocturnas.toDoubleOrNull()
        
        when {
            salario == null -> {
                mensajeError = "Ingrese un salario válido"
                resultado = null
            }
            salario < SMMLV_2026 -> {
                mensajeError = "El salario no puede ser inferior al mínimo ($ 1.750.905)"
                resultado = null
            }
            hDiurnas == null || hNocturnas == null || hDiurnas < 0 || hNocturnas < 0 -> {
                mensajeError = "Las horas extra deben ser un número mayor o igual a cero"
                resultado = null
            }
            (hDiurnas + hNocturnas) > 48 -> {
                mensajeError = "El total de horas extra no puede superar 48 en el mes"
                resultado = null
            }
            else -> {
                mensajeError = null
                resultado = calcularNomina(
                    salarioBasico = salario,
                    horasDiurnas = hDiurnas,
                    horasNocturnas = hNocturnas,
                    esDominical = esDominical,
                    transporteEmpresa = transporteEmpresa
                )
            }
        }
    }
    
    // Si cambia algun switch y ya hay un resultado válido, recalcular automáticamente (como pide el requerimiento)
    val onSwitchChange = {
        if (resultado != null && mensajeError == null) {
            calcular()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        CampoNumerico(
            etiqueta = R.string.salario_basico_label,
            valor = salarioBasico,
            onValueChange = { 
                salarioBasico = it 
                mensajeError = null
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = mensajeError != null && salarioBasico.toDoubleOrNull()?.let { it < SMMLV_2026 } ?: true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CampoNumerico(
            etiqueta = R.string.horas_diurnas_label,
            valor = horasDiurnas,
            onValueChange = { 
                horasDiurnas = it
                mensajeError = null
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CampoNumerico(
            etiqueta = R.string.horas_nocturnas_label,
            valor = horasNocturnas,
            onValueChange = { 
                horasNocturnas = it 
                mensajeError = null
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
        
        mensajeError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FilaInterruptor(
            etiqueta = R.string.es_dominical_label,
            valor = esDominical,
            onCheckedChange = { 
                esDominical = it
                onSwitchChange()
            }
        )
        
        FilaInterruptor(
            etiqueta = R.string.transporte_empresa_label,
            valor = transporteEmpresa,
            onCheckedChange = { 
                transporteEmpresa = it
                onSwitchChange()
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { calcular() },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.btn_calcular))
            }
            
            OutlinedButton(
                onClick = {
                    salarioBasico = ""
                    horasDiurnas = ""
                    horasNocturnas = ""
                    esDominical = false
                    transporteEmpresa = false
                    resultado = null
                    mensajeError = null
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(stringResource(R.string.btn_limpiar))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        resultado?.let { res ->
            DesgloseNomina(res)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val rango = clasificarRango(salarioBasico.toDoubleOrNull() ?: 0.0)
            ImagenRango(rango)
        }
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    TextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        singleLine = true
    )
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    valor: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(etiqueta),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = valor,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DesgloseNomina(resultado: ResultadoNomina) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.devengado_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LineaDesglose(stringResource(R.string.valor_hora), resultado.valorHora)
        LineaDesglose(stringResource(R.string.salario_basico_title), resultado.salarioBasico)
        LineaDesglose(stringResource(R.string.horas_extra), resultado.totalHorasExtra)
        LineaDesglose(stringResource(R.string.auxilio_transporte), resultado.auxilioTransporte)
        LineaDesglose(stringResource(R.string.total_devengado), resultado.totalDevengado, isBold = true)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.deducciones_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LineaDesglose(stringResource(R.string.salud_deduccion), resultado.aporteSalud)
        LineaDesglose(stringResource(R.string.pension_deduccion), resultado.aportePension)
        LineaDesglose(stringResource(R.string.fondo_solidaridad), resultado.fondoSolidaridad)
        LineaDesglose(stringResource(R.string.total_deducciones), resultado.totalDeducciones, isBold = true)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.salario_neto),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatearMoneda(resultado.salarioNeto),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LineaDesglose(etiqueta: String, valor: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = formatearMoneda(valor),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun formatearMoneda(valor: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 0
    return formatter.format(valor)
}

@Composable
fun ImagenRango(rango: RangoSalarial) {
    val imageRes = when (rango) {
        RangoSalarial.RANGO_1 -> R.drawable.ic_rango_1
        RangoSalarial.RANGO_2 -> R.drawable.ic_rango_2
        RangoSalarial.RANGO_3 -> R.drawable.ic_rango_3
    }
    
    val textRes = when (rango) {
        RangoSalarial.RANGO_1 -> R.string.rango_1_desc
        RangoSalarial.RANGO_2 -> R.string.rango_2_desc
        RangoSalarial.RANGO_3 -> R.string.rango_3_desc
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(R.string.desc_imagen_rango),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculadoraNominaPreview() {
    TallerTheme {
        CalculadoraNominaApp()
    }
}