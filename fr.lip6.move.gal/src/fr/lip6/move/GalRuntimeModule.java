/*
 * generated by Xtext
 */
package fr.lip6.move;

//import org.eclipse.xtext.debug.IStratumBreakpointSupport;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.scoping.IScopeProvider;

//import fr.lip6.move.debug.GalStratumBreakpointSupport;
import fr.lip6.move.scoping.GalNameConverter;
import fr.lip6.move.scoping.GalScopeProvider;

/**
 * Use this class to register components to be used at runtime / without the
 * Equinox extension registry.
 */
public class GalRuntimeModule extends fr.lip6.move.AbstractGalRuntimeModule {

//	@Override
//	public Class<? extends IStratumBreakpointSupport> bindIStratumBreakpointSupport() {
//		return GalStratumBreakpointSupport.class;
//	}
	
	
	@Override
	public Class<? extends IScopeProvider> bindIScopeProvider() {
		return GalScopeProvider.class;
	}
	
	@Override
	public Class<? extends IQualifiedNameConverter> bindIQualifiedNameConverter() {
		return GalNameConverter.class;
	}
}
