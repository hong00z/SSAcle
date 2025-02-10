# Node.js 20.12.1 버전 (Alpine 기반) 이미지 사용
FROM node:22.13.1

# 시스템 패키지 업데이트 및 python3-pip 설치
RUN apt-get update && apt-get install -y python3-pip

# 작업 디렉토리 설정
WORKDIR /app

# 의존성 설치를 위해 package.json 및 package-lock.json 복사
COPY package*.json .

# 의존성 설치
RUN npm install

# 나머지 소스 코드 복사
COPY . .

# 필요에 따라 애플리케이션이 사용하는 포트를 EXPOSE
EXPOSE 4000

# 컨테이너 시작 시 실행할 명령어 (package.json에 start 스크립트가 정의되어 있어야 함)
CMD ["npm", "start"]
