package com.example.firstproject.ui.ai.face

import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import com.example.firstproject.ui.ai.LetterboxInfo
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.min

/**
 * FaceExpressionDetector 흐름도
 * 1) loadModel
 * 2) detect (오버로드 가능)
 * 3) 전처리: letterboxResize → inputBuffer에 저장
 * 4) 모델 추론 → 후처리
 * 5) NMS 적용
 * 6) 리소스 정리: close()
 */
class FaceExpressionDetector(
    private val modelPath: String,
    private val isQuantized: Boolean = false  // 양자화
) {
    private lateinit var interpreter: Interpreter
    private var gpuDelegate: GpuDelegate? = null

    // emotion Fragment 형식에 맞게.
    companion object {
        private const val MODEL_INPUT_WIDTH = 384
        private const val MODEL_INPUT_HEIGHT = 640

        // 양자화 파라미터
        private const val OUTPUT_SCALE = 2.8370347f
        private const val OUTPUT_ZERO_POINT = -115

        // 감정 검출 임계값 및 NMS 파라미터
        private const val EXPRESSION_THRESHOLD = 0.5f
        private const val NMS_THRESHOLD = 0.3f

        // 후보 개수 (모델에 따라 조정)
        private const val CANDIDATE_COUNT = 5040
    }


    private var inputBufferSize = if (isQuantized) {
        1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3
    } else {
        1 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * 3 * 4
    }
    private var inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(inputBufferSize).apply {
        order(ByteOrder.nativeOrder())
    }
    private val pixels = IntArray(MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT)

    // 색상 RGB
    private val letterboxBitmap: Bitmap =
        Bitmap.createBitmap(MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT, Bitmap.Config.ARGB_8888)
    private val letterboxCanvas = Canvas(letterboxBitmap)

    // 감정 레이블
    private val expressions = arrayOf("Negative", "Positive", "Neutral")

    /**
     * TFLite 모델 로드
     */
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

    /**
     * 기본 detect – viewMatrix 없이 호출
     */
    fun detect(bitmap: Bitmap): FaceExpressionResult {
        return detect(bitmap, viewMatrix = null)
    }

    /**
     * viewMatrix 등을 적용한 detect
     * (EmotionFragment에서는 viewMatrix가 없는 경우가 많으므로 null로 호출)
     */
    fun detect(bitmap: Bitmap, viewMatrix: Matrix?): FaceExpressionResult {
        // 1) 전처리: 입력 Bitmap을 모델 입력 크기로 letterbox 리사이즈 후 픽셀 추출
        val lbInfo = preprocessBitmapToBuffer(bitmap)

        // 2) 모델 추론
        val floatOutput: Array<Array<FloatArray>> = if (isQuantized) {
            val outputSize = 1 * 7 * CANDIDATE_COUNT
            val outputByteBuffer = ByteBuffer.allocateDirect(outputSize).apply {
                order(ByteOrder.nativeOrder())
            }
            interpreter.run(inputBuffer, outputByteBuffer)

            outputByteBuffer.rewind()
            // quantized 값을 float 배열로 변환
            val outArray = Array(1) { Array(7) { FloatArray(CANDIDATE_COUNT) } }
            for (i in 0 until 1) {
                for (j in 0 until 7) {
                    for (k in 0 until CANDIDATE_COUNT) {
                        val quantized = outputByteBuffer.get().toInt() and 0xFF
                        outArray[i][j][k] = (quantized - OUTPUT_ZERO_POINT) * OUTPUT_SCALE
                    }
                }
            }
            outArray
        } else {
            val outputShape = arrayOf(1, 7, 5040)

            val outBuffer =
                Array(outputShape[0]) {
                    Array(outputShape[1]) { FloatArray(outputShape[2]) }
                }
            interpreter.run(inputBuffer, outBuffer)
            outBuffer
        }

        // 3) 후처리: bbox 좌표 변환 및 소프트맥스 적용 후 NMS 처리
        val detections = postProcess(
            output = floatOutput,
            lbInfo = lbInfo,
            originalWidth = bitmap.width,
            originalHeight = bitmap.height,
            viewMatrix = viewMatrix
        )
        val nmsDetections = nonMaximumSuppression(detections, NMS_THRESHOLD)

        return FaceExpressionResult(nmsDetections)
    }

    private fun postProcess(
        output: Array<Array<FloatArray>>,
        lbInfo: LetterboxInfo,
        originalWidth: Int,
        originalHeight: Int,
        viewMatrix: Matrix? = null
    ): List<FaceExpressionDetection> {
        val detections = mutableListOf<FaceExpressionDetection>()
        val candidateCount = output[0][0].size


        for (i in 0 until candidateCount) {
            // 모델 출력의 앞 4개 값: center x, center y, box width, box height
            val cx = output[0][0][i] * MODEL_INPUT_WIDTH
            val cy = output[0][1][i] * MODEL_INPUT_HEIGHT
            val bw = output[0][2][i] * MODEL_INPUT_WIDTH
            val bh = output[0][3][i] * MODEL_INPUT_HEIGHT

            val x1 = cx - bw / 2f
            val y1 = cy - bh / 2f
            val x2 = x1 + bw
            val y2 = y1 + bh

            val letterboxRect = RectF(x1, y1, x2, y2)
            // letterbox 좌표를 뷰 좌표로 변환
            var originalRect = letterboxToOriginalCoords(letterboxRect, lbInfo, viewMatrix)

            val offsetY = 50f
            originalRect = RectF(
                originalRect.left,
                originalRect.top - offsetY,
                originalRect.right,
                originalRect.bottom - offsetY
            )
            // 감정 로짓 3개 (예: Negative, Positive, Neutral)
            val exprLogits = floatArrayOf(
                output[0][4][i],
                output[0][5][i],
                output[0][6][i]
            )

            if (output[0][4][i] >= 0.1f || output[0][5][i] >= 0.1f || output[0][6][i] >= 0.1f) {

            }
            // softmax 확률 계산
            val exprScores = softmax(exprLogits)
            val maxScore = exprScores.maxOrNull() ?: 0f
            val maxIndex = exprScores.indexOfFirst { it == maxScore }

            // 임계값 이상인 경우에만 detection 추가
            if (maxScore > EXPRESSION_THRESHOLD) {

                detections.add(
                    FaceExpressionDetection(
                        expression = expressions[maxIndex],
                        score = maxScore,
                        box = originalRect
                    )
                )
            }
        }
        return detections
    }

    /**
     * 전처리: 입력 Bitmap을 letterbox 리사이즈한 후 픽셀 데이터를 입력 버퍼에 저장
     */
    private fun preprocessBitmapToBuffer(original: Bitmap): LetterboxInfo {
        // 모델 입력 크기로 letterbox 리사이즈 (targetWidth, targetHeight)
        val lbInfo = letterboxResize(original, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT)

        inputBuffer.rewind()
        lbInfo.bitmap.getPixels(
            pixels,
            0,
            MODEL_INPUT_WIDTH, //384
            0,
            0,
            MODEL_INPUT_WIDTH, // 384
            MODEL_INPUT_HEIGHT // 640
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

    /**
     * Letterbox 리사이즈
     * 입력 Bitmap을 targetWidth x targetHeight 크기로 리사이즈하며 비율 유지 후 패딩 적용
     */
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
     * Letterbox 좌표를 원본(또는 뷰) 좌표로 변환
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
     * Non-Maximum Suppression (NMS)
     */
    private fun nonMaximumSuppression(
        detections: List<FaceExpressionDetection>,
        iouThreshold: Float
    ): List<FaceExpressionDetection> {
        if (detections.isEmpty()) return emptyList()

        val sorted = detections.sortedByDescending { it.score }.toMutableList()
        val finalDetections = mutableListOf<FaceExpressionDetection>()

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

    /**
     * IoU (Intersection over Union) 계산
     */
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

    /**
     * 소프트맥스 함수
     */
    fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sum = exps.sum()
        return exps.map { it / sum }.toFloatArray()
    }

    /**
     * 리소스 정리
     */
    fun close() {
        interpreter.close()
        gpuDelegate?.close()
    }
}