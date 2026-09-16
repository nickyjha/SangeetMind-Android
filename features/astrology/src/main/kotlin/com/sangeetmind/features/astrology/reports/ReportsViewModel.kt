package com.sangeetmind.features.astrology.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.payments.PaymentsRepository
import com.sangeetmind.libs.models.BirthDetailsPayload
import com.sangeetmind.libs.models.MyReportItem
import com.sangeetmind.libs.models.Sku
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = false,
    val skus: List<Sku> = emptyList(),
    val myReports: List<MyReportItem> = emptyList(),
    val activeJobId: String? = null,
    val activeSkuId: String? = null,
    val activeStatus: String? = null,
    val pdfUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val kundliRepository: KundliRepository,
    private val paymentsRepository: PaymentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadSkus()
        loadMyReports()
    }

    private fun loadSkus() {
        viewModelScope.launch {
            when (val result = paymentsRepository.getSkus("one_time")) {
                is Result.Success -> _uiState.update {
                    it.copy(skus = result.data.filter { sku -> sku.skuId.startsWith("report_") })
                }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadMyReports() {
        viewModelScope.launch {
            when (val result = reportsRepository.listMine()) {
                is Result.Success -> _uiState.update { it.copy(myReports = result.data) }
                is Result.Error -> {
                    // Keep shop usable even if history fails; surface message once.
                    if (_uiState.value.error == null) {
                        _uiState.update { it.copy(error = result.message) }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun purchase(sku: Sku) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, pdfUri = null) }
            val kundliResult = kundliRepository.listKundlis()
            val kundlis = (kundliResult as? Result.Success)?.data
            val primary = kundlis?.firstOrNull { it.isPrimary } ?: kundlis?.firstOrNull()
            if (primary == null) {
                _uiState.update { it.copy(isLoading = false, error = "Add a kundli first") }
                return@launch
            }
            val birthDetails = BirthDetailsPayload(
                date = primary.birthDate,
                time = primary.birthTime,
                place = primary.birthPlace,
                lat = primary.latitude,
                lon = primary.longitude,
                timezone = primary.timezone
            )
            when (val result = reportsRepository.purchase(sku.skuId, birthDetails)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            activeJobId = result.data.jobId,
                            activeSkuId = sku.skuId,
                            activeStatus = result.data.status
                        )
                    }
                    pollStatus(result.data.jobId)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun pollStatus(jobId: String) {
        viewModelScope.launch {
            repeat(15) {
                when (val result = reportsRepository.getStatus(jobId)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(activeStatus = result.data.status) }
                        if (result.data.status == "completed" || result.data.status == "failed") {
                            _uiState.update { it.copy(isLoading = false) }
                            loadMyReports()
                            return@launch
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        return@launch
                    }
                    is Result.Loading -> Unit
                }
                delay(2000)
            }
            _uiState.update { it.copy(isLoading = false) }
            loadMyReports()
        }
    }

    fun viewPdf(jobId: String? = null, skuId: String? = null) {
        val resolvedJobId = jobId ?: _uiState.value.activeJobId ?: return
        val resolvedSkuId = skuId ?: _uiState.value.activeSkuId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = reportsRepository.downloadAndGetViewUri(resolvedJobId, resolvedSkuId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, pdfUri = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearPdfUri() {
        _uiState.update { it.copy(pdfUri = null) }
    }
}
