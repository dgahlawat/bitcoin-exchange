import at.outdated.bitcoin.exchange.api.currency.Currency;
import at.outdated.bitcoin.exchange.api.market.MarketDepth;
import at.outdated.bitcoin.exchange.api.market.TickerValue;
import at.outdated.bitcoin.exchange.kraken.KrakenClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 17.09.13
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
public class KrakenTest {

    KrakenClient client = new KrakenClient();

    @Test
    public void testTickerclient() {


        TickerValue ticker = client.getTicker(Currency.EUR);

        Assert.assertNotNull(ticker.getBid());
        Assert.assertNotNull(ticker.getAsk());
        Assert.assertNotNull(ticker.getCurrency());

        Assert.assertNotNull(ticker.getCurrency());

        System.out.println(ticker);

    }

    @Test
    public void testDepthClient() {


        MarketDepth depth = client.getMarketDepth(Currency.BTC, Currency.EUR);

        Assert.assertNotNull(depth);
        Assert.assertNotNull(depth.getBaseCurrency());

        Assert.assertFalse(depth.getAsks().isEmpty());
        Assert.assertFalse(depth.getBids().isEmpty());
    }
}