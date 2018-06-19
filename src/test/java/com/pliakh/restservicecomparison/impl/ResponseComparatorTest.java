package com.pliakh.restservicecomparison.impl;

import com.pliakh.restservicecomparison.api.IResponseComparator;
import com.pliakh.restservicecomparison.api.RestResponse;
import com.pliakh.restservicecomparison.core.RestServiceComparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringRunner.class)
@SpringBootConfiguration
//@ActiveProfiles({"test"})
public class ResponseComparatorTest {

    //        @Autowired
    private RestServiceComparator restServiceComparator;

    //    @Autowired
    private IResponseComparator responseComparator;

    //    @MockBean
    private RestCaller restCaller;

    @Before
    public void setUp() {
        responseComparator = new ResponseComparator();
        restCaller = Mockito.mock(RestCaller.class);
        restServiceComparator = new RestServiceComparator(restCaller, responseComparator);
    }

    @Test
    public void testNoErrorsDuringWork_Get() {
        // given
        String url1 = "OldUrl";
        String url2 = "NewUrl";
        prepareResponse(url1, new RestResponse(10, HttpStatus.OK, "{123}"), "get");
        prepareResponse(url2, new RestResponse(11, HttpStatus.ACCEPTED, "val:qwe}"), "get");

        // when
        restServiceComparator.doCompareRest(url1, url2, new HashMap<>(), new ArrayList<>());

        // then no errors
        Assert.assertTrue(true);
    }


    @Test
    public void testNoErrorsDuringWork_Post() {
        // given
        String url1 = "OldUrl";
        String url2 = "NewUrl";
        prepareResponse(url1, new RestResponse(10, HttpStatus.OK, "{123}"), "post");
        prepareResponse(url2, new RestResponse(11, HttpStatus.ACCEPTED, "val:qwe}"), "post");

        // when
        restServiceComparator.doCompareRest(url1, url2, new String(), new ArrayList<>());

        // then no errors
        Assert.assertTrue(true);
    }

    private void prepareResponse(String url, RestResponse restResponse, String method) {
        if ("get".equals(method)) {
            Mockito
                    .when(restCaller.doGet(eq(url), any()))
                    .thenReturn(restResponse);
        } else {
            Mockito
                    .when(restCaller.doPost(eq(url), any()))
                    .thenReturn(restResponse);
        }
    }
}
