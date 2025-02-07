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
        failure {
            echo 'Pipeline 실패! 로그를 확인하세요.'
        }
    }

}