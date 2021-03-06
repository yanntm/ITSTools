package fr.lip6.move.gal.gal2smt;

import fr.lip6.move.gal.Property;

/**
 * Some object interested in being notified of the progression of SMT solving.
 * Register it onto the {@link Gal2SMTFrontEnd}.
 */
public interface ISMTObserver {

	/**
 	 * Notify of result each time one is obtained.
	 * @param prop the property the run was done for
	 * @param res the result obtained, SAT/UNSAT/UNKNOWN
	 * @param desc a description of how we determined this result
	 */
	void notifyResult(Property prop, Result res, String desc);

}
