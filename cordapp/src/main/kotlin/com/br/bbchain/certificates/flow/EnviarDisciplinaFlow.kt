package com.br.bbchain.certificates.flow

import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

object EnviarDisciplinaFlow {

    class ReqFlow(val disciplinaId: UUID, val para: Party): FlowLogic<SignedTransaction>() {

        override fun call(): SignedTransaction {

            val disciplinaStateAndRef = serviceHub.vaultService.queryBy<DisciplinaState>(
                    QueryCriteria.LinearStateQueryCriteria(
                            linearId = listOf(UniqueIdentifier(id = disciplinaId))))
                    .states.single()

            val disciplinaState = disciplinaStateAndRef.state.data

            val novoDisciplinaState = disciplinaState.copy(faculdadesReceptoras =
                        disciplinaState.faculdadesReceptoras + para)

            val txBuilder = TransactionBuilder()

            txBuilder.addInputState(disciplinaStateAndRef)
            txBuilder.addOutputState(novoDisciplinaState, "")

            txBuilder.verify(serviceHub)

            val transacaoParcialmenteAssinada = serviceHub.signInitialTransaction(txBuilder)

            val listaSessao = novoDisciplinaState.faculdadesReceptoras.map { initiateFlow(it) }

            val transacaoTotalmenteAssinada = subFlow(CollectSignaturesFlow(
                    transacaoParcialmenteAssinada,
                    listaSessao))

            return subFlow(FinalityFlow(transacaoTotalmenteAssinada))


        }

        fun validarDisciplina(disciplina: Disciplina){
            requireThat{
                "Eu tenho que ser a faculdade emissora." using
                        (disciplina.faculdade == ourIdentity)
            }
        }

    }

    class RespFlow(): FlowLogic<SignedTransaction>() {

        override fun call(): SignedTransaction {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}