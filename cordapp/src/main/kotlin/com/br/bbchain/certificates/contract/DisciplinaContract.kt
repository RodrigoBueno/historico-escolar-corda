package com.br.bbchain.certificates.contract

import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class DisciplinaContract : Contract {

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commandsOfType<DisciplinaContract.Commands>().single()
        when(command.value){
            is Commands.CriarDisciplina -> verifyCriarDisciplina(tx)
            is Commands.EnviarDisciplina -> verifyEnviarDisciplina(tx)
            else -> throw IllegalStateException("Não reconheço este comando.")
        }

        val output = tx.outputsOfType<DisciplinaState>().single()

        command.signers.containsAll((output.participants).map { it.owningKey })
    }

    fun verifyCriarDisciplina(tx: LedgerTransaction){
        requireThat {
            // Regras de entradas
            "Tem que ter um e apenas um Output." using (tx.outputsOfType<DisciplinaState>().size == 1)
            "Não pode haver Input." using (tx.inputsOfType<DisciplinaState>().isEmpty())


            val outputs = tx.outputsOfType<DisciplinaState>()
            // Regras de criacao de Disciplina
            "É necessário que a disciplina tenha um professor." using outputs.all {  it.disciplina.professor != "" }
            "É necessário que tenha um id de aluno." using outputs.all { it.disciplina.idAluno > 0 }
            "A nota não pode ser negativa." using outputs.none { it.disciplina.nota < 0 }
        }
    }

    fun verifyEnviarDisciplina(tx: LedgerTransaction){
        requireThat {
            "Tem que ter um e apenas um input." using (tx.inputsOfType<DisciplinaState>().size == 1)
            "Tem que ter um e apenas um output." using (tx.outputsOfType<DisciplinaState>().size == 1)

            val input = tx.inputsOfType<DisciplinaState>().single()
            val output = tx.outputsOfType<DisciplinaState>().single()
            // Regras de Envio de Disciplina
            "Não pode ser removida uma faculdade da lista de faculdades receptoras." using (output.faculdadesReceptoras.containsAll(input.faculdadesReceptoras))
            "A lista de faculdades receptoras precisa ter mais uma faculdade." using (
                            (output.faculdadesReceptoras.size == input.faculdadesReceptoras.size + 1))
            "A disciplina não pode ser alterada." using (output.disciplina == input.disciplina)
            "As disciplinas tem que ser todas de um mesmo aluno." using (tx.outputsOfType<DisciplinaState>().all { it.disciplina.idAluno == output.disciplina.idAluno })
        }
    }

    interface Commands: CommandData {
        class CriarDisciplina : Commands
        class EnviarDisciplina : Commands
    }

}