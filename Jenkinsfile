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
                    sh 'chmod +x gradlew'
                    sh './gradlew build'
                    sh 'cd build/libs'
                    sh 'java -jar ttalkkag-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=dev'
                }
            }
        }

        stage('Build Vue.js Frontend') {
            steps {
                script {
                    sh 'cd frontend && npm install && npm run build'
                    sh 'npm run serve'
                }
            }
        }
    }
}
