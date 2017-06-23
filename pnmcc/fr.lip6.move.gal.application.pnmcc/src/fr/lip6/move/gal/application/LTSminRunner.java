package fr.lip6.move.gal.application;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import Interpreter.LTSminInterpreter;
import fr.lip6.move.gal.Comparison;
import fr.lip6.move.gal.InvariantProp;
import fr.lip6.move.gal.LTLNext;
import fr.lip6.move.gal.LTLProp;
import fr.lip6.move.gal.NeverProp;
import fr.lip6.move.gal.Property;
import fr.lip6.move.gal.ReachableProp;
import fr.lip6.move.gal.gal2pins.Gal2PinsTransformerNext;
import fr.lip6.move.gal.gal2smt.Gal2SMTFrontEnd;
import fr.lip6.move.gal.gal2smt.Solver;
import fr.lip6.move.gal.itstools.CommandLine;
import fr.lip6.move.gal.itstools.ProcessController.TimeOutException;
import fr.lip6.move.gal.itstools.Runner;

public class LTSminRunner extends AbstractRunner {

	private String ltsminpath;
	private String solverPath;
	private boolean doPOR;
	private boolean onlyGal;
	private String workFolder;
	private Solver solver;
	private int timeout;
	private List<String> todo;

	public LTSminRunner(String ltsminpath, String solverPath, Solver solver, boolean doPOR, boolean onlyGal,
			String workFolder, int timeout) {
		this.ltsminpath = ltsminpath;
		this.solverPath = solverPath;
		this.solver = solver;
		this.doPOR = doPOR;
		this.onlyGal = onlyGal;
		this.workFolder = workFolder;
		this.timeout = timeout;
	}

	private static boolean isStutterInvariant(Property prop) {
		for (TreeIterator<EObject> it = prop.eAllContents(); it.hasNext();) {
			EObject obj = it.next();
			if (obj instanceof LTLNext) {
				return false;
			} else if (obj instanceof Comparison) {
				it.prune();
			}
		}
		return true;
	}
	
	public void setInterpreter() {
		
	}

	public Boolean taskDone() {
		todo.removeAll(doneProps);
		if(todo.isEmpty())
			System.out.println("Ltsmin has all solved");
		else
			System.out.println("Ltsmin didnt solve everything");
		return (todo.isEmpty()) ? true : false;
	}

	public void solve() {
		try {
			System.out.println("Built C files in : \n" + new File(workFolder + "/"));
			final Gal2PinsTransformerNext g2p = new Gal2PinsTransformerNext();
			final Gal2SMTFrontEnd gsf = new Gal2SMTFrontEnd(solverPath, solver, 300000);
			
			g2p.setSmtConfig(gsf);
			g2p.initSolver();
			g2p.transform(spec, workFolder, doPOR);

			if (ltsminpath != null) {
				{
					// compile
					CommandLine clgcc = new CommandLine();
					clgcc.setWorkingDir(new File(workFolder));
					clgcc.addArg("gcc");
					clgcc.addArg("-c");
					clgcc.addArg("-I" + ltsminpath + "/include");
					clgcc.addArg("-I.");
					clgcc.addArg("-std=c99");
					clgcc.addArg("-fPIC");
					clgcc.addArg("-O3");
					clgcc.addArg("model.c");
					try {
						System.out.println("Running compilation step : " + clgcc);
						IStatus status = Runner.runTool(100, clgcc);
						if (!status.isOK()) {
							throw new RuntimeException("Could not compile executable ." + clgcc);
						}
					} catch (TimeOutException to) {
						throw new RuntimeException("Compilation of executable timed out or was killed." + clgcc);
					}
				}
				{
					// link
					CommandLine clgcc = new CommandLine();
					clgcc.setWorkingDir(new File(workFolder));
					clgcc.addArg("gcc");
					clgcc.addArg("-shared");
					clgcc.addArg("-o");
					clgcc.addArg("gal.so");
					clgcc.addArg("model.o");
					try {
						System.out.println("Running link step : " + clgcc);
						IStatus status = Runner.runTool(100, clgcc);
						if (!status.isOK()) {
							throw new RuntimeException("Could not link executable ." + clgcc);
						}
					} catch (TimeOutException to) {
						throw new RuntimeException("Link of executable timed out or was killed." + clgcc);
					}
				}
				if (onlyGal) {
					System.out.println("Successfully built gal.so in :" + workFolder);
					System.out.println("It has labels for :" + (spec.getProperties().stream()
							.map(p -> p.getName().replaceAll("-", "")).collect(Collectors.toList())));
					return;
				}
				todo = spec.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList());
				
				for (Property prop : spec.getProperties()) {
					if (doneProps.contains(prop.getName())) {
						continue;
					}
					CommandLine ltsmin = new CommandLine();
					ltsmin.setWorkingDir(new File(workFolder));
					ltsmin.addArg(ltsminpath + "/bin/pins2lts-mc");
					ltsmin.addArg("./gal.so");

					ltsmin.addArg("--threads=1");
					if (doPOR && isStutterInvariant(prop)) {
						ltsmin.addArg("-p");
						ltsmin.addArg("--pins-guards");
					}
					ltsmin.addArg("--when");
					boolean isdeadlock = false;
					boolean isLTL = false;
					if (prop.getName().contains("Deadlock")) {
						ltsmin.addArg("-d");
						isdeadlock = true;
					} else if (prop.getBody() instanceof LTLProp) {
						ltsmin.addArg("--ltl");
						ltsmin.addArg(g2p.printLTLProperty((LTLProp) prop.getBody()));
						// ltsmin.addArg("--ltl-semantics");
						// ltsmin.addArg("spin");

						isLTL = true;
					} else {
						ltsmin.addArg("-i");
						ltsmin.addArg(prop.getName().replaceAll("-", "") + "==true");
					}
					try {
						IStatus status = Runner.runTool(timeout, ltsmin, bufferWIO.getPout(), true);
						if (!status.isOK() && status.getCode() != 1) {
							throw new RuntimeException(
									"Unexpected exception when executing ltsmin :" + ltsmin + "\n" + status);
						}
						
						LTSminInterpreter interp= new LTSminInterpreter(isdeadlock,isLTL,status,);
						
					} catch (TimeOutException to) {
						System.err.println("LTSmin timed out on command " + ltsmin);
						continue;
					}
				}
			}
			System.out.println("LTMIN HAS FINISHED !");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.err.println("LTS min runner thread failed on error :" + e);
			e.printStackTrace();
		}

	}


}
