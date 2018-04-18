package com.br.bbchain.certificates.flow

import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object CriarDisciplinaFlow {

    class ReqFlow(val disciplina: Disciplina): FlowLogic<SignedTransaction>(){

        override fun call(): SignedTransaction {
            validarDisciplina(disciplina)

            val disciplinaState = DisciplinaState(disciplina)

            val txBuilder = TransactionBuilder()

            /*
            * Inputs são um estado inicial do ledger para um determinado State
            * Ouputs são um estado final do ledger para um determinado State
            *
            * */

            txBuilder.addOutputState(disciplinaState, "")

            txBuilder.verify(serviceHub)

            return serviceHub.signInitialTransaction(txBuilder)
        }

        fun validarDisciplina(disciplina: Disciplina){
            requireThat{
                "Eu tenho que ser a faculdade emissora." using
                        (disciplina.faculdade == ourIdentity)
            }
        }

    }

}