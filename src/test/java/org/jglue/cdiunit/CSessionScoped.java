package org.jglue.cdiunit;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class CSessionScoped implements Serializable {
private String _foo;
	
	public String getFoo() {
		return _foo;
	}
	
	public void setFoo(String foo) {
		_foo = foo;
	}

}