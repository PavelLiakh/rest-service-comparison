package com.pliakh.restservicecomparison;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
// TODO run mock rest server for test
public class EntryPointIntegrationTests {

    @Test
    public void contextLoads_Get() throws IOException {
        // FIXME mock rest services for test
        RestServiceComparatorApplication.main(new String[]{"-d", "--file=get_example.json"});
    }

    @Test
    public void contextLoads_Post() throws IOException {
        // FIXME mock rest services for test
        RestServiceComparatorApplication.main(new String[]{"--file=src/main/resources/post_example.json"});
    }
}
