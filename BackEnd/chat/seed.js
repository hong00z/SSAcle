// seed.js
require("dotenv").config()
const mongoose = require("mongoose")
const User = require("./models/User")
const Study = require("./models/Study")

async function seed() {
  // MongoDB 연결
  await mongoose.connect(process.env.MONGO_URI)
  console.log("MongoDB 연결 성공")

  // 기존 데이터 삭제 (테스트 환경에서만 사용)
  await User.deleteMany({})
  await Study.deleteMany({})
  console.log("기존 데이터 삭제 완료")

  // 6명의 샘플 사용자 생성
  const usersData = [
    {
      // 가입하지 않은 유저
      nickname: "유저1_미가입",
      userImageUrl: "",
      term: 1,
      campus: "서울",
      topics: ["Node.js", "MongoDB"],
      meetingDays: ["월요일"],
      joinedStudies: [],
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user1",
    },
    {
      nickname: "유저2_스터디1",
      userImageUrl: "",
      term: 2,
      campus: "부산",
      topics: ["React", "Express"],
      meetingDays: ["화요일"],
      joinedStudies: [], // 나중에 study1의 ObjectId 추가
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user2",
    },
    {
      nickname: "유저3_스터디1",
      userImageUrl: "",
      term: 3,
      campus: "대전",
      topics: ["Vue", "Firebase"],
      meetingDays: ["수요일"],
      joinedStudies: [], // 나중에 study1의 ObjectId 추가
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user3",
    },
    {
      nickname: "유저4_스터디2",
      userImageUrl: "",
      term: 1,
      campus: "서울",
      topics: ["Angular", "TypeScript"],
      meetingDays: ["목요일"],
      joinedStudies: [], // 나중에 study2의 ObjectId 추가
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user4",
    },
    {
      nickname: "유저5_스터디2",
      userImageUrl: "",
      term: 2,
      campus: "인천",
      topics: ["Python", "Django"],
      meetingDays: ["금요일"],
      joinedStudies: [], // 나중에 study2의 ObjectId 추가
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user5",
    },
    {
      nickname: "유저6_스터디2",
      userImageUrl: "",
      term: 3,
      campus: "대구",
      topics: ["Java", "Spring"],
      meetingDays: ["토요일"],
      joinedStudies: [], // 나중에 study2의 ObjectId 추가
      wishStudies: [],
      invitedStudies: [],
      refreshToken: "token_user6",
    },
  ]

  const createdUsers = []
  for (const userData of usersData) {
    const user = new User(userData)
    await user.save()
    createdUsers.push(user)
  }
  console.log(
    "샘플 사용자 생성 완료:",
    createdUsers.map((u) => u.nickname)
  )

  // 스터디 생성
  // 첫 번째 스터디에는 2명의 유저(유저2, 유저3)가 가입
  const study1 = new Study({
    studyName: "스터디1",
    image: "",
    topic: "웹 개발",
    meetingDays: ["월요일", "수요일"],
    count: 10,
    members: [createdUsers[1]._id, createdUsers[2]._id],
    studyContent: "첫 번째 스터디 내용",
    wishMembers: [],
    preMembers: [],
    feeds: [],
  })
  await study1.save()
  console.log("스터디1 생성 완료:", study1.studyName)

  // 두 번째 스터디에는 3명의 유저(유저4, 유저5, 유저6)가 가입
  const study2 = new Study({
    studyName: "스터디2",
    image: "",
    topic: "모바일 개발",
    meetingDays: ["화요일", "목요일"],
    count: 8,
    members: [createdUsers[3]._id, createdUsers[4]._id, createdUsers[5]._id],
    studyContent: "두 번째 스터디 내용",
    wishMembers: [],
    preMembers: [],
    feeds: [],
  })
  await study2.save()
  console.log("스터디2 생성 완료:", study2.studyName)

  // 각 유저의 joinedStudies 필드 업데이트
  // 유저1은 가입하지 않음 → 그대로 []
  // 유저2, 유저3 가입: study1
  await User.findByIdAndUpdate(createdUsers[1]._id, { joinedStudies: [study1._id] })
  await User.findByIdAndUpdate(createdUsers[2]._id, { joinedStudies: [study1._id] })
  // 유저4, 유저5, 유저6 가입: study2
  await User.findByIdAndUpdate(createdUsers[3]._id, { joinedStudies: [study2._id] })
  await User.findByIdAndUpdate(createdUsers[4]._id, { joinedStudies: [study2._id] })
  await User.findByIdAndUpdate(createdUsers[5]._id, { joinedStudies: [study2._id] })
  console.log("유저의 joinedStudies 필드 업데이트 완료")

  // MongoDB 연결 종료
  mongoose.connection.close()
  console.log("샘플 데이터 생성 완료 및 MongoDB 연결 종료")
}

seed().catch((err) => {
  console.error("샘플 데이터 생성 중 에러 발생:", err)
  mongoose.connection.close()
})
