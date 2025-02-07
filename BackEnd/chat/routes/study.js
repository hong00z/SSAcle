// routes/study.js
const express = require("express")
const router = express.Router()
const Study = require("../models/Study")

/**
 * @openapi
 * /api/studies:
 *   get:
 *     tags:
 *       - Study
 *     summary: 전체 스터디 목록 조회
 *     description: 모든 스터디의 목록을 반환합니다.
 *     responses:
 *       200:
 *         description: 스터디 목록 반환
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                  $ref: '#/components/schemas/Study'
 *       500:
 *         description: 서버 에러
 */
router.get("/", async (req, res) => {
  try {
    const studies = await Study.find({})
    console.log(`스터디 목록 조회 - 총 ${studies.length}개`)
    res.status(200).json(studies)
  } catch (err) {
    console.error("스터디 목록 조회 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

module.exports = router
