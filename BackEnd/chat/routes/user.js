// routes/user.js
const express = require("express")
const router = express.Router()
const User = require("../models/User")

/**
 * @openapi
 * /api/users/{userId}:
 *   get:
 *     tags:
 *       - User
 *     summary: 사용자 데이터 조회
 *     description: 주어진 userId에 해당하는 사용자의 상세 정보를 반환합니다.
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 사용자 고유 ID
 *     responses:
 *       200:
 *         description: 사용자 정보 반환
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/User'
 *       404:
 *         description: 사용자를 찾을 수 없음
 */
router.get("/:userId", async (req, res) => {
  try {
    const user = await User.findById(req.params.userId)
    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }
    res.status(200).json(user)
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

/**
 * @openapi
 * /api/users/{userId}/token:
 *   post:
 *     tags:
 *       - User
 *     summary: 사용자 토큰 업데이트
 *     description: 주어진 userId에 해당하는 사용자의 FCM 토큰을 저장하거나 업데이트합니다.
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 사용자 고유 ID
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               fcmToken:
 *                 type: string
 *                 description: FCM 토큰 값
 *     responses:
 *       200:
 *         description: 토큰 등록 성공 메시지 반환
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post("/:userId/token", async (req, res) => {
  const { userId } = req.params
  const { fcmToken } = req.body
  console.log(`토큰 등록 요청: userId=${userId}, fcmToken=${fcmToken}`)
  res.status(200).json({ message: "토큰 등록 성공" })
})

/**
 * @openapi
 * /api/users/{userId}/studies:
 *   get:
 *     tags:
 *       - User
 *     summary: 사용자가 가입한 스터디 조회
 *     description: 주어진 userId가 가입한 스터디 목록을 반환합니다.
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 사용자 고유 ID
 *     responses:
 *       200:
 *         description: 가입한 스터디 목록 반환
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/Study'
 *       500:
 *         description: 서버 에러
 */
router.get("/:userId/studies", async (req, res) => {
  const { userId } = req.params
  try {
    const user = await User.findById(userId).populate("joinedStudies")
    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }
    console.log(`사용자 ${userId}의 가입한 스터디 조회 - 총 ${user.joinedStudies.length}개`)
    res.status(200).json(user.joinedStudies)
  } catch (err) {
    console.error("가입한 스터디 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

module.exports = router
