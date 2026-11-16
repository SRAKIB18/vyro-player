package com.vyro.player.presentation.decoder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.DecoderCapability
import com.vyro.player.domain.usecase.GetDecoderCapabilitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DecoderInfoUiState(
    val decoders: List<DecoderCapability> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DecoderInfoViewModel @Inject constructor(
    private val getDecoderCapabilitiesUseCase: GetDecoderCapabilitiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DecoderInfoUiState())
    val uiState: StateFlow<DecoderInfoUiState> = _uiState.asStateFlow()

    init {
        loadDecoders()
    }

    private fun loadDecoders() {
        viewModelScope.launch {
            _uiState.value = DecoderInfoUiState(isLoading = true)
            val decoders = getDecoderCapabilitiesUseCase()
            _uiState.value = DecoderInfoUiState(decoders = decoders, isLoading = false)
        }
    }
}
