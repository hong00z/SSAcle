# SSAcle - SSAFY 생을 위한 스터디 관리 통합 앱

## 개요
**SSAcle(싸클)** 은 SSAFY 교육생뿐만 아니라 수료생까지, **싸피 출신이라면 누구든 참여할 수 있는 스터디 관리 통합 앱**입니다.



## 개발 기간
**2025. 01. 13 ~ 2025. 02. 21 (총 6주)**


## 주요 서비스

1. **스터디 개설**
2. **스터디 및 스터디원 매칭 서비스**
    - 관심 주제 및 모임 요일을 기반으로 스터디 및 스터디원을 추천
3. **라이브 스터디**
    - WebRTC 기반 실시간 스트리밍 기능을 제공하여 스터디 모임을 원활하게 운영 가능
4. **스터디 채팅방**
    - 실시간 채팅을 통한 스터디원 간의 원활한 커뮤니케이션 지원
5. **AI 자기소개서 첨삭 기능**
    - OpenAI API를 활용한 자기소개서 첨삭 및 피드백 제공
6. **AI 면접 피드백 서비스 (시선 및 표정 분석)**
    - 표정 감지, 동공 감지 AI 모델을 적용하여 면접 피드백 제공


## 기술 스택
![시스템 아키텍처](images/시스템아키텍처.png)
### FE
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)![Android Studio](https://img.shields.io/badge/android%20studio-346ac1?style=for-the-badge&logo=android%20studio&logoColor=white) (LadyBug)

### AI
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=for-the-badge&logo=PyTorch&logoColor=white)![TensorFlow](https://img.shields.io/badge/TensorFlow-%23FF6F00.svg?style=for-the-badge&logo=TensorFlow&logoColor=white), **TensorFlow Lite**

### BE
![Spring Boot](https://img.shields.io/badge/spring%20boot-6DB33F?style=for-the-badge&logo=springboot%20studio&logoColor=white)(주요 백엔드 API), ![NodeJS](https://img.shields.io/badge/node.js-6DA55F?style=for-the-badge&logo=node.js&logoColor=white) (실시간 통신 및 미디어 처리)



### DB
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white) (Token 관리), ![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white) (NoSQL 데이터 관리)


### INFRA
![Jenkins](https://img.shields.io/badge/jenkins-%232C5263.svg?style=for-the-badge&logo=jenkins&logoColor=white)![Cady](https://img.shields.io/badge/caddy-1f88c0?style=for-the-badge&logo=caddyc2&logoColor=white)![AWS EC2](https://img.shields.io/badge/AWS%20EC2-ff9900?style=for-the-badge&logo=amazonec2&logoColor=white)![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) , **Docker Compose**


### 협업 및 관리 도구

![Gitlba](https://img.shields.io/badge/gitlab-fc6d26?style=for-the-badge&logo=gitlab&logoColor=white) (형상 관리), ![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)![Jira](https://img.shields.io/badge/jira-%230A0FFF.svg?style=for-the-badge&logo=jira&logoColor=white) (협업 및 프로젝트 관리)


## 핵심 기능 상세 설명

### **1. 온보딩 및 메인 화면**
| <img src="images/온보딩1.png" width="200"> | <img src="images/온보딩2.png" width="200"> | <img src="images/온보딩3.png" width="200"> | <img src="images/온보딩4.png" width="200"> | <img src="images/온보딩5.png" width="200"> |
|:---:|:---:|:---:|:---:|:---:|
| 로그인 화면 | 카카오 로그인 | 학적 인증 | 닉네임 중복 검사 | 사용자 정보 입력 |

| <img src="images/메인_스켈레톤UI.png" width="200"> | <img src="images/메인_초기가입.png" width="200"> | <img src="images/메인.png" width="200"> |
|:---:|:---:|:---:|
| 스켈레톤 UI | 최초 메인 화면 | 메인 화면 |

- 카카오 로그인 API 활용하여 JWT 기반 인증 로그인 구현
- DB에 있는 가상의 학적정보를 활용해 SSAFY 교육생 및 수료생 본인 인증 지원

### **2. 매칭 서비스**
| <img src="images/매칭_스터디원추천1.png" width="200"> | <img src="images/매칭_스터디원추천2.png" width="200"> | <img src="images/매칭_스터디추천.png" width="200"> |
|:---:|:---:|:---:|
| 스터디원 구인 시 희망 스터디 선택 | 스터디원 구인 결과 | 스터디 찾기 결과 |

- 필터링 및 코사인 유사도 알고리즘을 활용하여 최적의 스터디 및 스터디원 매칭

### **3. 라이브 스터디**
| <img src="images/라이브스터디2.png" width="200"> | <img src="images/스터디상세.png" width="200"> | <img src="images/라이브스터디.png" width="200"> |
|:---:|:---:|:---:|
| Navbar 라이브 | 스터디 상세 | 라이브 스터디 |

- WebRTC 기반 실시간 채팅 및 영상/음성 스트리밍 지원
- MediaSoup + Socket.io를 활용한 시그널링 서버 및 미디어 서버 구축

### 4. 스터디 단체 채팅방
| <img src="images/스터디채팅방.png" width="200"> | <img src="images/스터디채팅방_상세.png" width="200"> |
|:---:|:---:|
| Navbar 채팅 | 스터디 채팅방 |

- Socket.io + MongoDB를 사용한 실시간 채팅 서버 구현

### **5. AI 자기소개서 첨삭 기능**
| <img src="images/AI자소서피드백.png" width="200"> | <img src="images/AI자소서피드백2.png" width="200"> | <img src="images/AI자소서피드백3.png" width="200"> |
|:---:|:---:|:---:|
| AI 자소서 피드백 | 자소서 피드백 상세 | 피드백 결과 |

- OpenAI API 기반 프롬프트 엔지니어링 적용
- 프롬프트 최적화(4,500자 -> 1,500자)하여 토큰 효율성 증대
- 수정된 부분을 스트리밍 UI를 사용하여 붉은색으로 표시 후 PDF 파일로 저장 가능 (스크랩 기능 지원)

### **6. AI 면접 피드백 서비스**
| <img src="images/AI시선피드백.png" width="200"> | <img src="images/AI표정피드백1.png" width="200"> |
|:---:|:---:|
| AI 시선 피드백 | AI 표정 피드백 |

| <img src="images/AI표정피드백2.png" width="200"> | <img src="images/AI표정피드백3.png" width="200"> | <img src="images/AI시선피드백2.png" width="200"> | <img src="images/AI표정피드백4.png" width="200"> | <img src="images/AI표정피드백5.png" width="200"> |
|:---:|:---:|:---:|:---:|:---:|
|  |  |  |  |  |

- **표정  및 동공 감지 AI**
    - Yolov8n 모델을 활용해 모델 학습
    - 화면 응시 여부 분석 및 비율 계산 후 피드백 제공 (PNG)
- **온디바이스 AI를 위한 최적화**: 9초 영상 분석 시 100초 → 7초로 감소


## 팀원

| 역할 | 이름 |
| --- | --- |
| Android | 이호정, 장홍준, 정찬우 |
| BackEnd | 김민주, 이문경 |
| AI | 김웅기, 이호정 |
| WebRTC | 정찬우 |
| Infra | 장홍준 |


## 요구 사항

### [API 명세서](https://www.notion.so/0f095bdffce24ece9c096314463bc30d?pvs=21)

### [스키마](https://www.notion.so/e4c9075a2b574e7888fff138a5b2d5ea?pvs=21)

### **외부 API**

- **카카오 로그인 API**
- **OpenAI API**
- **Firebase Cloud Message**

### **배포 및 개발 환경**

- **CI/CD**: Jenkins, Docker, Docker Compose


## **Git 컨벤션**

**⚠️형식에서 대소문자 구분을 유의해주세요!!!**

### **1. Issue 등록**

Jira에 담당할 작업에서 개발해야할 내용들을 issue 템플릿 형식에 맞게 등록한다.

**Issue 제목 형식**

> [Part/Type] 이슈 제목
> 
- (예시) [Android/Feat] 기능 개발

### **2. branch 생성**

생성한 이슈의 내용만을 개발할 branch를 이름 형식에 맞춰 생성한다. 해당 기능 개발이 끝나면 master branch로 Merge Request를 한다.

**branch 이름 형식**

> type/#issue_number
> 
- (예시) feat/#3

### **3. commit 등록**

변경 사항을 commit 할 때, 제목(title) 형식을 맞춰 commit한다.

commit은 issue 등록할 때 to-do 리스트에 작성한 내용마다 하는 것을 추천...

**commit 제목 형식**

> branch이름 : commit 제목
> 
- (예시) feat/#3 : example 기능 구현

### **4. Merge Request 등록**

작업이 끝난 issue에 해당하는 branch를 master branch와 merge하기 위해, Merge Request를 MR template 형식에 맞춰 작성하고 병합을 요청한다.

**MR 제목 형식**

> [Part/Type] MR 제목
> 
- (예시) [BE/Fix] 기능 수정