# backend

# 🧭 Munaro Backend

관광지 퀴즈를 통해 지역을 탐방하고 시즌 랭킹을 경쟁하는 서비스 **Munaro**의 백엔드 프로젝트입니다.


## ✨ 주요 기능

- 👤 회원 관리
- 🗺️ 현재 위치 기반 관광지 조회
- ❓ 관광지 퀴즈
- 🏆 시즌 랭킹
- 🎖️ 배지 시스템

---

# 🛠 Tech Stack

| Category | Stack |
|----------|-------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL (Supabase) |
| ORM | Spring Data JPA |
| Authentication | Spring Security, JWT |
| AI | OpenAI API (gpt-4.1-mini) |
| Deployment | AWS Elastic Beanstalk |

---

# 📌 ERD

> <img width="1896" height="1299" alt="image" src="https://github.com/user-attachments/assets/aefd230c-77cb-46f1-8913-69c422e66a02" />


---

# 📂 프로젝트 구조

```text
backend
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── scripts/
│   └── import_tourist_spots.sh
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/carrot/munaro/
│   │   │       ├── MunaroApplication.java
│   │   │       ├── auth/              # 회원가입, 로그인, 회원탈퇴
│   │   │       ├── global/            # 공통 응답, 예외 처리
│   │   │       ├── quiz/              # 퀴즈 조회, 제출, 결과
│   │   │       ├── score/             # 점수, 랭킹, 시즌
│   │   │       ├── security/          # JWT, Spring Security
│   │   │       ├── tourist_spot/      # 관광지 조회
│   │   │       └── user/              # 사용자, 프로필, 뱃지
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   └── test/
└── README.md
```

---

# 📖 API

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/auth/email/signup` | 회원가입 |
| POST | `/auth/email/login` | 이메일 로그인 |
| POST | `/auth/kakao/login` | 카카오 로그인 |
| POST | `/auth/google/login` | 구글 로그인 |
| DELETE | `/auth/withdraw` | 회원탈퇴 |
| GET | `/users/me` | 내 정보 조회 |
| PATCH | `/users/me/profile` | 프로필 수정 |
| GET | `/tourist-spots/nearby` | 주변 관광지 조회 |
| GET | `/tourist-spots/{spotId}` | 관광지 상세 조회 |
| GET | `/tourist-spots/{spotId}/quizzes` | 관광지 퀴즈 목록 조회 |
| GET | `/quizzes/{quizId}` | 퀴즈 조회 |
| POST | `/quizzes/{quizId}/submit` | 퀴즈 제출 |
| GET | `/quizzes/{quizId}/result` | 퀴즈 결과 조회 |
| GET | `/quizzes/history` | 퀴즈 기록 조회 |
| GET | `/quizzes/history/tourist-spots/{touristSpotId}` | 관광지별 퀴즈 기록 조회 |
| GET | `/rankings/current` | 현재 시즌 랭킹 |
| GET | `/rankings/current/me` | 내 랭킹 조회 |
| GET | `/rankings/current/top3` | TOP3 조회 |
| GET | `/rankings/seasons/{seasonId}` | 시즌 랭킹 조회 |
| GET | `/rankings/seasons/{seasonId}/me` | 시즌 내 랭킹 조회 |
| GET | `/rankings/seasons/{seasonId}/top3` | 시즌 TOP3 조회 |
| GET | `/seasons` | 시즌 목록 조회 |
| GET | `/seasons/current` | 현재 시즌 조회 |

---

# 🚀 실행 방법

## 프로젝트 실행

```bash
git clone <repository-url>
cd backend
./gradlew bootRun
```

## 환경 변수

실행 전 아래 환경 변수가 필요합니다.

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=
JWT_ACCESS_EXPIRATION=
JWT_REFRESH_EXPIRATION=

OPENAI_API_KEY=
```

---

# 📌 주요 기능

## 👤 회원

- 이메일 회원가입
- 이메일 로그인
- 카카오 로그인
- 구글 로그인
- 회원탈퇴
- 프로필 수정

---

## 🗺️ 관광지

- 현재 위치 기반 관광지 조회
- 관광지 상세 조회
- 관광지별 퀴즈 제공

---

## ❓ 퀴즈

- 관광지별 퀴즈 조회
- 문제 제출
- 실시간 정답 여부 확인
- 퀴즈 결과 조회
- 퀴즈 히스토리 조회

---

## 🏆 랭킹

- 시즌별 랭킹
- TOP3 조회
- 내 랭킹 조회
- 시즌 점수 집계

---

## 🎖️ 배지

- 시즌 1~3등 배지
- 획득 배지 조회

---

# ☁️ Deployment

- AWS Elastic Beanstalk
- PostgreSQL (Supabase)
