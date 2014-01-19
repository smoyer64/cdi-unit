package org.jglue.cdiunit;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Rule;
import org.junit.Test;

public class TestCircularInjectUsingRule {
	
	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

	@Inject
	private Provider<CircularA> circularA;

	@Test(expected=DeploymentException.class)
	public void testCircularDependency() {
		circularA.get();
	}
}
