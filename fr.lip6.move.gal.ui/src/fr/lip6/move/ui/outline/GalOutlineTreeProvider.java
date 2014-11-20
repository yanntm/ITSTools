/*
 * generated by Xtext
 */
package fr.lip6.move.ui.outline;

import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider;

import fr.lip6.move.gal.Constant;
import fr.lip6.move.gal.UnaryMinus;
import fr.lip6.move.gal.Variable;
import fr.lip6.move.gal.impl.AndImpl;
import fr.lip6.move.gal.impl.ArrayVarAccessImpl;
import fr.lip6.move.gal.impl.AssignmentImpl;
import fr.lip6.move.gal.impl.BinaryIntExpressionImpl;
import fr.lip6.move.gal.impl.BitComplementImpl;
import fr.lip6.move.gal.impl.BooleanExpressionImpl;
import fr.lip6.move.gal.impl.ComparisonImpl;
import fr.lip6.move.gal.impl.ConstantImpl;
import fr.lip6.move.gal.impl.FalseImpl;
import fr.lip6.move.gal.impl.GALTypeDeclarationImpl;
import fr.lip6.move.gal.impl.NotImpl;
import fr.lip6.move.gal.impl.OrImpl;
import fr.lip6.move.gal.impl.TransientImpl;
import fr.lip6.move.gal.impl.TrueImpl;
import fr.lip6.move.gal.impl.UnaryMinusImpl;
import fr.lip6.move.gal.impl.VariableRefImpl;
import fr.lip6.move.gal.impl.WrapBoolExprImpl;

/** Customization of the default outline structure.
 * 
 * Added description for other AST elements in Gal structure.
 *
 */
public class GalOutlineTreeProvider extends DefaultOutlineTreeProvider {
	public Object _text(BinaryIntExpressionImpl e)
	{
		/* Print arithmetic operation */
		String op = e.getOp() ;
		if("+".equals(op)) return "PLUS"  ;  
		if("-".equals(op)) return "MINUS" ;
		if("*".equals(op)) return "MULTIPLICATION" ;
		if("/".equals(op)) return "DIV" ;
		
		if("|".equals(op)) return "BIT OR" ;
		if("^".equals(op)) return "BIT XOR" ;
		if("&".equals(op)) return "BIT AND" ;
		
		if("<<".equals(op)) return "LEFT SHIFT" ;
		if(">>".equals(op)) return "RIGHT SHIFT" ;
		
		if("**".equals(op)) return "POWER" ;
		
		
		// Never reached
		return "Binary Operator" ; 
		
	}
	
	public Object _text(Variable v) 
	{
		return "int " + v.getName() + " =" + v.getValue();
	}
	
	public Object _text(VariableRefImpl e)
	{
		return  e.getReferencedVar().getName() + " : Variable";
	}

		
	public Object _text(UnaryMinusImpl e)
	{
		if(e.getValue() instanceof Constant)
		{
			return "-" + ((Constant) e.getValue()).getValue() + " : Integer" ; 
		}
		return "Unary Minus" ; 
			
	}
	
	public Object _text(ConstantImpl e)
	{
		return e.getValue() + " : Integer" ;
	}

	
	
	public Object _text(BitComplementImpl e)
	{
		return "Bit Complement" ; 
	}
	
	public Object _text(WrapBoolExprImpl e)
	{
		return "Integer Boolean Expression (0 or 1)" ; 
	}
	
	/*
	 * Bit à bit
	 */
	public Object _text(BooleanExpressionImpl e)
	{
		return "Boolean Expression" ; 
	}
	

	
	/* -----------------------------------------------*
	 * ====== Actions on list : PUSH, POP, PEEK  ======
	 *------------------------------------------------*/
//	public Object _text(PushImpl p)
//	{
//		try {
//			return "PUSH on list '" + p.getList().getName() + "'";
//		}
//		catch(Exception e)
//		{
//			return "PUSH" ;
//		}
//	}
//	
//	// Important de mettre try car ici, on écris désormais le mot clé 
//	// Avant de mettre le nom de la liste.
//	public Object _text(PopImpl p)
//	{
//		try 
//		{
//			return "POP on list '" + p.getList().getName() + "'";
//		}
//		catch(Exception e)
//		{
//			return "POP" ;
//		}
//	}
//	
//	public Object _text(PeekImpl e)
//	{
//		try 
//		{
//			return "PEEK on list '"  + e.getList().getName() + "'"; 
//		}
//		catch(Exception ee) 
//		{
//			return "PEEK";
//		}
//		
//	}
	
	/*
	 * System
	 */
	public Object _text(GALTypeDeclarationImpl system)
	{
		try 
		{
			return system.getName() + " : GAL System" ;
		}
		catch(Exception e) 
		{ 
			return "GAL System" ; 
		}
	}
	
	/*
	 * Array
	 */
	public Object _text(ArrayVarAccessImpl e)
	{
		return e.getPrefix().getName() + " - Array" ; 
	}
	
	/*
	 * Assignment
	 */
	public Object _text(AssignmentImpl e)
	{
		return "Assignment";
	}
	
	/* -----------------------------*
	 * Boolean operations
	 * -----------------------------*/
	public Object _text(AndImpl e)
	{
		return "AND" ; 
	}
	
	public Object _text(OrImpl e)
	{
		return "OR" ; 
	}
	
	public Object _text(TrueImpl e)
	{
		return "True";
	}
	
	public Object _text(FalseImpl e)
	{
		return "False" ;
	}
	
	public Object _text(ComparisonImpl e)
	{
		return "Comparison : " + e.getOperator().getName(); 
	}
	
	
	public Object _text(NotImpl e)
	{
		return "Not" ; 
	}
	
	
	public Object _text(TransientImpl e)
	{
		return "Transient" ;
	}
	
	public boolean _isLeaf(UnaryMinus e)
	{
		if(e.getValue() instanceof Constant)
			return true ;
		
		return false;
	}

}