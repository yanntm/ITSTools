/*
 * generated by Xtext
 */
package fr.lip6.move.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class GalExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return fr.lip6.move.ui.internal.GalActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return fr.lip6.move.ui.internal.GalActivator.getInstance().getInjector("fr.lip6.move.Gal");
	}
	
}
