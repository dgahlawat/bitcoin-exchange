package at.outdated.bitcoin.exchange.bitcurex.jaxb;

import at.outdated.bitcoin.exchange.api.jaxb.StringNumberAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by ebirn on 20.10.13.
 */

public class BitcurexOrder {

    @XmlElement
    String oid;

    @XmlElement
    BitcurexOrderType type;

    @XmlElement
    @XmlJavaTypeAdapter(StringNumberAdapter.class)
    Number amount;

    @XmlElement
    @XmlJavaTypeAdapter(StringNumberAdapter.class)
    Number price;

    public String getOid() {
        return oid;
    }

    public BitcurexOrderType getType() {
        return type;
    }

    public Number getAmount() {
        return amount;
    }

    public Number getPrice() {
        return price;
    }
}
