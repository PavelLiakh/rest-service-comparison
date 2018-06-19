package com.pliakh.restservicecomparison.api;

import org.springframework.http.HttpStatus;

public class RestResponse {

    private long time;
    private HttpStatus httpStatus;
    private String responseBody;
    private String url;

    public RestResponse(long time, HttpStatus httpStatus, String responseBody) {
        this.time = time;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public RestResponse(long time, HttpStatus httpStatus, String responseBody, String url) {
        this.time = time;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.url = url;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
