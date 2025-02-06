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

                    // 백그라운드 실행
                    sh 'nohup java -Dspring.profiles.active=dev -jar build/libs/ttalkkag-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &'

                    // 실행 후 3초 대기 (Spring Boot 초기화 시간 확보)
                    sh 'sleep 3'

                    // 실행 중인지 확인
                    sh 'ps -ef | grep java'
                }
            }
        }

        stage('Build Vue.js Frontend') {
            steps {
                script {
                    sh 'cd frontend && npm install && npm run build'
                    // Vue 서버도 백그라운드 실행
                    sh 'nohup npm run serve > frontend.log 2>&1 &'

                    // 실행 중인지 확인
                    sh 'ps -ef | grep node'
                }
            }
        }
    }
}
