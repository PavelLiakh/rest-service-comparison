package com.pliakh.restservicecomparison.impl;

import com.pliakh.restservicecomparison.api.IRestCaller;
import com.pliakh.restservicecomparison.api.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@Component("restCaller")
// TODO add test with mock rest server
public class RestCaller implements IRestCaller {

    @Override
    public RestResponse doGet(String url, Map<String, String> parameters) {
        RequestSpecification requestSpecification = RestAssured
            .given().baseUri(url).queryParams(parameters).header(new Header("Content-Type", "application/json"));
        Response restResponse = requestSpecification.when().get();
        return new RestResponse(
                restResponse.time(),
                HttpStatus.valueOf(restResponse.getStatusCode()),
                restResponse.getBody().asString(),
                url);
    }

    @Override
    public RestResponse doPost(String url, String body) {
        RequestSpecification requestSpecification = RestAssured
            .given()
            .baseUri(url)
            .header("Content-Type", "application/json")
            .body(body);
        Response restResponse = requestSpecification.when().post();
        return new RestResponse(
            restResponse.time(),
            HttpStatus.valueOf(restResponse.getStatusCode()),
            restResponse.getBody().asString(),
            url);
    }
}
