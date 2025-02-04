pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: 'http://slc.conv.site:13203/newbie/ttalkkag.git'
            }
        }

        stage('Build & Test Backend') {
            steps {
                script {
                    sh 'cd backend && ./gradlew build'
                }
            }
        }

        stage('Build Vue.js Frontend') {
            steps {
                script {
                    sh 'cd frontend && npm install && npm run build'
                }
            }
        }

        stage('Stop & Remove Existing Containers') {
            steps {
                script {
                    sh 'docker-compose down || true'
                }
            }
        }

        stage('Build & Start Containers') {
            steps {
                script {
                    sh 'docker-compose up --build -d'
                }
            }
        }
    }
}
