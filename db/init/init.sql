USE ttalkkag;

-- ttalkkag.users definition

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `auto_save_term` int(11) DEFAULT NULL COMMENT '사용자가 입력을 멈춘 후 저장되는 시간',
  `auto_save_time` int(11) DEFAULT NULL COMMENT '사용자가 입력 중 자동으로 저장되는 시간',
  `auto_save_use` tinyint(1) DEFAULT NULL COMMENT '자동 저장 여부',
  `auto_save_path` bigint(20) DEFAULT NULL COMMENT '자동 저장 경로',
  `show_response` tinyint(1) DEFAULT NULL COMMENT '결과 값만 보이게 할지, 전체 결과를 볼지 여부',
  `verified` tinyint(1) DEFAULT 0 COMMENT '최초 로그인 여부',
  `social_provider` varchar(100) DEFAULT NULL COMMENT '소셜 로그인 제공자',
  `profile_image` varchar(255) DEFAULT NULL COMMENT '사용자 프로필 이미지',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ttalkkag.project definition

CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 테이블 PK',
  `user_id` bigint(20) NOT NULL COMMENT '프로젝트 생성 유저 id',
  `name` varchar(100) NOT NULL COMMENT '프로젝트 이름',
  `create_at` datetime DEFAULT NULL COMMENT '프로젝트 생성일',
  PRIMARY KEY (`id`),
  KEY `project_users_FK` (`user_id`),
  CONSTRAINT `project_users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=160 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='API를 보관하는 프로젝트 테이블';

-- ttalkkag.project_items definition

CREATE TABLE `project_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '프로젝트 요소 PK',
  `project_id` bigint(20) NOT NULL COMMENT '요소가 속한 프로젝트 id',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '상위 요소 id',
  `type` varchar(100) NOT NULL COMMENT '타입 - 폴더, API',
  `name` varchar(100) NOT NULL COMMENT '요소 이름',
  `depth` int(11) NOT NULL COMMENT '현재 위치한 깊이',
  `create_at` datetime NOT NULL COMMENT '요소 생성 날짜',
  `item_order` int(11) NOT NULL COMMENT '요소 정렬 index',
  PRIMARY KEY (`id`),
  KEY `project_items_project_FK` (`project_id`),
  CONSTRAINT `project_items_project_FK` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=745 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='프로젝트 하위(폴더, API) 테이블';

-- ttalkkag.apis definition

CREATE TABLE `apis` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'apis 테이블 PK',
  `item_id` bigint(20) NOT NULL COMMENT 'project_items에 위치한 api 요소의 id',
  `environment_id` bigint(20) DEFAULT NULL COMMENT 'API Test 탭에서 선택한 개발 환경',
  `name` varchar(100) NOT NULL COMMENT 'API 이름',
  `method` varchar(100) NOT NULL COMMENT 'API 메소드명',
  `url` varchar(100) DEFAULT NULL COMMENT 'API URL',
  `headers` varchar(1024) DEFAULT NULL COMMENT 'API Headers',
  `query_parameters` varchar(1024) DEFAULT NULL COMMENT 'API Query Parameters',
  `form_parameters` varchar(1024) DEFAULT NULL COMMENT 'API Form Parameters',
  `file` varchar(100) DEFAULT NULL COMMENT 'API 전송 file',
  `selected_body_type` varchar(100) DEFAULT NULL COMMENT 'API Test 탭에서 선택한 Body Type',
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `apis_project_items_FK` (`item_id`),
  KEY `apis_environment_FK` (`environment_id`),
  CONSTRAINT `apis_project_items_FK` FOREIGN KEY (`item_id`) REFERENCES `project_items` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=670 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='API 저장 테이블';

-- ttalkkag.invite_code definition

CREATE TABLE `invite_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL COMMENT '초대 코드를 발급한 프로젝트 id',
  `user_email` varchar(100) DEFAULT NULL COMMENT '초대받은 유저 이메일',
  `code` varchar(100) DEFAULT NULL COMMENT '초대 코드',
  `availability` tinyint(1) DEFAULT 1 COMMENT '사용 가능 여부',
  `expiry_time` datetime DEFAULT NULL COMMENT '초대 코드 유효 시간',
  PRIMARY KEY (`id`),
  KEY `invite_code_project_FK` (`project_id`),
  CONSTRAINT `invite_code_project_FK` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='초대 코드 관리 테이블';

-- ttalkkag.project_participants definition

CREATE TABLE `project_participants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL COMMENT '프로젝트 id',
  `user_id` bigint(20) NOT NULL COMMENT '소유/참여한 유저 id',
  `name` varchar(100) DEFAULT NULL COMMENT '프로젝트 이름',
  `role` varchar(20) NOT NULL COMMENT '소유자, 참여자 구분 (owner, participant)',
  `permission_level` varchar(10) NOT NULL COMMENT '읽기, 쓰기 권한',
  PRIMARY KEY (`id`),
  KEY `project_participants_project_FK` (`project_id`),
  KEY `project_participants_users_FK` (`user_id`),
  CONSTRAINT `project_participants_project_FK` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_participants_users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='프로젝트 참가자 테이블';

-- ttalkkag.history definition

CREATE TABLE `history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '요청을 보낸 유저 id',
  `project_id` bigint(20) DEFAULT NULL,
  `environment_id` bigint(20) DEFAULT NULL,
  `site_id` bigint(20) DEFAULT NULL,
  `method` varchar(10) DEFAULT NULL COMMENT 'HTTP 메서드',
  `url` varchar(100) DEFAULT NULL COMMENT '요청 URL',
  `response_code` int(11) DEFAULT NULL COMMENT '응답 코드',
  `response_time` int(11) DEFAULT NULL COMMENT '응답 시간(ms)',
  `logged_time` timestamp NULL DEFAULT NULL COMMENT '요청 기록 시간',
  `header` text DEFAULT NULL COMMENT '요청 헤더 정보',
  `parameter` text DEFAULT NULL COMMENT '요청 파라미터 정보',
  `form_parameter` text DEFAULT NULL,
  `response_body` longtext DEFAULT NULL COMMENT '응답 바디',
  `response_header` text DEFAULT NULL COMMENT '응답 헤더',
  PRIMARY KEY (`id`),
  KEY `history_project_FK` (`project_id`),
  KEY `history_users_FK` (`user_id`),
  CONSTRAINT `history_users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=484 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='API 요청 기록을 관리하는 테이블';

-- ttalkkag.dataset definition

CREATE TABLE `dataset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'dataset 테이블 PK',
  `project_id` bigint(20) NOT NULL COMMENT '데이터셋이 생성된 프로젝트 id',
  `name` varchar(100) DEFAULT NULL COMMENT 'dataset 이름',
  `description` varchar(100) DEFAULT NULL COMMENT 'dataset 설명',
  `created_at` datetime DEFAULT NULL COMMENT 'dataset 생성 시간',
  PRIMARY KEY (`id`),
  KEY `dataset_project_FK` (`project_id`),
  CONSTRAINT `dataset_project_FK` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='사용자가 자주 사용하는 데이터셋이 등록된 테이블';

-- ttalkkag.dataset_variable definition

CREATE TABLE `dataset_variable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'dataset_variables 테이블 PK',
  `dataset_id` bigint(20) NOT NULL COMMENT '포함된 dataset id',
  `type` varchar(100) NOT NULL COMMENT '변수 타입',
  `name` varchar(100) NOT NULL COMMENT '변수명',
  `description` varchar(100) DEFAULT NULL COMMENT '변수 설명',
  `created_at` datetime DEFAULT NULL COMMENT '변수 생성 시간',
  PRIMARY KEY (`id`),
  KEY `dataset_variables_datasets_FK` (`dataset_id`),
  CONSTRAINT `dataset_variables_datasets_FK` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='데이터셋에 포함된 변수 모음 테이블';

-- ttalkkag.site definition

CREATE TABLE `site` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL COMMENT '해당 사이트가 속한 프로젝트 id',
  `name` varchar(100) NOT NULL,
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `site_project_FK` (`project_id`),
  CONSTRAINT `site_project_FK` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='환경변수를 관리하기 위한 프로젝트 하위 사이트';

-- ttalkkag.environment definition

CREATE TABLE `environment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '개발 환경 테이블 PK',
  `site_id` bigint(20) NOT NULL COMMENT '현재 개발 환경이 속한 사이트 id',
  `name` varchar(100) DEFAULT NULL COMMENT '개발 환경 이름',
  PRIMARY KEY (`id`),
  KEY `environment_site_FK` (`site_id`),
  CONSTRAINT `environment_site_FK` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='개발 환경 테이블';

-- ttalkkag.environment_variable definition

CREATE TABLE `environment_variable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '개발 환경 변수 PK',
  `environment_id` bigint(20) NOT NULL COMMENT '환경 변수가 속한 개발 환경 id',
  `key` varchar(100) DEFAULT NULL COMMENT '환경 변수 키',
  `value` varchar(100) NOT NULL COMMENT '환경 변수 값',
  PRIMARY KEY (`id`),
  KEY `environment_variable_environment_FK` (`environment_id`),
  CONSTRAINT `environment_variable_environment_FK` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='개발 환경 변수 테이블';



