package main;
/*
 * Author: Abdessalam BAHAFID - M2SI
 * Time: 19:09:00 19 Jun 2021
/*

 * exemple de sudoku
5.3..2..
27......1
....2....
.2.4....7
43..8....2
.5....8..
.........
...59...4
3.5.1.9.6
*/
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.Choco;

public class SodukuSolver {

    public static int n = 9;
    // les zeros dans le variable initialVars signifie les composants à calculer
    public static int[][] initialVars = {
		{5,0,3,0,0,4,2,0,0},
		{2,7,0,0,0,0,0,0,1},
		{0,0,0,0,2,0,0,0,0},
		{0,2,0,4,0,0,0,0,7},
		{4,3,0,0,8,0,0,0,2},
		{0,5,0,0,0,0,8,0,0},
		{0,0,0,0,0,0,0,0,0},
		{0,0,0,5,9,0,0,0,4},
		{3,0,5,0,1,0,9,0,6}
    };
    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        
        Model m = new CPModel();
        IntegerVariable[][] vars = createVariables();
        postConstraints(m, vars);
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        displayResultArrays(s, vars);
        
        System.out.println("\nSolved in: " + (System.currentTimeMillis() - time) + "ms.");
    }

    // 1. Création des variables, et leur domaines
    private static IntegerVariable[][] createVariables() {
		IntegerVariable[][] vars = new IntegerVariable[n][n];
		for (int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(initialVars[i][j] == 0)
					vars[i][j] = Choco.makeIntVar(""+initialVars[i][j], 1, n);
				else
					vars[i][j] = Choco.makeIntVar(""+initialVars[i][j], initialVars[i][j], initialVars[i][j]);
			}
		}
		return vars;
    }

    // 2. Création des contraintes sur les lignes, colonnes, carrés
    private static void postConstraints(Model m, IntegerVariable[][] vars) {
    	int sqrtn = (int) Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            IntegerVariable[] lignes = new IntegerVariable[n];
            IntegerVariable[] colonnes = new IntegerVariable[n];
            IntegerVariable[] carres = new IntegerVariable[n];
            for (int j = 0; j < n; j++) {
                lignes[j] = vars[i][j];
                colonnes[j] = vars[j][i];
                carres[j] = vars[j % sqrtn + (i % sqrtn) * sqrtn][j / sqrtn + (i / sqrtn) * sqrtn];
            }
            postAlldiff(m, lignes);
            postAlldiff(m, colonnes);
            postAlldiff(m, carres);
        }
    }

    // 3. Création d'une contrainte Alldifferent
    private static void postAlldiff(Model m, IntegerVariable[] vars) {
        for (int i = 0; i < vars.length; i++) {
            for (int j = i + 1; j < vars.length; j++) {
                m.addConstraint(Choco.neq(vars[i], vars[j]));
            }
        }
    }

    // 4. Affichages des résultats
    private static void displayResultArrays(Solver s, IntegerVariable[][] vars) {
        int sqrtn = (int) Math.sqrt(n);
        int tmpValue;
        for (int i = 0; i < n; i++) {
        	if (i!=0 && i % sqrtn == 0)
        		System.out.println("---------------");
            for (int j = 0; j < n; j++) {
                if (j!=0 && j % sqrtn == 0)
                	System.out.print(" | ");
                
                tmpValue = s.getVar(vars[i][j]).getVal();
                if(initialVars[i][j] == tmpValue)
                	System.out.print(tmpValue);
                else
                	System.err.print(tmpValue);
            }
            System.out.println();
        }
    }
}