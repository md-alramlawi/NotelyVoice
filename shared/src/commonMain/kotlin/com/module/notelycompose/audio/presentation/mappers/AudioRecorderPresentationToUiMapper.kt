package com.module.notelycompose.audio.presentation.mappers

import com.module.notelycompose.audio.domain.AudioRecorderPresentationState
import com.module.notelycompose.audio.ui.recorder.AudioRecorderUiState

class AudioRecorderPresentationToUiMapper {
    fun mapToUiState(presentationState: AudioRecorderPresentationState): AudioRecorderUiState {
        return AudioRecorderUiState(
            recordCounterString = presentationState.recordCounterString,
            recordingPath = presentationState.recordingPath,
            isRecordPaused = presentationState.isRecordPaused
        )
    }
}
