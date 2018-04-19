package com.br.bbchain.certificates.flow

import co.paralleluniverse.fibers.Suspendable
import com.br.bbchain.certificates.contract.DisciplinaContract
import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

object EnviarDisciplinaFlow {

    @InitiatingFlow
    class ReqFlow(val disciplinaId: UUID, val para: Party): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            val disciplinaStateAndRef = serviceHub.vaultService.queryBy<DisciplinaState>(
                    QueryCriteria.LinearStateQueryCriteria(
                            linearId = listOf(UniqueIdentifier(id = disciplinaId))))
                    .states.single()

            val notary = disciplinaStateAndRef.state.notary

            val disciplinaState = disciplinaStateAndRef.state.data

            val novoDisciplinaState = disciplinaState.copy(faculdadesReceptoras =
                        disciplinaState.faculdadesReceptoras + para)

            val comando = Command(DisciplinaContract.Commands.EnviarDisciplina(),
                    novoDisciplinaState.participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary)

            txBuilder.addInputState(disciplinaStateAndRef)
            txBuilder.addOutputState(novoDisciplinaState, DisciplinaContract::class.java.canonicalName)
            txBuilder.addCommand(comando)
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

    @InitiatedBy(ReqFlow::class)
    class RespFlow(val session: FlowSession): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) =
                        requireThat {
                    val outputs = stx.coreTransaction.outputsOfType<DisciplinaState>()
                    "Tinha que ter recebido Disciplinas!" using outputs.isNotEmpty()
                    "As disciplinas não podem ser emitidas no meu nome." using outputs.all { it.disciplina.faculdade != ourIdentity }
                }
            }

            return subFlow(signTransactionFlow)
        }

    }

}