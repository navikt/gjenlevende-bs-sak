package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.ApplicationLocal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [ApplicationLocal::class], properties = ["scheduling.enabled=true"])
@ActiveProfiles("integrasjonstest")
class DummyTaskTest {
    @Autowired
    private lateinit var taskService: TaskService

    private lateinit var dummyTask: DummyTask

    @BeforeEach
    fun setup() {
        dummyTask = DummyTask()
    }

    @Test
    fun `Sjekk at man kan opprette task for arbeidsavklaringspenger endringer og at den har riktig metadata`() {
        val payload = "123"
        val task = DummyTask.opprettTask(payload)
        taskService.save(task)
        val taskList = taskService.findAll()
        val taskFraDB = taskList.first()
        assertThat(taskFraDB.metadata).isNotEmpty
        assertThat(taskFraDB.metadataWrapper.properties.keys.size).isEqualTo(1)
        dummyTask.doTask(task)
    }
}
