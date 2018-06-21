package com.pliakh.restservicecomparison.impl;

import com.pliakh.restservicecomparison.RestServiceComparatorApplication;
import com.pliakh.restservicecomparison.api.RestResponse;
import com.pliakh.restservicecomparison.core.RestServiceComparator;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestServiceComparatorApplication.class, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles({"test"})
public class ResponseComparatorTest {

    @Autowired
    private RestServiceComparator restServiceComparator;

    @MockBean
    private RestCaller restCaller;

    @Before
    public void setUp() {
        setDebugLvl();
    }

    @Test
    @Ignore
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
    @Ignore
    public void testNoErrorsDuringWork_Post() {
        // given
        String url1 = "OldUrl";
        String url2 = "NewUrl";
        prepareResponse(url1, new RestResponse(10, HttpStatus.OK, "{123}"), "post");
        prepareResponse(url2, new RestResponse(11, HttpStatus.ACCEPTED, "val:qwe}"), "post");

        // when
        restServiceComparator.doCompareRest(url1, url2, new HashMap<>(), "", new ArrayList<>());

        // then no errors
        Assert.assertTrue(true);
    }

    @Test
    public void testParsingAndLog() throws IOException {
        // given
        String url1 = "OldUrl";
        String url2 = "NewUrl";
        prepareResponse(url1, new RestResponse(10, HttpStatus.OK, FileUtils.readFileToString(new File("src/test/resources/response1.json")), url2), "post");
        prepareResponse(url2, new RestResponse(11, HttpStatus.OK, FileUtils.readFileToString(new File("src/test/resources/response2.json")), url2), "post");

        // when
        restServiceComparator.doCompareRest(url1, url2, new HashMap<>(), "", new ArrayList<>());

        // then no errors
        Assert.assertTrue(true);
    }

    private void setDebugLvl() {
        ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(PrettyPrinter.class.getName());
        root.setLevel(ch.qos.logback.classic.Level.DEBUG);
    }

    private void prepareResponse(String url, RestResponse restResponse, String method) {
        if ("get".equals(method)) {
            Mockito
                    .when(restCaller.doGet(eq(url), any()))
                    .thenReturn(restResponse);
        } else {
            Mockito
                    .when(restCaller.doPost(eq(url), any(), any()))
                    .thenReturn(restResponse);
        }
    }
}
