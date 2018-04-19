package com.br.bbchain.certificates.state

import com.br.bbchain.certificates.model.Disciplina
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class DisciplinaState(
        val disciplina: Disciplina,
        val faculdadesReceptoras: List<Party> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<AbstractParty> =
            faculdadesReceptoras + disciplina.faculdade
}