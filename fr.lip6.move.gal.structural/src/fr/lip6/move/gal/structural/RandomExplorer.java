package fr.lip6.move.gal.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import android.util.SparseIntArray;
import fr.lip6.move.gal.util.MatrixCol;

public class RandomExplorer {

	private StructuralReduction sr;
	private MatrixCol combFlow;
	private boolean [][] conflictSet;
	private int [][] mayEnableSet;

	public RandomExplorer(StructuralReduction sr) {
		this.sr = sr;
		
		combFlow = new MatrixCol(sr.getPnames().size(),0);
		for (int i = 0 ;  i < sr.getFlowPT().getColumnCount() ; i ++) {
			combFlow.appendColumn(SparseIntArray.sumProd(-1, sr.getFlowPT().getColumn(i), 1, sr.getFlowTP().getColumn(i)));
		}
	
		List<Set<Integer>> lconflictSet = new ArrayList<>();
		List<Set<Integer>> lmayEnableSet = new ArrayList<>();
		for (int  t=0 ; t < sr.getTnames().size() ; t++) {
			lconflictSet.add(new TreeSet<>());
			lmayEnableSet.add(new TreeSet<>());
		}
		
		MatrixCol tComb = combFlow.transpose();
		MatrixCol tFlowPT = sr.getFlowPT().transpose();
		for (int  p = 0 ; p < tComb.getColumnCount() ; p++) {
			SparseIntArray col = tComb.getColumn(p);
			SparseIntArray colPT = tFlowPT.getColumn(p);
			// every transition with <0 effect in this set is in conflict with every other one
			for (int i = 0 ; i < col.size() ; i++) {
				int ki = col.keyAt(i);
				int vi = col.valueAt(i);
				
				if (vi < 0) {
					for (int j = 0 ; j < colPT.size() ; j++) {
						int kj = colPT.keyAt(j);
						lconflictSet.get(ki).add(kj);						
					}
				}
			}
		}
		// stored as an array of 0/1 entries
		conflictSet = new boolean[lconflictSet.size()][lconflictSet.size()];
		for (int i = 0; i < lconflictSet.size() ; i++) {
			for ( Integer tind : lconflictSet.get(i)) {
				conflictSet[i][tind] = true;
			}
		}
				
		MatrixCol tFlowTP = sr.getFlowTP().transpose();
		for (int  p = 0 ; p < tFlowPT.getColumnCount() ; p++) {
			// the set of transitions taking from this place
			SparseIntArray col = tFlowPT.getColumn(p);
			// the set of transitions feeding this place
			SparseIntArray feed = tFlowTP.getColumn(p);
			for (int i = 0 ; i < col.size() ; i++) {
				for (int j = 0 ; j < feed.size() ; j++) {
					int ki = col.keyAt(i);
					int kj = feed.keyAt(j);
					lmayEnableSet.get(kj).add(ki);
				}	
			}
		}
		mayEnableSet = new int[lmayEnableSet.size()][];
		for (int i = 0; i < lmayEnableSet.size() ; i++) {
			mayEnableSet[i] = new int [lmayEnableSet.get(i).size()];
			int j =0;
			for (Integer tind : lmayEnableSet.get(i)) {
				mayEnableSet[i][j++] = tind;
			}
		}
		
	}

	private int [] computeEnabled(SparseIntArray state) {		
		int [] list  = new int [sr.getTnames().size()+1];
		int li = 1;
		for (int t = 0, e =  sr.getTnames().size(); t < e; t++) {
			if (greaterOrEqual(state, sr.getFlowPT().getColumn(t))) {
				list[li++] = t;
			}
		}
		list[0] = li -1 ;
		return list;
	}
	
	private void dropAt (int [] enabled, int index) {
		if (index < enabled[0]) {
			enabled[index] = enabled[enabled[0]];
		}
		enabled [0] --;
	}
	private void add (int [] enabled, int value) {
		enabled[enabled[0]+1] = value;
		enabled [0] ++;
	}

	
	// we just reached "state" by firing tfired
	public void updateEnabled (SparseIntArray state, int [] enabled, int tfired) {
		if (combFlow.getColumn(tfired).size() == 0) {
			return ;
		}
		
		boolean [] seen = new boolean [sr.getTnames().size()];
		for (int i = enabled[0] ; i  >= 1  ; i--) {
			int t = enabled [i];
			if (seen[t]) {
				dropAt(enabled,i);
				continue;
			}
			if (! conflictSet[tfired][t]) {
				// keep it
				seen[t] = true;
				continue;
			} else {
				if (greaterOrEqual(state, sr.getFlowPT().getColumn(t))) {					
					seen[t] = true;
					continue;
				} else {
					dropAt(enabled,i);
				}
			}
		}
		
		for (int t : mayEnableSet[tfired]) {
			if (seen[t])
				continue;
		
			if (combFlow.getColumn(t).size()==0) {
				continue;
			}
			
			if (greaterOrEqual(state, sr.getFlowPT().getColumn(t))) {
				add(enabled, t);				
				seen[t] = true;
			}
		}						
	}
	
	public int[] run (long nbSteps, List<Expression> exprs) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		
		SparseIntArray state = new SparseIntArray(sr.getMarks());
		int [] list = computeEnabled(state);
		dropEmpty(list);		
		
		int last = -1;
		long nbresets = 0;
		
		int [] verdicts = new int [exprs.size()];
		
		
		for (int  i=0; i < nbSteps ; i++) {
			if (! updateVerdicts(exprs, state, verdicts)) {
				return verdicts;
			}
			
			int r = rand.nextInt(list[0])+1;
			int tfired = list[r];			
			
			if (last != -1 && rand.nextDouble() < 0.98 && greaterOrEqual(state, sr.getFlowPT().getColumn(last))) {
				tfired = last;				
				// iterate firing
				do {
					state = fire ( tfired, state);
					i++;
				} while (greaterOrEqual(state, sr.getFlowPT().getColumn(tfired)));
				updateEnabled(state, list, tfired);
				last = -1;
			} else {
				SparseIntArray newstate = fire ( tfired, state);				
				// NB : discards empty events
				updateEnabled(newstate, list, tfired);

				last = tfired;				
				state = newstate;
			}
			if (list[0] == 0){
				//System.out.println("Dead end with self loop(s) found at step " + i);
				nbresets ++;
				last = -1;
				state = new SparseIntArray(sr.getMarks());
				list = computeEnabled(state);
				dropEmpty(list);
				continue;
			}
			
		}
		System.out.println("After "+nbSteps + (nbresets > 0 ? " including "+ nbresets + " reset to initial state" : "") + " reached state " + state);
		System.out.println("Properties met during traversal : "+ Arrays.toString(verdicts));
		
		return verdicts;
	}

	private boolean updateVerdicts(List<Expression> exprs, SparseIntArray state, int[] verdicts) {
		boolean remains = false;
		for (int v = 0; v < verdicts.length; v++) {
			if (verdicts[v] == 0) {
				remains = true;
				if (exprs.get(v).eval(state) == 1) {
					verdicts[v] = 1;
				}
			}
		}
		return remains;
	}

	
	public void run (long nbSteps) throws DeadlockFound {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		
		SparseIntArray state = new SparseIntArray(sr.getMarks());
		int [] list = computeEnabled(state);
		dropEmpty(list);		
		
		int last = -1;
		
		long nbresets = 0;
		
		for (int  i=0; i < nbSteps ; i++) {			
			if (list[0] == 0) {
				// includes empty effects 
				list = computeEnabled(state);
				if (list[0] == 0) {
					System.out.println("Deadlock found at step " + i);
					throw new DeadlockFound();
				} else {
					//System.out.println("Dead end with self loop(s) found at step " + i);
					nbresets ++;
					last = -1;
					state = new SparseIntArray(sr.getMarks());
					list = computeEnabled(state);
					dropEmpty(list);
					continue;
				}
			}						
			int r = rand.nextInt(list[0])+1;
			int tfired = list[r];			
			
			if (last != -1 && rand.nextDouble() < 0.98 && greaterOrEqual(state, sr.getFlowPT().getColumn(last))) {
				tfired = last;				
				// iterate firing
				do {
					state = fire ( tfired, state);
					i++;
				} while (greaterOrEqual(state, sr.getFlowPT().getColumn(tfired)));
				updateEnabled(state, list, tfired);
				last = -1;
				continue;
			}
			
			SparseIntArray newstate = fire ( tfired, state);
			
			// NB : discards empty events
			updateEnabled(newstate, list, tfired);
			

			/*{
				Set<Integer> s1 = new TreeSet<Integer>(newlist);
				Set<Integer> s2 = new TreeSet<Integer>(computeEnabled(newstate));
				
				
				if (newlist.size() > s1.size()) {
					System.err.println("Repeat transitions in list  "  + newlist);
				}
				
				s2.removeIf( n -> combFlow.getColumn(n).size()==0);
				
				if (! s1.equals(s2)) {
					System.err.println("Mismatch " + s1 + " vs. " + s2);
					System.err.println("Enabled as list " + newlist);
				}
			}*/
						
			last = tfired;
			state = newstate;
		}
		System.out.println("After "+nbSteps + (nbresets > 0 ? " including "+ nbresets + " reset to initial state" : "") + " reached state " + state);
	}
	
	/** update a list of enabling to remove empty effect transitions*/ 
	private void dropEmpty(int [] enabled) {
		for (int i = enabled[0] ; i  >= 1  ; i--) {
			int t = enabled [i];
			if (combFlow.getColumn(t).size() == 0) {
				dropAt(enabled,i);				
			}
		}
	}

	public SparseIntArray fire (int t, SparseIntArray state) {
		// NB no enabling check
		return SparseIntArray.sumProd(1, state, 1, combFlow.getColumn(t));
	}

	private boolean greaterOrEqual(SparseIntArray s1, SparseIntArray s2) {
		if (s1.size() < s2.size()) {
			return false;
		}
		for (int j = 0 ; j < s2.size() ; j++) {
			int sk2 = s2.keyAt(j); 
			int sv1 = s1.get(sk2);
			if (sv1 < s2.valueAt(j)) {
				return false;
			}
		}
		return true;
	}
	
}