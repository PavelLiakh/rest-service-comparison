package com.pliakh.restservicecomparison;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
// TODO run mock rest server for test
public class EntryPointIntegrationTests {

	@Test
	public void contextLoads_Get() {
	    // FIXME mock rest services for test
		EntryPoint.main(new String[]{"--file=extOrgKey_dev1_prec1.json"});
	}

	@Test
	public void contextLoads_Post() {
		// FIXME mock rest services for test
		EntryPoint.main(new String[]{"--file=getRights_dev1_prec1.json"});
	}
}
