package com.br.bbchain.certificates.flow

import com.br.bbchain.certificates.model.Disciplina
import com.br.bbchain.certificates.state.DisciplinaState
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class CriarDisciplinaFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(listOf("com.br.bbchain.certificates.contract"))
        a = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `deve criar disciplina`() {
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
        assertEquals(output.disciplina, disciplina)
    }

}