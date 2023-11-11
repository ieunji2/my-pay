package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.CreateWalletServiceDto;
import com.hello.money.v1.dto.GetWalletServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import com.hello.money.v1.repository.TransactionRepository;
import com.hello.money.v1.repository.WalletRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
public class MoneyServiceTest {

  @Autowired
  private MoneyService moneyService;

  @Autowired
  private WalletPort walletPort;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private MoneyExceptionTest exceptionTest;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  private static Stream<Arguments> createWalletServiceDtoParam() {
    return Stream.of(
            arguments(new CreateWalletServiceDto(1L, "이름")));
  }

  private static Stream<Arguments> getWalletServiceDtoParam() {
    return Stream.of(
            arguments(new GetWalletServiceDto(1L, "이름")));
  }

  private static Stream<Arguments> chargeMoneyServiceDtoParam() {
    return Stream.of(
            arguments(new ChargeMoneyServiceDto(1L, "이름", BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyServiceDtoParam() {
    return Stream.of(
            arguments(new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.valueOf(2000), "적요")));
  }

  @ParameterizedTest
  @MethodSource("createWalletServiceDtoParam")
  @DisplayName("지갑을 생성한다")
  void 지갑생성(final CreateWalletServiceDto dto) {
    //given, when
    final Wallet wallet = moneyService.createWallet(dto);

    //then
    assertThat(wallet.getAccountId()).isEqualTo(dto.accountId());
    assertThat(wallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("createWalletServiceDtoParam")
  @DisplayName("지갑 생성 후 계정 아이디로 지갑이 있는지 확인한다.")
  void 지갑_있는지_확인(final CreateWalletServiceDto dto) {
    //given
    walletPort.saveWallet(new Wallet(dto.accountId()));

    //when
    final boolean exists = walletPort.existsWalletByAccountId(dto.accountId());

    //then
    assertThat(exists).isTrue();
  }

  @ParameterizedTest
  @MethodSource("createWalletServiceDtoParam")
  @DisplayName("동일한 계정으로 지갑 중복 생성 시 예외가 발생한다.")
  void 지갑_중복_생성_예외_발생(final CreateWalletServiceDto dto) {
    //given
    moneyService.createWallet(dto);

    //when, then
    assertThatThrownBy(() -> {
      moneyService.createWallet(dto);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("createWalletServiceDtoParam")
  @DisplayName("지갑 생성 후 지갑을 조회한다.")
  void 지갑조회(final CreateWalletServiceDto dto) {
    //given
    moneyService.createWallet(dto);

    //when
    final Wallet wallet = moneyService.getWallet(new GetWalletServiceDto(dto.accountId(), dto.accountName()));

    //then
    assertThat(wallet.getAccountId()).isEqualTo(dto.accountId());
    assertThat(wallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("getWalletServiceDtoParam")
  @DisplayName("계정에 해당하는 지갑이 없으면 조회 시 예외가 발생한다.")
  void 지갑조회_예외_발생(final GetWalletServiceDto dto) {
    //given, when, then
    assertThatThrownBy(() -> {
      moneyService.getWallet(dto);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyServiceDtoParam")
  @DisplayName("지갑 생성 후 금액을 충전한다.")
  void 머니충전(final ChargeMoneyServiceDto dto) {
    //given
    walletPort.saveWallet(new Wallet(dto.accountId()));

    //when
    final Wallet wallet = moneyService.chargeMoney(dto);

    //then
    assertThat(wallet.getAccountId()).isEqualTo(dto.accountId());
    assertThat(wallet.getBalance()).isEqualTo(dto.amount());
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyServiceDtoParam")
  @DisplayName("충전 금액이 0보다 크지 않으면 ChargeMoneyServiceDto 생성 시 예외가 발생한다.")
  void ChargeMoneyServiceDto_생성_예외_발생(final ChargeMoneyServiceDto dto) {
    //given
    final BigInteger amount = BigInteger.ZERO;

    //when, then
    assertThatThrownBy(() -> {
      new ChargeMoneyServiceDto(
              dto.accountId(),
              dto.accountName(),
              amount,
              dto.summary());
    }).isInstanceOf(ConstraintViolationException.class);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyServiceDtoParam")
  @DisplayName("머니충전의 executeSave()에서 예외 발생 시 롤백된다.")
  void 머니충전_예외_발생_시_잔액_충전_안됨(final ChargeMoneyServiceDto dto) {
    //given
    final Wallet wallet = walletPort.saveWallet(new Wallet(dto.accountId()));

    wallet.addMoney(dto.amount());

    //when
    assertThatThrownBy(() -> {
      exceptionTest.executeCharge(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Rollback executeSave");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyServiceDtoParam")
  @DisplayName("동일한 Bean 안에서 상위 메서드에 Transactional 어노테이션이 없으면 하위에 선언되어 있어도 전이되지 않는다.")
  void 머니충전_예외_발생_시_잔액_충전_됨(final ChargeMoneyServiceDto dto) {
    //given
    final Wallet wallet = walletPort.saveWallet(new Wallet(dto.accountId()));

    wallet.addMoney(dto.amount());

    //when
    assertThatThrownBy(() -> {
      executeCharge(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Transactional not working");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(dto.amount());
  }

  @Transactional
  public void executeCharge(final Wallet wallet) {
    walletPort.saveWallet(wallet);
    throw new RuntimeException("Transactional not working");
  }

  @ParameterizedTest
  @MethodSource("sendMoneyServiceDtoParam")
  @DisplayName("지갑생성 및 머니충전 후 금액을 송금한다.")
  void 머니송금(final SendMoneyServiceDto dto) {
    //given
    moneyService.createWallet(new CreateWalletServiceDto(dto.accountId(), dto.accountName()));
    moneyService.chargeMoney(new ChargeMoneyServiceDto(dto.accountId(), dto.accountName(), BigInteger.valueOf(3000), "적요"));

    final Wallet receiverWallet = moneyService.createWallet(new CreateWalletServiceDto(2L, "이름2"));

    //when
    final Wallet senderWallet = moneyService.sendMoney(new SendMoneyServiceDto(
            dto.accountId(),
            dto.accountName(),
            receiverWallet.getId(),
            dto.amount(),
            dto.summary()));

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    //then
    assertThat(senderWallet.getBalance()).isEqualTo(BigInteger.valueOf(1000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
  }

  @ParameterizedTest
  @MethodSource("sendMoneyServiceDtoParam")
  @DisplayName("머니송금의 executeSend()에서 예외 발생 시 롤백된다.")
  void 머니송금_예외_발생_시_잔액_변경_안됨(final SendMoneyServiceDto dto) {
    //given
    moneyService.createWallet(new CreateWalletServiceDto(dto.accountId(), dto.accountName()));
    moneyService.chargeMoney(new ChargeMoneyServiceDto(dto.accountId(), dto.accountName(), BigInteger.valueOf(3000), "적요"));

    moneyService.createWallet(new CreateWalletServiceDto(2L, "이름2"));

    final Wallet senderWallet = walletPort.findWalletByAccountId(dto.accountId());
    final Wallet receiverWallet = walletPort.findWalletByAccountId(2L);

    senderWallet.subtractMoney(dto.amount());
    receiverWallet.addMoney(dto.amount());

    //when
    assertThatThrownBy(() -> {
      exceptionTest.executeSend(senderWallet, receiverWallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Rollback executeSend");

    final Wallet savedSenderWallet = walletPort.findWalletById(senderWallet.getId());
    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    //then
    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(3000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }
}
