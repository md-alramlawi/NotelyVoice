package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import kotlinx.cinterop.*
import kotlinx.cinterop.nativeHeap.alloc
import platform.Foundation.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import kotlin.coroutines.resume
import kotlin.random.Random
import platform.AVFAudio.*
import platform.CoreAudioTypes.kAudioFormatLinearPCM

private const val RECORDING_PREFIX = "recording_"
private const val RECORDING_EXTENSION = ".wav"

actual class AudioRecorder {

    private var audioRecorder: AVAudioRecorder? = null
    private var recordingSession: AVAudioSession = AVAudioSession.sharedInstance()
    private lateinit var recordingURL: NSURL
    private var isCurrentlyPaused = false

    /**
     * Call when entering recording screen
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun setup() {
        try {
            recordingSession.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
                null
            )
            recordingSession.setActive(true, null)
        } catch (e: Exception) {
            debugPrintln{"Audio session setup failed: ${e.message}"}
        }
    }

    /**
     * Call when leaving recording screen
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun teardown() {
        // 1. Stop any active recording
        if (isRecording()) {
            stopRecording()
        }

        // 2. Deactivate audio session
        try {
            recordingSession.setActive(false, null)
        } catch (e: Exception) {
            debugPrintln{"Audio session teardown failed: ${e.message}"}
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun startRecording() {
        // 1. Request permissions early
        if (!hasRecordingPermission()) {
            debugPrintln{"Recording permission not granted"}
            return
        }

        val randomNumber = Random.nextInt(100000, 999999)
        val fileName = "$RECORDING_PREFIX$randomNumber$RECORDING_EXTENSION"

        debugPrintln{"Start Recording: $fileName"}
        val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL

        this.recordingURL = documentsDirectory.URLByAppendingPathComponent(fileName) ?: run {
            debugPrintln{"Failed to create recording URL"}
            return
        }

        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatLinearPCM,
            AVSampleRateKey to 16000.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to AVAudioQualityHigh,
        )
            audioRecorder = AVAudioRecorder(recordingURL, settings, null)
            if (audioRecorder?.prepareToRecord() == true) {
                val isRecording = audioRecorder?.record()
                isCurrentlyPaused = false
                debugPrintln{"Recording started successfully $isRecording"}
            } else {
                debugPrintln{"Failed to prepare recording"}
                audioRecorder = null
            }

    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopRecording() {
        audioRecorder?.let { recorder ->
            if (recorder.isRecording()) {
                recorder.stop()
            }
        }

        audioRecorder = null
        isCurrentlyPaused = false
    }

    actual fun isRecording(): Boolean {
        return audioRecorder?.isRecording() ?: false
    }

    actual fun hasRecordingPermission(): Boolean {
        return recordingSession.recordPermission() == AVAudioSessionRecordPermissionGranted
    }

    actual suspend fun requestRecordingPermission() : Boolean {
        if (hasRecordingPermission()) return true

        return suspendCancellableCoroutine { continuation ->
            recordingSession.requestRecordPermission { granted ->
                continuation.resume(granted)
            }
        }
    }

    actual fun getRecordingFilePath(): String {
        return recordingURL.path.orEmpty()
    }

    actual fun pauseRecording() {
        if (isRecording() && !isCurrentlyPaused) {
            audioRecorder?.let { recorder ->
                recorder.pause()
                isCurrentlyPaused = true
                debugPrintln{"Recording paused successfully"}
            }
        }
    }

    actual fun resumeRecording() {
        if (isCurrentlyPaused) {
            audioRecorder?.let { recorder ->
                recorder.record()
                isCurrentlyPaused = false
                debugPrintln{"Recording resumed successfully"}
            }
        }
    }

    actual fun isPaused(): Boolean {
        return isCurrentlyPaused
    }
}