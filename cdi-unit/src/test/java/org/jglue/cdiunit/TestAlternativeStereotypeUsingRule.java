package org.jglue.cdiunit;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jglue.cdiunit.AImplementation3.StereotypeAlternative;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@AdditionalClasses(StereotypeAlternative.class)
public class TestAlternativeStereotypeUsingRule {

	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

	@Inject
    private AImplementation1 impl1;
    
    @Inject
    private AImplementation3 impl3;
    
    @Inject
    private AInterface impl;
    
    @Test
    public void testAlternativeSelected() {

        Assert.assertTrue("Should have been impl3", impl instanceof AImplementation3);
    }

   
}
