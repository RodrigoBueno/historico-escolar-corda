package com.br.bbchain.certificates.model

import net.corda.core.identity.Party
import java.time.Instant

data class Disciplina(
        val idAluno: Int,
        val nomeDisciplina: String,
        val data: Instant,
        val professor: String,
        val nota: Int,
        val cargaHoraria: Int,
        val faculdade: Party
        )