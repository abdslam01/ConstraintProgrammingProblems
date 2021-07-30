package main;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

public class Main_nReine {
	public static void main(String[] args) {
		//Taille du problème
		int nbvar = 4;
		//Declaration du modèle
		Model m = new CPModel();
		//Déclaration des variables et des domaines
		IntegerVariable[] var = new IntegerVariable[nbvar];
		for (int i = 0; i < nbvar; i++) {
		  var [i] = Choco.makeIntVar("R" + (i + 1), 1, nbvar);
		  m.addVariable(var [i]);
		}
		//Déclaration des contraintes
		//C1 : contrainte alla diff
		for (int i = 0; i < nbvar; i++) {
		  for (int j = i + 1; j < nbvar; j++) {
		    m.addConstraint(Choco.neq(var [i],
		      var [j]));
		  }
		}
		//C2 : contrainte DA
		for (int i = 0; i < nbvar; i++) {
		  for (int j = i + 1; j < nbvar; j++) {
		    int k = j - i;
		    m.addConstraint(Choco.neq(Choco.plus(var [i], k),
		      var [j]));
		  }
		}
		//C3 : contrainte DD
		for (int i = 0; i < nbvar; i++) {
		  for (int j = i + 1; j < nbvar; j++) {
		    int k = i - j;
		    m.addConstraint(Choco.neq(Choco.plus(var [i], k),
		      var [j]));
		  }
		}
		//Declaration du solveur
		Solver s = new CPSolver();
		//lecture du modele par le solveur
		s.read(m);
		//recherche de la premier solution
		s.solve();
		//Affichage des resultats
		int t = 1;
		do {
		  System.out.println("Solution : " + t);
		  for (int i = 0; i < nbvar; i++) {
		    for (int j = 0; j < nbvar; j++) {
		      if (s.getVar(var [i]).getVal() != j + 1)
		        System.out.print("__|");
		      else
		        System.out.print("R" + (i + 1) + "|");
		    }
		    System.out.println();
		  }
		  System.out.println();
		  t++;
		} while (s.nextSolution());
	}
}