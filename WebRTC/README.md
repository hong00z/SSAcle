# Mediasoup Server

## 실행 방법

- npm install -> 처음 clone시 해야함. (modules 설치)
- .env 파일 생성

  ```javascript
  # SSL 인증서 경로
  SSL_KEY_PATH=./cert/key.pem
  SSL_CERT_PATH=./cert/cert.pem

  # mediasoup 설정
  ANNOUNCED_IP= [IP 주소]
  LISTEN_IP=0.0.0.0

  # 서버 포트
  PORT=4000
  ```

- npm run build -> 1번 터미널(Client webpack으로 수정사항 반영)
- npm start -> 2번 터미널(서버 실행)
