package com.example.firstproject.ui.ai.eye

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.example.firstproject.ui.ai.LetterboxInfo
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel.MapMode
import kotlin.math.exp
import kotlin.math.min

class EyeDetector(
    private val modelPath: String,
    private val isQuantized: Boolean = false,
) {
    private lateinit var interpreter: Interpreter
    private var gpuDelegate: GpuDelegate? = null

    // ※ 모델에 맞는 출력 quantization 파라미터를 설정하세요.
    companion object {
        private const val MODEL_INPUT_WIDTH = 384
        private const val MODEL_INPUT_HEIGHT = 640

        // 양자화 파라미터
        private const val OUTPUT_SCALE = 2.8370347f
        private const val OUTPUT_ZERO_POINT = -115

        // 감정 검출 임계값 및 NMS 파라미터
        private const val EXPRESSION_THRESHOLD = 0.3f
        private const val NMS_THRESHOLD = 0.3f

        // 후보 개수 (모델에 따라 조정)
        private const val CANDIDATE_COUNT = 5040

    }

    private var inputBufferSize =
        if (isQuantized) {
            1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3
        } else {
            1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3 * 4
        }

    private var inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(inputBufferSize).apply {
        order(ByteOrder.nativeOrder())
    }
    private val pixels = IntArray(MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT)

    private val letterboxBitmap =
        Bitmap.createBitmap(MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT, Bitmap.Config.ARGB_8888)
    private val letterboxCanvas = Canvas(letterboxBitmap)


    private val expressions = arrayOf("left_true", "left_false", "right_true", "right_false")


    fun loadModel(assetManager: AssetManager) {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val mappedByteBuffer = fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )

        // Interpreter 옵션 설정
        val options = Interpreter.Options()
        if (isQuantized) {
            options.setUseNNAPI(true)
        } else {
            gpuDelegate = GpuDelegate(
                GpuDelegate.Options().apply {
                    inferencePreference = GpuDelegate.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
                }
            )
            options.addDelegate(gpuDelegate)
        }
        options.setNumThreads(6)


        // Interpreter 초기화
        interpreter = Interpreter(mappedByteBuffer, options)

        // 입력 버퍼 재할당
        inputBufferSize = if (isQuantized) {
            1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3
        } else {
            1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3 * 4
        }
        inputBuffer = ByteBuffer.allocateDirect(inputBufferSize).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    fun detect(bitmap: Bitmap): EyeDetectionResult {
        return detect(bitmap, viewMatrix = null)
    }

    /**
     * viewMatrix를 전달하면, 원본 좌표 → 화면(뷰) 좌표로 변환하여 반환
     */
    fun detect(bitmap: Bitmap, viewMatrix: Matrix?): EyeDetectionResult {
        // 전처리
        val lbInfo = preprocessBitmapToBuffer(bitmap)

        val floatOutput: Array<Array<FloatArray>> = if (isQuantized) {
            // 출력 텐서의 사이즈: [1, 8, 5040]
            val outputSize = 1 * 8 * CANDIDATE_COUNT
            val outputByteBuffer = ByteBuffer.allocateDirect(outputSize).apply {
                order(ByteOrder.nativeOrder())
            }
            // 추론 실행
            interpreter.run(inputBuffer, outputByteBuffer)

            // ByteBuffer → float 배열 변환
            outputByteBuffer.rewind()
            val outArray = Array(1) { Array(8) { FloatArray(CANDIDATE_COUNT) } }
            for (i in 0 until 1) {
                for (j in 0 until 8) {
                    for (k in 0 until CANDIDATE_COUNT) {
                        val quantized = outputByteBuffer.get().toInt() and 0xFF
                        outArray[i][j][k] = (quantized - OUTPUT_ZERO_POINT) * OUTPUT_SCALE
                    }
                }
            }
            outArray
        } else {
            // 비양자화된 모델: float 배열을 직접 출력 버퍼로 사용
            val outputShape = arrayOf(1, 8, 5040)

            val outBuffer =
                Array(outputShape[0]) {
                    Array(outputShape[1]) { FloatArray(outputShape[2]) }
                }
            interpreter.run(inputBuffer, outBuffer)
            outBuffer
        }

        val detections = postProcess(
            output = floatOutput,
            lbInfo = lbInfo,
            originalWidth = bitmap.width,
            originalHeight = bitmap.height,
            viewMatrix = viewMatrix
        )
        val nmsDetections = nonMaximumSuppression(detections, NMS_THRESHOLD)

        return EyeDetectionResult(nmsDetections)

    }

    // 후처리: 후보 필터링, 박스 확장 및 NMS 적용
    private fun postProcess(
        output: Array<Array<FloatArray>>,
        lbInfo: LetterboxInfo,
        originalWidth: Int,
        originalHeight: Int,
        viewMatrix: Matrix? = null
    ): List<EyeDetection> {
        val detections = mutableListOf<EyeDetection>()
        val candidateCount = output[0][0].size

        // 각 후보에 대해 처리
        for (i in 0 until candidateCount) {
            val cx = output[0][0][i] * MODEL_INPUT_WIDTH
            val cy = output[0][1][i] * MODEL_INPUT_HEIGHT
            val bw = output[0][2][i] * MODEL_INPUT_WIDTH
            val bh = output[0][3][i] * MODEL_INPUT_HEIGHT

            // letterbox 좌표 -> 원본 좌표 변환
            val x1 = cx - bw / 2f
            val y1 = cy - bh / 2f
            val x2 = x1 + bw
            val y2 = y1 + bh
//            Log.d("TAG", "postProcess: $x1, $y1, $x2, $y2")

            val letterboxRect = RectF(x1, y1, x2, y2)

            var originalRect = letterboxToOriginalCoords(letterboxRect, lbInfo, viewMatrix)
            val offsetY = 180f
            val expandLeft = 10f  // 왼쪽을 줄이는 값

            originalRect = RectF(
                originalRect.left + expandLeft,
                originalRect.top - offsetY,
                originalRect.right ,
                originalRect.bottom - offsetY
            )
            originalRect = expandBoundingBox(originalRect, factorX = 1.2f, factorY = 1.1f)

            val exprLogits = floatArrayOf(
                output[0][4][i],
                output[0][5][i],
                output[0][6][i],
                output[0][7][i]
            )

            // softmax 확률 계산
            val exprScores = softmax(exprLogits)
            val maxScore = exprScores.maxOrNull() ?: 0f
            val maxIndex = exprScores.indexOfFirst { it == maxScore }

            if (maxScore > EXPRESSION_THRESHOLD) {

                detections.add(
                    EyeDetection(
                        expression = expressions[maxIndex],
                        score = maxScore,
                        box = originalRect
                    )
                )
            }
        }
        return detections
    }

    private fun preprocessBitmapToBuffer(original: Bitmap): LetterboxInfo {
        val lbInfo = letterboxResize(original, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT)
        inputBuffer.rewind()
        lbInfo.bitmap.getPixels(
            pixels,
            0,
            MODEL_INPUT_WIDTH,
            0,
            0,
            MODEL_INPUT_WIDTH,
            MODEL_INPUT_HEIGHT
        )
        if (isQuantized) {
            // 각 픽셀의 R, G, B 값을 1바이트씩 저장
            for (pixel in pixels) {
                val r = ((pixel shr 16) and 0xFF).toByte()
                val g = ((pixel shr 8) and 0xFF).toByte()
                val b = (pixel and 0xFF).toByte()
                inputBuffer.put(r)
                inputBuffer.put(g)
                inputBuffer.put(b)
            }
        } else {
            // float 모델의 경우 [0,1]로 정규화하여 저장
            val floatBuffer = inputBuffer.asFloatBuffer()
            for (pixel in pixels) {
                val r = ((pixel shr 16) and 0xFF).toFloat() / 255f
                val g = ((pixel shr 8) and 0xFF).toFloat() / 255f
                val b = (pixel and 0xFF).toFloat() / 255f
                floatBuffer.put(r)
                floatBuffer.put(g)
                floatBuffer.put(b)
            }
        }
        inputBuffer.rewind()
        return lbInfo
    }

    // letterboxResize: Bitmap을 640×640 letterbox 이미지로 변환
    private fun letterboxResize(src: Bitmap, targetWidth: Int, targetHeight: Int): LetterboxInfo {
        val srcWidth = src.width
        val srcHeight = src.height

        val scale = min(targetWidth.toFloat() / srcWidth, targetHeight.toFloat() / srcHeight)
        val newWidth = (srcWidth * scale).toInt()
        val newHeight = (srcHeight * scale).toInt()

        val padLeft = (targetWidth - newWidth) / 2f
        val padTop = (targetHeight - newHeight) / 2f

        letterboxCanvas.drawColor(Color.BLACK)
        val dstRect = RectF(padLeft, padTop, padLeft + newWidth, padTop + newHeight)
        letterboxCanvas.drawBitmap(src, null, dstRect, null)

        return LetterboxInfo(letterboxBitmap, scale, padLeft, padTop)
    }


    /**
     * letterbox 좌표를 원본 이미지 좌표로 복원하고,
     * 선택적으로 viewMatrix가 있다면 화면 좌표로 변환
     */
    private fun letterboxToOriginalCoords(
        box: RectF,
        lbInfo: LetterboxInfo,
        viewMatrix: Matrix? = null
    ): RectF {
        val x1 = (box.left - lbInfo.padLeft) / lbInfo.scale
        val y1 = (box.top - lbInfo.padTop) / lbInfo.scale
        val x2 = (box.right - lbInfo.padLeft) / lbInfo.scale
        val y2 = (box.bottom - lbInfo.padTop) / lbInfo.scale
        val origRect = RectF(x1, y1, x2, y2)

        if (viewMatrix != null) {
            val pts = floatArrayOf(origRect.left, origRect.top, origRect.right, origRect.bottom)
            viewMatrix.mapPoints(pts)
            return RectF(pts[0], pts[1], pts[2], pts[3])
        }
        return origRect
    }

    /**
     * 좌우 반전: 주어진 rect를 원본 이미지 폭을 기준으로 좌우 반전
     */


    // Non-Maximum Suppression (NMS): IoU 기반 간단한 방식
    private fun nonMaximumSuppression(
        detections: List<EyeDetection>,
        iouThreshold: Float
    ): List<EyeDetection> {
        if (detections.isEmpty()) return emptyList()

        val sorted = detections.sortedByDescending { it.score }.toMutableList()
        val finalDetections = mutableListOf<EyeDetection>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            finalDetections.add(best)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val other = iterator.next()
                if (computeIoU(best.box, other.box) > iouThreshold) {
                    iterator.remove()
                }
            }
        }
        return finalDetections
    }


    private fun computeIoU(a: RectF, b: RectF): Float {
        val areaA = a.width() * a.height()
        val areaB = b.width() * b.height()
        if (areaA <= 0f || areaB <= 0f) return 0f

        val interLeft = maxOf(a.left, b.left)
        val interTop = maxOf(a.top, b.top)
        val interRight = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)
        val intersection = maxOf(0f, interRight - interLeft) * maxOf(0f, interBottom - interTop)
        return intersection / (areaA + areaB - intersection)
    }

    fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sum = exps.sum()
        return exps.map { it / sum }.toFloatArray()
    }

    private fun expandBoundingBox(box: RectF, factorX: Float, factorY: Float): RectF {
        val centerX = box.centerX()
        val centerY = box.centerY()
        val newWidth = box.width() * factorX
        val newHeight = box.height() * factorY
        return RectF(
            centerX - newWidth / 2,
            centerY - newHeight / 2,
            centerX + newWidth / 2,
            centerY + newHeight / 2
        )
    }

    fun close() {
        interpreter.close()
        gpuDelegate?.close()
    }
}