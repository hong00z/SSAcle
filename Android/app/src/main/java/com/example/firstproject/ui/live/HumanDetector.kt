package com.example.firstproject.ui.live

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.example.firstproject.ui.ai.LetterboxInfo
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HumanDetector(
    private val modelPath: String,
    private val isQuantized: Boolean = false
) {
    private lateinit var interpreter: Interpreter
    private var gpuDelegate: GpuDelegate? = null

    fun loadModel(assetManager: AssetManager) {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val mappedByteBuffer = fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )

        val options = Interpreter.Options()

        if (isQuantized) {
            options.setUseNNAPI(true)
        } else {

            gpuDelegate = GpuDelegate(GpuDelegate.Options().apply {
                inferencePreference = GpuDelegate.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
            })
            options.addDelegate(gpuDelegate)
        }

        options.setNumThreads(4)

        interpreter = Interpreter(mappedByteBuffer, options)
    }


    // Detection
    fun detect(bitmap: Bitmap): List<HumanDetection> {

        // 1. 전처리
        val preStart = System.currentTimeMillis()
        val (inputBuffer, lbInfo) = preprocessBitmap(bitmap)
        val preEnd = System.currentTimeMillis()

        // 2. output 형태 완성
        val outputShape = arrayOf(1, 84, 8400)
        val outputBuffer = Array(outputShape[0]) {
            Array(outputShape[1]) { FloatArray(outputShape[2]) }
        }

        // 3. 모델 추론 
        interpreter.run(inputBuffer, outputBuffer)

        // 4. 후처리
        val results = postProcess(outputBuffer, lbInfo, bitmap.width, bitmap.height)


        return results
    }


    // 이미지 전처리 (Bitmap → ByteBuffer)
    private fun preprocessBitmap(original: Bitmap): Pair<ByteBuffer, LetterboxInfo> {
        val lbInfo = letterboxResize(original, 640)

        val inputBuffer = if (isQuantized) {
            ByteBuffer.allocateDirect(1 * 3 * 640 * 640)
        } else {
            ByteBuffer.allocateDirect(1 * 3 * 640 * 640 * 4)
        }
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(640 * 640)
        lbInfo.bitmap.getPixels(pixels, 0, 640, 0, 0, 640, 640)

        if (isQuantized) {
            for (pixel in pixels) {
                inputBuffer.put(((pixel shr 16) and 0xFF).toByte())
                inputBuffer.put(((pixel shr 8) and 0xFF).toByte())
                inputBuffer.put((pixel and 0xFF).toByte())
            }
        } else {
            for (pixel in pixels) {
                inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
                inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
                inputBuffer.putFloat((pixel and 0xFF) / 255f)
            }
        }

        inputBuffer.rewind()
        return Pair(inputBuffer, lbInfo)
    }


    // 640 * 640으로 변경
    fun letterboxResize(src: Bitmap, targetSize: Int = 640): LetterboxInfo {
        val srcWidth = src.width
        val srcHeight = src.height

        // scale 계산
        val scale = minOf(targetSize.toFloat() / srcWidth, targetSize.toFloat() / srcHeight)
        val newWidth = (srcWidth * scale).toInt()
        val newHeight = (srcHeight * scale).toInt()

        val letterboxBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.RGB_565)
        val canvas = Canvas(letterboxBitmap)
        canvas.drawColor(Color.BLACK)

        val padLeft = (targetSize - newWidth) / 2f
        val padTop = (targetSize - newHeight) / 2f
        val dstRect = RectF(padLeft, padTop, padLeft + newWidth, padTop + newHeight)
        canvas.drawBitmap(src, null, dstRect, null)

        return LetterboxInfo(letterboxBitmap, scale, padLeft, padTop)
    }

    // 원본으로 다시 변환
    private fun letterboxToOriginalCoords(
        box: RectF,
        lbInfo: LetterboxInfo
    ): RectF {
        val x1 = (box.left - lbInfo.padLeft) / lbInfo.scale
        val y1 = (box.top - lbInfo.padTop) / lbInfo.scale
        val x2 = (box.right - lbInfo.padLeft) / lbInfo.scale
        val y2 = (box.bottom - lbInfo.padTop) / lbInfo.scale
        return RectF(x1, y1, x2, y2)
    }


    // 후처리
    private fun postProcess(
        output: Array<Array<FloatArray>>,
        lbInfo: LetterboxInfo,
        originalWidth: Int,
        originalHeight: Int
    ): List<HumanDetection> {
        val humanDetections = mutableListOf<HumanDetection>()
        val numCandidates = output[0][0].size

        for (j in 0 until numCandidates) {
            val candidate = FloatArray(84) { i -> output[0][i][j] }
            val cx = candidate[0]
            val cy = candidate[1]
            val bw = candidate[2]
            val bh = candidate[3]
            val classScoresRaw = candidate.sliceArray(4 until 84)
            val classScores = classScoresRaw.map { sigmoid(it) }.toFloatArray()
            val personScore = classScores[PERSON_CLASS_INDEX]

            if (personScore > CONFIDENCE_THRESHOLD) {
                val x1 = cx - bw / 2
                val y1 = cy - bh / 2
                val x2 = x1 + bw
                val y2 = y1 + bh
                val letterboxRect = RectF(x1, y1, x2, y2)
                val originalRect = letterboxToOriginalCoords(letterboxRect, lbInfo)
                humanDetections.add(HumanDetection(PERSON_CLASS_INDEX, personScore, originalRect))
            }
        }

        return nonMaximumSuppression(humanDetections, NMS_THRESHOLD)
    }

    private fun sigmoid(x: Float): Float =
        (1.0f / (1.0f + kotlin.math.exp(-x.toDouble()).toFloat()))

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.6f
        private const val NMS_THRESHOLD = 0.5f
        private const val PERSON_CLASS_INDEX = 0
    }

    // Non-Maximum Suppression (NMS)
    private fun nonMaximumSuppression(
        humanDetections: List<HumanDetection>,
        iouThreshold: Float
    ): List<HumanDetection> {
        if (humanDetections.isEmpty()) return emptyList()

        val sortedDetections = humanDetections.sortedByDescending { it.score }
        val finalHumanDetections = mutableListOf<HumanDetection>()
        val deque = ArrayDeque<HumanDetection>()

        for (detection in sortedDetections) {
            var shouldSuppress = false
            for (final in finalHumanDetections) {
                if (computeIoU(detection.box, final.box) > iouThreshold) {
                    shouldSuppress = true
                    break
                }
            }
            if (!shouldSuppress) finalHumanDetections.add(detection)
        }

        return finalHumanDetections
    }

    private fun computeIoU(a: RectF, b: RectF): Float {
        val areaA = a.width() * a.height()
        val areaB = b.width() * b.height()
        if (areaA <= 0 || areaB <= 0) return 0f

        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        val intersection = maxOf(0f, right - left) * maxOf(0f, bottom - top)
        return intersection / (areaA + areaB - intersection)
    }

    fun close() {
        interpreter.close()
        gpuDelegate?.close()
    }
}