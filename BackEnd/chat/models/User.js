// models/User.js
const mongoose = require("mongoose")
const { Schema } = mongoose

const UserSchema = new Schema({
  nickname: {
    type: String,
    required: true,
  },
})

// MongoDB는 각 도큐먼트마다 기본적으로 _id 필드를 생성합니다.
module.exports = mongoose.model("User", UserSchema)
