// models/Study.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const studySchema = new Schema(
  {
    studyName: {
      type: String,
      required: true,
    },
    createdBy: {
      type: Schema.Types.ObjectId,
      ref: "User",
    },
    count: {
      type: Number,
      required: true,
    },
    members: [
      {
        type: Schema.Types.ObjectId,
        ref: "User",
      },
    ],
  },
  {
    timestamps: true,
  }
)

// 모델 생성 후 내보내기
module.exports = mongoose.model("Study", studySchema)
