package io.github.opensmsrelay

import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.repository.LogRepository
import io.github.opensmsrelay.domain.usecase.ExportLogsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportLogsUseCaseTest {

    private val logRepository: LogRepository = mockk()
    private val useCase = ExportLogsUseCase(logRepository)

    @Test
    fun `exported CSV contains header row`() = runTest {
        coEvery { logRepository.getAll() } returns emptyList()
        val csv = useCase()
        assertTrue(csv.startsWith("ID,Timestamp"))
    }

    @Test
    fun `exported CSV contains log entries`() = runTest {
        val log = ForwardingLog(
            id = 1L,
            sender = "SAMPATH",
            body = "Your OTP is 1234",
            isMatched = true,
            matchedRuleName = "Bank Rule",
            emailStatus = ForwardingStatus.SUCCESS,
            smsStatus = ForwardingStatus.NOT_ATTEMPTED
        )
        coEvery { logRepository.getAll() } returns listOf(log)

        val csv = useCase()
        assertTrue(csv.contains("SAMPATH"))
        assertTrue(csv.contains("Bank Rule"))
        assertTrue(csv.contains("SUCCESS"))
    }

    @Test
    fun `CSV values with commas are escaped`() = runTest {
        val log = ForwardingLog(
            id = 1L,
            sender = "TEST",
            body = "Hello, World",
            isMatched = false,
            emailStatus = ForwardingStatus.NOT_ATTEMPTED,
            smsStatus = ForwardingStatus.NOT_ATTEMPTED
        )
        coEvery { logRepository.getAll() } returns listOf(log)

        val csv = useCase()
        // Body with comma should be quoted
        assertTrue(csv.contains("\"Hello, World\""))
    }
}
