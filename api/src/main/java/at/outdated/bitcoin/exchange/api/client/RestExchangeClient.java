package at.outdated.bitcoin.exchange.api.client;

import at.outdated.bitcoin.exchange.api.market.Market;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 24.05.13
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class RestExchangeClient extends ExchangeClient {

    protected Client client;

    protected final String userAgent = "RestExchangeClient/1.0a";

    public RestExchangeClient(Market market) {
        super(market);
        client  = ClientBuilder.newClient();
    }

    public RestExchangeClient(Market market, Client client) {
        super(market);
        this.client = client;
    }

    protected JsonObject jsonFromString(String s) {
        try {
            return Json.createReader(new StringReader(s)).readObject();
        }
        catch(Exception e) {
            log.error("unparsable json: {}", s);
            throw e;
        }
    }

    protected JsonArray jsonArrayFromString(String s) {
        return Json.createReader(new StringReader(s)).readArray();
    }

    protected BigDecimal[][] parseNestedArray(JsonArray jsonArray) {


        int len = jsonArray.size();
        BigDecimal[][] resultArray = new BigDecimal[len][];

        for(int i=0; i<len; i++) {


            JsonArray innerJsonArray = jsonArray.getJsonArray(i);
            int innerLen = innerJsonArray.size();
            BigDecimal[] inner = new BigDecimal[innerLen];

            for(int j=0; j<innerLen; j++) {

                // parse crappy
                switch(innerJsonArray.get(j).getValueType()) {
                    case STRING:
                        inner[j] = new BigDecimal(innerJsonArray.getString(j));
                        break;

                    case NUMBER:
                        inner[j] = innerJsonArray.getJsonNumber(j).bigDecimalValue();
                        break;
                }
            }
            resultArray[i] = inner;
        }

        return resultArray;
    }


    // simplified public api
    protected <R> R simpleGetRequest(WebTarget resource, Class<R> resultClass) {
        return syncRequest(resource, resultClass, HttpMethod.GET, null);
    }

    protected <R> R simplePostRequest(WebTarget resource, Class<R> resultClass, Entity payload) {
        return syncRequest(resource, resultClass, HttpMethod.POST, payload);
    }

    protected <R> R simplePutRequest(WebTarget resource, Class<R> resultClass, Entity payload) {
        return syncRequest(resource, resultClass, HttpMethod.PUT, payload);
    }


    // simplified protected api
    protected <R> R protectedGetRequest(WebTarget resource, Class<R> resultClass) {
        return syncRequest(resource, resultClass, HttpMethod.GET, null, true);
    }

    protected <R> R protectedGetRequest(WebTarget resource, Class<R> resultClass, Entity payload) {
        return syncRequest(resource, resultClass, HttpMethod.GET, payload, true);
    }


    protected <R> R protectedPostRequest(WebTarget resource, Class<R> resultClass, Entity payload) {
        return syncRequest(resource, resultClass, HttpMethod.POST, payload, true);
    }

    protected <R> R protectedPutRequest(WebTarget resource, Class<R> resultClass, Entity payload) {
        return syncRequest(resource, resultClass, HttpMethod.PUT, payload, true);
    }

    protected <T> Invocation.Builder setupResource(WebTarget res, Entity<T> e) {
        return res.request().header("User-Agent", userAgent);
    }

    protected abstract <T> Invocation.Builder setupProtectedResource(WebTarget res, Entity<T> entity);

    protected <R> Future<R> asyncRequest(WebTarget resource, Class<R> resultClass, String httpMethod, Entity payload) {
        return asyncRequest(resource, resultClass, httpMethod, payload, false);
    }

    protected <R> Future<R> asyncRequest(WebTarget resource, Class<R> resultClass, String httpMethod, Entity payload, boolean secure) {

        Future<R> result = null;

        Invocation.Builder builder = null;
        if(secure) {
            builder = setupProtectedResource(resource, payload);
        }
        else {
            builder = setupResource(resource, payload);
        }

        result = builder.async().method(httpMethod, payload, resultClass);

        return result;
    }

    // default is an unsecured request
    protected <R> R syncRequest(WebTarget resource, Class<R> resultClass, String httpMethod, Entity payload) {
        return syncRequest(resource, resultClass, httpMethod, payload, false);
    }

    protected <R> R syncRequest(WebTarget resource, Class<R> resultClass, String httpMethod, Entity payload, boolean secure) {

        R result = null;
        Date requestDate = new Date();
        Response response = null;
        try {
            Invocation.Builder builder = null;

            if(secure) {
                builder = setupProtectedResource(resource, payload);
            }
            else {
                builder = setupResource(resource, payload);
            }

            // better for debugging to do this in 2 lines ;-)
            if(httpMethod == HttpMethod.GET || httpMethod == HttpMethod.HEAD) {
                payload = null;
            }

            response = builder.header("User-Agent", userAgent).method(httpMethod, payload);
            result = response.readEntity(resultClass);
        }
        //
        catch (WebApplicationException wae) {
            handleApiError(wae);
        }
        catch(ProcessingException pe) {
            log.error("cannot process returned data", pe);
        }
        catch(Exception e) {
            log.error("unexpected exception", e);
        }
        finally {
            updateApiLag(requestDate);

            if(response != null) {
                response.close();
            }
        }

        return result;
    }


    // very basic request error handling: log it!
    protected void handleApiError(javax.ws.rs.WebApplicationException wae) {
        log.error("failed request: {}", wae.getResponse().getStatusInfo());
        log.error(wae.getMessage());
    }


    protected void updateApiLag(Date requestDate/*, Date responseDate*/) {
        Date responseDate = new Date();
        double apiDiff = (responseDate.getTime()-requestDate.getTime())/1000.0;
        apiLagTrack.insert(apiDiff);
    }


    // this is taken from Form MessageBodyWriter in Glassfish, should produce same output
    // necessary for API Sign headers
    protected String formData2String(Form form) {
        final StringBuilder sb = new StringBuilder();

        try {
            for (Map.Entry<String, List<String>> e : form.asMap().entrySet()) {
                for (String value : e.getValue()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                    if (value != null) {
                        sb.append('=');
                        sb.append(URLEncoder.encode(value, "UTF-8"));
                    }
                }
            }
        }
        catch(Exception e) {
            log.error("failed to convert form", e);
        }

        return sb.toString();
    }

}
