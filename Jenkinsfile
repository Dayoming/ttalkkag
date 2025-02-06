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

        stage('Build & Deploy Backend') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                    sh './gradlew build'
                    sh 'docker cp build/libs/ttalkkag-0.0.1-SNAPSHOT.jar springboot_container:/app/app.jar'
                    sh 'docker restart springboot_container'
                }
            }
        }

        stage('Build & Deploy Frontend') {
            steps {
                script {
                   sh 'cd frontend && npm install && npm run build'
                   sh 'docker cp frontend/dist/. vue_container:/usr/share/nginx/html'
                   sh 'docker restart vue_container'
                }
            }
        }
    }
}
