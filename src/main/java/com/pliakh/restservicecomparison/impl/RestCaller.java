package com.pliakh.restservicecomparison.impl;

import com.pliakh.restservicecomparison.api.IRestCaller;
import com.pliakh.restservicecomparison.api.RestResponse;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("restCaller")
// TODO add test with mock rest server
public class RestCaller implements IRestCaller {

    private static final Logger LOGGER = LoggerFactory.getLogger("application");

    @Override
    public RestResponse doGet(String url, Map<String, String> parameters) {
        RequestSpecification requestSpecification = RestAssured
                .given().baseUri(url).queryParams(parameters).header(new Header("Content-Type", "application/json"));
        return doCall(url, "get", requestSpecification);
    }

    @Override
    public RestResponse doPost(String url, Map<String, String> parameters, String body) {
        RequestSpecification requestSpecification = RestAssured
                .given()
                .baseUri(url)
                .queryParams(parameters)
                .header("Content-Type", "application/json")
                .body(body);
        return doCall(url, "post", requestSpecification);
    }

    private RestResponse doCall(String url, String method, RequestSpecification requestSpecification) {
        Response restResponse = null;
        LOGGER.debug("Start call: " + url);
        try {
            restResponse = method.equals("get")
                    ? requestSpecification.when().get()
                    : requestSpecification.when().post();
        } catch (NullPointerException e) {
            // cannot access endpoint
            LOGGER.warn(String.format("Cannot access url [%s]", url));
            return null;
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Target host is null")) {
                LOGGER.warn(String.format("Cannot access url. Check url [%s]. It must be <http://host/rest>", url));
            }
            return null;
        }
        return new RestResponse(
                restResponse.time(),
                HttpStatus.valueOf(restResponse.getStatusCode()),
                restResponse.getBody().asString(),
                url);
    }
}
