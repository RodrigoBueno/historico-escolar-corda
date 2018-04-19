package com.br.bbchain.certificates.flow

import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnviarDisciplinaFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var disciplinaState: DisciplinaState

    @Before
    fun setup() {
        network = MockNetwork(listOf("com.br.bbchain.certificates.contract"))
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b).forEach { it.registerInitiatedFlow(EnviarDisciplinaFlow.RespFlow::class.java) }

        network.runNetwork()
        disciplinaState = criarDisciplina()
    }

    fun criarDisciplina() : DisciplinaState {
        val disciplina = Disciplina(idAluno = 1,
                cargaHoraria = 0,
                data = Instant.now(),
                faculdade = a.info.legalIdentities.first(),
                nomeDisciplina = "Corda",
                nota = 10,
                professor = "Rodrigo")

        val flow = CriarDisciplinaFlow.ReqFlow(disciplina)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val output = signedTransaction.coreTransaction.outputsOfType<DisciplinaState>().single()

        return output
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `deve enviar disciplina`() {

        val flow = EnviarDisciplinaFlow.ReqFlow(disciplinaState.linearId.id, b.info.legalIdentities.first())
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val outputState = signedTransaction.coreTransaction.outputsOfType<DisciplinaState>().single()
        assertEquals(outputState.disciplina, disciplinaState.disciplina)
        assertTrue(outputState.faculdadesReceptoras.containsAll(disciplinaState.faculdadesReceptoras))
        assertTrue(disciplinaState.faculdadesReceptoras.size + 1 == outputState.faculdadesReceptoras.size)

        listOf(a, b).forEach {
            val vaultState = it.services.vaultService.queryBy<DisciplinaState>(
                    QueryCriteria.LinearStateQueryCriteria(
                            linearId = listOf(disciplinaState.linearId))).states.single().state.data
        assertEquals(vaultState, outputState)
        }
    }

}