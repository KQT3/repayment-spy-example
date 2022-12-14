package com.example.demo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.CONCURRENT)
public class RepaymentAmountTests {
    LoanCalculatorController controller;

    LoanApplication loanApplication = spy(new LoanApplication());
    LoanRepository repository = mock(LoanRepository.class);
    JavaMailSender mailSender = mock(JavaMailSender.class);
    RestTemplate restTemplate = mock(RestTemplate.class);

    @BeforeEach
    public void setup() {
        controller = new LoanCalculatorController(repository, mailSender, restTemplate);
    }

    @Test
    public void test1YearLoanWholePounds() {
        given(loanApplication.getPrincipal()).willReturn(1200);
        given(loanApplication.getTermInMonths()).willReturn(12);
        given(loanApplication.getInterestRate()).willReturn(new BigDecimal(10));

        controller.processNewLoanApplication(loanApplication);

        assertEquals(new BigDecimal(110), loanApplication.getRepayment());
    }

    @DisabledOnOs(OS.WINDOWS)
    @RepeatedTest(500)
    public void testWhenYearLoanWholePounds() {
        given(loanApplication.getPrincipal()).willReturn(1200);
        given(loanApplication.getTermInMonths()).willReturn(12);
        given(loanApplication.getInterestRate()).willReturn(new BigDecimal(10));

        when(controller.processNewLoanApplication(loanApplication)).then(InvocationOnMock::callRealMethod);

        verify(repository, times(1)).save(loanApplication);
        verify(repository, atLeastOnce()).save(any());
        verify(repository, never()).deleteAll();
        then(loanApplication).should().setRepayment(BigDecimal.valueOf(110));
        assertEquals(new BigDecimal(110), loanApplication.getRepayment());
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 400})
    public void test1888YearLoanWholePounds(int amount) {
        given(loanApplication.getPrincipal()).willReturn(1200);
        given(loanApplication.getTermInMonths()).willReturn(12);
        given(loanApplication.getInterestRate()).willReturn(new BigDecimal(10));
        System.out.println(amount);
        when(controller.processNewLoanApplication(loanApplication)).then(InvocationOnMock::callRealMethod);

        then(loanApplication).should().setRepayment(BigDecimal.valueOf(110));
    }

    @Test
    public void testWhenThenYearLoanWholePounds() {
        when(loanApplication.getPrincipal()).thenReturn(1200);
        when(loanApplication.getTermInMonths()).thenReturn(12);
        when(loanApplication.getInterestRate()).thenReturn(new BigDecimal(10));

        controller.processNewLoanApplication(loanApplication);

        assertEquals(new BigDecimal(110), loanApplication.getRepayment());
    }

    @ParameterizedTest
    @CsvSource({"100, Mary", "200, Rachid"})
    public void test2YearLoanWholePounds(int amount, String name) {
        //given
        loanApplication.setPrincipal(1200);
        loanApplication.setTermInMonths(24);
        doReturn(new BigDecimal(10)).when(loanApplication).getInterestRate();
        System.out.println(amount + " " + name);
        //when
        controller.processNewLoanApplication(loanApplication);

        //then
        assertEquals(new BigDecimal(60), loanApplication.getRepayment());
    }

    @SneakyThrows
    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void test6YearLoanWithRounding() {
        //given
        Thread.sleep(100);
        loanApplication.setPrincipal(5000);
        loanApplication.setTermInMonths(60);
        when(loanApplication.getInterestRate()).thenReturn(BigDecimal.valueOf(6.5));
        //when
        controller.processNewLoanApplication(loanApplication);

        //then
        assertEquals(new BigDecimal(111), loanApplication.getRepayment());
    }

    @Test
    public void test10YearLoanWholePounds() {
        given(loanApplication.getPrincipal()).willReturn(1200);
        given(loanApplication.getTermInMonths()).willReturn(12);
        given(loanApplication.getInterestRate()).willReturn(new BigDecimal(10));

        controller.processNewLoanApplication(loanApplication);

        assertEquals(new BigDecimal(110), loanApplication.getRepayment());
    }


    @Test
    public void test166YearLoanWholePounds() {
        given(loanApplication.getPrincipal()).willReturn(1200);
        given(loanApplication.getTermInMonths()).willReturn(12);
        given(loanApplication.getInterestRate()).willReturn(new BigDecimal(10));

        when(controller.processNewLoanApplication(loanApplication)).then(InvocationOnMock::callRealMethod);

        assertEquals(new BigDecimal(110), loanApplication.getRepayment());
        assertTimeout(Duration.ofSeconds(1), () -> Thread.sleep(900));
    }

    @Disabled
    @ParameterizedTest
    @DisplayName("mark resources as test-resources")
    @CsvFileSource(resources = "/details.csv")
    public void test5YearLoanWithRounding(double amount, String name) {
        //given
        loanApplication.setPrincipal(5000);
        loanApplication.setTermInMonths(60);
        when(loanApplication.getInterestRate()).thenReturn(BigDecimal.valueOf(6.5));

        //when
        controller.processNewLoanApplication(loanApplication);
        System.out.println(amount + " " + name);
        //then
        assertEquals(new BigDecimal(111), loanApplication.getRepayment());
    }
}
