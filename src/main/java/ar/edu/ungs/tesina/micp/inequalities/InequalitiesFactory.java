package ar.edu.ungs.tesina.micp.inequalities;

import java.util.List;

import ar.edu.ungs.tesina.micp.Color;
import ar.edu.ungs.tesina.micp.SolverConfig;
import ar.edu.ungs.tesina.micp.Vertex;

public class InequalitiesFactory<T extends Vertex, U extends Color> {
	private SolverConfig mConfig;
	private List<Integer> mInequalitiesEnabled;

	public InequalitiesFactory(SolverConfig solverConfig) {
		mConfig = solverConfig;
		mInequalitiesEnabled = solverConfig.getInequalitiesEnabled();

	}

	public CustomInequalities<T, U> createInequalities() {

		CustomInequalitiesComposite<T, U> composite = new CustomInequalitiesComposite<T, U>();

		boolean notAddedPartitioned = true, notAddedClique = true, notAddedTriangle = true,
				notAddedValid = true;

		for (Integer i : mInequalitiesEnabled) {
			switch (i) {
			case DefinedInequalitiesEnum.PARTITIONED_INEQUALITIES:
			case DefinedInequalitiesEnum.THREE_PARTITIONED_INEQUALITIES:
			case DefinedInequalitiesEnum.K_PARTITIONED_INEQUALITIES:
				if (notAddedPartitioned) {
					composite.add(new PartitionedInequalities<T, U>(mConfig));
					notAddedPartitioned = false;
				}
				break;
			case DefinedInequalitiesEnum.CLIQUE_PARTITIONED_INEQUALITIES:
			case DefinedInequalitiesEnum.VERTEX_CLIQUE_INEQUALITIES:
			case DefinedInequalitiesEnum.SUB_CLIQUE_INEQUALITIES:
			case DefinedInequalitiesEnum.TWO_COLOR_SUB_CLIQUE_INEQUALITIES:
				if (notAddedClique) {
					composite.add(new CliqueInequalities<T, U>(mConfig));
					notAddedClique = false;
				}
				break;
			case DefinedInequalitiesEnum.SEMI_TRIANGLE_INEQUALITIES:
			case DefinedInequalitiesEnum.SEMI_DIAMOND_INEQUALITIES:
				if (notAddedTriangle) {
					composite.add(new TriangleDiamondInequalities<T, U>(mConfig));
					notAddedTriangle = false;
				}
				break;
			case DefinedInequalitiesEnum.BOUNDING_INEQUALITIES:
			case DefinedInequalitiesEnum.REINFORCED_BOUNDING_INEQUALITIES:
				if (notAddedValid) {
					composite.add(new ValidInequalities<T, U>(mConfig));
					notAddedValid = false;
				}
				break;
			}
		}

		return composite;

	}
}