package org.jglue.cdiunit;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.filter.Filter;

@AdditionalClasses(ThresholdFilter.class)
public class TestNonCDIClassesUsingRule {
    
	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

    @Inject 
    private Filter foo;
    
    private ThresholdFilter bar;
    
    @Test(expected=DeploymentException.class)
    public void testNonCDIClassDiscovery() {
        
    }
}
