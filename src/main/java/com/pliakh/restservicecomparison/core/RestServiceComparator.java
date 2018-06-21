package com.pliakh.restservicecomparison.core;

import com.pliakh.restservicecomparison.api.IResponseComparator;
import com.pliakh.restservicecomparison.api.RestResponse;
import com.pliakh.restservicecomparison.impl.PrettyPrinter;
import com.pliakh.restservicecomparison.impl.RestCaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RestServiceComparator {

    @Autowired
    @Qualifier("restCaller")
    RestCaller restCaller;

    @Autowired
    IResponseComparator responseComparator;

    @Autowired
    PrettyPrinter prettyPrinter;

    public RestServiceComparator(RestCaller restCaller, IResponseComparator responseComparator) {
        this.restCaller = restCaller;
        this.responseComparator = responseComparator;
    }

    public void doCompareRest(String url1, String url2, Map<String, String> parameters, List<String> excludeFields) {
        RestResponse restResponse1 = restCaller.doGet(url1, parameters);
        RestResponse restResponse2 = restCaller.doGet(url2, parameters);
        if (Objects.isNull(restResponse1) || Objects.isNull(restResponse2)) {
            prettyPrinter.warn("Cannot access urls. Exit");
            System.exit(7);
        }
        responseComparator.doCompare(restResponse1, restResponse2, excludeFields);
    }

    public void doCompareRest(String url1, String url2, Map<String, String> parameters, String body,
                              List<String> excludeFields) {
        RestResponse restResponse1 = restCaller.doPost(url1, parameters, body);
        RestResponse restResponse2 = restCaller.doPost(url2, parameters, body);
        if (Objects.isNull(restResponse1) || Objects.isNull(restResponse2)) {
            prettyPrinter.warn("Cannot access urls. Exit");
            System.exit(8);
        }
        responseComparator.doCompare(restResponse1, restResponse2, excludeFields);
    }
}
