package com.example.engine

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Real Ultrasonic & Audio FSK modem for contactless acoustic transaction transmission.
 * Uses 17.5 kHz - 19.5 kHz Frequency-Shift Keying (BFSK):
 * - Sync/Preamble Frequency: 19,500 Hz (Frame alignment)
 * - Mark Frequency (Bit 1): 18,500 Hz
 * - Space Frequency (Bit 0): 17,500 Hz
 *
 * Includes AudioTrack streaming generation with real-time progress callbacks,
 * AudioRecord microphone listener with Goertzel frequency detection,
 * and CRC-32 checksum integrity verification against ambient noise corruption.
 */
class UltrasonicEngine(private val context: Context) {
    companion object {
        private const val TAG = "UltrasonicEngine"
        private const val SAMPLE_RATE = 44100
        private const val FREQ_SYNC = 19500.0  // 19.5 kHz preamble sync
        private const val FREQ_MARK = 18500.0  // 18.5 kHz for Bit 1
        private const val FREQ_SPACE = 17500.0 // 17.5 kHz for Bit 0
        private const val SAMPLES_PER_BIT = 441 // 10ms per symbol (100 baud)
    }

    private var activeAudioTrack: AudioTrack? = null
    private var activeAudioRecord: AudioRecord? = null
    private var listeningJob: Job? = null
    private var isTransmitting = false
    private var isListening = false

    private val _isListeningState = MutableStateFlow(false)
    val isListeningState: StateFlow<Boolean> = _isListeningState.asStateFlow()

    /**
     * Synthesizes and plays real high-frequency audio wave via device speaker.
     */
    suspend fun transmitPayloadAcoustic(
        payload: String,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            isTransmitting = true
            val payloadBytes = payload.toByteArray(StandardCharsets.UTF_8)
            val crcValue = calculateCrc32(payloadBytes)

            // Convert to bit sequence: Preamble (Sync pulses + Alternating bits) + Length + Payload + CRC32
            val bits = mutableListOf<Int>()

            // 1. Sync pulses (8 x FREQ_SYNC indicator, mapped as special code 2)
            val syncPulses = List(8) { 2 }
            bits.addAll(syncPulses)

            // 2. Alignment bit preamble (1, 0, 1, 0, 1, 0, 1, 1)
            val alignPreamble = listOf(1, 0, 1, 0, 1, 0, 1, 1)
            bits.addAll(alignPreamble)

            // 3. 16-bit Payload Length
            val payloadLength = payloadBytes.size
            for (i in 15 downTo 0) {
                bits.add((payloadLength shr i) and 1)
            }

            // 4. Payload Bytes
            for (b in payloadBytes) {
                for (i in 7 downTo 0) {
                    bits.add((b.toInt() shr i) and 1)
                }
            }

            // 5. 32-bit CRC32 Checksum
            for (i in 31 downTo 0) {
                bits.add(((crcValue.toLong() shr i) and 1L).toInt())
            }

            val totalSamples = bits.size * SAMPLES_PER_BIT
            val audioBuffer = ShortArray(totalSamples)

            var sampleIdx = 0
            val totalBitsCount = bits.size

            for ((bitIndex, bit) in bits.withIndex()) {
                if (!isTransmitting) break

                val freq = when (bit) {
                    2 -> FREQ_SYNC
                    1 -> FREQ_MARK
                    else -> FREQ_SPACE
                }

                val angularFreq = 2.0 * PI * freq / SAMPLE_RATE

                for (i in 0 until SAMPLES_PER_BIT) {
                    val angle = i * angularFreq
                    val sampleVal = (sin(angle) * Short.MAX_VALUE * 0.85).toInt().toShort()
                    if (sampleIdx < totalSamples) {
                        audioBuffer[sampleIdx++] = sampleVal
                    }
                }

                if (bitIndex % 10 == 0 || bitIndex == totalBitsCount - 1) {
                    val progress = (bitIndex + 1).toFloat() / totalBitsCount.toFloat()
                    withContext(Dispatchers.Main) {
                        onProgress(progress, "Transmitting Ultrasonic Pulse ${(progress * 100).toInt()}% (17.5-19.5 kHz)")
                    }
                }
            }

            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, audioBuffer.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBufferSize, audioBuffer.size * 2),
                    AudioTrack.MODE_STATIC
                )
            }

            activeAudioTrack = track
            track.write(audioBuffer, 0, audioBuffer.size)
            track.play()

            val totalDurationMs = (totalSamples * 1000L) / SAMPLE_RATE + 200L
            kotlinx.coroutines.delay(totalDurationMs)

            withContext(Dispatchers.Main) {
                onProgress(1.0f, "Soundwave transmission complete")
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}

            isTransmitting = false
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed acoustic soundwave generation: ${e.message}", e)
            isTransmitting = false
            false
        }
    }

    /**
     * Listens continuously via AudioRecord microphone input.
     * Uses Goertzel frequency filter to detect 17.5kHz/18.5kHz/19.5kHz tones,
     * computes real-time Audio RMS level, decodes bitstream, and validates CRC-32.
     */
    fun startListeningAcoustic(
        onAudioLevel: (Float) -> Unit,
        onResult: (isSuccess: Boolean, rawPayload: String, statusMessage: String) -> Unit
    ) {
        stopListening()
        isListening = true
        _isListeningState.value = true

        listeningJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                val minBufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val bufferSize = maxOf(minBufferSize, SAMPLES_PER_BIT * 4)
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "", "AudioRecord failed to initialize")
                    }
                    return@launch
                }

                activeAudioRecord = recorder
                recorder.startRecording()

                val frameBuffer = ShortArray(SAMPLES_PER_BIT)
                val bitStream = mutableListOf<Int>()
                var isSyncDetected = false
                var syncFrameCount = 0

                while (isListening) {
                    val readCount = recorder.read(frameBuffer, 0, SAMPLES_PER_BIT)
                    if (readCount < SAMPLES_PER_BIT) continue

                    // Calculate RMS Audio Level for UI Energy Meter
                    var sumSquare = 0.0
                    for (s in frameBuffer) {
                        sumSquare += s.toDouble() * s.toDouble()
                    }
                    val rms = sqrt(sumSquare / frameBuffer.size)
                    val normalizedLevel = (rms / 32768.0 * 8.0).coerceIn(0.0, 1.0).toFloat()

                    withContext(Dispatchers.Main) {
                        onAudioLevel(normalizedLevel)
                    }

                    // Compute Goertzel energies for target frequencies
                    val syncEnergy = computeGoertzelEnergy(frameBuffer, FREQ_SYNC)
                    val markEnergy = computeGoertzelEnergy(frameBuffer, FREQ_MARK)
                    val spaceEnergy = computeGoertzelEnergy(frameBuffer, FREQ_SPACE)

                    val maxEnergy = maxOf(syncEnergy, maxOf(markEnergy, spaceEnergy))

                    if (!isSyncDetected) {
                        // Look for 19.5 kHz Sync tone
                        if (syncEnergy > 0.01 && syncEnergy > markEnergy * 1.5 && syncEnergy > spaceEnergy * 1.5) {
                            syncFrameCount++
                            if (syncFrameCount >= 3) {
                                isSyncDetected = true
                                syncFrameCount = 0
                                bitStream.clear()
                            }
                        } else {
                            syncFrameCount = 0
                        }
                    } else {
                        // Decode FSK Bit (18.5 kHz = 1, 17.5 kHz = 0)
                        if (maxEnergy < 0.0005) {
                            // Signal lost or background silence
                            continue
                        }

                        val bit = if (markEnergy >= spaceEnergy) 1 else 0
                        bitStream.add(bit)

                        // Check if we have collected preamble (8 bits) + 16-bit length + minimum bytes + 32-bit CRC
                        if (bitStream.size >= 24) {
                            // Read Length (bits 8..23)
                            var len = 0
                            for (i in 8..23) {
                                len = (len shl 1) or bitStream[i]
                            }

                            // Total expected bits = 8 (align preamble) + 16 (length) + len * 8 (payload) + 32 (CRC32)
                            val expectedTotalBits = 24 + (len * 8) + 32
                            if (len in 1..400 && bitStream.size >= expectedTotalBits) {
                                // Extract Payload Bytes
                                val payloadBytes = ByteArray(len)
                                var bitIdx = 24
                                for (byteIdx in 0 until len) {
                                    var b = 0
                                    for (bBit in 0 until 8) {
                                        b = (b shl 1) or bitStream[bitIdx++]
                                    }
                                    payloadBytes[byteIdx] = b.toByte()
                                }

                                // Extract 32-bit CRC32
                                var rxCrc = 0L
                                for (cBit in 0 until 32) {
                                    rxCrc = (rxCrc shl 1) or bitStream[bitIdx++].toLong()
                                }

                                val calculatedCrc = calculateCrc32(payloadBytes)

                                isListening = false
                                recorder.stop()
                                recorder.release()
                                activeAudioRecord = null

                                if (rxCrc == calculatedCrc) {
                                    val decodedStr = String(payloadBytes, StandardCharsets.UTF_8)
                                    withContext(Dispatchers.Main) {
                                        onResult(true, decodedStr, "Ultrasonic soundwave CRC-32 validated successfully!")
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        onResult(false, "", "Acoustic Checksum Failed: Corrupted by ambient noise. Hold phones within 1 foot.")
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "AudioRecord listening error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "", "Microphone listening error: ${e.message}")
                }
            }
        }
    }

    /**
     * Goertzel Filter for single frequency energy detection in PCM frame.
     */
    private fun computeGoertzelEnergy(buffer: ShortArray, targetFreq: Double): Double {
        val numSamples = buffer.size
        val k = (0.5 + (numSamples * targetFreq / SAMPLE_RATE)).toInt()
        val w = (2.0 * PI * k) / numSamples
        val coeff = 2.0 * cos(w)
        var q0 = 0.0
        var q1 = 0.0
        var q2 = 0.0

        for (sample in buffer) {
            val normalizedSample = sample.toDouble() / 32768.0
            q0 = coeff * q1 - q2 + normalizedSample
            q2 = q1
            q1 = q0
        }

        val energy = q1 * q1 + q2 * q2 - q1 * q2 * coeff
        return energy / numSamples
    }

    private fun calculateCrc32(data: ByteArray): Long {
        val crc = CRC32()
        crc.update(data)
        return crc.value
    }

    fun stopTransmission() {
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
            activeAudioTrack = null
        } catch (_: Exception) {}
        isTransmitting = false
    }

    fun stopListening() {
        isListening = false
        _isListeningState.value = false
        listeningJob?.cancel()
        listeningJob = null
        try {
            activeAudioRecord?.stop()
            activeAudioRecord?.release()
            activeAudioRecord = null
        } catch (_: Exception) {}
    }
}
