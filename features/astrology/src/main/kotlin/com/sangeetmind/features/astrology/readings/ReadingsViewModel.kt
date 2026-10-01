package com.sangeetmind.features.astrology.readings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.ChildrenReadingResponse
import com.sangeetmind.libs.models.ForeignReadingResponse
import com.sangeetmind.libs.models.MarriageReadingResponse
import com.sangeetmind.libs.models.StrengthsReadingResponse
import com.sangeetmind.libs.models.CareerQuestionResponse
import com.sangeetmind.libs.models.DebtReadingResponse
import com.sangeetmind.libs.models.RelationshipReadingResponse
import com.sangeetmind.libs.models.EducationReadingResponse
import com.sangeetmind.libs.models.DashaStoryResponse
import com.sangeetmind.libs.models.HealthReadingResponse
import com.sangeetmind.libs.models.PropertyReadingResponse
import com.sangeetmind.libs.models.ReadingPreview
import com.sangeetmind.libs.models.SmallReadingResponse
import com.sangeetmind.libs.models.WealthReadingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val CAREER_QUESTIONS = listOf("job_change", "promotion", "govt_private", "job_business")
val SMALL_TOPICS = listOf("love_style", "ideal_partner", "in_laws")

enum class ReadingTab { CAREER, STRENGTHS, MARRIAGE, SMALL, CHILDREN, FOREIGN, WEALTH, PROPERTY, EDUCATION, DEBT, RELATIONSHIP, HEALTH, DASHA }

data class ReadingsUiState(
    val tab: ReadingTab = ReadingTab.CAREER,
    val isLoading: Boolean = false,
    val career: CareerReadingResponse? = null,
    // null = the full career reading; otherwise one of CAREER_QUESTIONS.
    val careerQuestion: String? = null,
    val careerAnswer: CareerQuestionResponse? = null,
    val strengths: StrengthsReadingResponse? = null,
    val marriage: MarriageReadingResponse? = null,
    val married: Boolean = false,
    val children: ChildrenReadingResponse? = null,
    val isParent: Boolean = false,
    val foreign: ForeignReadingResponse? = null,
    val livesAbroad: Boolean = false,
    val wealth: WealthReadingResponse? = null,
    val ownsBusiness: Boolean = false,
    val property: PropertyReadingResponse? = null,
    val aboutVehicle: Boolean = false,
    val education: EducationReadingResponse? = null,
    val higherStudies: Boolean = false,
    val health: HealthReadingResponse? = null,
    val aboutMind: Boolean = false,
    val dashaStory: DashaStoryResponse? = null,
    val relationship: RelationshipReadingResponse? = null,
    val aboutRemarriage: Boolean = false,
    val debt: DebtReadingResponse? = null,
    val aboutDispute: Boolean = false,
    val smallTopic: String = SMALL_TOPICS.first(),
    // Small readings made this session, by topic.
    val small: Map<String, SmallReadingResponse> = emptyMap(),
    // Free previews by "topic|status" (see previewKey); a missing key is not loaded (yet).
    val previews: Map<String, ReadingPreview> = emptyMap(),
    val error: String? = null
) {
    /** The preview topic and status for the open tab, or null when it has no preview. */
    val previewKey: String?
        get() = when (tab) {
            ReadingTab.CAREER -> careerQuestion?.let { "career_question|$it" }
            ReadingTab.STRENGTHS, ReadingTab.SMALL -> null
            ReadingTab.MARRIAGE -> "marriage|" + if (married) "married" else "single"
            ReadingTab.CHILDREN -> "children|" + if (isParent) "parent" else "planning"
            ReadingTab.FOREIGN -> "foreign|" + if (livesAbroad) "abroad" else "planning"
            ReadingTab.WEALTH -> "wealth|" + if (ownsBusiness) "business" else "job"
            ReadingTab.PROPERTY -> "property|" + if (aboutVehicle) "vehicle" else "property"
            ReadingTab.EDUCATION -> "education|" + if (higherStudies) "higher" else "student"
            ReadingTab.DEBT -> "debt|" + if (aboutDispute) "dispute" else "debt"
            ReadingTab.RELATIONSHIP -> "relationship|" + if (aboutRemarriage) "remarriage" else "strain"
            ReadingTab.HEALTH -> "health|" + if (aboutMind) "mind" else "body"
            ReadingTab.DASHA -> "dasha_story|life"
        }

    /** True once the open tab shows a paid reading, so the sales card can step aside. */
    val tabHasReading: Boolean
        get() = when (tab) {
            ReadingTab.CAREER -> if (careerQuestion == null) career != null else careerAnswer?.question == careerQuestion
            ReadingTab.STRENGTHS -> strengths != null
            ReadingTab.SMALL -> smallTopic in small
            ReadingTab.MARRIAGE -> marriage?.reading != null
            ReadingTab.CHILDREN -> children?.reading != null
            ReadingTab.FOREIGN -> foreign?.reading != null
            ReadingTab.WEALTH -> wealth?.reading != null
            ReadingTab.PROPERTY -> property?.reading != null
            ReadingTab.EDUCATION -> education?.reading != null
            ReadingTab.DEBT -> debt?.reading != null
            ReadingTab.RELATIONSHIP -> relationship?.reading != null
            ReadingTab.HEALTH -> health?.reading != null
            ReadingTab.DASHA -> dashaStory?.reading != null
        }
}

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val repository: ReadingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // "readings?tab=DASHA" opens straight on that tab (the Chart screen's dasha story button).
    private val initialTab = savedStateHandle.get<String>("tab")
        ?.let { name -> ReadingTab.values().firstOrNull { it.name == name } }
        ?: ReadingTab.CAREER

    private val _uiState = MutableStateFlow(ReadingsUiState(tab = initialTab))
    val uiState: StateFlow<ReadingsUiState> = _uiState.asStateFlow()

    private val previewsLoading = mutableSetOf<String>()

    init {
        loadPreview()
    }

    /** Fetches the free preview for the open tab and status once; failures just hide it. */
    private fun loadPreview() {
        val key = _uiState.value.previewKey ?: return
        if (key in _uiState.value.previews || !previewsLoading.add(key)) return
        val (topic, status) = key.split("|", limit = 2)
        viewModelScope.launch {
            val result = repository.getPreview(topic, status)
            previewsLoading.remove(key)
            if (result is Result.Success) {
                _uiState.update { it.copy(previews = it.previews + (key to result.data)) }
            }
        }
    }

    private fun updateAndPreview(change: (ReadingsUiState) -> ReadingsUiState) {
        _uiState.update(change)
        loadPreview()
    }

    fun setTab(tab: ReadingTab) {
        updateAndPreview { it.copy(tab = tab, error = null) }
    }

    fun setCareerQuestion(question: String?) {
        updateAndPreview { it.copy(careerQuestion = question, error = null) }
    }

    fun generateCareerReading() {
        val question = _uiState.value.careerQuestion
        if (question != null) return generateCareerQuestion(question)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getCareerReading()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, career = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun generateCareerQuestion(question: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getCareerQuestionReading(question)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, careerAnswer = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun generateStrengthsReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStrengthsReading()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, strengths = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setMarried(married: Boolean) {
        updateAndPreview { it.copy(married = married) }
    }

    fun generateMarriageReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.married) "married" else "single"
            when (val result = repository.getMarriageReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, marriage = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setParent(isParent: Boolean) {
        updateAndPreview { it.copy(isParent = isParent) }
    }

    fun generateChildrenReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.isParent) "parent" else "planning"
            when (val result = repository.getChildrenReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, children = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setLivesAbroad(livesAbroad: Boolean) {
        updateAndPreview { it.copy(livesAbroad = livesAbroad) }
    }

    fun generateForeignReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.livesAbroad) "abroad" else "planning"
            when (val result = repository.getForeignReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, foreign = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setOwnsBusiness(ownsBusiness: Boolean) {
        updateAndPreview { it.copy(ownsBusiness = ownsBusiness) }
    }

    fun generateWealthReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.ownsBusiness) "business" else "job"
            when (val result = repository.getWealthReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, wealth = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setAboutVehicle(aboutVehicle: Boolean) {
        updateAndPreview { it.copy(aboutVehicle = aboutVehicle) }
    }

    fun generatePropertyReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.aboutVehicle) "vehicle" else "property"
            when (val result = repository.getPropertyReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, property = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setHigherStudies(higherStudies: Boolean) {
        updateAndPreview { it.copy(higherStudies = higherStudies) }
    }

    fun generateEducationReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.higherStudies) "higher" else "student"
            when (val result = repository.getEducationReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, education = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setAboutDispute(aboutDispute: Boolean) {
        updateAndPreview { it.copy(aboutDispute = aboutDispute) }
    }

    fun generateDebtReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.aboutDispute) "dispute" else "debt"
            when (val result = repository.getDebtReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, debt = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setAboutRemarriage(aboutRemarriage: Boolean) {
        updateAndPreview { it.copy(aboutRemarriage = aboutRemarriage) }
    }

    fun generateRelationshipReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.aboutRemarriage) "remarriage" else "strain"
            when (val result = repository.getRelationshipReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, relationship = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setSmallTopic(topic: String) {
        _uiState.update { it.copy(smallTopic = topic, error = null) }
    }

    fun generateSmallReading() {
        val topic = _uiState.value.smallTopic
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSmallReading(topic)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, small = it.small + (topic to result.data))
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setAboutMind(aboutMind: Boolean) {
        updateAndPreview { it.copy(aboutMind = aboutMind) }
    }

    fun generateDashaStoryReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getDashaStoryReading()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, dashaStory = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun generateHealthReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = if (_uiState.value.aboutMind) "mind" else "body"
            when (val result = repository.getHealthReading(status)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, health = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }
}
