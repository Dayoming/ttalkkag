# API Tester - ttalkkag
> API Test를 편리하게 수행하고 결과를 한눈에 확인할 수 있도록 하는 프로젝트, 딸깍🖱️

* * *

## Table of Contents
- [About The Project](##About The Project)
  - [개발 환경](###개발 환경)
  - [구동 방법](###구동 방법)
  - [파일 구조](###파일 구조)
    - [Frontend](####Frontend)
    - [Backend](####Backend)
    - [History Module](####History Module)
- [Debugging & Logging](##Debugging)

* * *

## About The Project
- 사용자의 요청 데이터를 관리, 분석, 시뮬레이션, 그리고 다양한 환경에서의 API 호출을 테스트하기 위한 통합 관리 시스템을 제공한다.
- 주요 기능은 API 요청 이력 관리, 데이터셋 및 변수 관리, 사용자 환경 설정, 프록시 요청 처리, 테스트 환경 구축, 다양한 HTTP 응답 시뮬레이션을 포함한다.
- 사용자의 데이터 흐름을 직관적으로 관리하고, 프로젝트별 효율적인 데이터 추적 및 분석을 관리한다.


### 개발 환경

<ul>
    <li style="list-style: none;"><b>🔨 Spring Boot</b>: 3.4.0</li>
    <li style="list-style: none;"><b>🔨 Java</b>: 21 (Amazon Corretto 21.0.5)</li>
    <li style="list-style: none;"><b>🔨 Vue-cli</b>: @vue.cli 5.0.8</li>
    <li style="list-style: none;"><b>🔨 Nginx</b>: 1.27.4</li>
    <li style="list-style: none;"><b>🔨 Docker</b>: 27.5.1</li>
    <li style="list-style: none;"><b>🔨 MariaDB</b>: 11.5.2</li>
    <li style="list-style: none;"><b>🔨 Redis</b>: 3.0.504</li>
    <li style="list-style: none;"><b>🔨 Gradle</b>: 8.11.1</li>
</ul>

### 구동 방법

서버 실행 시 
```shell
# local
$ java -jar ttalkkag-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=dev
$ java -jar history-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=dev

# development
$ java -jar ttalkkag-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=prod
$ java -jar history-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=prod
```

프론트엔드 실행 시
```shell
# local
$ npm run dev

# development
$ npm run serve
```

### 배포 방법

Docker 배포
```shell
$ docker compose up -d
```
- `docker-compose.yml` 파일을 수정하여 Nginx, Spring Boot, MariaDB, Redis 컨테이너를 관리
- `Jenkinsfile`을 사용해 자동 배포 구성

### 파일 구조

#### Frontend

```
📦src
 ┣ 📂assets
 ┃ ┣ 📂img
 ┃ ┃ ┣ 📜kakao_login_icon.png
 ┃ ┃ ┣ 📜kakao_login_medium_wide.png
 ┃ ┃ ┗ 📜web_light_rd_na@2x.png
 ┃ ┣ 📂profiles
 ┃ ┃ ┗ 📜profile-default-icon.png
 ┃ ┗ 📜logo.png
 ┣ 📂components
 ┃ ┣ 📂layouts
 ┃ ┃ ┣ 📜CommonModal.vue
 ┃ ┃ ┣ 📜CommonSideBar.vue
 ┃ ┃ ┣ 📜NavHeader.vue
 ┃ ┃ ┗ 📜NotificationsVue.vue
 ┃ ┣ 📜ApiTest.vue
 ┃ ┣ 📜DatasetImportModal.vue
 ┃ ┣ 📜DatasetModal.vue
 ┃ ┣ 📜DatasetVue.vue
 ┃ ┣ 📜EnvironmentVue.vue
 ┃ ┣ 📜GoogleLoginCallback.vue
 ┃ ┣ 📜HelloWorld.vue
 ┃ ┣ 📜HistoryManageModal.vue
 ┃ ┣ 📜HistoryVue.vue
 ┃ ┣ 📜KakaoLoginCallback.vue
 ┃ ┣ 📜LoginVue.vue
 ┃ ┣ 📜ProjectsVue.vue
 ┃ ┣ 📜RecursiveFolderItem.vue
 ┃ ┣ 📜ReportsVue.vue
 ┃ ┣ 📜SaveModal.vue
 ┃ ┣ 📜SaveModalItem.vue
 ┃ ┣ 📜SettingModal.vue
 ┃ ┣ 📜SignUpVue.vue
 ┃ ┗ 📜SiteEnvironmentModal.vue
 ┣ 📂router
 ┃ ┗ 📜index.js
 ┣ 📜App.vue
 ┣ 📜axios.js
 ┗ 📜main.js
```

#### Backend

```
📦ttalkkag
 ┣ 📂config
 ┃ ┣ 📜JwtAuthenticationFilter.java
 ┃ ┣ 📜JwtTokenUtil.java
 ┃ ┣ 📜MessageSourceConfig.java
 ┃ ┣ 📜MessageUtil.java
 ┃ ┣ 📜OAuth2UserServiceConfig.java
 ┃ ┣ 📜RedisConfig.java
 ┃ ┣ 📜RestTemplateConfig.java
 ┃ ┣ 📜SecurityConfig.java
 ┃ ┣ 📜WebConfig.java
 ┃ ┗ 📜WebSocketConfig.java
 ┣ 📂controller
 ┃ ┣ 📜ApiController.java
 ┃ ┣ 📜AuthController.java
 ┃ ┣ 📜DatasetController.java
 ┃ ┣ 📜EnvironmentController.java
 ┃ ┣ 📜HistoryController.java
 ┃ ┣ 📜ProjectController.java
 ┃ ┣ 📜ProxyController.java
 ┃ ┣ 📜TestController.java
 ┃ ┗ 📜UserController.java
 ┣ 📂dto
 ┃ ┣ 📜ApiChangeHistory.java
 ┃ ┣ 📜ApiHistory.java
 ┃ ┣ 📜ApiHistoryResponse.java
 ┃ ┣ 📜Apis.java
 ┃ ┣ 📜ApiUsage.java
 ┃ ┣ 📜Dataset.java
 ┃ ┣ 📜DatasetVariable.java
 ┃ ┣ 📜Environment.java
 ┃ ┣ 📜EnvironmentVariable.java
 ┃ ┣ 📜InviteCode.java
 ┃ ┣ 📜ItemOrderUpdateRequest.java
 ┃ ┣ 📜NotificationMessage.java
 ┃ ┣ 📜Project.java
 ┃ ┣ 📜ProjectItems.java
 ┃ ┣ 📜ProjectParticipants.java
 ┃ ┣ 📜ProxyRequest.java
 ┃ ┣ 📜Site.java
 ┃ ┗ 📜User.java
 ┣ 📂exception
 ┃ ┣ 📜DatabaseExceptionHandler.java
 ┃ ┣ 📜GlobalResponseHandler.java
 ┃ ┣ 📜JwtAuthenticationException.java
 ┃ ┗ 📜JwtExceptionFilter.java
 ┣ 📂layout
 ┃ ┣ 📜Message.java
 ┃ ┗ 📜StatusEnum.java
 ┣ 📂log
 ┃ ┣ 📜DiskSpaceMonitor.java
 ┃ ┣ 📜HangLogScheduler.java
 ┃ ┣ 📜LoggingFilter.java
 ┃ ┣ 📜LoggingUtil.java
 ┃ ┣ 📜ResultCode.java
 ┃ ┣ 📜Tlo.java
 ┃ ┣ 📜UrlMapping.java
 ┃ ┣ 📜UrlMappingLoader.java
 ┃ ┗ 📜UrlMappingResolver.java
 ┣ 📂mapper
 ┃ ┣ 📜ApiMapper.java
 ┃ ┣ 📜DatasetMapper.java
 ┃ ┣ 📜DatasetVariableMapper.java
 ┃ ┣ 📜EnvironmentMapper.java
 ┃ ┣ 📜EnvironmentVariableMapper.java
 ┃ ┣ 📜HistoryMapper.java
 ┃ ┣ 📜ProjectMapper.java
 ┃ ┗ 📜UserMapper.java
 ┣ 📂service
 ┃ ┣ 📜ApiService.java
 ┃ ┣ 📜CustomOAuth2UserService.java
 ┃ ┣ 📜DatasetService.java
 ┃ ┣ 📜EmailService.java
 ┃ ┣ 📜EnvironmentService.java
 ┃ ┣ 📜ExternalApiHistoryService.java
 ┃ ┣ 📜FileService.java
 ┃ ┣ 📜GoogleOAuthService.java
 ┃ ┣ 📜HistoryService.java
 ┃ ┣ 📜KakaoOAuthService.java
 ┃ ┣ 📜NotificationService.java
 ┃ ┣ 📜ProjectService.java
 ┃ ┣ 📜ResponseService.java
 ┃ ┗ 📜UserService.java
 ┗ 📜TtalkkagApplication.java
```

#### History Module

```
📦history
 ┣ 📂config
 ┃ ┗ 📜RestTemplateConfig.java
 ┣ 📂controller
 ┃ ┗ 📜ApiChangeHistoryController.java
 ┣ 📂dto
 ┃ ┗ 📜ApiChangeHistory.java
 ┣ 📂mapper
 ┃ ┗ 📜ApiChangeHistoryMapper.java
 ┣ 📂service
 ┃ ┣ 📜ApiChangeHistoryService.java
 ┃ ┣ 📜HistoryCleanUpService.java
 ┃ ┗ 📜JwtValidationService.java
 ┗ 📜HistoryApplication.java
```

## Debugging

### Backend Debugging

- 로컬 개발 환경에서 디버깅

```shell
$ ./gradlew bootRun --args='--spring.profiles.active=dev'
```

- 로그 확인
  - 기본적으로 `logs/` 디렉토리에 로그 파일 저장
  - 실시간 로그 확인:
    ```shell
    $ tail -f logs/transaction.log
    ```

### Frontend Debugging
- 콘솔 로그 확인
  - 브라우저 개발자 도구 `Console` 탭 확인
  - API 요청 오류 시 `Network` 탭에서 요청 상태 확인