package com.universidad.taller

// Decreto 1469 de 2025
const val SMMLV_2026 = 1_750_905.0
// Decreto 1470 de 2025
const val AUXILIO_TRANSPORTE_2026 = 249_095.0
// Jornada de 42 horas semanales (Ley 2101 de 2021), vigente desde el 15 de julio de 2026
const val HORAS_ORDINARIAS_MENSUALES = 210.0
// Ley 100 de 1993
const val PORCENTAJE_SALUD = 0.04
// Ley 100 de 1993
const val PORCENTAJE_PENSION = 0.04
// Ley 797 de 2003
const val PORCENTAJE_FONDO_SOLIDARIDAD = 0.01

data class ResultadoNomina(
    val salarioBasico: Double,
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double
)

enum class RangoSalarial {
    RANGO_1, // <= 2 SMMLV
    RANGO_2, // > 2 SMMLV y < 4 SMMLV
    RANGO_3  // >= 4 SMMLV
}

fun clasificarRango(salarioBasico: Double): RangoSalarial {
    return when {
        salarioBasico <= 2 * SMMLV_2026 -> RangoSalarial.RANGO_1
        salarioBasico < 4 * SMMLV_2026 -> RangoSalarial.RANGO_2
        else -> RangoSalarial.RANGO_3
    }
}

fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    // Regla 1
    val valorHora = salarioBasico / HORAS_ORDINARIAS_MENSUALES
    
    // Regla 2
    val factorDiurno = if (esDominical) 2.15 else 1.25
    val factorNocturno = if (esDominical) 2.65 else 1.75
    
    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas
    
    // Regla 3
    val ibc = salarioBasico + totalHorasExtra
    
    // Regla 4
    val auxilioTransporte = if (salarioBasico <= 2 * SMMLV_2026 && !transporteEmpresa) {
        AUXILIO_TRANSPORTE_2026
    } else {
        0.0
    }
    
    // Regla 5
    val totalDevengado = ibc + auxilioTransporte
    
    // Regla 6
    val aporteSalud = ibc * PORCENTAJE_SALUD
    val aportePension = ibc * PORCENTAJE_PENSION
    val fondoSolidaridad = if (ibc >= 4 * SMMLV_2026) {
        ibc * PORCENTAJE_FONDO_SOLIDARIDAD
    } else {
        0.0
    }
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad
    
    // Regla 7
    val salarioNeto = totalDevengado - totalDeducciones
    
    return ResultadoNomina(
        salarioBasico = salarioBasico,
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto
    )
}