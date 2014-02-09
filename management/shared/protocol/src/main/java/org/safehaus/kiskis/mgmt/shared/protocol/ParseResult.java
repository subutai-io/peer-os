package org.safehaus.kiskis.mgmt.shared.protocol;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/2/13
 * Time: 11:18 PM
 */
public class ParseResult {
    private Response response;
    private Request request;

    public ParseResult(Request request, Response response){
        this.request = request;
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
