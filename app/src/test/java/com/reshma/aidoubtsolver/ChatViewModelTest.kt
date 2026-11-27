package com.reshma.aidoubtsolver

import app.cash.turbine.test
import com.reshma.aidoubtsolver.domain.AiException
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.ChatRepository
import com.reshma.aidoubtsolver.domain.Role
import com.reshma.aidoubtsolver.domain.Subject
import com.reshma.aidoubtsolver.ui.chat.ChatViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val repo = mockk<ChatRepository>(relaxed = true)
    private val question = ChatMessage("q1", Subject.MATHS, Role.USER, "2 + 2?", 0L)

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repo.observe(any()) } returns flowOf(emptyList())
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `send stores question and asks AI`() = runTest {
        coEvery { repo.addUserQuestion(any(), "2 + 2?") } returns question
        val vm = ChatViewModel(repo)

        vm.send("  2 + 2?  ")

        coVerify { repo.addUserQuestion(Subject.GENERAL, "2 + 2?") }
        coVerify { repo.answer(question) }
    }

    @Test fun `blank input is ignored`() = runTest {
        val vm = ChatViewModel(repo)
        vm.send("   ")
        coVerify(exactly = 0) { repo.addUserQuestion(any(), any()) }
    }

    @Test fun `AI failure is surfaced as an error`() = runTest {
        coEvery { repo.addUserQuestion(any(), any()) } returns question
        coEvery { repo.answer(any()) } throws AiException("Network error")
        val vm = ChatViewModel(repo)

        vm.errors.test {
            vm.send("2 + 2?")
            assertEquals("Network error", awaitItem())
        }
    }
}
