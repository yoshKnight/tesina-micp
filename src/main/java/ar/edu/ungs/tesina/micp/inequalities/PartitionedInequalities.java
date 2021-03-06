/**
 * 
 */
package ar.edu.ungs.tesina.micp.inequalities;

import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import ar.edu.ungs.tesina.micp.Color;
import ar.edu.ungs.tesina.micp.Edge;
import ar.edu.ungs.tesina.micp.MicpScipSolver;
import ar.edu.ungs.tesina.micp.SolverConfig;
import ar.edu.ungs.tesina.micp.Vertex;
import jscip.Constraint;
import jscip.Variable;

/**
 * @author yoshknight
 *
 */
public class PartitionedInequalities<T extends Vertex, U extends Color>
		extends CustomInequalities<T, U> {

	protected List<Integer> mInequalitiesEnabled;

	public PartitionedInequalities(SolverConfig solverConfig) {
		mInequalitiesEnabled = solverConfig.getInequalitiesEnabled();
	}

	@Override
	public void addInequalities(MicpScipSolver<T, U> micpSolver, List<T> vertices, List<U> colors,
			Graph<T, Edge<T>> conflictGraph, Graph<T, Edge<T>> relationshipGraph) {

		// Si no se selecciono ninguna inequality, se termina el metodo.
		if (mInequalitiesEnabled.isEmpty())
			return;

		if (mInequalitiesEnabled.contains(DefinedInequalitiesEnum.PARTITIONED_INEQUALITIES)) {
			addPartitionedInequality(micpSolver, vertices, colors, conflictGraph,
					relationshipGraph);
		}

		if (mInequalitiesEnabled.contains(DefinedInequalitiesEnum.THREE_PARTITIONED_INEQUALITIES)) {
			addThreePartitionedInequality(micpSolver, vertices, colors, conflictGraph,
					relationshipGraph);
		}

		if (mInequalitiesEnabled.contains(DefinedInequalitiesEnum.K_PARTITIONED_INEQUALITIES)) {
			addKPartitionedInequality(micpSolver, vertices, colors, conflictGraph,
					relationshipGraph);
		}

	}

	/**
	 * Para cada relacion en H Agrega na partitioned inequality
	 * 
	 * @param micpSolver
	 * @param vertices
	 * @param colors
	 * @param conflictGraph
	 * @param relationshipGraph
	 */
	public void addPartitionedInequality(MicpScipSolver<T, U> micpSolver, List<T> vertices,
			List<U> colors, Graph<T, Edge<T>> conflictGraph, Graph<T, Edge<T>> relationshipGraph) {

		int len = colors.size() - 1; // 1 <= len <= |colors|-1
		Set<U> D = generateColorsSubset(colors, len);
		Set<U> Dcomplement = generateComplement(colors, D);

		int cantFactors = 1 + colors.size();
		if (D.size() + Dcomplement.size() != colors.size())
			throw new RuntimeException("Los subconjuntos de D estan mal calculados");

		System.out.println("Agrego PartitionedInequality para cada relacion de H: " + cantFactors);

		// Yij <= sum Xid' + sum Xjd
		Variable[] vars = new Variable[cantFactors];
		double[] factors = new double[cantFactors];
		int i;

		for (Edge<T> e : relationshipGraph.edgeSet()) {
			T vi = e.getSource();
			T vj = e.getTarget();
			i = 0;
			factors[i] = 1;
			vars[i++] = micpSolver.getVarY(vi, vj);

			if (vars[i - 1] == null)
				System.out.println("debug : la variable Yij es nula! ");

			for (U c : D) {
				factors[i] = -1;
				vars[i++] = micpSolver.getVarX(vj, c);
				if (vars[i - 1] == null)
					System.out.println("debug : la variable Xjc es nula! ");
			}

			for (U c : Dcomplement) {
				factors[i] = -1;
				vars[i++] = micpSolver.getVarX(vi, c);
				if (vars[i - 1] == null)
					System.out.println("debug : la variable Xic es nula! ");
			}

			if (i < vars.length)
				throw new RuntimeException("Hay un campo vacio");

			Constraint diferentColorsOnConflict = micpSolver.getSolver().createConsLinear(
					"VertexCliqueInequality-" + vi + "-" + vj, vars, factors, -2, 0);

			micpSolver.getSolver().addCons(diferentColorsOnConflict);
			micpSolver.getSolver().releaseCons(diferentColorsOnConflict);
		}

	}

	public void addThreePartitionedInequality(MicpScipSolver<T, U> micpSolver, List<T> vertices,
			List<U> colors, Graph<T, Edge<T>> conflictGraph, Graph<T, Edge<T>> relationshipGraph) {

		int len = colors.size() - 1; // 2 <= len <= |colors|-1
		Set<U> D = generateColorsSubset(colors, len);
		Set<U> D1 = generateColorsSubset(D, D.size() / 2);
		Set<U> D2 = generateComplement(D, D1);

		int cantFactors = 3 + 2 * D.size();
		if (D1.size() + D2.size() != D.size())
			throw new RuntimeException("Los subconjuntos de D1 y D2 estan mal calculados");

		System.out.println("Agrego 3-PartitionedInequality para cada relaion de H: " + cantFactors);

		// Yij + Yjk + Yik <= 3 + 2sum( Xjd1 - Xid1 ) + 2sum( Xk2 - Xid2 )
		Variable[] vars = new Variable[cantFactors];
		double[] factors = new double[cantFactors];
		int i;

		for (Edge<T> e : relationshipGraph.edgeSet()) {
			T vi = e.getSource();
			T vj = e.getTarget();

			for (T vk : Graphs.neighborListOf(relationshipGraph, vj)) {
				if (!vk.equals(vi) && relationshipGraph.containsEdge(vk, vi)) {

					i = 0;
					factors[i] = 1;
					vars[i++] = micpSolver.getVarY(vi, vj);
					if (vars[i - 1] == null)
						System.out.println("debug : la variable Yij es nula! ");
					factors[i] = 1;
					vars[i++] = micpSolver.getVarY(vj, vk);
					if (vars[i - 1] == null)
						System.out.println("debug : la variable Yjk es nula! ");
					factors[i] = 1;
					vars[i++] = micpSolver.getVarY(vi, vk);
					if (vars[i - 1] == null)
						System.out.println("debug : la variable Yik es nula! ");

					for (U c : D1) {
						factors[i] = -2;
						vars[i++] = micpSolver.getVarX(vj, c);
						if (vars[i - 1] == null)
							System.out.println("debug : la variable Xid1 es nula! ");
						factors[i] = 2;
						vars[i++] = micpSolver.getVarX(vi, c);
						if (vars[i - 1] == null)
							System.out.println("debug : la variable Xid2 es nula! ");
					}

					for (U c : D2) {
						factors[i] = -2;
						vars[i++] = micpSolver.getVarX(vk, c);
						if (vars[i - 1] == null)
							System.out.println("debug : la variable Xic es nula! ");
						factors[i] = 2;
						vars[i++] = micpSolver.getVarX(vi, c);
						if (vars[i - 1] == null)
							System.out.println("debug : la variable Xic es nula! ");
					}

					if (i < vars.length)
						throw new RuntimeException("Hay un campo vacio");

					Constraint diferentColorsOnConflict = micpSolver.getSolver().createConsLinear(
							"ThreePartitionedInequality-" + vi + "-" + vj + "-" + vk, vars, factors,
							-micpSolver.getSolver().infinity(), 3);
					micpSolver.getSolver().addCons(diferentColorsOnConflict);
					micpSolver.getSolver().releaseCons(diferentColorsOnConflict);
				}
			}
		}

	}

	public void addKPartitionedInequality(MicpScipSolver<T, U> micpSolver, List<T> vertices,
			List<U> colors, Graph<T, Edge<T>> conflictGraph, Graph<T, Edge<T>> relationshipGraph) {

		throw new RuntimeException(
				"ERROR - Not Implemented Method PartitionedInequalities::addKPartitionedInequality");
	}
}
