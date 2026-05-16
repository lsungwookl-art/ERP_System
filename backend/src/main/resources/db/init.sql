-- ERP System 초기 데이터
-- MySQL 8.x 기준

CREATE DATABASE IF NOT EXISTS erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_db;

-- 회사 데이터 (테넌트)
INSERT IGNORE INTO company (id, name, business_no, address, representative_name, active, created_at, updated_at, is_deleted)
VALUES (1, '(주)테스트컴퍼니', '123-45-67890', '서울시 강남구 테헤란로 123', '홍길동', 1, NOW(), NOW(), 0);

-- 사용자 (비밀번호: password123)
INSERT IGNORE INTO users (id, company_id, email, password, name, phone, active, created_at, updated_at, is_deleted)
VALUES (1, 1, 'admin@test.com',
        '$2a$10$AE34vgafLQTrLcIREhaeoOZ8AP5k6hy2wCBIiVHhN6093RwPq7AK.',
        '관리자', '010-1234-5678', 1, NOW(), NOW(), 0);

-- 계정과목 (기본)
INSERT IGNORE INTO account (id, company_id, account_code, account_name, account_type, parent_id, active, created_at, updated_at, is_deleted) VALUES
(1,  1, '1000', '유동자산',   'ASSET',     NULL, 1, NOW(), NOW(), 0),
(2,  1, '1200', '매출채권',   'ASSET',     1,    1, NOW(), NOW(), 0),
(3,  1, '1300', '재고자산',   'ASSET',     1,    1, NOW(), NOW(), 0),
(4,  1, '2000', '유동부채',   'LIABILITY', NULL, 1, NOW(), NOW(), 0),
(5,  1, '2100', '매입채무',   'LIABILITY', 4,    1, NOW(), NOW(), 0),
(6,  1, '3000', '자본',       'EQUITY',    NULL, 1, NOW(), NOW(), 0),
(7,  1, '4000', '매출',       'REVENUE',   NULL, 1, NOW(), NOW(), 0),
(8,  1, '4100', '상품매출',   'REVENUE',   7,    1, NOW(), NOW(), 0),
(9,  1, '5000', '매출원가',   'EXPENSE',   NULL, 1, NOW(), NOW(), 0),
(10, 1, '5100', '매출원가',   'EXPENSE',   9,    1, NOW(), NOW(), 0);

-- 창고 기본값
INSERT IGNORE INTO warehouse (id, company_id, warehouse_name, address, description, active, created_at, updated_at, is_deleted)
VALUES (1, 1, '본사 창고', '서울시 강남구', '기본 창고', 1, NOW(), NOW(), 0);
