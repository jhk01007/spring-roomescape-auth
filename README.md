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

### [✅] 로그인 API
**Request**

```http
POST /auth/login HTTP/1.1
Content-Type: application/json

{
  "loginId": "jaehee123",
  "password": "pass1234"
}
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|-|
| 유효하지 않는 입력값(아이디, 비밀번호 형식) | `400` |✅|
| 로그인 실패(아이디 없음 / 비번 안맞음)   | `401` | ✅|

### [✅] 로그아웃 API
**Request**

```http
POST /auth/logout HTTP/1.1
Content-Type: application/json
```

**Response**

```http
HTTP/1.1 204
```
