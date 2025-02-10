// routes/user.js
const express = require("express")
const router = express.Router()
const User = require("../models/User")
const Message = require("../models/Message")

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

    // 각 Study마다 읽지 않은 메시지 개수를 계산하여 Study 객체에 추가
    const studiesWithUnreadCount = await Promise.all(
      user.joinedStudies.map(async (study) => {
        // 사용자의 studyReadTimestamps 배열에서 해당 Study의 마지막 읽은 시각을 찾음
        let lastReadTime = new Date(0) // 기본값: epoch (읽은 기록이 없으면 전체 메시지 모두 읽지 않음)
        if (user.studyReadTimestamps && Array.isArray(user.studyReadTimestamps)) {
          const readObj = user.studyReadTimestamps.find((item) => item.studyId.toString() === study._id.toString())
          if (readObj && readObj.lastRead) {
            lastReadTime = readObj.lastRead
          }
        }
        // 해당 Study에 대해, lastReadTime 이후에 생성된 메시지 개수를 unreadCount로 계산
        const unreadCount = await Message.countDocuments({
          studyId: study._id,
          createdAt: { $gt: lastReadTime },
        })

        // 마지막 메시지 조회 (생성시간 기준 내림차순)
        const lastMsg = await Message.findOne({ studyId: study._id }).sort({ createdAt: -1 }).exec()

        // Study 객체를 일반 객체로 변환하고, 필요한 필드를 추가
        const studyObj = study.toObject()
        if (lastMsg) {
          studyObj.lastMessage = lastMsg.message
          studyObj.lastMessageCreatedAt = lastMsg.createdAt
        } else {
          studyObj.lastMessage = ""
          studyObj.lastMessageCreatedAt = null
        }
        studyObj.unreadCount = unreadCount
        return studyObj
      })
    )

    res.status(200).json(studiesWithUnreadCount)
  } catch (err) {
    console.error("가입한 스터디 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

/**
 * 사용자의 채팅방별 마지막 읽은 시간을 업데이트하는 API
 * POST /api/users/:userId/lastRead
 * Request Body: { studyId: String, lastReadTime: Number }
 */
router.post("/:userId/lastRead", async (req, res) => {
  const { userId } = req.params
  const { studyId, lastReadTime } = req.body

  // lastReadTime은 Unix epoch 밀리초 값이므로 Date 객체로 변환
  const newLastRead = new Date(lastReadTime)

  try {
    const user = await User.findById(userId)
    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }

    // studyReadTimestamps 배열에서 해당 studyId 항목을 찾음
    const index = user.studyReadTimestamps.findIndex((entry) => entry.studyId.toString() === studyId.toString())
    if (index !== -1) {
      // 이미 존재하면 lastRead를 업데이트
      console.log("기존에 추가 -> ", index)
      user.studyReadTimestamps[index].lastRead = newLastRead
    } else {
      // 없으면 새 항목을 추가
      console.log("새로 만들기 ->", studyId)
      user.studyReadTimestamps.push({ studyId: studyId, lastRead: newLastRead })
    }

    await user.save()
    return res.status(200).json({ message: "마지막 읽은 시간이 업데이트되었습니다." })
  } catch (error) {
    console.error("마지막 읽은 시간 업데이트 중 오류 발생:", error)
    return res.status(500).json({ error: error.message })
  }
})

module.exports = router
