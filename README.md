# ERP System (이카운트 참조 설계)

Java 21 / Spring Boot 3.3 / MySQL 8.x / React 기반 웹 ERP 시스템

## 기술 스택
- **Backend**: Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, MySQL 8.x
- **Frontend**: React + TypeScript, Vite, AG Grid, Zustand, Tailwind CSS

## 빠른 시작

### 1. MySQL 준비
```sql
CREATE DATABASE erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 이후 backend/src/main/resources/db/init.sql 실행
```

### 2. 설정 수정
`backend/src/main/resources/application.yml`에서 DB 비밀번호 수정:
```yaml
spring.datasource.password: 본인비밀번호
```

### 3. 백엔드 실행
```bash
cd backend
mvn spring-boot:run
# http://localhost:8080/api/v1
```

### 4. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

### 기본 계정
- 이메일: `admin@test.com`
- 비밀번호: `password123`

## 프로젝트 구조
```
erp-system/
├── backend/                    # Spring Boot API 서버
│   └── src/main/java/com/erp/
│       ├── auth/               # JWT 인증
│       ├── master/             # 품목, 거래처, 창고
│       ├── inventory/          # 재고 (낙관적 락, 이동평균법)
│       ├── purchase/           # 구매/발주/입고
│       ├── sales/              # 판매/수주/출고
│       ├── accounting/         # 전표 자동분개
│       └── common/             # 공통 (DocSequence, Audit)
└── frontend/                   # React SPA
    └── src/
        ├── pages/              # 화면별 컴포넌트
        ├── components/         # 공통 컴포넌트 (AG Grid)
        ├── store/              # Zustand 상태 관리
        └── api/                # Axios 클라이언트
```

## ERP 핵심 흐름
```
구매 입고 → 재고 증가(이동평균법) → 전표 자동생성 (차: 재고자산 / 대: 매입채무)
판매 출고 → 재고 차감(이동평균단가) → 전표 자동생성 (4줄 분개)
```

## Phase 별 구현 현황
- [x] Phase 1: JWT 인증, 기준정보 CRUD, 재고 기본
- [x] Phase 2: 구매/판매 SCM 사이클, 자동분개
- [ ] Phase 3: 회계 원장, 시산표
- [ ] Phase 4: 대시보드, BOM, 전자결재
