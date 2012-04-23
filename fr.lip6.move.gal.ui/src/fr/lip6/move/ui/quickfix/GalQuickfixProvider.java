
package fr.lip6.move.ui.quickfix;

import java.util.Random;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

import fr.lip6.move.gal.ArrayPrefix;
import fr.lip6.move.gal.List;
import fr.lip6.move.gal.Transition;
import fr.lip6.move.gal.Variable;
import fr.lip6.move.validation.GalJavaValidator;

/**
 * Quick Fixer
 * @author steph
 *
 */
public class GalQuickfixProvider extends DefaultQuickfixProvider {

	
	/**
	 * Cette méthode renvoie une chaine aléatoire de longueur length
	 * et met en majuscule le premier caractère
	 * @param length
	 * @return
	 */
	public String generateRandomString(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		char chars[] = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		Random r = new Random();
		
		for(int i=0; i<length; i++)
		{
			sb.append(chars[r.nextInt(chars.length)]);
		}
		// Met en majuscule le premier caractère
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}
	
	/** Génère un nouveau nom à partir de l'ancien */
	public String generateNewNameFromOld(String oldName) {
		
		return oldName + "_" + generateRandomString(3);
	}
	
	
	
	
	
	/**
	 * Corrige le nom existant en lui rajoutant 3
	 * lettres aléatoires
	 * @param issue
	 * @param acceptor
	 */
	@Fix(GalJavaValidator.GAL_ERROR_NAME_EXISTS)
	public void suggestOtherVariableName(final Issue issue, IssueResolutionAcceptor acceptor)
	{
		acceptor.accept(issue, "Change name", "Choose other name", null, new ISemanticModification() {
			
			@Override
			public void apply(EObject element, IModificationContext context)
					throws Exception 
			{
				if(element instanceof Variable) 
				{
					Variable var = (Variable) element ; 
					var.setName(generateNewNameFromOld(var.getName()));
				}
				else if(element instanceof ArrayPrefix)
				{
					ArrayPrefix array = (ArrayPrefix) element ; 
					array.setName(generateNewNameFromOld(array.getName())) ; 
				}
				else if(element instanceof List)
				{
					List l = (List) element ; 
					l.setName(generateNewNameFromOld(l.getName()));
				}
				else if(element instanceof Transition)
				{
					Transition t = (Transition) element ; 
					t.setName(generateNewNameFromOld(t.getName()));
				}
				else 
				{
					System.err.println("Not yet implemented");
					System.out.println(element.getClass().getName());
				}
			}

			
		});
	}
	
	
	
	/**
	 * Complète les elements manquant d'un tableau
	 * @param issue
	 * @param acceptor
	 */
	@Fix(GalJavaValidator.GAL_ERROR_MISSING_ELEMENTS)
	public void completeMissingElements(final Issue issue, IssueResolutionAcceptor acceptor)
	{
		acceptor.accept(issue, "Complete missing elements", "Complete missing elements with zeros", null, new ISemanticModification() {
			
			@Override
			public void apply(EObject element, IModificationContext context)
					throws Exception 
			{
				if(element instanceof ArrayPrefix)
				{
					ArrayPrefix array = (ArrayPrefix) element ; 
					int nbElementsToAdd = GalJavaValidator.arrayMissingValues.get(array.getName()) ; 
					for(int i=0; i<nbElementsToAdd; i++)
					{
						array.getValues().getValues().add(0);
					}
				}
				else 
				{
					System.err.println("Not yet implemented");
					System.out.println(element.getClass().getName());
				}
			}

			
		});
	}
	
}