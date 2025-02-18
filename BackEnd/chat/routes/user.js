// routes/user.js
const express = require("express")
const router = express.Router()
const User = require("../models/User")
const Message = require("../models/Message")

/**
 * @openapi
 * /api/users/{userId}/fcmToken:
 *   post:
 *     tags:
 *       - User
 *     summary: 사용자 FCM 토큰 업데이트
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
 *         description: FCM 토큰 등록 성공 메시지 반환
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post("/:userId/fcmToken", async (req, res) => {
  const { userId } = req.params
  const { fcmToken } = req.body
  console.log(`FCM 토큰 등록 요청: userId=${userId}, fcmToken=${fcmToken}`)

  try {
    // userId로 사용자 검색
    const user = await User.findById(userId)
    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }

    // fcmToken 업데이트 (또는 새로 등록)
    user.fcmToken = fcmToken
    await user.save()

    return res.status(200).json({ message: "토큰 등록 성공" })
  } catch (error) {
    console.error("토큰 저장 중 오류 발생:", error)
    return res.status(500).json({ error: error.message })
  }
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
    // 1. User를 찾고, joinedStudies 필드를 populate합니다.
    //    joinedStudies의 각 Study에서 members 배열도 populate하여, User의 nickname과 image만 선택합니다.
    const user = await User.findById(userId).populate({
      path: "joinedStudies",
      populate: {
        path: "members",
        select: "nickname image",
      },
    })

    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }
    console.log(`사용자 ${userId}의 가입한 스터디 조회 - 총 ${user.joinedStudies.length}개`)

    // 2. 각 Study마다 사용자가 마지막으로 읽은 시각 이후 생성된 메시지 개수(unreadCount)와
    //    마지막 메시지 정보를 가져와 Study 객체에 추가합니다.
    const studiesWithUnreadCount = await Promise.all(
      user.joinedStudies.map(async (study) => {
        // studyReadTimestamps 배열에서 해당 Study의 마지막 읽은 시각을 찾습니다.
        // 만약 읽은 기록이 없으면 기본값으로 epoch를 사용합니다.
        let lastReadTime = new Date(0)
        if (user.studyReadTimestamps && Array.isArray(user.studyReadTimestamps)) {
          const readObj = user.studyReadTimestamps.find((item) => item.studyId.toString() === study._id.toString())
          if (readObj && readObj.lastRead) {
            lastReadTime = readObj.lastRead
          }
        }

        // 해당 Study에서 lastReadTime 이후에 생성된 메시지의 개수를 계산합니다.
        const unreadCount = await Message.countDocuments({
          studyId: study._id,
          createdAt: { $gt: lastReadTime },
        })

        // 마지막 메시지를 조회합니다. (생성시간 내림차순 정렬)
        const lastMsg = await Message.findOne({ studyId: study._id }).sort({ createdAt: -1 }).exec()

        // Mongoose 문서를 일반 객체로 변환하여 추가 필드를 삽입할 수 있도록 합니다.
        const studyObj = study.toObject()
        studyObj.unreadCount = unreadCount
        if (lastMsg) {
          studyObj.lastMessage = lastMsg.message
          studyObj.lastMessageCreatedAt = lastMsg.createdAt
        } else {
          studyObj.lastMessage = ""
          studyObj.lastMessageCreatedAt = null
        }

        // 3. members 배열을 순회하면서, populate된 User 객체에서 nickname과 image를 가져옵니다.
        //    Study 스키마의 members가 단순 ObjectId 배열이라면, populate 후 각 member는 User 객체가 됩니다.
        if (studyObj.members && Array.isArray(studyObj.members)) {
          studyObj.members = studyObj.members.map((member) => {
            // member가 객체이고 _id가 있다면, 이는 populate된 User 객체입니다.
            if (typeof member === "object" && member._id) {
              return {
                userId: member._id, // User의 ObjectId
                nickname: member.nickname,
                image: member.image,
              }
            } else {
              // 그렇지 않으면 그대로 반환
              return member
            }
          })
        }

        // 4. study.image 필드에 study.createdBy에 해당하는 User의 image를 추가합니다.
        //    먼저, createdBy가 population되어 있다면 바로 사용하고,
        //    아니라면 별도로 User.findById로 조회하여 가져옵니다.
        if (studyObj.createdBy) {
          if (typeof studyObj.createdBy === "object" && studyObj.createdBy.image) {
            // 이미 population된 경우
            studyObj.image = studyObj.createdBy.image
          } else {
            // population되지 않은 경우, 별도로 조회
            const creator = await User.findById(studyObj.createdBy).select("image")
            studyObj.image = creator ? creator.image : ""
          }
        } else {
          studyObj.image = ""
        }

        return studyObj
      })
    )

    // 4. 최종 결과를 클라이언트에 JSON 형태로 반환합니다.
    res.status(200).json(studiesWithUnreadCount)
  } catch (err) {
    console.error("가입한 스터디 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

/**
 * @openapi
 * /api/users/{userId}/studies/{studyId}:
 *   get:
 *     tags:
 *       - User
 *     summary: 사용자가 가입한 특정 스터디 조회
 *     description: 주어진 userId가 가입한 스터디 중 studyId에 해당하는 스터디 정보를 반환합니다.
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 사용자 고유 ID
 *       - in: path
 *         name: studyId
 *         required: true
 *         schema:
 *           type: string
 *         description: 스터디 고유 ID
 *     responses:
 *       200:
 *         description: 가입한 특정 스터디 정보 반환
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/Study'
 *       404:
 *         description: 사용자 또는 스터디를 찾을 수 없음
 *       500:
 *         description: 서버 에러
 */
router.get("/:userId/studies/:studyId", async (req, res) => {
  const { userId, studyId } = req.params
  try {
    // 사용자를 조회하며 joinedStudies를 populate
    const user = await User.findById(userId).populate("joinedStudies")
    if (!user) {
      return res.status(404).json({ error: "사용자를 찾을 수 없습니다." })
    }

    // 사용자가 가입한 스터디 중 해당 studyId와 일치하는 스터디를 찾음
    const study = user.joinedStudies.find((study) => study._id.toString() === studyId)
    if (!study) {
      return res.status(404).json({ error: "가입한 스터디 중 해당 스터디를 찾을 수 없습니다." })
    }

    res.status(200).json(study)
  } catch (err) {
    console.error("특정 스터디 조회 중 에러 발생:", err)
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
