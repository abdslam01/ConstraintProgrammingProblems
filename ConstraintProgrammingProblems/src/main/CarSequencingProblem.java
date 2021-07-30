package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

public class CarSequencingProblem {
   int noCars;	// nombre de voitures
   int noOpt;	// nombre d options
   int noClass;	// nombre de classes
   int[] p;		// nombre de voitures avec option i dans un bloc
   int[] q;		// taille d'un bloc pour l'option i
   int[] demand; //nombre de voitures de classe i
   int[][] option;// option[i,j]=1 <-> classe i requires option j
   Model model;
   Solver solver;
   IntegerVariable[] position; //the class of car that is made in posiotion[i] of a sequence
   IntegerVariable[][] optPos; // optPos[i,j]=1 <-> if option i is made in position j 
   
   public static void main(String[] args) throws IOException, InterruptedException {
	   new CarSequencingProblem("src\\files\\carseq_100_8_20_19");
   }
   
   public CarSequencingProblem(String fname) throws IOException, InterruptedException {
	   Scanner sc = new Scanner(new File(fname));
	   noCars = sc.nextInt();
	   noOpt = sc.nextInt(); 
	   noClass = sc.nextInt();
	   p = new int[noOpt];
	   q = new int[noOpt];
	   demand = new int[noClass]; 
	   option = new int[noClass][noOpt]; 
	   model = new CPModel(); 
	   solver = new CPSolver();
	   position=new IntegerVariable[noCars];
	   for(int i=0; i<noCars; i++)
		   position[i]= Choco.makeIntVar("position_"+i, 0, noClass-1);
	   optPos=new IntegerVariable[noOpt][noCars];
	   for(int i=0; i<noOpt; i++)
		   for(int j=0; j<noCars; j++)
			   optPos[i][j]=Choco.makeIntVar("optPos_"+i+","+j, 0, 1);
	   
	   for(int i=0; i<noOpt; i++) p[i]=sc.nextInt();
	   for(int i=0; i<noOpt; i++) q[i]=sc.nextInt();
	   for (int c = 0; c < noClass; c++) {
           sc.nextInt(); // skip
           demand[c] = sc.nextInt();
           for (int o = 0; o < noOpt; o++)
        	   option[c][o] = sc.nextInt();
       }
	   
	   System.out.println("---Les donnees de probleme---");
	   System.out.println("noCars="+noCars);
	   System.out.println("noOpt="+noOpt);
	   System.out.println("noClass="+noClass);
	   System.out.println("p="+Arrays.toString(p));
	   System.out.println("q="+Arrays.toString(q));
	   System.out.println("demand="+Arrays.toString(demand));
	   for(int i=0; i<noClass; i++)
		   System.out.println("option="+Arrays.toString(option[i]));

	   for(int i=0; i<noOpt; i++)
		   pqConstraint(p[i], q[i], i);
	   
	   demandConstraint();
	   //l'appelle a cette méthode est optionnel, il nous serve dans les grands probleme, car elle reduit enormement l'espace de recherche
	   //redundantConstraint();
	   
	   for(int cls=0; cls<noClass; cls++)
		   for(int pos=0; pos<noCars; pos++)
			   linkConstraint(pos, cls);
	   
	   solver.read(model);
	   System.out.println(solver.toString());
	   solver.solve();
		
		System.out.println("\n------------Resultat---------------------");
		System.out.println("Classe\tOptions requise");
		for(int i=0; i<noCars; i++) {
			System.err.print(solver.getVar(position[i]).getVal()+"\t");
			Thread.sleep(5);
			for(int j=0; j<noOpt; j++) {
				System.out.print(solver.getVar(optPos[j][i]).getVal()+" ");
			}
			System.out.println();
		}
   }
   
   public void pqConstraint(int p, int q, int opt) {
	   for(int i=0; i<noCars-q; i++) {
		   IntegerVariable[] v=new IntegerVariable[q];
		   for(int j=0; j<q; j++)
			   v[j]=optPos[opt][i+j];
		   model.addConstraint(Choco.leq(Choco.sum(v), p));
	   }
   }
   
   public void linkConstraint(int pos, int cls) {
	   Constraint[] C=new Constraint[noOpt];
	   for(int opt=0; opt<noOpt; opt++)
		   C[opt]=Choco.eq(optPos[opt][pos], option[cls][opt]);
	   model.addConstraint(Choco.implies(Choco.eq(position[pos], cls), Choco.and(C)));
   }
   
   public void redundantConstraint() {
	   int[] vectorDemand=new int[noOpt];
	   IntegerVariable[][] optPosArray;
	   for(int o=0; o<noOpt; o++) {
		   for(int c=0; c<noClass; c++)
			   vectorDemand[o]+=demand[c]*option[c][o];
		   if(noCars-q[o]*(o+1)>=0){
			   optPosArray=new IntegerVariable[noCars][noCars-q[o]*(o+1)];
			   for(int p=0; p<noCars-q[o]*(o+1); p++)
				   optPosArray[o][p]=optPos[o][p];
			   model.addConstraint(Choco.geq(Choco.sum(optPosArray[o]), vectorDemand[o]-p[o]*(o+1)));
		   }
	   }
   }
   
   public void demandConstraint() {
	   int[] classes = new int[noClass];
	   IntegerVariable[] classDemand = new IntegerVariable[noClass];
	   for(int i=0; i<noClass; i++) { 
		   classes[i] = i;
		   classDemand[i]=Choco.constant(demand[i]);
	   }
	   model.addConstraint(Choco.globalCardinality(position, classes, classDemand));
   }
}