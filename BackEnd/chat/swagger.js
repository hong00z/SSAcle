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
        url: `https://${process.env.ANNOUNCED_IP || "localhost"}:${process.env.PORT || 4001}`,
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
