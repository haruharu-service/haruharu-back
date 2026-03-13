# 하루하나 관리자 API

## 패키지 구조

```
org.kwakmunsu.haruhana.admin.{domain}
  controller/
  controller/dto/request/
  service/
  service/dto/response/
```

---

## 공통

- 모든 admin API는 `/v1/admin/**` 경로 사용
- `ROLE_ADMIN` 권한 필요
- 공통 응답 형식: `ApiResponse<T>`

---

## 1. 통계

### 서비스 통계 조회

```
GET /v1/admin/statistics
```

**Response Body**

```json
{
  "totalMemberCount": 1024,
  "todayProblemCount": 5,
  "todayOnTimeSubmissionCount": 312
}
```

| 필드 | 설명 |
|------|------|
| `totalMemberCount` | 총 활성 회원 수 |
| `todayProblemCount` | 오늘 날짜로 배정된 문제 수 |
| `todayOnTimeSubmissionCount` | 오늘 배정된 문제의 제시간 제출 수 |

---

## 2. 회원 관리

### 회원 목록 조회

```
GET /v1/admin/members
```

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| search | String | null | 닉네임 또는 로그인ID LIKE 검색 |
| sortBy | Enum | CREATED_AT | `CREATED_AT` \| `LAST_LOGIN_AT` |
| sortDirection | Enum | DESC | `ASC` \| `DESC` |
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |

**정렬 기준**
- `CREATED_AT` : 가입일 기준 (기본값, 신규 가입자 파악)
- `LAST_LOGIN_AT` : 마지막 활동 기준 (휴면 회원 파악, null은 마지막 정렬)

**Response Body**

```json
{
  "members": [
    {
      "id": 1,
      "loginId": "user01",
      "nickname": "닉네임",
      "role": "ROLE_MEMBER",
      "lastLoginAt": "2026-02-20T10:30:00",
      "createdAt": "2026-01-01T00:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

### 회원 설정 조회

```
GET /v1/admin/members/{memberId}/preferences
```

**Response Body**

```json
{
  "memberId": 1,
  "difficulty": "EASY",
  "categoryTopicName": "정렬 알고리즘",
  "categoryGroupName": "알고리즘",
  "categoryName": "컴퓨터공학",
  "effectiveAt": "2026-02-21"
}
```

---

### 회원 닉네임 수정

```
PATCH /v1/admin/members/{memberId}/nicknames
```

**Request Body**

```json
{ "nickname": "새닉네임" }
```

---

### 회원 권한 변경

```
PATCH /v1/admin/members/{memberId}/roles
```

**Request Body**

```json
{ "role": "ROLE_ADMIN" }
```

---

### 회원 삭제 (논리 삭제)

```
DELETE /v1/admin/members/{memberId}
```

---

## 3. 문제 관리

### 문제 목록 조회

```
GET /v1/admin/problems
```

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |

**Response Body**

```json
{
  "problems": [
    {
      "id": 10,
      "title": "버블 정렬이란?",
      "difficulty": "EASY",
      "categoryTopicName": "정렬 알고리즘",
      "problemAt": "2026-02-20",
      "promptVersion": "v1.0"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

---

## 4. 카테고리 관리

> 논리 삭제(status = DELETED) 방식 사용

### 카테고리 (대분류)

```
POST   /v1/admin/categories                        카테고리 생성
PATCH  /v1/admin/categories/{categoryId}           카테고리 수정
DELETE /v1/admin/categories/{categoryId}           카테고리 삭제
```

### 카테고리 그룹 (중분류)

```
POST   /v1/admin/categories/groups                 그룹 생성
PATCH  /v1/admin/categories/groups/{groupId}       그룹 수정
DELETE /v1/admin/categories/groups/{groupId}       그룹 삭제
```

### 카테고리 토픽 (소분류)

```
POST   /v1/admin/categories/topics                 토픽 생성
PATCH  /v1/admin/categories/topics/{topicId}       토픽 수정
DELETE /v1/admin/categories/topics/{topicId}       토픽 삭제
```

**POST Request Body (공통)**
```json
{ "name": "카테고리명" }
```

**PATCH Request Body (공통)**
```json
{ "name": "새 이름" }
```
