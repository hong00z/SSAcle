pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Git 저장소에서 소스를 체크아웃합니다.
                checkout scm
            }
        }
        
        stage ('Parallel Build & Test') {
            parallel {
                
                // Spring Boot 빌드 및 테스트 단계
                stage('Spring Boot Build & Test') {
                    steps {
                        dir('BackEnd/ssacle') {
                            // Spring Boot 애플리케이션 빌드 (예: bootJar 생성) 및 테스트
                            sh './gradlew --no-daemon clean bootJar'
                            // sh './gradlew --no-daemon test'
                        }
                    }
                }
                
                // Node.js 빌드 및 테스트 단계
                stage('Node.js Build & Test') {
                    agent {
                        docker {
                            image 'node:20.12.1'
                            // 필요에 따라 추가 옵션 설정 가능
                        }
                    }
                    steps {
                        dir('WebRTC') {
                            // Node.js 프로젝트의 의존성 설치 및 테스트 실행
                            sh 'npm install'
                            // sh 'npm test'
                        }
                    }
                }
            }
        }

    }
    
    // 빌드 후 항상 실행할 작업들을 정의합니다.
    post {
        always {
            echo 'Post-build 작업: 산출물 아카이브 및 테스트 리포트 수집'
            // BackEnd/ssacle에서 생성된 JAR 파일 보관 (경로는 실제 빌드 산출물 위치에 맞게 수정)
            archiveArtifacts artifacts: 'BackEnd/ssacle/build/libs/*.jar', allowEmptyArchive: true
        }
        failure {
            echo "빌드 실패! 로그를 확인하세요."
            // 추가: 빌드 실패 시 이메일 발송, Slack 알림 등 추가 작업 가능
        }
    }
}