// models/Message.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const messageSchema = new Schema(
  {
    studyId: {
      type: Schema.Types.ObjectId,
      ref: "Study",
      swaggertype: "string",
      required: true,
    },
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      swaggertype: "string",
      required: true,
    },
    nickname: {
      type: String,
      required: true,
    },
    message: {
      type: String,
      required: true,
    },
    isInOut: {
      type: Boolean,
      required: true,
    },
  },
  {
    timestamps: {
      createdAt: true,
      updatedAt: false,
    },
  }
)

module.exports = mongoose.model("Message", messageSchema)
