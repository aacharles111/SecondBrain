package com.secondbrain.ui.card

import com.secondbrain.data.model.CardType
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.service.AiService
import com.secondbrain.data.service.WebSearchService
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.util.ContentExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class CreateCardViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var cardRepository: CardRepository
    private lateinit var aiService: AiService
    private lateinit var aiServiceManager: AiServiceManager
    private lateinit var contentExtractor: ContentExtractor
    private lateinit var webSearchService: WebSearchService

    private lateinit var viewModel: CreateCardViewModel

    @Before
    fun setup() {
        cardRepository = Mockito.mock(CardRepository::class.java)
        aiService = Mockito.mock(AiService::class.java)
        aiServiceManager = Mockito.mock(AiServiceManager::class.java)
        contentExtractor = Mockito.mock(ContentExtractor::class.java)
        webSearchService = Mockito.mock(WebSearchService::class.java)

        Dispatchers.setMain(testDispatcher)

        viewModel = CreateCardViewModel(
            cardRepository = cardRepository,
            aiService = aiService,
            aiServiceManager = aiServiceManager,
            contentExtractor = contentExtractor,
            webSearchService = webSearchService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `summary toggle states are initialized to true by default`() {
        // Assert that all summary toggle states are true by default
        assert(viewModel.urlSummaryEnabled)
        assert(viewModel.searchSummaryEnabled)
        assert(viewModel.pdfSummaryEnabled)
        assert(viewModel.noteSummaryEnabled)
        assert(viewModel.audioSummaryEnabled)
    }

    @Test
    fun `toggling summary states works correctly`() {
        // Toggle URL summary state
        viewModel.urlSummaryEnabled = false
        assert(!viewModel.urlSummaryEnabled)
        viewModel.urlSummaryEnabled = true
        assert(viewModel.urlSummaryEnabled)

        // Toggle Search summary state
        viewModel.searchSummaryEnabled = false
        assert(!viewModel.searchSummaryEnabled)
        viewModel.searchSummaryEnabled = true
        assert(viewModel.searchSummaryEnabled)

        // Toggle PDF summary state
        viewModel.pdfSummaryEnabled = false
        assert(!viewModel.pdfSummaryEnabled)
        viewModel.pdfSummaryEnabled = true
        assert(viewModel.pdfSummaryEnabled)

        // Toggle Note summary state
        viewModel.noteSummaryEnabled = false
        assert(!viewModel.noteSummaryEnabled)
        viewModel.noteSummaryEnabled = true
        assert(viewModel.noteSummaryEnabled)

        // Toggle Audio summary state
        viewModel.audioSummaryEnabled = false
        assert(!viewModel.audioSummaryEnabled)
        viewModel.audioSummaryEnabled = true
        assert(viewModel.audioSummaryEnabled)
    }
}
