// models/Message.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const MessageSchema = new Schema({
  studyId: {
    type: Schema.Types.ObjectId,
    ref: "Study",
    required: true,
  },
  userId: {
    type: Schema.Types.ObjectId,
    ref: "User",
    required: true,
  },
  nickname: {
    type: Schema.Types.String,
    ref: "User",
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
})

module.exports = mongoose.model("Message", MessageSchema)
