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
    // 1. studyId에 해당하는 메시지를 생성시간 기준 오름차순으로 조회합니다.
    // userId 필드를 populate하여, 해당 사용자의 image 정보를 가져옵니다.
    const messages = await Message.find({ studyId }).populate({ path: "userId", select: "image" }).sort({ createdAt: 1 })
    console.log(`메시지 내역 조회 - 스터디 ID: ${studyId}, 조회된 메시지 수: ${messages.length}`)

    // 2. 각 메시지에서 userId가 populate된 경우, 해당 User의 image로 message.image 필드를 업데이트합니다.
    const messagesWithUserImage = messages.map((msg) => {
      const msgObj = msg.toObject()
      // userId가 populate되어 있다면 해당 User의 image를 추가 (없으면 빈 문자열로 설정)
      msgObj.image = msgObj.userId && msgObj.userId.image ? msgObj.userId.image : ""
      return msgObj
    })

    res.status(200).json(messagesWithUserImage)
  } catch (err) {
    console.error("메시지 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

module.exports = router
