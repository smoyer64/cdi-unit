/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit;

import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;

import junit.framework.Assert;

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

@AdditionalClasses({ ESupportClass.class, DummyHttpSession.class, DummyHttpRequest.class, ScopedFactory.class,
		ExcludeExtension.class })
public class TestCdiUnitRule extends BaseTest {
	
	private CdiRule cdiRule = new CdiRule();
	
	@Rule
	public CdiRule getRule() {
		return cdiRule;
	}

	@Inject
	private AImplementation1 aImpl;

	private boolean postConstructCalled;

	@Inject
	private Provider<BRequestScoped> requestScoped;

	@Inject
	private Provider<CSessionScoped> sessionScoped;

	@Inject
	private Provider<DConversationScoped> conversationScoped;

	@Inject
	private Provider<AInterface> a;

	@Inject
	private BeanManager beanManager;

	@Inject
	private FApplicationScoped f1;

	@Inject
	private FApplicationScoped f2;

	@Inject
	private ContextController contextController;

	@Inject
	private BRequestScoped request;
	
	@Inject
	private Conversation conversation;

	@Produces
	private ProducedViaField produced;
	
	@Produces
	public ProducedViaMethod getProducedViaMethod() {
		return new ProducedViaMethod(2);
	}
	
	public TestCdiUnitRule() {
		System.out.println("TestCdiUnitRule()");
	}

	@Test
	@InRequestScope
	public void testRequestScope() {
		BRequestScoped b1 = requestScoped.get();
		b1.setFoo("test"); // Force scoping
		BRequestScoped b2 = requestScoped.get();
		Assert.assertEquals(b1, b2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testRequestScopeFail() {
		BRequestScoped b1 = requestScoped.get();
		b1.setFoo("test"); // Force scoping
	}

	@Test
	@InRequestScope
	@InSessionScope
	public void testSessionScope() {
		CSessionScoped c1 = sessionScoped.get();
		c1.setFoo("test"); // Force scoping
		CSessionScoped c2 = sessionScoped.get();
		Assert.assertEquals(c1, c2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testSessionScopeFail() {
		CSessionScoped c1 = sessionScoped.get();
		c1.setFoo("test"); // Force scoping
	}

	@Test
	@InRequestScope
	@InConversationScope
	public void testConversationScope() {

		DConversationScoped d1 = conversationScoped.get();
		d1.setFoo("test"); // Force scoping
		DConversationScoped d2 = conversationScoped.get();
		Assert.assertEquals(d1, d2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testConversationScopeFail() {
		DConversationScoped d1 = conversationScoped.get();
		d1.setFoo("test"); // Force scoping
	}

	@Mock
	@ProducesAlternative
	@Produces
	private AInterface mockA;

	/**
	 * Test that we can use the test alternative annotation to specify that a mock is used
	 */
	@Test
	public void testTestAlternative() {
		AInterface a1 = a.get();
		Assert.assertEquals(mockA, a1);
	}

	@Test
	public void testPostConstruct() {
		Assert.assertTrue(postConstructCalled);
	}

	@PostConstruct
	public void postConstruct() {
		postConstructCalled = true;
	}

	@Test
	public void testBeanManager() {
		Assert.assertNotNull(getBeanManager());
		Assert.assertNotNull(beanManager);
	}

	@Test
	public void testSuper() {
		Assert.assertNotNull(aImpl.getBeanManager());
	}

	@Test
	public void testApplicationScoped() {
		Assert.assertNotNull(f1);
		Assert.assertNotNull(f2);
		Assert.assertEquals(f1, f2);

		AInterface a1 = f1.getA();
		Assert.assertEquals(mockA, a1);
	}

	@Inject
	private DummyHttpRequest dummyHttpRequest;

	@Inject
	private Provider<Scoped> scoped;

	@Mock
	private Runnable disposeListener;

	@Test
	public void testContextController() {
		contextController.openRequest(dummyHttpRequest);

		Scoped b1 = scoped.get();
		Scoped b2 = scoped.get();
		Assert.assertEquals(b1, b2);
		b1.setDisposedListener(disposeListener);
		contextController.closeRequest();
		Mockito.verify(disposeListener).run();
	}
	
	@Test
	public void testContextControllerRequestScoped() {
		contextController.openRequest(new DummyHttpRequest());

		BRequestScoped b1 = requestScoped.get();
		b1.setFoo("Bar");
		BRequestScoped b2 = requestScoped.get();
		Assert.assertSame(b1.getFoo(), b2.getFoo());
		contextController.closeRequest();
		contextController.openRequest(new DummyHttpRequest());
		BRequestScoped b3 = requestScoped.get();
		Assert.assertEquals(null, b3.getFoo());
	}
	
	@Test
	public void testContextControllerSessionScoped() {
		contextController.openRequest(new DummyHttpRequest());
		
		
		
		CSessionScoped b1 = sessionScoped.get();
		b1.setFoo("Bar");
		CSessionScoped b2 = sessionScoped.get();
		Assert.assertEquals(b1.getFoo(), b2.getFoo());
		contextController.closeRequest();
		contextController.closeSession();
		
		
		contextController.openRequest(new DummyHttpRequest());
		CSessionScoped b3 = sessionScoped.get();
		Assert.assertEquals(null, b3.getFoo());
		
	}
	
	@Test
	public void testContextControllerSessionScopedWithRequest() {
		contextController.openRequest(new DummyHttpRequest());

		
		CSessionScoped b1 = sessionScoped.get();
		b1.setFoo("Bar");	

		BRequestScoped r1 = requestScoped.get();
		b1.setFoo("Bar");
		BRequestScoped r2 = requestScoped.get();
		Assert.assertSame(r1.getFoo(), r2.getFoo());
		contextController.closeRequest();
		contextController.openRequest(new DummyHttpRequest());
		BRequestScoped r3 = requestScoped.get();
		Assert.assertEquals(null, r3.getFoo());
		
		
		CSessionScoped b2 = sessionScoped.get();
		Assert.assertEquals(b1.getFoo(), b2.getFoo());
		Assert.assertNotNull(b2.getFoo());
		
	}

	@Test
	public void testContextControllerConversationScoped() {
		contextController.openRequest(new DummyHttpRequest());
		conversation.begin();

		DConversationScoped b1 = conversationScoped.get();
		b1.setFoo("Bar");
		DConversationScoped b2 = conversationScoped.get();
		Assert.assertEquals(b1.getFoo(), b2.getFoo());
		conversation.end();
		contextController.closeRequest();
		contextController.openRequest(new DummyHttpRequest());
		conversation.begin();
		DConversationScoped b3 = conversationScoped.get();
		Assert.assertEquals(null, b3.getFoo());
	}
	

	@Test
	public void testProducedViaField() {
		produced = new ProducedViaField(2);
		ProducedViaField produced = getContextualInstance(beanManager, ProducedViaField.class);
		Assert.assertEquals(produced, produced);
	}
	
	@Test
	public void testProducedViaMethod() {
		ProducedViaMethod produced = getContextualInstance(beanManager, ProducedViaMethod.class);
		Assert.assertNotNull(produced);
	}

	public static <T> T getContextualInstance(final BeanManager manager, final Class<T> type, Annotation... qualifiers) {
		T result = null;
		Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type, qualifiers));
		if (bean != null) {
			CreationalContext<T> context = manager.createCreationalContext(bean);
			if (context != null) {
				result = (T) manager.getReference(bean, type, context);
			}
		}
		return result;
	}
}