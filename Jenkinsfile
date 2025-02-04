pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
    }

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    checkout([$class: 'GitSCM',
                              branches: [[name: '*/develop']],
                              userRemoteConfigs: [[
                                  url: 'http://slc.conv.site:13203/newbie/ttalkkag.git',
                                  credentialsId: '2144f85d-68a5-4934-a84f-d53ee3bfc8ba'  // GitLab Credentials 사용
                              ]]
                    ])
                }
            }
        }

        stage('Build & Test Backend') {
            steps {
                script {
                    sh 'gradlew build'
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
