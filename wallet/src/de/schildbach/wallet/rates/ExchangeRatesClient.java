package de.schildbach.wallet.rates;

import javax.annotation.Nullable;

import java.util.List;


/**
 * @author Samuel Barbosa
 */
public interface ExchangeRatesClient {

    @Nullable
    List<ExchangeRate> getRates() throws Exception;

}
