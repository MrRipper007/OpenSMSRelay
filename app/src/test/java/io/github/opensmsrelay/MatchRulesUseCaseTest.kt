package io.github.opensmsrelay

import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SenderMatchType
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.domain.repository.RuleRepository
import io.github.opensmsrelay.domain.usecase.MatchRulesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MatchRulesUseCaseTest {

    private val ruleRepository: RuleRepository = mockk()
    private lateinit var useCase: MatchRulesUseCase

    @Before
    fun setup() {
        useCase = MatchRulesUseCase(ruleRepository)
    }

    private fun makeRule(
        matchType: SenderMatchType,
        senderValue: String,
        bodyFilter: String? = null,
        isEnabled: Boolean = true
    ) = Rule(
        id = 1L,
        name = "Test Rule",
        isEnabled = isEnabled,
        matchType = matchType,
        senderValue = senderValue,
        bodyFilter = bodyFilter
    )

    @Test
    fun `exact match succeeds for identical sender`() = runTest {
        val rule = makeRule(SenderMatchType.EXACT, "SAMPATH")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "SAMPATH", body = "Balance: 1000"))
        assertEquals(1, result.size)
    }

    @Test
    fun `exact match is case insensitive`() = runTest {
        val rule = makeRule(SenderMatchType.EXACT, "sampath")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "SAMPATH", body = "test"))
        assertEquals(1, result.size)
    }

    @Test
    fun `exact match fails for different sender`() = runTest {
        val rule = makeRule(SenderMatchType.EXACT, "SAMPATH")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "HNB", body = "test"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `contains match succeeds when sender contains value`() = runTest {
        val rule = makeRule(SenderMatchType.CONTAINS, "BANK")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "MY-BANK-PROMO", body = "test"))
        assertEquals(1, result.size)
    }

    @Test
    fun `contains match is case insensitive`() = runTest {
        val rule = makeRule(SenderMatchType.CONTAINS, "bank")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "BANK-LK", body = "test"))
        assertEquals(1, result.size)
    }

    @Test
    fun `regex match succeeds for valid pattern`() = runTest {
        val rule = makeRule(SenderMatchType.REGEX, "^\\+94[0-9]{9}$")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "+94771234567", body = "test"))
        assertEquals(1, result.size)
    }

    @Test
    fun `regex match fails for non-matching sender`() = runTest {
        val rule = makeRule(SenderMatchType.REGEX, "^\\+94[0-9]{9}$")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "+1234567890", body = "test"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invalid regex does not crash, returns no match`() = runTest {
        val rule = makeRule(SenderMatchType.REGEX, "[invalid(")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "anything", body = "test"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `disabled rule is never matched`() = runTest {
        val rule = makeRule(SenderMatchType.EXACT, "SAMPATH", isEnabled = false)
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val result = useCase(SmsMessage(sender = "SAMPATH", body = "test"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `body filter requires keyword in body`() = runTest {
        val rule = makeRule(SenderMatchType.EXACT, "BANK", bodyFilter = "OTP")
        coEvery { ruleRepository.getAll() } returns listOf(rule)

        val matched = useCase(SmsMessage(sender = "BANK", body = "Your OTP is 1234"))
        val ignored = useCase(SmsMessage(sender = "BANK", body = "Your balance is Rs. 5000"))

        assertEquals(1, matched.size)
        assertTrue(ignored.isEmpty())
    }

    @Test
    fun `multiple matching rules are all returned`() = runTest {
        val rule1 = makeRule(SenderMatchType.EXACT, "SAMPATH")
        val rule2 = makeRule(SenderMatchType.CONTAINS, "SAM")
        coEvery { ruleRepository.getAll() } returns listOf(rule1, rule2)

        val result = useCase(SmsMessage(sender = "SAMPATH", body = "test"))
        assertEquals(2, result.size)
    }

    @Test
    fun `no rules returns empty list`() = runTest {
        coEvery { ruleRepository.getAll() } returns emptyList()

        val result = useCase(SmsMessage(sender = "ANY", body = "test"))
        assertTrue(result.isEmpty())
    }
}
