CREATE DATABASE if not exists headerdb;

CREATE USER if not exists 'ohgiraffers'@'%' IDENTIFIED BY 'ohgiraffers';

GRANT ALL PRIVILEGES ON headerdb.* TO 'ohgiraffers'@'%';

USE headerdb;

-- 가장 먼저 참조가 많은 테이블의 자식부터 삭제
DROP TABLE IF EXISTS `TBL_SHOP_MSG_HISTORY`;
DROP TABLE IF EXISTS `TBL_MSG_SEND_BATCH`;
DROP TABLE IF EXISTS `TBL_VISITORS`;
DROP TABLE IF EXISTS `TBL_SALES`;
DROP TABLE IF EXISTS `TBL_RESERVATION`;
DROP TABLE IF EXISTS `TBL_MESSAGE_TEMPLATE`;
DROP TABLE IF EXISTS `TBL_MENU`;
DROP TABLE IF EXISTS `TBL_MENU_CATEGORY`;
DROP TABLE IF EXISTS `TBL_SHOP`;
DROP TABLE IF EXISTS `TBL_SHOP_CATEGORY`;
DROP TABLE IF EXISTS `TBL_USER`;

-- USER table
CREATE TABLE IF NOT EXISTS `TBL_USER` (
    `USER_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `USER_ID` VARCHAR(20) NULL COMMENT '아이디',
    `USER_PWD` VARCHAR(255) NULL COMMENT '비밀번호',
    `IS_ADMIN` BOOLEAN NOT NULL DEFAULT 0 COMMENT '관리자여부',
    `USER_NAME` VARCHAR(255) NOT NULL COMMENT '이름',
    `USER_PHONE` VARCHAR(20) NOT NULL COMMENT '전화번호',
    `BIRTHDAY` DATE NULL COMMENT '고객생일',
    `IS_LEAVE` BOOLEAN NOT NULL DEFAULT 0 COMMENT '탈퇴여부'
    );

-- SHOP_CATEGORY table
CREATE TABLE IF NOT EXISTS `TBL_SHOP_CATEGORY` (
   `CATEGORY_CODE` INT PRIMARY KEY AUTO_INCREMENT,
   `CATEGORY_NAME` VARCHAR(50) NOT NULL COMMENT '카테고리 이름'
    );

-- SHOP table
CREATE TABLE IF NOT EXISTS `TBL_SHOP` (
    `SHOP_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `CATEGORY_CODE` INT NOT NULL COMMENT '샵 유형 코드',
    `ADMIN_CODE` INT NOT NULL COMMENT '관리자 코드',
    `SHOP_NAME` VARCHAR(50) NOT NULL COMMENT '샵 이름',
    `SHOP_PHONE` VARCHAR(20) NOT NULL COMMENT '샵 전화번호',
    `SHOP_LOCATION` VARCHAR(255) NOT NULL COMMENT '샵 주소',
    `SHOP_LONG` DECIMAL(10,7) NOT NULL COMMENT '샵 경도',
    `SHOP_LA` DECIMAL(10,7) NOT NULL COMMENT '샵 위도',
    `SHOP_STATUS` VARCHAR(20) NOT NULL COMMENT '샵 상태',
    `SHOP_OPEN` VARCHAR(5) NOT NULL COMMENT '샵 운영시간',
    `SHOP_CLOSE` VARCHAR(5) NOT NULL COMMENT '샵 닫는시간',
    `IS_ACTIVE` BOOLEAN NOT NULL DEFAULT 1 COMMENT '활성 여부'
    );

-- MENU_CATEGORY table (modified to include SHOP_CODE as FK and composite PK)
CREATE TABLE IF NOT EXISTS `TBL_MENU_CATEGORY` (
    `CATEGORY_CODE` INT NOT NULL COMMENT '카테고리 코드',
    `SHOP_CODE` INT NOT NULL COMMENT '샵 코드',
    `CATEGORY_NAME` VARCHAR(40) NOT NULL COMMENT '카테고리명',
    `MENU_COLOR` VARCHAR(20) NOT NULL COMMENT '대표 색상',
    `IS_ACTIVE` BOOLEAN NOT NULL DEFAULT 1 COMMENT '활성 여부',
    PRIMARY KEY (`CATEGORY_CODE`, `SHOP_CODE`)
    );

-- MENU table (modified to remove SHOP_CODE direct FK, now references through MENU_CATEGORY)
CREATE TABLE IF NOT EXISTS `TBL_MENU` (
    `MENU_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `CATEGORY_CODE` INT NOT NULL COMMENT '카테고리 코드',
    `SHOP_CODE` INT NOT NULL COMMENT '샵 코드',
    `MENU_NAME` VARCHAR(40) NOT NULL COMMENT '시술명',
    `MENU_PRICE` INT NOT NULL COMMENT '시술가격',
    `EST_TIME` INT NOT NULL COMMENT '예상소요시간',
    `IS_ACTIVE` BOOLEAN NOT NULL DEFAULT 1 COMMENT '활성 여부'
    );

-- MESSAGE_TEMPLATE table
CREATE TABLE IF NOT EXISTS `TBL_MESSAGE_TEMPLATE` (
    `TEMPLATE_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `SHOP_CODE` INT COMMENT '샵 코드',
    `TEMPLATE_TITLE` VARCHAR(50) NOT NULL COMMENT '템플릿 제목',
    `TEMPLATE_CONTENT` VARCHAR(255) NOT NULL COMMENT '템플릿 내용',
    `TEMPLATE_TYPE` VARCHAR(20) NOT NULL COMMENT '템플릿 타입'
    );

-- RESERVATION table
CREATE TABLE IF NOT EXISTS `TBL_RESERVATION` (
    `RESV_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `USER_CODE` INT NOT NULL COMMENT '회원코드',
    `SHOP_CODE` INT NOT NULL COMMENT '샵 코드',
    `MENU_CODE` INT NOT NULL COMMENT '시술 코드',
    `RESV_DATE` DATE NOT NULL COMMENT '예약 날짜',
    `RESV_TIME` TIME NOT NULL COMMENT '예약 시간',
    `USER_COMMENT` VARCHAR(255) NULL COMMENT '메모',
    `RESV_STATE` VARCHAR(20) NOT NULL DEFAULT '예약확정' COMMENT '예약 상태'
    );

-- SALES table (modified PAY_DATETIME to DATETIME)
CREATE TABLE IF NOT EXISTS `TBL_SALES` (
    `SALES_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `RESV_CODE` INT NOT NULL COMMENT '예약코드',
    `PAY_AMOUNT` INT NOT NULL COMMENT '결제 금액',
    `PAY_METHOD` VARCHAR(20) NOT NULL COMMENT '결제 수단',
    `PAY_DATETIME` DATETIME NOT NULL COMMENT '결제일시',
    `PAY_STATUS` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '결제상태 (COMPLETED, CANCELLED, PARTIAL_CANCELLED, DELETED)',

    -- 취소 관련 컬럼 추가
    `CANCEL_AMOUNT` INT NULL DEFAULT 0 COMMENT '취소 금액',
    `CANCEL_DATETIME` DATETIME NULL COMMENT '취소일시',
    `CANCEL_REASON` VARCHAR(255) NULL COMMENT '취소 사유',
    `FINAL_AMOUNT` INT NOT NULL COMMENT '최종 결제 금액 (결제금액 - 취소금액)'
    );

-- VISITORS table
CREATE TABLE IF NOT EXISTS `TBL_VISITORS` (
    `CLIENT_CODE` INT PRIMARY KEY AUTO_INCREMENT,
    `USER_CODE` INT NOT NULL COMMENT '회원코드',
    `SHOP_CODE` INT NOT NULL COMMENT '샵 코드',
    `MEMO` VARCHAR(255) NULL COMMENT '메모',
    `SENDABLE` BOOLEAN NOT NULL DEFAULT 0 COMMENT '광고성수신여부',
    `IS_ACTIVE` BOOLEAN NOT NULL DEFAULT 1 COMMENT '활성여부'
    );

-- TBL_MSG_SEND_BATCH
CREATE TABLE IF NOT EXISTS `TBL_MSG_SEND_BATCH` (
    `BATCH_CODE` INT PRIMARY KEY AUTO_INCREMENT COMMENT '발송 배치 코드',
    `SHOP_CODE` INT NOT NULL COMMENT '샵 코드',
    `TEMPLATE_CODE` INT COMMENT '사용된 템플릿 코드',
    `SEND_DATE` DATE NOT NULL COMMENT '발송 날짜',
    `SEND_TIME` TIME NOT NULL COMMENT '발송 시간',
    `SEND_TYPE` VARCHAR(20) NOT NULL COMMENT '발송 타입 (INDIVIDUAL, GROUP)',
    `SUBJECT` VARCHAR(100) COMMENT '메시지 제목/요약',
    `TOTAL_COUNT` INT DEFAULT 0 COMMENT '총 발송 건수',
    `SUCCESS_COUNT` INT DEFAULT 0 COMMENT '성공 건수',
    `FAIL_COUNT` INT DEFAULT 0 COMMENT '실패 건수',
    `CREATED_AT` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_shop_batch (`SHOP_CODE`, `BATCH_CODE`),
    INDEX idx_shop_date (`SHOP_CODE`, `SEND_DATE`),
    INDEX idx_created_at (CREATED_AT)
    );

-- SHOP_MSG_HISTORY table
CREATE TABLE IF NOT EXISTS `TBL_SHOP_MSG_HISTORY` (
    `HISTORY_CODE` INT PRIMARY KEY AUTO_INCREMENT COMMENT '히스토리 코드',
    `BATCH_CODE` INT NOT NULL COMMENT '발송 배치 코드',
    `USER_CODE` INT NOT NULL COMMENT '수신자',
    `MSG_CONTENT` TEXT NOT NULL COMMENT '실제 발송된 메시지 내용',
    `SEND_STATUS` VARCHAR(20) NOT NULL COMMENT '발송 상태 (PENDING, SUCCESS, FAIL)',
    `ERROR_MESSAGE` VARCHAR(255) COMMENT '오류 메시지',
    `SENT_AT` TIMESTAMP COMMENT '실제 발송 시간',
    FOREIGN KEY (`BATCH_CODE`) REFERENCES `TBL_MSG_SEND_BATCH`(`BATCH_CODE`) ON DELETE CASCADE,
    INDEX idx_batch_code (`BATCH_CODE`),
    INDEX idx_user_code (`USER_CODE`),
    INDEX idx_send_status (`SEND_STATUS`),
    INDEX idx_history_user (`HISTORY_CODE`, `USER_CODE`)
    );

-- Set Foreign Key Constraints
ALTER TABLE `TBL_SHOP` ADD CONSTRAINT `FK_TBL_SHOP_CATEGORY_TO_SHOP_1` FOREIGN KEY (`CATEGORY_CODE`) REFERENCES `TBL_SHOP_CATEGORY` (`CATEGORY_CODE`);
ALTER TABLE `TBL_SHOP` ADD CONSTRAINT `FK_TBL_USER_TO_SHOP_1` FOREIGN KEY (`ADMIN_CODE`) REFERENCES `TBL_USER` (`USER_CODE`);

-- Modified FK for MENU_CATEGORY (now references SHOP table)
ALTER TABLE `TBL_MENU_CATEGORY` ADD CONSTRAINT `FK_TBL_SHOP_TO_MENU_CATEGORY_1` FOREIGN KEY (`SHOP_CODE`) REFERENCES `TBL_SHOP` (`SHOP_CODE`);

-- Modified FK for MENU (now references MENU_CATEGORY with composite key)
ALTER TABLE `TBL_MENU` ADD CONSTRAINT `FK_TBL_MENU_CATEGORY_TO_MENU_1` FOREIGN KEY (`CATEGORY_CODE`, `SHOP_CODE`) REFERENCES `TBL_MENU_CATEGORY` (`CATEGORY_CODE`, `SHOP_CODE`);

ALTER TABLE `TBL_MESSAGE_TEMPLATE` ADD CONSTRAINT `FK_TBL_SHOP_TO_MESSAGE_TEMPLATE_1` FOREIGN KEY (`SHOP_CODE`) REFERENCES `TBL_SHOP` (`SHOP_CODE`);

ALTER TABLE `TBL_RESERVATION` ADD CONSTRAINT `FK_TBL_USER_TO_RESERVATION_1` FOREIGN KEY (`USER_CODE`) REFERENCES `TBL_USER` (`USER_CODE`);
ALTER TABLE `TBL_RESERVATION` ADD CONSTRAINT `FK_TBL_SHOP_TO_RESERVATION_1` FOREIGN KEY (`SHOP_CODE`) REFERENCES `TBL_SHOP` (`SHOP_CODE`);
ALTER TABLE `TBL_RESERVATION` ADD CONSTRAINT `FK_TBL_MENU_TO_RESERVATION_1` FOREIGN KEY (`MENU_CODE`) REFERENCES `TBL_MENU` (`MENU_CODE`);

ALTER TABLE `TBL_SALES` ADD CONSTRAINT `FK_TBL_RESERVATION_TO_SALES_1` FOREIGN KEY (`RESV_CODE`) REFERENCES `TBL_RESERVATION` (`RESV_CODE`);

ALTER TABLE `TBL_VISITORS` ADD CONSTRAINT `FK_TBL_USER_TO_VISITORS_1` FOREIGN KEY (`USER_CODE`) REFERENCES `TBL_USER` (`USER_CODE`);
ALTER TABLE `TBL_VISITORS` ADD CONSTRAINT `FK_TBL_SHOP_TO_VISITORS_1` FOREIGN KEY (`SHOP_CODE`) REFERENCES `TBL_SHOP` (`SHOP_CODE`);

ALTER TABLE `TBL_SHOP_MSG_HISTORY` ADD CONSTRAINT `FK_TBL_USER_TO_SHOP_MSG_HISTORY_1` FOREIGN KEY (`USER_CODE`) REFERENCES `TBL_USER` (`USER_CODE`);
ALTER TABLE `TBL_SHOP_MSG_HISTORY` ADD CONSTRAINT `FK_TBL_MSG_SEND_BATCH_TO_SHOP_MSG_HISTORY_1` FOREIGN KEY (`BATCH_CODE`) REFERENCES `TBL_MSG_SEND_BATCH` (`BATCH_CODE`);

ALTER TABLE `TBL_MSG_SEND_BATCH` ADD CONSTRAINT `FK_TBL_MESSAGE_TEMPLATE_TO_MSG_SEND_BATCH_1` FOREIGN KEY (`TEMPLATE_CODE`) REFERENCES `TBL_MESSAGE_TEMPLATE` (`TEMPLATE_CODE`);
ALTER TABLE `TBL_MSG_SEND_BATCH` ADD CONSTRAINT `FK_TBL_SHOP_TO_MSG_SEND_BATCH_1` FOREIGN KEY (`SHOP_CODE`) REFERENCES `TBL_SHOP` (`SHOP_CODE`);

-- 테이블별 데이터 삽입
-- TBL_USER (2명 관리자, 98명 일반)
INSERT INTO TBL_USER (USER_ID, USER_PWD, IS_ADMIN, USER_NAME, USER_PHONE, BIRTHDAY, IS_LEAVE) VALUES
-- 관리자
('hwangkyungmi', 'hwangkyungmi', 1, '황경미', '010-1001-1001', '1985-03-15', 0),
('kwoneunji', 'kwoneunji', 1, '권은지', '010-1002-1002', '1987-07-22', 0),

-- 일반 사용자 98명
('honggildong', 'honggildong', 0, '홍길동', '010-1003-1003', '1995-05-12', 0),
('kimcheolsu', 'kimcheolsu', 0, '김철수', '010-1004-1004', '1988-08-08', 0),
('leeyounghee', 'leeyounghee', 0, '이영희', '010-1005-1005', '1992-11-04', 0),
('choiminsu', 'choiminsu', 0, '최민수', '010-1006-1006', '1990-03-18', 0),
('parkjisung', 'parkjisung', 0, '박지성', '010-1007-1007', '1985-07-10', 0),
('sonheungmin', 'sonheungmin', 0, '손흥민', '010-1008-1008', '1998-03-22', 0),
('jungyumi', 'jungyumi', 0, '정유미', '010-1009-1009', '1996-09-14', 0),
('kimtaehee', 'kimtaehee', 0, '김태희', '010-1010-1010', '1980-03-29', 0),
('yoojaesuk', 'yoojaesuk', 0, '유재석', '010-1011-1011', '1970-08-14', 0),
('kanghodong', 'kanghodong', 0, '강호동', '010-1012-1012', '1972-06-11', 0),
('leesugeun', 'leesugeun', 0, '이수근', '010-1013-1013', '1975-02-03', 0),
('haha14', 'haha14', 0, '하하', '010-1014-1014', '1979-08-20', 0),
('parkmyungsu', 'parkmyungsu', 0, '박명수', '010-1015-1015', '1970-12-01', 0),
('chosaeho', 'chosaeho', 0, '조세호', '010-1016-1016', '1982-08-09', 0),
('yangsehyung', 'yangsehyung', 0, '양세형', '010-1017-1017', '1985-04-21', 0),
('kimjunho', 'kimjunho', 0, '김준호', '010-1018-1018', '1975-10-25', 0),
('leekyunggyu', 'leekyunggyu', 0, '이경규', '010-1019-1019', '1960-09-21', 0),
('jiseokjin', 'jiseokjin', 0, '지석진', '010-1020-1020', '1966-02-10', 0),
('kimjongkook', 'kimjongkook', 0, '김종국', '010-1021-1021', '1976-04-25', 0),
('yooheeyeol', 'yooheeyeol', 0, '유희열', '010-1022-1022', '1971-04-19', 0),
('yoonjongshin', 'yoonjongshin', 0, '윤종신', '010-1023-1023', '1969-10-15', 0),
('jangbeomjun', 'jangbeomjun', 0, '장범준', '010-1024-1024', '1989-05-01', 0),
('leejeok', 'leejeok', 0, '이적', '010-1025-1025', '1974-02-28', 0),
('leeseunggi', 'leeseunggi', 0, '이승기', '010-1026-1026', '1987-01-13', 0),
('iu1234', 'iu1234', 0, '아이유', '010-1027-1027', '1993-05-16', 0),
('parkbogum', 'parkbogum', 0, '박보검', '010-1028-1028', '1993-06-16', 0),
('suzy29', 'suzy29', 0, '수지', '010-1029-1029', '1994-10-10', 0),
('junghaein', 'junghaein', 0, '정해인', '010-1030-1030', '1988-04-01', 0),
('songjoongki', 'songjoongki', 0, '송중기', '010-1031-1031', '1985-09-19', 0),
('songhyekyo', 'songhyekyo', 0, '송혜교', '010-1032-1032', '1981-11-22', 0),
('leejongsuk', 'leejongsuk', 0, '이종석', '010-1033-1033', '1989-09-14', 0),
('hanjiho', 'hanjiho', 0, '한지호', '010-1034-1034', '1990-12-05', 0),
('parkshinhye', 'parkshinhye', 0, '박신혜', '010-1035-1035', '1990-02-18', 0),
('leedongwook', 'leedongwook', 0, '이동욱', '010-1036-1036', '1981-11-06', 0),
('kimyoojung', 'kimyoojung', 0, '김유정', '010-1037-1037', '1999-09-22', 0),
('namjoohyuk', 'namjoohyuk', 0, '남주혁', '010-1038-1038', '1994-02-22', 0),
('leesungkyung', 'leesungkyung', 0, '이성경', '010-1039-1039', '1990-08-10', 0),
('parkseojoon', 'parkseojoon', 0, '박서준', '010-1040-1040', '1988-12-16', 0),
('kimsoeun', 'kimsoeun', 0, '김소은', '010-1041-1041', '1989-09-06', 0),
('yooseungho', 'yooseungho', 0, '유승호', '010-1042-1042', '1993-08-17', 0),
('parkminah', 'parkminah', 0, '박민아', '010-1043-1043', '1986-03-02', 0),
('limyoona', 'limyoona', 0, '임윤아', '010-1044-1044', '1990-05-30', 0),
('kwonyu', 'kwonyu', 0, '권유', '010-1045-1045', '1988-12-09', 0),
('jeonjiho', 'jeonjiho', 0, '전지호', '010-1046-1046', '1992-07-11', 0),
('kangmina', 'kangmina', 0, '강민아', '010-1047-1047', '1991-04-03', 0),
('choijiwoo', 'choijiwoo', 0, '최지우', '010-1048-1048', '1975-06-11', 0),
('leeboyoung', 'leeboyoung', 0, '이보영', '010-1049-1049', '1979-01-12', 0),
('kimnamjoo', 'kimnamjoo', 0, '김남주', '010-1050-1050', '1971-05-10', 0),
('chajihyun', 'chajihyun', 0, '차지현', '010-1051-1051', '1982-02-22', 0),
('ohyeonsu', 'ohyeonsu', 0, '오현수', '010-1052-1052', '1962-10-17', 0),
('leebyunghun', 'leebyunghun', 0, '이병헌', '010-1053-1053', '1970-07-12', 0),
('choiminjung', 'choiminjung', 0, '최민정', '010-1054-1054', '1990-03-15', 0),
('parkjinyoung', 'parkjinyoung', 0, '박진영', '010-1055-1055', '1971-12-13', 0),
('leehyori', 'leehyori', 0, '이효리', '010-1056-1056', '1979-05-10', 0),
('rain57', 'rain57', 0, '비', '010-1057-1057', '1982-06-25', 0),
('boa058', 'boa058', 0, '보아', '010-1058-1058', '1986-11-05', 0),
('sevenkim', 'sevenkim', 0, '세븐', '010-1059-1059', '1984-11-09', 0),
('shindongyeop', 'shindongyeop', 0, '신동엽', '010-1060-1060', '1971-02-17', 0),
('kimgurae', 'kimgurae', 0, '김구라', '010-1061-1061', '1970-01-03', 0),
('seojanghoon', 'seojanghoon', 0, '서장훈', '010-1062-1062', '1974-01-05', 0),
('leesangmin', 'leesangmin', 0, '이상민', '010-1063-1063', '1973-01-13', 0),
('kimjongmin', 'kimjongmin', 0, '김종민', '010-1064-1064', '1979-09-24', 0),
('moonhejun', 'moonhejun', 0, '문희준', '010-1065-1065', '1978-03-19', 0),
('kimtaewoo', 'kimtaewoo', 0, '김태우', '010-1066-1066', '1981-05-12', 0),
('kangta', 'kangta', 0, '강타', '010-1067-1067', '1979-10-10', 0),
('leejaewon', 'leejaewon', 0, '이재원', '010-1068-1068', '1980-02-11', 0),
('anyoungmi', 'anyoungmi', 0, '안영미', '010-1069-1069', '1983-10-30', 0),
('kimsukyung', 'kimsukyung', 0, '김숙경', '010-1070-1070', '1975-04-16', 0),
('parkmisun', 'parkmisun', 0, '박미선', '010-1071-1071', '1967-12-25', 0),
('songeunji', 'songeunji', 0, '송은지', '010-1072-1072', '1973-02-08', 0),
('kimjimin', 'kimjimin', 0, '김지민', '010-1073-1073', '1984-09-05', 0),
('leejiyeon', 'leejiyeon', 0, '이지연', '010-1074-1074', '1987-06-18', 0),
('parkjihyun', 'parkjihyun', 0, '박지현', '010-1075-1075', '1990-11-12', 0),
('choihyojung', 'choihyojung', 0, '최효정', '010-1076-1076', '1993-01-24', 0),
('kangseulgi', 'kangseulgi', 0, '강슬기', '010-1077-1077', '1994-02-10', 0),
('parksooyoung', 'parksooyoung', 0, '박수영', '010-1078-1078', '1996-09-03', 0),
('kimyeri', 'kimyeri', 0, '김예리', '010-1079-1079', '1999-03-05', 0),
('leedonghyuk', 'leedonghyuk', 0, '이동혁', '010-1080-1080', '2000-07-06', 0),
('parkjaemin', 'parkjaemin', 0, '박재민', '010-1081-1081', '2000-08-13', 0),
('leejeno', 'leejeno', 0, '이제노', '010-1082-1082', '2000-04-23', 0),
('hwangrenjun', 'hwangrenjun', 0, '황런쥔', '010-1083-1083', '2000-03-23', 0),
('najaeemin', 'najaeemin', 0, '나재민', '010-1084-1084', '2000-08-13', 0),
('zhongchenle', 'zhongchenle', 0, '종천러', '010-1085-1085', '2001-11-22', 0),
('parkjisung', 'parkjisung', 0, '박지성', '010-1086-1086', '2002-02-05', 0),
('kimminju', 'kimminju', 0, '김민주', '010-1087-1087', '2001-02-05', 0),
('jangwonyoung', 'jangwonyoung', 0, '장원영', '010-1088-1088', '2004-08-31', 0),
('anyu-jin', 'anyu-jin', 0, '안유진', '010-1089-1089', '2003-09-01', 0),
('choiyena', 'choiyena', 0, '최예나', '010-1090-1090', '1999-09-17', 0),
('leeseoya', 'leeseoya', 0, '이서연', '010-1091-1091', '2001-06-04', 0),
('kwaneunbi', 'kwaneunbi', 0, '권은비', '010-1092-1092', '1995-09-27', 0),
('miyawakisakura', 'miyawakisakura', 0, '미야와키사쿠라', '010-1093-1093', '1998-03-19', 0),
('kimchaewon', 'kimchaewon', 0, '김채원', '010-1094-1094', '2000-08-01', 0),
('hongeunchae', 'hongeunchae', 0, '홍은채', '010-1095-1095', '2006-11-10', 0),
('huhjin', 'huhjin', 0, '허윤진', '010-1096-1096', '2001-10-08', 0),
('nakoyabuki', 'nakoyabuki', 0, '나코야부키', '010-1097-1097', '2001-06-04', 0),
('leegahyeon', 'leegahyeon', 0, '이가현', '010-1098-1098', '1999-02-03', 0),
('kimyoobin', 'kimyoobin', 0, '김유빈', '010-1099-1099', '2002-09-02', 0),
('parkseoeun', 'parkseoeun', 0, '박서은', '010-1100-1100', '2003-01-21', 0);

-- TBL_SHOP_CATEGORY (6 rows)
INSERT INTO TBL_SHOP_CATEGORY (CATEGORY_NAME) VALUES
('헤어샵'),
('네일샵'),
('왁싱샵'),
('마사지샵'),
('에스테틱'),
('바버샵');

-- 수정된 샘플 데이터 (변경된 스키마에 맞춤)

-- TBL_SHOP (5 rows - 관리자별로 2-3개씩)
INSERT INTO TBL_SHOP (CATEGORY_CODE, ADMIN_CODE, SHOP_NAME, SHOP_PHONE, SHOP_LOCATION, SHOP_LONG, SHOP_LA, SHOP_STATUS, SHOP_OPEN, SHOP_CLOSE, IS_ACTIVE) VALUES
-- 황경미 관리자 (3개 샵)
(1, 1, '뷰티헤어 강남점', '02-111-2222', '서울 강남구 강남대로 123', 127.0275830, 37.4979420, '영업중', '09:00', '21:00', 1),
(1, 1, '뷰티헤어 홍대점', '02-333-4444', '서울 마포구 양화로 44', 126.9106540, 37.5563700, '영업중', '10:00', '20:00', 1),
(1, 1, '뷰티헤어 신촌점', '02-555-6666', '서울 서대문구 신촌로 88', 126.9365680, 37.5581470, '영업중', '09:30', '20:30', 1),
-- 권은지 관리자 (2개 샵)
(1, 2, '엘레강스 헤어 잠실점', '02-777-8888', '서울 송파구 올림픽로 300', 127.0983120, 37.5133070, '영업중', '09:00', '21:00', 1),
(1, 2, '엘레강스 헤어 압구정점', '02-999-0000', '서울 강남구 압구정로 145', 127.0276430, 37.5272970, '영업중', '10:00', '20:00', 1);

-- TBL_MENU_CATEGORY (수정: 각 샵별로 카테고리 생성, 복합키 사용)
INSERT INTO TBL_MENU_CATEGORY (CATEGORY_CODE, SHOP_CODE, CATEGORY_NAME, MENU_COLOR, IS_ACTIVE) VALUES
-- 뷰티헤어 강남점 (SHOP_CODE: 1)
(1, 1, '컷', '#F7CAC9', 1),
(2, 1, '염색', '#779ECB', 1),
(3, 1, '파마', '#F49AC2', 1),
(4, 1, '스타일링', '#B284BE', 1),
(5, 1, '두피케어', '#FFB347', 1),

-- 뷰티헤어 홍대점 (SHOP_CODE: 2)
(1, 2, '컷', '#F7CAC9', 1),
(2, 2, '염색', '#779ECB', 1),
(3, 2, '파마', '#F49AC2', 1),
(4, 2, '스타일링', '#B284BE', 1),
(5, 2, '두피케어', '#FFB347', 1),

-- 뷰티헤어 신촌점 (SHOP_CODE: 3)
(1, 3, '컷', '#F7CAC9', 1),
(2, 3, '염색', '#779ECB', 1),
(3, 3, '파마', '#F49AC2', 1),
(4, 3, '스타일링', '#B284BE', 1),
(5, 3, '두피케어', '#FFB347', 1),

-- 엘레강스 헤어 잠실점 (SHOP_CODE: 4)
(1, 4, '컷', '#F7CAC9', 1),
(2, 4, '염색', '#779ECB', 1),
(3, 4, '파마', '#F49AC2', 1),
(4, 4, '스타일링', '#B284BE', 1),
(5, 4, '두피케어', '#FFB347', 1),

-- 엘레강스 헤어 압구정점 (SHOP_CODE: 5)
(1, 5, '컷', '#F7CAC9', 1),
(2, 5, '염색', '#779ECB', 1),
(3, 5, '파마', '#F49AC2', 1),
(4, 5, '스타일링', '#B284BE', 1),
(5, 5, '두피케어', '#FFB347', 1);

-- TBL_MENU (수정: 복합키 참조 CATEGORY_CODE, SHOP_CODE 사용)
INSERT INTO TBL_MENU (CATEGORY_CODE, SHOP_CODE, MENU_NAME, MENU_PRICE, EST_TIME, IS_ACTIVE) VALUES
-- 뷰티헤어 강남점 (SHOP_CODE: 1)
(1, 1, '여성 컷', 25000, 30, 1),
(1, 1, '남성 컷', 20000, 25, 1),
(2, 1, '전체 염색', 80000, 120, 1),
(2, 1, '포인트 염색', 50000, 60, 1),
(3, 1, '디지털 파마', 120000, 150, 1),
(3, 1, '일반 파마', 80000, 120, 1),
(4, 1, '업스타일링', 40000, 45, 1),
(5, 1, '두피 스케일링', 50000, 60, 1),

-- 뷰티헤어 홍대점 (SHOP_CODE: 2)
(1, 2, '여성 컷', 23000, 30, 1),
(1, 2, '남성 컷', 18000, 25, 1),
(2, 2, '전체 염색', 75000, 120, 1),
(2, 2, '탈색 + 염색', 120000, 180, 1),
(3, 2, '볼륨 파마', 90000, 120, 1),
(3, 2, '매직 스트레이트', 100000, 150, 1),
(4, 2, '웨딩 스타일링', 80000, 90, 1),
(5, 2, '탈모 케어', 70000, 80, 1),

-- 뷰티헤어 신촌점 (SHOP_CODE: 3)
(1, 3, '여성 컷', 22000, 30, 1),
(1, 3, '남성 컷', 17000, 25, 1),
(2, 3, '전체 염색', 70000, 120, 1),
(2, 3, '하이라이트', 90000, 150, 1),
(3, 3, '셋팅 파마', 85000, 120, 1),
(4, 3, '일반 스타일링', 35000, 40, 1),
(5, 3, '두피 마사지', 40000, 50, 1),

-- 엘레강스 헤어 잠실점 (SHOP_CODE: 4)
(1, 4, '프리미엄 여성 컷', 35000, 40, 1),
(1, 4, '프리미엄 남성 컷', 28000, 30, 1),
(2, 4, '프리미엄 염색', 100000, 150, 1),
(2, 4, '옴브레 염색', 130000, 180, 1),
(3, 4, '프리미엄 파마', 150000, 180, 1),
(4, 4, '특별 스타일링', 60000, 60, 1),
(5, 4, '프리미엄 두피케어', 80000, 90, 1),

-- 엘레강스 헤어 압구정점 (SHOP_CODE: 5)
(1, 5, '럭셔리 여성 컷', 40000, 45, 1),
(1, 5, '럭셔리 남성 컷', 32000, 35, 1),
(2, 5, '럭셔리 염색', 120000, 180, 1),
(2, 5, '발레아주 염색', 150000, 210, 1),
(3, 5, '럭셔리 파마', 180000, 200, 1),
(4, 5, '럭셔리 스타일링', 70000, 70, 1),
(5, 5, 'VIP 두피케어', 100000, 100, 1);

-- TBL_MESSAGE_TEMPLATE (각 샵별 템플릿) - 25건
INSERT INTO TBL_MESSAGE_TEMPLATE (SHOP_CODE, TEMPLATE_TITLE, TEMPLATE_CONTENT, TEMPLATE_TYPE) VALUES
(1, '예약확정안내', '예약이 확정되었습니다. 뷰티헤어 강남점', 'RESV_CONFIRM'),
(1, '결제완료안내', '결제가 완료되었습니다. 감사합니다.', 'PAYMENT'),
(1, '예약취소안내', '예약이 취소되었습니다.', 'RESV_CANCEL'),
(1, '환영인사', '뷰티헤어 강남점 방문을 환영합니다.', 'WELCOME'),
(1, '시술완료안내', '시술이 완료되었습니다. 만족스러우셨길 바랍니다.', 'COMPLETE'),
(2, '홍대점예약확정', '홍대점 예약 확정 안내', 'RESV_CONFIRM'),
(2, '홍대점결제완료', '결제 완료되었습니다.', 'PAYMENT'),
(2, '홍대점예약취소', '예약 취소 처리되었습니다.', 'RESV_CANCEL'),
(2, '홍대점환영', '뷰티헤어 홍대점에 오신 것을 환영합니다.', 'WELCOME'),
(2, '홍대점시술완료', '시술 완료! 다음에도 방문해주세요.', 'COMPLETE'),
(3, '신촌점예약확정', '신촌점 예약 확정', 'RESV_CONFIRM'),
(3, '신촌점결제완료', '결제가 정상 처리되었습니다.', 'PAYMENT'),
(3, '신촌점예약취소', '고객 요청으로 예약이 취소되었습니다.', 'RESV_CANCEL'),
(3, '신촌점환영', '뷰티헤어 신촌점 방문 감사합니다.', 'WELCOME'),
(3, '신촌점시술완료', '오늘 시술이 모두 완료되었습니다.', 'COMPLETE'),
(4, '잠실점예약확정', '엘레강스 헤어 잠실점 예약 확정', 'RESV_CONFIRM'),
(4, '잠실점결제완료', '프리미엄 서비스 결제 완료', 'PAYMENT'),
(4, '잠실점예약취소', '예약 취소 안내', 'RESV_CANCEL'),
(4, '잠실점환영', '엘레강스 헤어 잠실점 VIP 고객님 환영합니다', 'WELCOME'),
(4, '잠실점시술완료', '프리미엄 서비스가 완료되었습니다', 'COMPLETE'),
(5, '압구정점예약확정', '압구정점 럭셔리 예약 확정', 'RESV_CONFIRM'),
(5, '압구정점결제완료', '럭셔리 서비스 결제 완료', 'PAYMENT'),
(5, '압구정점예약취소', '예약 취소 처리 완료', 'RESV_CANCEL'),
(5, '압구정점환영', '엘레강스 헤어 압구정점 귀빈 고객님 환영', 'WELCOME'),
(5, '압구정점시술완료', '럭셔리 서비스 완료. 다시 찾아주세요', 'COMPLETE');

-- TBL_RESERVATION (100건 - 1번샵 1,2번만 시술완료, 나머지는 예약확정)
INSERT INTO TBL_RESERVATION (USER_CODE, SHOP_CODE, MENU_CODE, RESV_DATE, RESV_TIME, USER_COMMENT, RESV_STATE) VALUES
-- 1번샵 1,2번 예약
(3, 1, 1, '2025-06-15', '10:00:00', '레이어드 컷 원해요', '시술완료'),
(4, 1, 2, '2025-06-16', '11:00:00', NULL, '시술완료'),

-- 나머지 98건은 모두 예약확정
(5, 1, 3, '2025-06-17', '14:00:00', '애쉬브라운으로', '예약확정'),
(6, 1, 4, '2025-06-18', '15:30:00', '포인트만 살짝', '예약확정'),
(7, 1, 5, '2025-06-19', '09:30:00', '자연스럽게', '예약확정'),
(8, 1, 6, '2025-06-20', '10:30:00', '볼륨 살려주세요', '예약확정'),
(9, 1, 7, '2025-06-21', '13:00:00', '특별한 날이에요', '예약확정'),
(10, 1, 8, '2025-06-22', '15:00:00', '두피가 민감해요', '예약확정'),
(11, 2, 9, '2025-06-23', '10:00:00', '단정하게', '예약확정'),
(12, 2, 10, '2025-06-24', '11:30:00', '숱 많아요', '예약확정'),
(13, 2, 11, '2025-06-25', '14:00:00', '밝은 색으로', '예약확정'),
(14, 2, 12, '2025-06-26', '16:00:00', '완전 탈색', '예약확정'),
(15, 2, 13, '2025-06-27', '10:00:00', '자연스럽게', '예약확정'),
(16, 2, 14, '2025-06-28', '14:00:00', '매직으로', '예약확정'),
(17, 2, 15, '2025-06-29', '12:00:00', '웨딩용', '예약확정'),
(18, 2, 16, '2025-06-30', '16:30:00', '탈모 걱정돼요', '예약확정'),
(19, 3, 17, '2025-07-01', '11:00:00', '짧게 잘라주세요', '예약확정'),
(20, 3, 18, '2025-07-01', '13:00:00', '남자친구랑 와요', '예약확정'),
(21, 3, 19, '2025-07-02', '15:30:00', '갈색 톤으로', '예약확정'),
(22, 3, 20, '2025-07-02', '16:30:00', '하이라이트만', '예약확정'),
(23, 3, 21, '2025-07-03', '10:00:00', '자연 파마', '예약확정'),
(24, 3, 22, '2025-07-03', '14:00:00', '깔끔하게', '예약확정'),
(25, 3, 23, '2025-07-04', '09:30:00', '마사지 강하게', '예약확정'),
(26, 4, 24, '2025-07-04', '11:00:00', 'VIP 서비스', '예약확정'),
(27, 4, 25, '2025-07-05', '15:00:00', '프리미엄으로', '예약확정'),
(28, 4, 26, '2025-07-05', '16:30:00', '염색 진하게', '예약확정'),
(29, 4, 27, '2025-07-06', '13:00:00', '옴브레 스타일', '예약확정'),
(30, 4, 28, '2025-07-06', '14:00:00', '파마 자연스럽게', '예약확정'),
(31, 4, 29, '2025-07-07', '10:00:00', '특별한 스타일링', '예약확정'),
(32, 4, 30, '2025-07-07', '11:00:00', '두피 관리 필요', '예약확정'),
(33, 5, 31, '2025-07-08', '14:00:00', '럭셔리 컷', '예약확정'),
(34, 5, 32, '2025-07-08', '15:30:00', '남성다운 스타일', '예약확정'),
(35, 5, 33, '2025-07-09', '10:00:00', '발레아주 원해요', '예약확정'),
(36, 5, 34, '2025-07-09', '12:00:00', '최고급 염색', '예약확정'),
(37, 5, 35, '2025-07-10', '14:00:00', '럭셔리 파마', '예약확정'),
(38, 5, 36, '2025-07-10', '16:00:00', 'VIP 스타일링', '예약확정'),
(39, 5, 37, '2025-07-11', '11:00:00', '프리미엄 케어', '예약확정'),
(40, 1, 1, '2025-07-12', '10:00:00', '여성 컷 예약', '예약확정'),
(41, 1, 3, '2025-07-12', '14:00:00', '염색 예약', '예약확정'),
(42, 2, 9, '2025-07-13', '15:00:00', '컷 예약', '예약확정'),
(43, 2, 11, '2025-07-14', '10:30:00', '염색 예약', '예약확정'),
(44, 2, 13, '2025-07-14', '16:00:00', '파마 예약', '예약확정'),
(45, 2, 15, '2025-07-15', '09:30:00', '스타일링 예약', '예약확정'),
(46, 2, 16, '2025-07-15', '14:30:00', '탈모 케어 예약', '예약확정'),
(47, 3, 17, '2025-07-16', '10:00:00', '여성 컷 예약', '예약확정'),
(48, 3, 18, '2025-07-16', '11:30:00', '남성 컷 예약', '예약확정'),
(49, 3, 19, '2025-07-17', '14:00:00', '염색 예약', '예약확정'),
(50, 3, 20, '2025-07-17', '15:30:00', '파마 예약', '예약확정'),
(51, 3, 21, '2025-07-18', '11:00:00', '파마 예약', '예약확정'),
(52, 3, 22, '2025-07-18', '12:00:00', '일반 스타일링 예약', '예약확정'),
(53, 3, 23, '2025-07-19', '14:00:00', '두피 마사지 예약', '예약확정'),
(54, 4, 24, '2025-07-19', '15:30:00', '프리미엄 여성 컷 예약', '예약확정'),
(55, 4, 25, '2025-07-20', '10:00:00', '프리미엄 남성 컷 예약', '예약확정'),
(56, 4, 26, '2025-07-20', '11:30:00', '프리미엄 염색 예약', '예약확정'),
(57, 4, 27, '2025-07-21', '14:00:00', '옴브레 염색 예약', '예약확정'),
(58, 4, 28, '2025-07-21', '15:00:00', '프리미엄 파마 예약', '예약확정'),
(59, 4, 29, '2025-07-22', '10:30:00', '특별 스타일링 예약', '예약확정'),
(60, 4, 30, '2025-07-22', '11:30:00', '프리미엄 두피케어 예약', '예약확정'),
(61, 5, 31, '2025-07-23', '14:00:00', '럭셔리 여성 컷 예약', '예약확정'),
(62, 5, 32, '2025-07-23', '15:30:00', '럭셔리 남성 컷 예약', '예약확정'),
(63, 5, 33, '2025-07-24', '10:00:00', '럭셔리 염색 예약', '예약확정'),
(64, 5, 34, '2025-07-24', '11:30:00', '발레아주 염색 예약', '예약확정'),
(65, 5, 35, '2025-07-25', '14:00:00', '럭셔리 파마 예약', '예약확정'),
(66, 5, 36, '2025-07-25', '15:30:00', '럭셔리 스타일링 예약', '예약확정'),
(67, 5, 37, '2025-07-26', '10:00:00', 'VIP 두피케어 예약', '예약확정'),
(68, 1, 2, '2025-07-26', '11:00:00', '남성 컷 예약', '예약확정'),
(69, 1, 4, '2025-07-27', '13:00:00', '포인트 염색 예약', '예약확정'),
(70, 1, 5, '2025-07-27', '14:30:00', '디지털 파마 예약', '예약확정'),
(71, 1, 6, '2025-07-28', '10:00:00', '일반 파마 예약', '예약확정'),
(72, 1, 7, '2025-07-28', '15:00:00', '업스타일링 예약', '예약확정'),
(73, 1, 8, '2025-07-29', '11:00:00', '두피 스케일링 예약', '예약확정'),
(74, 2, 10, '2025-07-29', '12:30:00', '남성 컷 예약', '예약확정'),
(75, 2, 12, '2025-07-30', '14:00:00', '탈색+염색 예약', '예약확정'),
(76, 2, 14, '2025-07-30', '16:00:00', '매직 스트레이트 예약', '예약확정'),
(77, 3, 17, '2025-07-31', '09:30:00', '여성 컷 예약', '예약확정'),
(78, 3, 19, '2025-07-31', '11:00:00', '전체 염색 예약', '예약확정'),
(79, 3, 21, '2025-08-01', '13:30:00', '셋팅 파마 예약', '예약확정'),
(80, 3, 23, '2025-08-01', '15:00:00', '두피 마사지 예약', '예약확정'),
(81, 4, 24, '2025-08-02', '10:00:00', '프리미엄 여성 컷 예약', '예약확정'),
(82, 4, 26, '2025-08-02', '12:00:00', '프리미엄 염색 예약', '예약확정'),
(83, 4, 28, '2025-08-03', '14:30:00', '프리미엄 파마 예약', '예약확정'),
(84, 4, 30, '2025-08-03', '16:00:00', '프리미엄 두피케어 예약', '예약확정'),
(85, 5, 31, '2025-08-04', '11:00:00', '럭셔리 여성 컷 예약', '예약확정'),
(86, 5, 33, '2025-08-04', '13:30:00', '럭셔리 염색 예약', '예약확정'),
(87, 5, 35, '2025-08-05', '15:00:00', '럭셔리 파마 예약', '예약확정'),
(88, 5, 37, '2025-08-05', '16:30:00', 'VIP 두피케어 예약', '예약확정'),
(89, 1, 1, '2025-08-06', '10:30:00', '여성 컷 예약', '예약확정'),
(90, 1, 3, '2025-08-06', '14:00:00', '전체 염색 예약', '예약확정'),
(91, 2, 9, '2025-08-07', '11:30:00', '여성 컷 예약', '예약확정'),
(92, 2, 11, '2025-08-07', '15:30:00', '전체 염색 예약', '예약확정'),
(93, 3, 17, '2025-08-08', '10:00:00', '여성 컷 예약', '예약확정'),
(94, 3, 19, '2025-08-08', '12:30:00', '전체 염색 예약', '예약확정'),
(95, 4, 24, '2025-08-09', '14:00:00', '프리미엄 여성 컷 예약', '예약확정'),
(96, 4, 26, '2025-08-09', '16:00:00', '프리미엄 염색 예약', '예약확정'),
(97, 5, 31, '2025-08-10', '11:00:00', '럭셔리 여성 컷 예약', '예약확정'),
(98, 5, 33, '2025-08-10', '13:00:00', '럭셔리 염색 예약', '예약확정'),
(99, 1, 5, '2025-08-11', '15:00:00', '디지털 파마 예약', '예약확정'),
(100, 2, 13, '2025-08-11', '16:30:00', '볼륨 파마 예약', '예약확정');

-- TBL_SALES (2건 - 1번 예약은 결제완료, 2번 예약은 결제취소)
INSERT INTO TBL_SALES (RESV_CODE, PAY_AMOUNT, PAY_METHOD, PAY_DATETIME, PAY_STATUS, CANCEL_AMOUNT, CANCEL_DATETIME, CANCEL_REASON, FINAL_AMOUNT) VALUES
-- 1번 예약 - 결제완료
(1, 25000, '신용카드', '2025-06-15 10:30:00', 'COMPLETED', 0, NULL, NULL, 25000),
-- 2번 예약 - 결제취소
(2, 20000, '현금', '2025-06-16 11:30:00', 'CANCELLED', 20000, '2025-06-16 12:00:00', '고객 요청에 의한 취소', 0);

-- TBL_VISITORS (150건 - 기존과 동일)
INSERT INTO TBL_VISITORS (USER_CODE, SHOP_CODE, MEMO, SENDABLE, IS_ACTIVE) VALUES
-- 뷰티헤어 강남점 고객 (30명)
(3, 1, '단골고객', 1, 1),
(4, 1, '신규고객', 1, 1),
(5, 1, '리뷰작성고객', 0, 1),
(6, 1, 'VIP고객', 1, 1),
(7, 1, '추천', 1, 1),
(8, 1, '이벤트참여', 0, 1),
(9, 1, '정기방문', 1, 1),
(10, 1, '만족고객', 1, 1),
(11, 1, '할인쿠폰사용', 0, 1),
(12, 1, '재방문고객', 1, 1),
(13, 1, '신규', 1, 1),
(14, 1, '단골', 0, 1),
(15, 1, 'VIP', 1, 1),
(16, 1, '추천', 1, 1),
(17, 1, '이벤트', 0, 1),
(18, 1, '정기', 1, 1),
(19, 1, '만족', 1, 1),
(20, 1, '할인', 0, 1),
(21, 1, '재방문', 1, 1),
(22, 1, '특별관리', 1, 1),
(23, 1, '우수고객', 0, 1),
(24, 1, '단골VIP', 1, 1),
(25, 1, '추천VIP', 1, 1),
(26, 1, '이벤트VIP', 0, 1),
(27, 1, '정기VIP', 1, 1),
(28, 1, '프리미엄', 1, 1),
(29, 1, '특급VIP', 0, 1),
(30, 1, '최고급', 1, 1),
(31, 1, '럭셔리', 1, 1),
(32, 1, 'VVIP', 0, 1),

-- 뷰티헤어 홍대점 고객 (30명)
(33, 2, '홍대 단골', 1, 1),
(34, 2, '홍대 신규', 1, 1),
(35, 2, '홍대 VIP', 0, 1),
(36, 2, '홍대 추천', 1, 1),
(37, 2, '홍대 이벤트', 1, 1),
(38, 2, '홍대 단골', 0, 1),
(39, 2, '홍대 만족', 1, 1),
(40, 2, '홍대 할인', 1, 1),
(41, 2, '홍대 재방문', 0, 1),
(42, 2, '대학생고객', 1, 1),
(43, 2, '대학생고객', 1, 1),
(44, 2, '트렌드고객', 0, 1),
(45, 2, '스타일링 위주 고객', 1, 1),
(46, 2, '모근 케어 단골', 1, 1),
(47, 2, '장기 고객(컷 단골)', 0, 1),
(48, 2, '월 1회 방문 고객', 1, 1),
(49, 2, '격주 1회 뿌염 단골', 1, 1),
(50, 2, '파마 단골', 0, 1),
(51, 2, '파마 단골', 1, 1),
(52, 2, '대학생', 1, 1),
(53, 2, '대학생', 0, 1),
(54, 2, '월 2회 정기 방문 고객', 1, 1),
(55, 2, '인플루언서', 1, 1),
(56, 2, '블로거', 0, 1),
(57, 2, '유튜버', 1, 1),
(58, 2, '모델', 1, 1),
(59, 2, '연예인', 0, 1),
(60, 2, '방송인', 1, 1),
(61, 2, '가수', 1, 1),
(62, 2, '배우', 0, 1),

-- 뷰티헤어 신촌점 고객 (30명)
(63, 3, '신촌 단골', 1, 1),
(64, 3, '신촌 신규', 1, 1),
(65, 3, '신촌 VIP', 0, 1),
(66, 3, '신촌 추천', 1, 1),
(67, 3, '신촌 이벤트', 1, 1),
(68, 3, '신촌 정기', 0, 1),
(69, 3, '신촌 만족', 1, 1),
(70, 3, '신촌 할인', 1, 1),
(71, 3, '신촌 재방문', 0, 1),
(72, 3, '대학생', 1, 1),
(73, 3, '학생할인', 1, 1),
(74, 3, '교직원', 0, 1),
(75, 3, '연구원', 1, 1),
(76, 3, '대학원생', 1, 1),
(77, 3, '교수님', 0, 1),
(78, 3, '직장인', 1, 1),
(79, 3, '대학원생', 1, 1),
(80, 3, '회사원', 0, 1),
(81, 3, '직장인', 1, 1),
(82, 3, 'CEO', 1, 1),
(83, 3, '임원', 0, 1),
(84, 3, '대학원생', 1, 1),
(85, 3, '교수', 1, 1),
(86, 3, '직장인', 0, 1),
(87, 3, '직장인', 1, 1),
(88, 3, '두피 클리닉 단골', 1, 1),
(89, 3, '근처 거주', 0, 1),
(90, 3, '격월 1회 염색 단골', 1, 1),
(91, 3, '장기 고객', 1, 1),
(92, 3, '장기 고객', 0, 1),

-- 엘레강스 헤어 압구정점 고객 (30명)
(23, 5, '압구정 파마 단골', 1, 1),
(24, 5, '압구정 염색 단골', 1, 1),
(25, 5, '압구정 VIP', 0, 1),
(26, 5, '압구정 VIP', 1, 1),
(27, 5, '압구정 VVIP', 1, 1),
(28, 5, '압구정 주 1회 방문', 0, 1),
(29, 5, '압구정 VVIP', 1, 1),
(30, 5, '압구정 VIP', 1, 1),
(31, 5, '압구정 장기 단골', 0, 1),
(32, 5, '압구정 VVIP', 1, 1),
(33, 5, '압구정 VIP', 1, 1),
(34, 5, 'VIP 고객', 0, 1),
(35, 5, '마무리 스타일링 필수', 1, 1),
(36, 5, '동네 마당발', 1, 1),
(37, 5, 'VIP 고객', 0, 1),
(38, 5, 'VVIP 고객', 1, 1),
(39, 5, '두피 클리닉 단골', 1, 1),
(40, 5, '숏컷 선호', 0, 1),
(41, 5, '숏컷 선호', 1, 1),
(42, 5, '매직 필수', 1, 1),
(43, 5, '파마 선호', 0, 1),
(44, 5, '격주 1호 뿌염 고객', 1, 1),
(45, 5, '염색 선호', 1, 1),
(46, 5, '긴머리 선호', 0, 1),
(47, 5, '숱치기 자주함', 1, 1),
(48, 5, '단발 선호', 1, 1),
(49, 5, '장기 고객', 0, 1),
(50, 5, '월 2회 방문 고객', 1, 1),
(51, 5, '장기 고객', 1, 1),
(52, 5, '근처 거주', 0, 1);

-- TBL_MSG_SEND_BATCH 더미 데이터 (3개 배치)
INSERT INTO `TBL_MSG_SEND_BATCH` (
    `BATCH_CODE`,
    `SHOP_CODE`,
    `TEMPLATE_CODE`,
    `SEND_DATE`,
    `SEND_TIME`,
    `SEND_TYPE`,
    `SUBJECT`,
    `TOTAL_COUNT`,
    `SUCCESS_COUNT`,
    `FAIL_COUNT`
) VALUES
-- 배치 1: 샵 1에서 예약 확정 메시지 개별 발송
(1, 1, 1, '2025-07-01', '10:00:00', 'INDIVIDUAL', '7월 5일 예약 확정 안내', 12, 10, 2),
-- 배치 2: 샵 2에서 프로모션 메시지 그룹 발송
(2, 2, 6, '2025-07-15', '14:00:00', 'GROUP', '7월 특가 이벤트 안내', 15, 13, 2),
-- 배치 3: 샵 1에서 리뷰 요청 그룹 발송
(3, 1, 16, '2025-07-25', '16:30:00', 'GROUP', '서비스 만족도 및 리뷰 요청', 3, 3, 0);

-- TBL_SHOP_MSG_HISTORY 더미 데이터 (30건)
INSERT INTO `TBL_SHOP_MSG_HISTORY` (
    `BATCH_CODE`,
    `USER_CODE`,
    `MSG_CONTENT`,
    `SEND_STATUS`,
    `ERROR_MESSAGE`,
    `SENT_AT`
) VALUES
-- 배치 1 (BATCH_CODE = 1, 샵 1) - 12건
(1, 2, '예약이 확정되었습니다.', 'SUCCESS', NULL, '2025-07-01 10:00:15'),

-- 배치 2 (BATCH_CODE = 2, 샵 2) - 15건 (동일한 프로모션 메시지)
(2, 14, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:00:10'),
(2, 15, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:00:25'),
(2, 16, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:00:40'),
(2, 17, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:00:55'),
(2, 18, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:01:10'),
(2, 19, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:01:25'),
(2, 20, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'FAIL', '번호 오류', '2025-07-15 14:01:40'),
(2, 21, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:01:55'),
(2, 22, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:02:10'),
(2, 23, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:02:25'),
(2, 24, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:02:40'),
(2, 25, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:02:55'),
(2, 26, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:03:10'),
(2, 27, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'FAIL', '네트워크 오류', '2025-07-15 14:03:25'),
(2, 28, '[네일샵 이름] 7월 특가 이벤트! 젤네일 30% 할인 (~7/31까지) 예약: 02-9876-5432', 'SUCCESS', NULL, '2025-07-15 14:03:40'),

-- 배치 3 (BATCH_CODE = 3, 샵 1) - 3건 (동일한 리뷰 요청 메시지)
(3, 29, '[미용실 이름] 서비스는 만족하셨나요? 리뷰 작성하고 1만원 쿠폰 받으세요! bit.ly/review123', 'SUCCESS', NULL, '2025-07-25 16:30:05'),
(3, 30, '[미용실 이름] 서비스는 만족하셨나요? 리뷰 작성하고 1만원 쿠폰 받으세요! bit.ly/review123', 'SUCCESS', NULL, '2025-07-25 16:30:20'),
(3, 2, '[미용실 이름] 서비스는 만족하셨나요? 리뷰 작성하고 1만원 쿠폰 받으세요! bit.ly/review123', 'SUCCESS', NULL, '2025-07-25 16:30:35');