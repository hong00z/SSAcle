pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Git 저장소에서 소스를 체크아웃합니다.
                checkout scm
            }
        }
        stage('Docker Compose Build & Run') {
            steps {
                script {
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d --build'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline 실행 완료!'
        }
        success {
            script {
                def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                mattermostSend(color: 'good',
                    message: "✅ 빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                    endpoint: 'https://meeting.ssafy.com/hooks/8xib9irpwiyn8r6zho939pt98h',
                    channel: 'Jenkins_Build_Result'
                        )
            }
        }
        failure {
            echo 'Pipeline 실패! 로그를 확인하세요.'
            script {
                def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                mattermostSend(color: 'danger',
                    message: "❌ 빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                    endpoint: 'https://meeting.ssafy.com/hooks/8xib9irpwiyn8r6zho939pt98h',
                    channel: 'Jenkins_Build_Result'
                        )
            }
        }
    }

}