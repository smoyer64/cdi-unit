package org.jglue.cdiunit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@AdditionalClasses({InheretedExtension.class})
public class TestExtensionInheritanceUsingRule {

	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

	@Test
	public void test() {
	
	}
	
}