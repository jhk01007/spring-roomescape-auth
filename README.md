# 1️⃣ 1단계 

## 🙆 ‍요구사항
### 로그인
- [x] 사용자는 로그인할 수 있다. 
- [x] 로그인에 성공하면 이후 요청에서 같은 사용자를 식별할 수 있어야 한다. 
- [x] 로그인에 실패하면 적절한 응답을 반환한다.

### 예약 생성
- [x] 로그인한 사용자는 예약을 생성할 수 있다. 
- [x] 예약 생성 시 요청으로 받은 이름이 아니라 로그인한 사용자를 기준으로 예약을 만든다. 
- [x] 로그인하지 않은 사용자는 예약을 생성할 수 없다.

### 예약 조회
- [x] 로그인한 사용자는 자신의 예약을 조회할 수 있다. 
- [x] 로그인하지 않은 사용자는 인증이 필요한 예약 조회 기능을 사용할 수 없다.

### 인증 공통 처리
- [x] 로그인 여부 확인 로직을 컨트롤러마다 반복하지 않는다.
- [x] 인증이 필요한 API와 필요하지 않은 API를 구분한다.
- [x] 인증 실패 시 일관된 응답을 반환한다.


## 추가해야할 API
### [✅] 회원가입 API
**Request**

```http
POST /members HTTP/1.1
Content-Type: application/json

{
  "loginId": "jaehee123",
  "password": "pass1234",
  "nickname": "제이"
}
```

**Response**

```http
HTTP/1.1 201
Content-Type: application/json

{
  "id": 1,
  "nickname": "제이",
  "loginId": "jaehee123",
  "role": "USER"
}
```

에러 상황:

| 에러 상황                          | 상태코드  | 구현여부 |
|--------------------------------|-------|--|
| 유효하지 않는 입력값(아이디, 닉네임, 비밀번호 형식) | `400` | ✅|
| 아이디가 중복될 때                     | `409` | ✅|
| 닉네임이 중복될 때                     | `409` | ✅ |

### [✅] 웹 로그인 API
**Request**

```http
POST /auth/login HTTP/1.1
Content-Type: application/json

{
  "loginId": "jaehee123",
  "password": "pass1234"
}
```

**Response** <br>
> 세션에 id 값이 세팅되고 쿠키에 JSessionId가 추가된다.
```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|-|
| 유효하지 않는 입력값(아이디, 비밀번호 형식) | `400` |✅|
| 로그인 실패(아이디 없음 / 비번 안맞음)   | `401` | ✅|

### [✅] 웹 로그아웃 API
**Request**
> 해당 세션을 무효화한다.
```http
POST /auth/logout HTTP/1.1
Content-Type: application/json
```

**Response**

```http
HTTP/1.1 204
```

---

# 2️⃣ 2단계

## 🙆 ‍요구사항
### 모바일 로그인
- [x] 모바일 앱 사용자는 로그인할 수 있다. 
- [x] 로그인 성공 후 모바일 앱이 이후 요청에서 사용할 인증 정보를 받을 수 있어야 한다. 
- [x] 인증 정보는 이후 요청마다 서버가 사용자를 식별할 수 있는 형태여야 한다.

### 모바일 인증 요청
- [x] 모바일 앱은 인증이 필요한 API를 호출할 때 인증 정보를 함께 전달한다. 
- [x] 서버는 전달된 인증 정보를 검증한다. 
- [x] 인증 정보가 유효하면 로그인한 사용자로 요청을 처리한다. 
- [x] 인증 정보가 없거나 유효하지 않으면 요청을 거부한다.

### 웹 인증과의 관계
- [x] 웹 인증 흐름과 모바일 인증 흐름이 어떤 점에서 같은지 설명할 수 있어야 한다. 
- [x] 웹 인증 흐름과 모바일 인증 흐름이 어떤 점에서 다른지 설명할 수 있어야 한다. 
- [x] 가능한 한 중복된 인증 로직을 줄인다.

## 추가해야할 API
### [✅] 모바일 로그인 API
**Request**

```http
POST /auth/mobile/login HTTP/1.1
Content-Type: application/json

{
  "loginId": "jaehee123",
  "password": "pass1234"
}
```

**Response** <br>
> 토큰이 응답바디로 전달된다.
```http
HTTP/1.1 204
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
}
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|-|
| 유효하지 않는 입력값(아이디, 비밀번호 형식) | `400` |✅|
| 로그인 실패(아이디 없음 / 비번 안맞음)   | `401` | ✅|

### [✅] 모바일 로그아웃 API
**Request**
```http
POST /auth/mobile/logout HTTP/1.1
Content-Type: application/json
```

**Response**

```http
HTTP/1.1 204
```

# 3️⃣ 3단계

## 🙆 ‍요구사항

### 매장 매니저 식별
- [ ] 로그인한 사용자가 매장 매니저인지 확인할 수 있어야 한다. 
- [ ] 매장 매니저가 어떤 매장을 관리하는지 확인할 수 있어야 한다.

### 예약 접근 제한
- [ ] 매장 매니저는 자기 매장의 예약만 조회할 수 있다. 
- [ ] 매장 매니저는 자기 매장의 예약만 변경할 수 있다. 
- [ ] 매장 매니저는 자기 매장의 예약만 삭제할 수 있다. 
- [ ] 다른 매장의 예약에 접근하려는 요청은 거부한다.

### 실패 처리
- [ ] 로그인하지 않은 요청은 인증 실패로 처리한다. 
- [ ] 로그인했지만 권한이 없는 요청은 인가 실패로 처리한다. 
- [ ] 인증 실패와 인가 실패를 같은 문제로 뭉개지 않는다.

## 추가해야할 API

### [ ] 내 매장 매니저 정보 조회 API
**Request**
> 로그인한 사용자가 매장 매니저인지, 매장 매니저라면 자신이 관리하는 매장을 조회한다.

```http
GET /store-managers/me HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "manager": true,
  "store": {
    "id": 1,
    "name": "잠실점"
  }
}
```

> 로그인한 사용자가 매장 매니저가 아닌 경우

```http
HTTP/1.1 200
Content-Type: application/json

{
  "manager": false,
  "store": null
}
```

에러 상황:

| 에러 상황     | 상태코드  | 구현여부 |
|-----------|-------|-|
| 로그인하지 않은 요청 | `401` | |

### [ ] 관리자 예약 목록 조회 API
**Request**
> 기존 관리자 예약 조회 기능을 사용한다.  
> 매장 매니저는 자신이 관리하는 매장의 예약만 조회할 수 있다.

```http
GET /admin/reservations?storeId=1&page=1&size=10 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "reservations": [
    {
      "id": 1,
      "guestName": "제이",
      "date": "2026-05-21",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "브라운의 방"
      }
    }
  ]
}
```

에러 상황:

| 에러 상황                 | 상태코드  | 구현여부 |
|-----------------------|-------|-|
| 로그인하지 않은 요청           | `401` | |
| 매장 매니저가 아닌 사용자의 요청    | `403` | |
| 자신이 관리하지 않는 매장 조회 요청 | `403` | |
| 존재하지 않는 매장에 대한 요청     | `404` | |

### [ ] 관리자 예약 변경 API
**Request**
> 기존 관리자 예약 변경 기능을 사용한다.  
> 매장 매니저는 자신이 관리하는 매장의 예약만 변경할 수 있다.

```http
PATCH /admin/reservations/{reservationId} HTTP/1.1
Content-Type: application/json

{
  "date": "2026-05-22",
  "timeId": 2
}
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "id": 1,
  "guestName": "제이",
  "date": "2026-05-22",
  "time": {
    "id": 2,
    "startAt": "12:00"
  },
  "theme": {
    "id": 1,
    "name": "브라운의 방"
  }
}
```

에러 상황:

| 에러 상황                         | 상태코드  | 구현여부 |
|-------------------------------|-------|-|
| 로그인하지 않은 요청                   | `401` | |
| 매장 매니저가 아닌 사용자의 요청            | `403` | |
| 자신이 관리하지 않는 매장의 예약 변경 요청      | `403` | |
| 존재하지 않는 예약에 대한 요청              | `404` | |
| 유효하지 않는 입력값(날짜, 시간 형식 등)       | `400` | |
| 이미 예약된 날짜와 시간으로 변경하려는 요청       | `409` | |
| 이미 시작된 예약을 변경하려는 요청            | `400` | |

### [ ] 관리자 예약 삭제 API
**Request**
> 기존 관리자 예약 삭제 기능을 사용한다.  
> 매장 매니저는 자신이 관리하는 매장의 예약만 삭제할 수 있다.

```http
DELETE /admin/reservations/{reservationId} HTTP/1.1
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|-|
| 로그인하지 않은 요청               | `401` | |
| 매장 매니저가 아닌 사용자의 요청        | `403` | |
| 자신이 관리하지 않는 매장의 예약 삭제 요청 | `403` | |
| 존재하지 않는 예약에 대한 요청         | `404` | |

### 인증/인가 실패 응답
**인증 실패 Response**
> 로그인하지 않은 요청은 인증 실패로 처리한다.

```http
HTTP/1.1 401
Content-Type: application/json

{
  "path": "/admin/reservations",
  "code": "AUTHENTICATION_ERROR",
  "message": "인증에 실패했습니다.",
  "timeStamp": "2026-05-21T10:00:00"
}
```

**인가 실패 Response**
> 로그인했지만 권한이 없는 요청은 인가 실패로 처리한다.

```http
HTTP/1.1 403
Content-Type: application/json

{
  "path": "/admin/reservations",
  "code": "AUTHORIZATION_ERROR",
  "message": "접근 권한이 없습니다.",
  "timeStamp": "2026-05-21T10:00:00"
}
```
