// models/Study.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const studySchema = new Schema(
  {
    // 스터디 이름
    studyName: {
      type: String,
      required: true,
    },
    // 프로필 사진 URL
    image: {
      type: String,
      default: "",
    },
    // 스터디 주제
    topic: {
      type: String,
      required: true,
    },
    // 모임 요일 및 날짜 (복수 선택 가능)
    meetingDays: {
      type: [String],
      default: [],
    },
    // 정원
    count: {
      type: Number,
      required: true,
    },
    // 스터디 멤버 (User 컬렉션 참조)
    members: [
      {
        type: Schema.Types.ObjectId,
        ref: "User",
        swaggertype: "string",
        default: [],
      },
    ],
    // 스터디 소개
    studyContent: {
      type: String,
      default: "",
    },
    // 스카웃하고 싶은 스터디원 (내 스터디 → 사용자)
    wishMembers: [
      {
        type: Schema.Types.ObjectId,
        ref: "User",
        swaggertype: "string",
        default: [],
      },
    ],
    // 신청한 스터디원 (사용자 → 내 스터디)
    preMembers: [
      {
        type: Schema.Types.ObjectId,
        ref: "User",
        swaggertype: "string",
        default: [],
      },
    ],
    // 피드 (필요에 따라 Feed 컬렉션 참조 가능, 여기서는 간단하게 문자열 배열로 예시)
    feeds: {
      type: [String],
      default: [],
    },
  },
  {
    timestamps: { createdAt: true, updatedAt: false },
  }
)

// 모델 생성 후 내보내기
module.exports = mongoose.model("Study", studySchema)
