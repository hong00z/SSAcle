// models/User.js
const mongoose = require("mongoose")
const { Schema } = mongoose

// studyReadTimestamps 서브 스키마
const studyReadTimestampSchema = new Schema({
  studyId: { type: Schema.Types.ObjectId, ref: "Study" },
  lastRead: { type: Date, default: Date.now },
})

const userSchema = new Schema({
  // 사용자 닉네임
  nickname: {
    type: String,
    required: true,
  },
  // 프로필 이미지 URL
  image: {
    type: String,
    default: "",
  },
  // 사용자가 가입한 스터디 (다른 컬렉션의 ObjectId를 배열로 저장)
  joinedStudies: [
    {
      type: Schema.Types.ObjectId,
      ref: "Study",
      swaggertype: "string",
      default: [],
    },
  ],
  studyReadTimestamps: [studyReadTimestampSchema],
  // FCM 토큰
  fcmToken: {
    type: String,
    default: "",
  },
})

// 모델을 생성하여 내보냅니다.
module.exports = mongoose.model("User", userSchema)
