package org.jglue.cdiunit.mockito;

import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.CdiRule;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.ProducesAlternative;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

public class TestIgnoreMockedClassesUsingRule {

	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

	@Inject
	private AService service;

	@Produces
	@ProducesAlternative
	@Mock
	private BService mock;

	@Test
	public void testConfiguration() {
		assertTrue("AService must have a mocked BService", service.hasService());
	}

}