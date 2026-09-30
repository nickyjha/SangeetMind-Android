package com.sangeetmind.features.astrology.readings

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
import com.sangeetmind.libs.models.PropertyReadingResponse
import com.sangeetmind.libs.models.WealthReadingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val CAREER_QUESTIONS = listOf("job_change", "promotion", "govt_private", "job_business")

enum class ReadingTab { CAREER, STRENGTHS, MARRIAGE, CHILDREN, FOREIGN, WEALTH, PROPERTY, EDUCATION, DEBT, RELATIONSHIP }

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
    val relationship: RelationshipReadingResponse? = null,
    val aboutRemarriage: Boolean = false,
    val debt: DebtReadingResponse? = null,
    val aboutDispute: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val repository: ReadingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingsUiState())
    val uiState: StateFlow<ReadingsUiState> = _uiState.asStateFlow()

    fun setTab(tab: ReadingTab) {
        _uiState.update { it.copy(tab = tab, error = null) }
    }

    fun setCareerQuestion(question: String?) {
        _uiState.update { it.copy(careerQuestion = question, error = null) }
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
        _uiState.update { it.copy(married = married) }
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
        _uiState.update { it.copy(isParent = isParent) }
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
        _uiState.update { it.copy(livesAbroad = livesAbroad) }
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
        _uiState.update { it.copy(ownsBusiness = ownsBusiness) }
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
        _uiState.update { it.copy(aboutVehicle = aboutVehicle) }
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
        _uiState.update { it.copy(higherStudies = higherStudies) }
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
        _uiState.update { it.copy(aboutDispute = aboutDispute) }
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
        _uiState.update { it.copy(aboutRemarriage = aboutRemarriage) }
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
}
