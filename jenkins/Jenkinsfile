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
                    sh 'cd /var/jenkins_home/workspace/ttalkkag && ./gradlew build -x test'  // 백엔드 빌드 (테스트 제외)
                    sh 'cd /var/jenkins_home/workspace/ttalkkag/history && chmod +x gradlew && ./gradlew build -x test' // history 빌드
                    sh 'ls -l /var/jenkins_home/workspace/ttalkkag/build/libs' // JAR 파일 확인
                    sh 'ls -l /var/jenkins_home/workspace/ttalkkag/history/build/libs'
                    sh 'mkdir -p backend_build' // JAR 저장 폴더 생성
                    sh 'mkdir -p history_build'
                    sh 'cp /var/jenkins_home/workspace/ttalkkag/build/libs/*.jar /var/jenkins_home/workspace/ttalkkag/backend_build/' // 빌드된 JAR 파일 복사
                    sh 'cp /var/jenkins_home/workspace/ttalkkag/history/build/libs/*.jar /var/jenkins_home/workspace/ttalkkag/history_build/'
                }
            }
        }

        stage('Build Vue.js Frontend') {
            steps {
                script {
                    sh 'cd frontend && npm install && npm run build'
                    sh 'mkdir -p frontend_build' // Vue 배포 폴더 생성
                    sh 'cp -r /var/jenkins_home/workspace/ttalkkag/frontend/dist/* /var/jenkins_home/workspace/ttalkkag/frontend_build/' // Vue 빌드 결과 복사
                }
            }
        }

        stage('Restart Containers') {
            steps {
                script {
                    sh 'docker-compose restart backend'  // 백엔드 컨테이너만 재시작
                    sh 'docker-compose restart history' // history 컨테이너만 재시작
                    sh 'docker-compose restart nginx'    // 프론트엔드 컨테이너만 재시작
                }
            }
        }
    }
}
