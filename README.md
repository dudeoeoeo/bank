# bank
# 서버 구동

개발환경

- SpringBoot 2.7.3
- Gradle 7.5
- Java 11

# DB Table

```
drop table if exists account;
CREATE TABLE account (
    id BIGINT primary key ,
   user_id varchar(50) not null ,
   public_token varchar(100) not null ,
   private_token varchar(100) not null ,
    balance int check (balance >= 0),
    created_dt datetime  null,
    update_dt datetime  null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

drop table if exists bank_record;
CREATE TABLE bank_record (
    id BIGINT primary key ,
   transactionKey varchar(100) not null ,
   public_token varchar(100) null ,
   private_token varchar(100) null ,
   bankType varchar(20) not null ,
   resultCode varchar(10) not null ,
    balance int check (balance >= 0),
    created_dt datetime  null,
    update_dt datetime  null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

$ git clone https://github.com/dudeoeoeo/bank -b main
$ cd bank
$ ./gradlew clean build

// docker compose 설치 必
// mysql 은 docker-compose.yml 에 포함

$ docker-compose up -d

// Public Token 생성

AES 암호화 public token

JWT 토큰 같이 인증에 사용하는 토큰이 아닌 유저

고유의 토큰이므로 복호화해서 사용은 하지 않는다.

// Private Token 생성

SHA-256 단방향 복호화 private token

암호화 토큰

유저 고유 private token 이므로 복호화 못 함


// 입금 응답 코드

- 공통 실패 코드
    - BA00 // 계좌 조회 실패
    - BA01 // 계좌 비밀번호 오류
    - BA02 // 서버 에러
- 입금 성공 코드
    - BD00 // 입금 성공


// 출금 응답 코드

- 공통 실패 코드
    - BA00 // 계좌 조회 실패
    - BA01 // 계좌 비밀번호 오류
    - BA02 // 서버 에러
- 입금 성공 코드
    - BW00 // 출금 성공
- 입금 실패 코드
    - BW90 // 계좌 잔고 부족


// 송금 응답 코드

- 공통 실패 코드
    - BA00 // 계좌 조회 실패
    - BA01 // 계좌 비밀번호 오류
    - BA02 // 서버 에러
- 송금 성공 코드
    - BR00 // 송금 성공
- 송금 실패 코드
    - BR90 // 송금 조회 식별 키 오류


# API

# 계좌 생성

POST http://localhost:8080/api/v1/account

Content-Type: application/json;charset=UTF-8

Request Body

{
    "userId" : "userId"
}

# 입금

POST http://localhost:8080/api/v1/account

Content-Type: application/json;charset=UTF-8

Request Body

{
    "publicToken" : "publicToken",

    “depositBalance": 10000
}

# 출금

POST http://localhost:8080/api/v1/account

Content-Type: application/json;charset=UTF-8

Request Body

{
    "privateToken" : "privateToken",

    “withdrawalBalance”: 10000
}

# 송금

POST http://localhost:8080/api/v1/account

Content-Type: application/json;charset=UTF-8

Request Body

{
    "publicToken" : "publicToken",
    "privateToken" : "privateToken",   

    “remittanceBalance”: 10000
}

# 송금 내역 조회

GET http://localhost:8080/api/v1/account/8c93005a-6bb4-4245-a08f-9b9cd2b751b9

Content-Type: application/json;charset=UTF-8

Path Parameter

{
    transactionKey: 거래 식별 키
}
