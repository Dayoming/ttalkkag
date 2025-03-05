FROM jenkins/jenkins:lts-jdk21

# Root 사용자 설정
USER root

# Node.js 22.11.0 설치 (NVM 사용)
RUN apt-get update && apt-get install -y curl bash \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g npm@latest

# Node.js 버전 확인
RUN node -v && npm -v

# Docker Compose 설치
RUN curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose

# Docker Compose 버전 확인
RUN docker-compose -v