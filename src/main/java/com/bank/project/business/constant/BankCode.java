package com.bank.project.business.constant;

public enum BankCode {
    // 공통 코드 , 계좌 조회 실패, 서버 응답 없음, 거래 제한
    BA00, // 계좌 조회 실패
    BA01, // 계좌 비밀번호 오류
    BA02, // 서버 에러

    BD00, // 입금 성공 코드

    BW00, // 출금 성공 코드
    BW90, // 출금 실패 코드 계좌 잔액 부족

    BR00, // 송금 성공 코드
    BR90, // 송금 실패 코드 송금 결과 조회 실패
}
