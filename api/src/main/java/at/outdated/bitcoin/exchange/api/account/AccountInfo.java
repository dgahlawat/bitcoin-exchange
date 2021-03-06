package at.outdated.bitcoin.exchange.api.account;

import at.outdated.bitcoin.exchange.api.container.CurrencyContainer;
import at.outdated.bitcoin.exchange.api.currency.Currency;
import at.outdated.bitcoin.exchange.api.currency.CurrencyValue;
import at.outdated.bitcoin.exchange.api.market.ExchangeRateCalculator;
import at.outdated.bitcoin.exchange.api.market.OrderType;
import at.outdated.bitcoin.exchange.api.market.fee.Fee;
import at.outdated.bitcoin.exchange.api.performance.CombinedPerformance;
import at.outdated.bitcoin.exchange.api.performance.Performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 24.05.13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public abstract class AccountInfo {

    protected Currency defaultCurrency = Currency.EUR;

    protected CurrencyContainer<Wallet> wallets = new CurrencyContainer<>();

    abstract public Fee getTradeFee(OrderType trade);

    public CurrencyValue getTradeFee(OrderType trade, CurrencyValue volume) {
        return getTradeFee(trade).calculate(trade, volume);
    }

    public Wallet getWallet(Currency curr) {
        return wallets.get(curr);
    }

    public void addWallet(Wallet wallet) {
        wallets.set(wallet.getCurrency(), wallet);
    }

    public List<WalletTransaction> getAllTransactions() {

        List<WalletTransaction> transactionList = new ArrayList<>();

        for(Wallet w : wallets) {
            transactionList.addAll(w.getTransactions());
        }

        return transactionList;
    }


    public CurrencyValue getOverallBalance(Currency currency) {

        CurrencyValue totalBalance = new CurrencyValue(currency);

        for(Wallet w : wallets) {
            totalBalance.add(w.getBalance());
        }

        return totalBalance;
    }


    public Performance getOverallPerformance(Currency inCurrency, ExchangeRateCalculator calculator) {
        CombinedPerformance perf = new CombinedPerformance(inCurrency, calculator);

        for(Wallet w : wallets) {
           perf.includeWallet(w);
        }

        return perf;
    }

}
