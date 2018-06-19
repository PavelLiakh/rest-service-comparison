package com.pliakh.restservicecomparison.api;

import java.util.List;

public interface IResponseComparator {

    void doCompare(RestResponse restResponse1, RestResponse restResponse2, List<String> excludeFields);
}
