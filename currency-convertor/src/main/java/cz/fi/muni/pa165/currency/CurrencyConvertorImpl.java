package cz.fi.muni.pa165.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    private static final Logger log = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
        if (sourceCurrency == null || targetCurrency == null || sourceAmount == null || sourceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid parametres");
        }
        log.trace("Converting \n"
                + "sourceCurrency " + sourceCurrency.toString() + "\n"
                + "targetCurrency " + targetCurrency.toString() + "\n"
                + "sourceAmmout " + sourceAmount);

        BigDecimal result = null;
        try {
            BigDecimal exchangeRate = exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency);
            if (exchangeRate != null) {
                result = exchangeRate.multiply(sourceAmount).setScale(2, RoundingMode.HALF_EVEN);
            } else {
                log.warn("ExchangeRate is unknown");
                throw new UnknownExchangeRateException("ExchangeRate is unknown");
            }
        } catch (ExternalServiceFailureException e) {
            log.error("Lookup for current exchange rate failed.");
            throw new UnknownExchangeRateException("Lookup for current exchange rate failed.");
        }

        return result;
    }

}
