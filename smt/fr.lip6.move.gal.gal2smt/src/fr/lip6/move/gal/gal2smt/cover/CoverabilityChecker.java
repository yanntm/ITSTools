package fr.lip6.move.gal.gal2smt.cover;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.smtlib.IExpr;
import org.smtlib.IResponse;
import org.smtlib.SMT.Configuration;
import org.smtlib.impl.Script;

import fr.lip6.move.gal.GALTypeDeclaration;
import fr.lip6.move.gal.InvariantProp;
import fr.lip6.move.gal.LogicProp;
import fr.lip6.move.gal.NeverProp;
import fr.lip6.move.gal.Property;
import fr.lip6.move.gal.ReachableProp;
import fr.lip6.move.gal.Specification;
import fr.lip6.move.gal.Transition;
import fr.lip6.move.gal.TypeDeclaration;
import fr.lip6.move.gal.gal2smt.smt.SMTSolver;
import fr.lip6.move.gal.gal2smt.Result;
import fr.lip6.move.gal.gal2smt.Solver;

public class CoverabilityChecker extends SMTSolver {
	public CoverabilityChecker(Solver engine, Configuration smtConfig) {
		super(engine, smtConfig,new CoverabilityVariableHandler(smtConfig));
		setShowSatState(true);
	}

	public boolean isInit=false;
	
	public void init (Specification spec) {
		super.init(spec);
		Script script = new Script();
		TypeDeclaration td = spec.getMain();
		if (td instanceof GALTypeDeclaration) {
			GALTypeDeclaration gal = (GALTypeDeclaration) td;
			FlowMatrix fm = new FlowMatrix(conf,vh);			
			isInit = fm.init(gal);
			
			if (! isInit) {
				exit();
				return;
			}
			int step = 0;

			Map<Transition, IExpr> sigmamap = fm.addFlowConstraintsAtStep(step,script,gal);

//			// algorithm 3.1, p 122 of Proc of APN'90, Silva & Colom
//			// [1+2] M = M0 + C. \hat sigma
//			
//			// define e
//			Map<Transition, IExpr> trmap = fm.declareTransitionVectorAtStep(1, script, gal);
//
//			// [3] 1^T . e = 1
//			// i.e. one single transition in e
//			List<IExpr> sum = new ArrayList<IExpr>();
//			for (Entry<Transition, IExpr> trent : trmap.entrySet()) {
//				sum.add(trent.getValue()); 
//			}
//			script.commands().add(new C_assert(efactory.fcn(efactory.symbol("="), 
//					efactory.fcn(efactory.symbol("+"), sum), 
//					efactory.numeral(1))));
//
//			// [5] \hat sigma - e >= 0
//			for (Entry<Transition, IExpr> trent : trmap.entrySet()) {
//				script.commands().add(
//						new C_assert(
//								efactory.fcn(efactory.symbol("<="), 
//										trent.getValue(),  // e[t]
//										sigmamap.get(trent.getKey()) // sigma[t]
//								))
//						); 
//			}
//			
//			// [2+4] M-POST.e >= 0
//			
			
			
			// check sat
			IResponse res = script.execute(solver);
			if (res.isError()) {
				throw new RuntimeException("Could not initialize marking equation.");
			}
			
			boolean disabled =false;
			if (disabled ) {
				testAndReduceQuasiLiveness(gal, sigmamap);
			}
		}
	}

	public void testAndReduceQuasiLiveness(GALTypeDeclaration gal,
			Map<Transition, IExpr> sigmamap) {
		setShowSatState(false);
		long timestamp = System.currentTimeMillis();
		// check quasi liveness
		int nbred=0;
		int totaltest = 0;
		Set<Transition> done = new HashSet<Transition>();
		do {
			nbred =0;
			for (Transition tr : gal.getTransitions()) {
				if (done.contains(tr) || ( tr.getLabel() != null && ! "".equals(tr.getLabel().getName()))) {
					continue;
				}
				totaltest++;
				IExpr zeroTrOcc= efactory.fcn(efactory.symbol("="), sigmamap.get(tr), efactory.numeral(0));
				Result restr = super.verifyAssertion(
						efactory.fcn(efactory.symbol("and"),
								et.translateBool(tr.getGuard(), null),
								zeroTrOcc
								));
				if (restr == Result.UNSAT) {
					nbred++;
					done.add(tr);
					solver.assertExpr(zeroTrOcc);
					Logger.getLogger("fr.lip6.move.gal").info("Quasi live test discarded unfeasible transition "+tr.getName());
				}
			} 
		} while (nbred>0);
		if (done.size() >0) {
			Logger.getLogger("fr.lip6.move.gal").info("Quasi live test discarded a total of "+ done.size() +" transitions.");
			gal.getTransitions().removeAll(done);
		}
		Logger.getLogger("fr.lip6.move.gal").info("Quasi live test ("+totaltest+" SAT runs) took "+(System.currentTimeMillis()- timestamp)+" ms.");
		setShowSatState(true);
	}

	
	@Override
	public Result verify(Property prop) {
		if (!isInit)
			return Result.UNKNOWN;
		
		Logger.getLogger("fr.lip6.move.gal").info("Verifying coverability of "+prop.getName());
		LogicProp body = prop.getBody();
		IExpr totest =null ;
		if (body  instanceof ReachableProp || body instanceof NeverProp){
			// SAT = trace to state satisfying P for reach (verdict TRUE)
			// SAT = trace to c-e satisfying P for never (verdict FALSE)
			totest= et.translateBool(body.getPredicate(), null);
		} else if (body instanceof InvariantProp) {
			// SAT = trace to c-e satisfying !P for invariant (verdict FALSE)
			totest= efactory.fcn(
					efactory.symbol("not"),
					et.translateBool(body.getPredicate(), null));			
		} 
		return super.verifyAssertion(totest);
	}
	
	@Override
	public List<IExpr> listVariablesToShow() {
		return Collections.<IExpr>singletonList(efactory.symbol(FlowMatrix.SUMT+"0"));
	}
}