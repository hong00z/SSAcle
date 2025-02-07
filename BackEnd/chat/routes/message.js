// routes/chat.js
const express = require("express")
const router = express.Router()
const Message = require("../models/Message")

/**
 * @openapi
 * /api/chat/{studyId}/messages:
 *   get:
 *     tags:
 *       - Chat
 *     summary: 특정 채팅방의 메시지 내역 조회
 *     description: 주어진 studyId에 해당하는 채팅방의 메시지를 생성 시간 기준 오름차순으로 반환합니다.
 *     parameters:
 *       - in: path
 *         name: studyId
 *         required: true
 *         schema:
 *           type: string
 *         description: 채팅방(스터디)의 고유 ID
 *     responses:
 *       200:
 *         description: 채팅 메시지 목록 반환
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/Message'
 *       500:
 *         description: 서버 에러
 */
router.get("/:studyId/messages", async (req, res) => {
  const { studyId } = req.params
  try {
    const messages = await Message.find({ studyId }).sort({ createdAt: 1 })
    console.log(`메시지 내역 조회 - 스터디 ID: ${studyId}, 조회된 메시지 수: ${messages.length}`)
    res.status(200).json(messages)
  } catch (err) {
    console.error("메시지 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

module.exports = router
