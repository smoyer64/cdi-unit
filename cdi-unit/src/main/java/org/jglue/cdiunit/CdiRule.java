/**
 * 
 */
package org.jglue.cdiunit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.context.ContextNotActiveException;
import javax.naming.InitialContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author smoyer1
 *
 */
public class CdiRule implements TestRule {

    private Class<?> clazz;
    private Weld weld;
    private WeldContainer container;
    private Throwable startupException;

	/**
	 * 
	 */
	public CdiRule() {
		System.out.println("CdiRule()");
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.junit.rules.TestRule#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
	 */
	@Override
	public Statement apply(Statement base, Description description) {
		System.out.println("CdiRule.apply()");
		final Statement originalStatement = base;
		
		System.out.println("CdiRule.apply() - Class: " + description.getTestClass().getName());
		System.out.println("CdiRule.apply() - Method: " + description.getMethodName());
		System.out.println("CdiRule.apply() - Is test?: " + description.isTest());
		
		System.out.println("clazz = description.getTestClass()");
		clazz = description.getTestClass();
		Object object = null;
		try {
			object = createTest();
		} catch (Exception e) {
			startupException = new Exception("Failed to create the test object", e);
		}
		
		Method method = null;
		try {
			method = clazz.getMethod(description.getMethodName(), new Class<?>[0]);
		} catch (NoSuchMethodException e) {
			startupException = new Exception("Couldn't find the named method", e);
		} catch (SecurityException e) {
			startupException = new Exception("Permission denied to the named method", e);
		}
		
		return createStatement(object, method);
//		return new Statement() {
//			
//			public void evaluate() throws Throwable {
//				System.out.println("evaluate()");
//				//originalStatement.evaluate();
//				throw new ContextNotActiveException();
//			}
//			
//		};
	}
	
	private Statement createStatement(final Object object, final Method method) {
		return new Statement() {

            @Override
            public void evaluate() throws Throwable {
            	System.out.println("evaluate()");

                if (startupException != null) {
                	System.out.println("startException is not null");
                	System.out.println(startupException);
                	System.out.println(startupException.getCause());
                    if (method.getAnnotation(Test.class).expected() == startupException.getClass()) {
                    	System.out.println("equals matched");
                        return;
                    }
                    if(method.getAnnotation(Test.class).expected().isAssignableFrom(startupException.getClass())) {
                    	System.out.println("isAssignableFrom matched");
                    	return;
                    }
                    throw startupException;
                }
                
                System.setProperty("java.naming.factory.initial", "org.jglue.cdiunit.internal.CdiUnitContextFactory");
                InitialContext initialContext = new InitialContext();
                initialContext.bind("java:comp/BeanManager", container.getBeanManager());

                try {
                	System.out.println("Invoking");
                	method.invoke(object, new Object[0]);
                } catch(InvocationTargetException e) {
                	System.out.println("Got here");
                	Throwable cause = e.getCause();
                	if(cause != null) {
	                	Class<? extends Throwable> expected = method.getAnnotation(Test.class).expected();
	                	System.out.println("Cause: " + cause.getClass().getName());
	                	System.out.println("Expected: " + expected.getName());
	                	System.out.println("Equals? " + (expected == cause.getClass()));
	                	if(expected == cause.getClass()) {
	                		System.out.println("equals matched");
	                		return;
	                	}
	                    if (expected != null && expected.isAssignableFrom(cause.getClass())) {
	                    	System.out.println("isAssignableFrom matched");
	                        return;
	                    }
	                    throw cause;
                	}
                	throw e;
                } finally {
                    initialContext.close();
                    weld.shutdown();
                }
            }
            
        };
	}
    
	private Object createTest() throws Exception {
        try {
            Weld.class.getDeclaredMethod("createDeployment", ResourceLoader.class, Bootstrap.class);

            weld = new Weld() {
                protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
                    try {
                        return new WeldTestUrlDeployment(resourceLoader, bootstrap, clazz);
                    } catch (IOException e) {
                        startupException = e;
                        throw new RuntimeException(e);
                    }
                };

            };

            try {
                container = weld.initialize();
            } catch (Throwable e) {
            	e.printStackTrace();
                if (startupException == null) {
                    startupException = e;
                }
            }

        } catch (NoSuchMethodException e) {
            startupException = new Exception(
                    "Weld 1.0.1 is not supported, please use weld 1.1.0 or newer. If you are using maven add\n<dependency>\n  <groupId>org.jboss.weld.se</groupId>\n  <artifactId>weld-se-core</artifactId>\n  <version>1.1.0.Final</version>\n</dependency>\n to your pom.");
        } catch (ClassFormatError e) {
            startupException = new Exception(
                    "There were class format errors. This is often caused by API only jars on the classpath. If you are using maven then you need to place these after the CDI unit dependency as 'provided' scope is still available during testing.", e);
        }
        catch (Throwable e) {
            startupException = new Exception(
                    "Unable to start weld", e);
        }

        return createTest(clazz);
    }

    private <T> T createTest(Class<T> testClass) {
    	System.out.println("is container null? " + (container == null));
    	System.out.println("clazz is: " + clazz.getName());

    	T t = null;
    	if(container != null) {
    		t = container.instance().select(testClass).get();
    	}

        return t;
    }

}
