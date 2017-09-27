package cz.fi.muni.pa165.currency;

import junit.extensions.TestSetup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.math.BigDecimal;
import java.util.Currency;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyConvertorImplTest {

    @Mock
    private ExchangeRateTable exchangeRateTable;
    private CurrencyConvertor currencyConvertor;
    // static resources
    Currency targetCurrency = Currency.getInstance("EUR");
    Currency sourceCurrency = Currency.getInstance("CZK");

    @Test
    public void testConvert() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency)).thenReturn(BigDecimal.TEN);
        // converison result
        BigDecimal convertResult;

        convertResult = currencyConvertor.convert(sourceCurrency, targetCurrency, new BigDecimal("10"));
        checkResult(convertResult, "100.00");

        convertResult = currencyConvertor.convert(sourceCurrency, targetCurrency, new BigDecimal("0"));
        checkResult(convertResult, "0.00");

        convertResult = currencyConvertor.convert(sourceCurrency, targetCurrency, new BigDecimal("0.123456"));
        checkResult(convertResult, "1.23");

        boolean isExceptionCatched = false;
        try {
            currencyConvertor.convert(sourceCurrency, targetCurrency, new BigDecimal("-1"));
        } catch (IllegalArgumentException ex) {
            isExceptionCatched = true;
        }

        assertThat(isExceptionCatched, is(true));
    }

    @Before
    public void beforeTest() {
        currencyConvertor = new CurrencyConvertorImpl(exchangeRateTable);
    }

    private void checkResult(BigDecimal result, String expectedVal) {
        assert result != null;
        assert expectedVal != null;

        assertThat(result, is(new BigDecimal(expectedVal)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullSourceCurrency() {
       currencyConvertor.convert(null, targetCurrency, new BigDecimal("0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullTargetCurrency() {
        currencyConvertor.convert(sourceCurrency, null, new BigDecimal("0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullSourceAmount() {
        currencyConvertor.convert(sourceCurrency, targetCurrency, null);
    }

    @Test(expected = UnknownExchangeRateException.class)
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency)).thenReturn(null);
        currencyConvertor.convert(sourceCurrency, targetCurrency, BigDecimal.TEN);
    }

    @Test(expected = UnknownExchangeRateException.class)
    public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency)).thenThrow(UnknownExchangeRateException.class);
        currencyConvertor.convert(sourceCurrency, targetCurrency, BigDecimal.TEN);
    }

}
