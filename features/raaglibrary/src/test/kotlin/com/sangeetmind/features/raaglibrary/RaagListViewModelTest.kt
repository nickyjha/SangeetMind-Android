package com.sangeetmind.features.raaglibrary

import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class RaagListViewModelTest {

    @Mock
    private lateinit var repository: RaagRepository

    private lateinit var viewModel: RaagListViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockRaags = listOf(
        Raag(
            id = "1",
            name = "Bhairav",
            nameHindi = "भैरव",
            description = "Morning raag",
            audioUrl = "https://example.com/audio1.mp3",
            durationSeconds = 300,
            tags = listOf("morning", "devotional"),
            timeOfDay = TimeOfDay.MORNING,
            mood = Mood.DEVOTIONAL,
            intensity = Intensity.HIGH
        ),
        Raag(
            id = "2",
            name = "Yaman",
            nameHindi = "यमन",
            description = "Evening raag",
            audioUrl = "https://example.com/audio2.mp3",
            durationSeconds = 420,
            tags = listOf("evening", "romantic"),
            timeOfDay = TimeOfDay.EVENING,
            mood = Mood.ROMANTIC,
            intensity = Intensity.MEDIUM
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRaags should update state with success`() = runTest {
        // Given
        `when`(repository.getRaags()).thenReturn(flowOf(Result.Success(mockRaags)))
        
        // When
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.raags.size)
        assertEquals(2, state.filteredRaags.size)
    }

    @Test
    fun `onSearchQueryChange should filter raags by name`() = runTest {
        // Given
        `when`(repository.getRaags()).thenReturn(flowOf(Result.Success(mockRaags)))
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Bhairav")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Bhairav", state.searchQuery)
        assertEquals(1, state.filteredRaags.size)
        assertEquals("Bhairav", state.filteredRaags[0].name)
    }

    @Test
    fun `onSearchQueryChange should filter raags by Hindi name`() = runTest {
        // Given
        `when`(repository.getRaags()).thenReturn(flowOf(Result.Success(mockRaags)))
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("यमन")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredRaags.size)
        assertEquals("Yaman", state.filteredRaags[0].name)
    }

    @Test
    fun `onSearchQueryChange should filter raags by tags`() = runTest {
        // Given
        `when`(repository.getRaags()).thenReturn(flowOf(Result.Success(mockRaags)))
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("romantic")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredRaags.size)
        assertEquals("Yaman", state.filteredRaags[0].name)
    }

    @Test
    fun `clearSearch should reset search query and show all raags`() = runTest {
        // Given
        `when`(repository.getRaags()).thenReturn(flowOf(Result.Success(mockRaags)))
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onSearchQueryChange("Bhairav")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(2, state.filteredRaags.size)
    }

    @Test
    fun `loadRaags should update state with error`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(repository.getRaags()).thenReturn(
            flowOf(Result.Error(Exception(errorMessage), errorMessage))
        )

        // When
        viewModel = RaagListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertTrue(state.raags.isEmpty())
    }
}

