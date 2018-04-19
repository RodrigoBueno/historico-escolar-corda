package com.br.bbchain.certificates.flow

import co.paralleluniverse.fibers.Suspendable
import com.br.bbchain.certificates.contract.DisciplinaContract
import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object CriarDisciplinaFlow {

    class ReqFlow(val disciplina: Disciplina): FlowLogic<SignedTransaction>(){

        @Suspendable
        override fun call(): SignedTransaction {
            validarDisciplina(disciplina)

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            val disciplinaState = DisciplinaState(disciplina)

            val comando = Command(
                    DisciplinaContract.Commands.CriarDisciplina(),
                    disciplinaState.participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary)

            /*
            * Inputs são um estado inicial do ledger para um determinado State
            * Ouputs são um estado final do ledger para um determinado State
            *
            * */

            txBuilder.addOutputState(disciplinaState, DisciplinaContract::class.java.canonicalName)
            txBuilder.addCommand(comando)
            txBuilder.verify(serviceHub)

            val transacao = serviceHub.signInitialTransaction(txBuilder)

            return subFlow(FinalityFlow(transacao))
        }

        fun validarDisciplina(disciplina: Disciplina){
            requireThat{
                "Eu tenho que ser a faculdade emissora." using
                        (disciplina.faculdade == ourIdentity)
            }
        }

    }

}