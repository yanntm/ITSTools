package fr.lip6.move.gal.interpreter;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;

import fr.lip6.move.gal.InvariantProp;
import fr.lip6.move.gal.NeverProp;
import fr.lip6.move.gal.Property;
import fr.lip6.move.gal.ReachableProp;
import fr.lip6.move.gal.application.LTSminRunner;
import fr.lip6.move.gal.itscl.interprete.ItsInterpreter;

public class LTSminInterpreter implements Runnable {

	private IStatus status;
	private boolean isLTL;
	private boolean isdeadlock;
	private ItsInterpreter bufferWIO;
	private Property prop;
	private Set<String> doneProps;
	private LTSminRunner ltsRunner;

	public LTSminInterpreter(LTSminRunner ltSminRunner, ItsInterpreter bufferWIO, Set<String> doneProps) {
		this.ltsRunner = ltSminRunner;
		this.doneProps = doneProps;
		this.bufferWIO = bufferWIO;
	}

	public void configure(boolean isdeadlock, boolean isLTL, IStatus status, Property prop) {
		this.isdeadlock = isdeadlock;
		this.isLTL = isLTL;
		this.status = status;
		this.prop = prop;
	}

	public void run() {

		while (true) {
			boolean result = false;
			try {
				System.out.println("im i interpreter k ? ");
				for (String line = ""; line != null; line = bufferWIO.getIn().readLine()) {

					if (line.length() != 0) {
						System.out.println(" buffer did read : " + line);
						if (isdeadlock) {
							result = line.contains("Deadlock found") || line.contains("deadlock () found");
						} else if (isLTL) {
							// accepting cycle = counter example to
							// formula
							result = !(status.getCode() == 1); // output.toLowerCase().contains("accepting
																// cycle found")
																// ;
						} else {
							boolean hasViol = line.contains("Invariant violation");

							if (hasViol) {
								System.out.println("Found Violation");
								if (prop.getBody() instanceof ReachableProp) {
									result = true;
								} else if (prop.getBody() instanceof NeverProp) {
									result = false;
								} else if (prop.getBody() instanceof InvariantProp) {
									result = false;
								} else {
									throw new RuntimeException("Unexpected property type " + prop);
								}
							} else {
								System.out.println("Invariant validated");
								if (prop.getBody() instanceof ReachableProp) {
									result = false;

								} else if (prop.getBody() instanceof NeverProp) {
									result = true;

								} else if (prop.getBody() instanceof InvariantProp) {
									result = true;

								} else {
									throw new RuntimeException("Unexpected property type " + prop);
								}
							}
						}
						String ress = (result + "").toUpperCase();
						System.out.println("FORMULA " + prop.getName() + " " + ress
								+ " TECHNIQUES PARTIAL_ORDER EXPLICIT LTSMIN SAT_SMT");
						doneProps.add(prop.getName());
						ltsRunner.setDoneProps(doneProps);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
