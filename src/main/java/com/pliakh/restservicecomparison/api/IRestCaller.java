package com.pliakh.restservicecomparison.api;

import java.util.Map;

public interface IRestCaller {

    RestResponse doGet(String url, Map<String, String> parameters);

    RestResponse doPost(String url, Map<String, String> parameters, String body);
}
