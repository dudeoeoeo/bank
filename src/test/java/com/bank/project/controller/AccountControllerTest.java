package com.bank.project.controller;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.constant.BankType;
import com.bank.project.business.dto.request.AccountReqSaveDto;
import com.bank.project.business.dto.request.DepositReqDto;
import com.bank.project.business.dto.request.RemittanceReqDto;
import com.bank.project.business.dto.request.WithdrawalReqDto;
import com.bank.project.business.entity.Account;
import com.bank.project.business.entity.AccountRepository;
import com.bank.project.business.entity.BankRecord;
import com.bank.project.business.entity.BankRecordRepository;
import com.bank.project.business.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private BankRecordRepository bankRecordRepository;

    private static ObjectMapper om;
    private static HttpHeaders headers;

    private static final String API_URL = "/api/v1";

    @BeforeAll
    public static void init() {
        om = new ObjectMapper();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public String createJson(Object object) throws Exception {
        return om.writeValueAsString(object);
    }

    public HttpEntity<String> getHeaderWithBody(String body) {
        return new HttpEntity<>(body, headers);
    }

    @BeforeEach
    public void getAccountEntity() {
        String userId1 = "tera-kay";
        String publicToken1 = "8nLQ69+5abTF+aB3B73HMY1L9ZIEYA5yy0gyRiKa/ImMSPsXl5Rd9qppU98PSvtOjy3KHJ0BdHfqvBqIOi4F3g==";
        String privateToken1 = "562494cf803718dc6baadf936353b00abaf7da05d39fa8625c21ad4494b750b6";
        Account account = Account
                .builder()
                .id(1L)
                .userId(userId1)
                .publicToken(publicToken1)
                .privateToken(privateToken1)
                .balance(0)
                .build();

        accountRepository.save(account);

        String userId2 = "bank-user-key";
        String publicToken2 = "Ut/vfAJ9BlbjCWoJk7l4SuWO21ke2M3FudzkqXoxQIWilz8ErF8Wh7rjPxQHEMRUq7P2Bto0fZKD4bO5Je/8Mg==";
        String privateToken2 = "88848bf5caf5e79fc69883f40440618b98304cf30b8a2d1d864fec63b704aa1f";
        Account account2 = Account
                .builder()
                .id(2L)
                .userId(userId2)
                .publicToken(publicToken2)
                .privateToken(privateToken2)
                .balance(50000)
                .build();

        accountRepository.save(account2);
    }

    public BankRecord getBankRecord() {
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "8nLQ69+5abTF+aB3B73HMY1L9ZIEYA5yy0gyRiKa/ImMSPsXl5Rd9qppU98PSvtOjy3KHJ0BdHfqvBqIOi4F3g==";
        String privateToken = "562494cf803718dc6baadf936353b00abaf7da05d39fa8625c21ad4494b750b6";
        BankRecord bankRecord = BankRecord.builder()
                .transactionKey(transactionKey)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .bankType(BankType.REMITTANCE)
                .resultCode("BR00")
                .balance(30000)
                .build();

        return bankRecordRepository.save(bankRecord);
    }

    @Test
    @DisplayName("계좌생성 테스트")
    public void createAccount() throws Exception {
        // given
        AccountReqSaveDto reqSaveDto = new AccountReqSaveDto();
        reqSaveDto.setUserId("tera-new-account");

        final String json = createJson(reqSaveDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account", HttpMethod.POST, request, String.class);
        System.out.println("response: " +response.getBody().toString());
        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String userId = dc.read("$.body.userId");
        String publicToken = dc.read("$.body.publicToken");
        String privateToken = dc.read("$.body.privateToken");

        assertThat(userId).isEqualTo(reqSaveDto.getUserId());
        assertThat(publicToken.isEmpty()).isFalse();
        assertThat(privateToken.isBlank()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("계좌 중복 생성 테스트")
    public void duplicateAccount() throws Exception {
        // given
        AccountReqSaveDto reqSaveDto = new AccountReqSaveDto();
        reqSaveDto.setUserId("tera-kay");

        final String json = createJson(reqSaveDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body.message");
        String errorCode = dc.read("$.body.code");

        assertThat(errorMessage).isEqualTo("Entity Already Exists");
        assertThat(errorCode).isEqualTo("A002");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("유저아이디 유효성실패 계좌생성 테스트")
    public void createAccountFail() throws Exception {
        // given
        AccountReqSaveDto reqSaveDto = new AccountReqSaveDto();
        reqSaveDto.setUserId("te");

        final String json = createJson(reqSaveDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorMessage).isEqualTo("{userId=id 는 최소 4자 최대 50자 입니다.}");
    }

    @Test
    @DisplayName("입금 테스트")
    public void deposit() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        DepositReqDto reqDto = new DepositReqDto();
        reqDto.setDepositBalance(30000);
        reqDto.setPublicToken(depositAccount.getPublicToken());

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/deposit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String userId = dc.read("$.body.userId");

        assertThat(userId).isEqualTo(depositAccount.getUserId());
        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BD00));
    }

    @Test
    @DisplayName("입금 계좌번호 조회 실패 테스트")
    public void depositAccountNumberFail() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        DepositReqDto reqDto = new DepositReqDto();
        reqDto.setDepositBalance(30000);
        reqDto.setPublicToken("publicAccount");

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/deposit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String userId = dc.read("$.body.userId");

        assertThat(userId).isEqualTo(reqDto.getPublicToken());
        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BA00));
    }

    @Test
    @DisplayName("입금 유효성 실패 테스트")
    public void depositInValidData() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        DepositReqDto reqDto = new DepositReqDto();
        reqDto.setDepositBalance(0);
        reqDto.setPublicToken("publicAccount");

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/deposit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorMessage).isEqualTo("{depositBalance=입금 최소 금액은 100원 이상입니다.}");
    }

    @Test
    @DisplayName("출금 테스트")
    public void withdraw() throws Exception {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setWithdrawalBalance(30000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/withdraw", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String userId = dc.read("$.body.userId");
        int balance = dc.read("$.body.balance");

        assertThat(userId).isEqualTo(withdrawAccount.getUserId());
        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BW00));
        assertThat(balance).isEqualTo(20000);
    }

    @Test
    @DisplayName("출금 계좌 비밀번호 오류 테스트")
    public void withdrawAccountPasswordFail() throws Exception {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken("private-password");
        reqDto.setWithdrawalBalance(30000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/withdraw", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String userId = dc.read("$.body.userId");
        int balance = dc.read("$.body.balance");

        assertThat(userId).isEqualTo(reqDto.getPrivateToken());
        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BA01));
        assertThat(balance).isEqualTo(30000);
    }

    @Test
    @DisplayName("출금 계좌 잔액 부족 테스트")
    public void withdrawAccountBalanceLack() throws Exception {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setWithdrawalBalance(100000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/withdraw", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String userId = dc.read("$.body.userId");
        int balance = dc.read("$.body.balance");

        assertThat(userId).isEqualTo(withdrawAccount.getUserId());
        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BW90));
        assertThat(balance).isEqualTo(withdrawAccount.getBalance());
    }

    @Test
    @DisplayName("출금 계좌 비밀번호 유효성 실패 테스트")
    public void withdrawInValidTokenData() throws Exception {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken("");
        reqDto.setWithdrawalBalance(40000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/withdraw", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(errorMessage).isEqualTo("{privateToken=비어 있을 수 없습니다}");
    }

    @Test
    @DisplayName("출금 금액 유효성 실패 테스트")
    public void withdrawInValidBalanceData() throws Exception {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setWithdrawalBalance(0);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/withdraw", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorMessage).isEqualTo("{withdrawalBalance=최소 출금 금액은 100원 이상입니다.}");
    }

    @Test
    @DisplayName("송금 테스트")
    public void remit() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setRemittanceBalance(40000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String transactionKey = dc.read("$.body.transactionKey");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BR00));
        assertThat(transactionKey.isEmpty()).isFalse();
        assertThat(transactionKey.isBlank()).isFalse();
    }

    @Test
    @DisplayName("송금 예금 계좌번호 조회 실패 테스트")
    public void remitAccountNumberFail() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken("public-token");
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setRemittanceBalance(40000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String transactionKey = dc.read("$.body.transactionKey");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BA00));
        assertThat(transactionKey.isEmpty()).isFalse();
        assertThat(transactionKey.isBlank()).isFalse();
    }

    @Test
    @DisplayName("송금 출금 계좌번호 실패 테스트")
    public void remitAccountPasswordFail() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken("private-token");
        reqDto.setRemittanceBalance(40000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String transactionKey = dc.read("$.body.transactionKey");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BA01));
        assertThat(transactionKey.isEmpty()).isFalse();
        assertThat(transactionKey.isBlank()).isFalse();
    }

    @Test
    @DisplayName("송금 출금 계좌 잔고 부족 테스트")
    public void remitAccountBalanceLack() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setRemittanceBalance(140000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");
        String transactionKey = dc.read("$.body.transactionKey");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BW90));
        assertThat(transactionKey.isEmpty()).isFalse();
        assertThat(transactionKey.isBlank()).isFalse();
    }

    @Test
    @DisplayName("송금 계좌번호 유효성 실패 테스트")
    public void remitInValidAccountNumber() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken("");
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setRemittanceBalance(140000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("송금 계좌 비밀번호 유효성 실패 테스트")
    public void remitInValidAccountPassword() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken("");
        reqDto.setRemittanceBalance(140000);

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("송금 최소 금액 유효성 실패 테스트")
    public void remitInValidBalance() throws Exception {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());

        final String json = createJson(reqDto);

        // when
        final HttpEntity<String> request = getHeaderWithBody(json);

        final ResponseEntity<String> response =
                template.exchange(API_URL + "/account/remit", HttpMethod.POST, request, String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String errorMessage = dc.read("$.body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorMessage).isEqualTo("{remittanceBalance=송금 최소 금액은 100원 이상입니다.}");
    }

    @Test
    @DisplayName("송금 결과 조회 테스트")
    public void inquiryRemittance() throws Exception {
        // given
        final BankRecord bankRecord = getBankRecord();

        // when
        final ResponseEntity<String> response =
                template.getForEntity(API_URL + "/account/" + bankRecord.getTransactionKey(),
                                     String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BR00));
    }

    @Test
    @DisplayName("송금 결과 조회 식별 키 실패 테스트")
    public void inquiryRemittanceNotFoundTransactionKey() throws Exception {
        // given
        final BankRecord bankRecord = getBankRecord();

        // when
        final ResponseEntity<String> response =
                template.getForEntity(API_URL + "/account/trKey",
                        String.class);

        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        String resultCode = dc.read("$.body.resultCode");

        assertThat(resultCode).isEqualTo(String.valueOf(BankCode.BR90));
    }

    @Test
    @DisplayName("예금 다중 동시 테스트")
    public void depositTransaction() throws InterruptedException {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        int numOfExecutor = 100;
        AtomicInteger successCnt = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(numOfExecutor);

        DepositReqDto reqDto = new DepositReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setDepositBalance(1000);

        // when
        for (int i = 0; i < numOfExecutor; i++) {
            executorService.execute(() -> {
                try {
                    accountService.depositBalance(reqDto);
                    successCnt.getAndIncrement();
                } catch (Exception e) {
                    System.out.println("exception: " + e.getMessage());
                }
                latch.countDown();
            });
        }
        latch.await();

        final Account updatedDepositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        // then
        assertThat(successCnt.get()).isEqualTo(100);
        assertThat(updatedDepositAccount.getBalance()).isEqualTo(100000);
    }

    @Test
    @DisplayName("출금 다중 동시 테스트")
    public void withdrawTransaction() throws InterruptedException {
        // given
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        int numOfExecutor = 100;
        AtomicInteger successCnt = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(numOfExecutor);

        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setWithdrawalBalance(1000);

        // when
        for (int i = 0; i < numOfExecutor; i++) {
            executorService.execute(() -> {
                try {
                    accountService.withdrawBalance(reqDto);
                    successCnt.getAndIncrement();
                } catch (Exception e) {
                    System.out.println("exception: " + e.getMessage());
                }
                latch.countDown();
            });
        }
        latch.await();

        final Account updatedWithdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        // then
        assertThat(successCnt.get()).isEqualTo(100);
        assertThat(updatedWithdrawAccount.getBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("송금 다중 동시 테스트")
    public void remitTransaction() throws InterruptedException {
        // given
        final Account depositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account withdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        int numOfExecutor = 100;
        AtomicInteger successCnt = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(numOfExecutor);

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(depositAccount.getPublicToken());
        reqDto.setPrivateToken(withdrawAccount.getPrivateToken());
        reqDto.setRemittanceBalance(2000);

        // when
        for (int i = 0; i < numOfExecutor; i++) {
            executorService.execute(() -> {
                try {
                    accountService.remittanceBalance(reqDto);
                    successCnt.getAndIncrement();
                } catch (Exception e) {
                    System.out.println("exception: " + e.getMessage());
                }
                latch.countDown();
            });
        }
        latch.await();

        final Account updatedDepositAccount = accountRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));
        final Account updatedWithdrawAccount = accountRepository.findById(2L)
                .orElseThrow(() -> new IllegalArgumentException("Entity Not Found"));

        // then
        assertThat(successCnt.get()).isEqualTo(100);
        assertThat(updatedDepositAccount.getBalance()).isEqualTo(50000);
        assertThat(updatedWithdrawAccount.getBalance()).isEqualTo(0);
    }
}
