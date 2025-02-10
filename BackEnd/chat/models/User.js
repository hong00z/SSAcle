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
  userImageUrl: {
    type: String,
    default: "",
  },
  // 기수 (숫자형 또는 문자열로 표현 가능하지만, 여기서는 숫자로 가정)
  term: {
    type: Number,
    required: true,
  },
  // 지역 정보 (캠퍼스)
  campus: {
    type: String,
    required: true,
  },
  // 관심 주제 (복수 선택 가능하므로 문자열 배열)
  topics: {
    type: [String],
    default: [],
  },
  // 스터디를 하길 원하는 요일 (복수 선택 가능하므로 문자열 배열)
  meetingDays: {
    type: [String],
    default: [],
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
  // 사용자가 신청한 스터디 (나 → 스터디)
  wishStudies: [
    {
      type: Schema.Types.ObjectId,
      ref: "Study",
      swaggertype: "string",
      default: [],
    },
  ],
  // 스터디로부터 스카웃 요청 받은 스터디 (스터디 → 나)
  invitedStudies: [
    {
      type: Schema.Types.ObjectId,
      ref: "Study",
      default: [],
    },
  ],
  studyReadTimestamps: [studyReadTimestampSchema],
  // 리프레시 토큰 (로그인 상태 유지 등 인증을 위해 사용)
  refreshToken: {
    type: String,
    default: "",
  },
})

// 모델을 생성하여 내보냅니다.
module.exports = mongoose.model("User", userSchema)
