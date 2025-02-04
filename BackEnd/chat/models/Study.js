// models/Study.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const StudySchema = new Schema({
  studyName: { type: String, required: true },
  members: [
    {
      type: Schema.Types.ObjectId,
      ref: "User", // 필요한 경우 사용자 컬렉션을 참조
    },
  ],
})

module.exports = mongoose.model("Study", StudySchema, "study")
