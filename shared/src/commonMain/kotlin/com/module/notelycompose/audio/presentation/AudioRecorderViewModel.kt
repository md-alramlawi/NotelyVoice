package com.module.notelycompose.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.audio.ui.recorder.AudioRecorderUiState
import com.module.notelycompose.audio.domain.AudioRecorderInteractor
import com.module.notelycompose.audio.domain.AudioRecorderPresentationState
import kotlinx.coroutines.flow.StateFlow

class AudioRecorderViewModel(
    private val interactor: AudioRecorderInteractor
) : ViewModel() {
    val audioRecorderPresentationState: StateFlow<AudioRecorderPresentationState> =
        interactor.state

    fun onStartRecording(noteId: Long?, updateUI: () -> Unit) {
        interactor.initState()
        interactor.onStartRecording(noteId,viewModelScope, updateUI)
    }

    fun onStopRecording() {
        interactor.onStopRecording(viewModelScope)
    }

    fun setupRecorder() {
        interactor.setupRecorder(viewModelScope)
    }

    fun finishRecorder() {
        interactor.finishRecorder(viewModelScope)
    }

    fun onPauseRecording() {
        interactor.onPauseRecording(viewModelScope)
    }

    fun onResumeRecording() {
        interactor.onResumeRecording(viewModelScope)
    }

    override fun onCleared() {
        interactor.onCleared()
    }

    fun onRequestAudioPermission() {
        interactor.onRequestAudioPermission(viewModelScope)
    }

    fun onGetUiState(presentationState: AudioRecorderPresentationState): AudioRecorderUiState {
        return interactor.onGetUiState(presentationState)
    }
}
