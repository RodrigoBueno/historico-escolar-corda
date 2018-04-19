package com.br.bbchain.certificates.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable
data class Disciplina(
        val idAluno: Int,
        val nomeDisciplina: String,
        val data: Instant,
        val professor: String,
        val nota: Int,
        val cargaHoraria: Int,
        val faculdade: Party
        )