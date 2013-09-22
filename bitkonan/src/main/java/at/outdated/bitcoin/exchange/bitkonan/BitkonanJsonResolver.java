package at.outdated.bitcoin.exchange.bitkonan;

import at.outdated.bitcoin.exchange.api.jaxb.JSONResolver;

import javax.ws.rs.ext.Provider;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 22.09.13
 * Time: 18:54
 * To change this template use File | Settings | File Templates.
 */
@Provider
public class BitkonanJsonResolver extends JSONResolver {

    @Override
    protected Class<?>[] getTypes() {
        return new Class<?>[] { BitkonanTickerValue.class };
    }
}
