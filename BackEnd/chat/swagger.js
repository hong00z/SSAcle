// swagger.js
const swaggerJsdoc = require("swagger-jsdoc")
const mongooseToSwagger = require("mongoose-to-swagger")

const User = require("./models/User") // Mongoose 모델 불러오기
const Study = require("./models/Study")
const Message = require("./models/Message")

// mongoose-to-swagger를 사용해 스키마 자동 변환
const swaggerUserSchema = mongooseToSwagger(User)
const swaggerStudySchema = mongooseToSwagger(Study)
const swaggerMessageSchema = mongooseToSwagger(Message)

// Study의 전체 스키마 대신, 해당 필드에는 Study의 id (문자열)만 표시하도록 재정의
// swaggerUserSchema.properties.joinedStudies = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "Study의 id",
//   },
// }
// swaggerUserSchema.properties.wishStudies = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "Study의 id",
//   },
// }
// swaggerUserSchema.properties.invitedStudies = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "Study의 id",
//   },
// }

// swaggerStudySchema.properties.members = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "User의 id",
//   },
// }
// swaggerStudySchema.properties.wishMembers = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "User의 id",
//   },
// }
// swaggerStudySchema.properties.preMembers = {
//   type: "array",
//   items: {
//     type: "string",
//     description: "User의 id",
//   },
// }

// swaggerMessageSchema.properties.studyId = {
//   type: "string",
//     description: "User의 id",
// }

// swagger-jsdoc 옵션 설정 (OpenAPI 3.0 기준)
const options = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "스터디 채팅 API",
      version: "1.0.0",
      description: "스터디 채팅 서버의 API 문서",
    },
    servers: [
      {
        url: `http://${process.env.ANNOUNCED_IP || "localhost"}:${process.env.PORT || 4001}`,
        description: "개발 서버",
      },
    ],
    components: {
      schemas: {
        // mongoose-to-swagger가 변환한 스키마를 할당
        User: swaggerUserSchema,
        Study: swaggerStudySchema,
        Message: swaggerMessageSchema,
      },
    },
  },
  // API 문서에 포함할 파일 경로 (예: routes 파일이나 server.js)
  apis: ["./routes/*.js"],
}

const swaggerSpec = swaggerJsdoc(options)

module.exports = swaggerSpec
