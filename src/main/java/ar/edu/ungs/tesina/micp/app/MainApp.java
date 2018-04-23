package ar.edu.ungs.tesina.micp.app;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import ar.edu.ungs.tesina.micp.Micp;
import ar.edu.ungs.tesina.micp.app.ui.MainFrame;
import ar.edu.ungs.tesina.micp.instancia.Aula;
import ar.edu.ungs.tesina.micp.instancia.Clase;
import ar.edu.ungs.tesina.micp.instancia.Instancia;
import jscip.Scip;

public class MainApp {

	private Micp mMicp;
	private Instancia mInstance;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainApp app = new MainApp();
					MainFrame window = new MainFrame(app);
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainApp() {
		InitializeMicp();
	}

	private void InitializeMicp() {

		try {
			System.loadLibrary("jscip");

			Scip solver = new Scip();
			solver.create("micp_app");
			mMicp = Micp.createMicp(solver);
		} catch (UnsatisfiedLinkError ex) {
			// TODO: Mostrar error y salid!
			JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (Exception ex) {
			// TODO: Mostrar error y salid!
			JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

	}
	
	public void close() {
		mMicp.free();
		
	}

	// -------------------------Se obtiene el contenido del
	// Archivo----------------//
	public boolean loadInstanceFCEN(String ruta, int cantAulas) {
		List<Aula> aulas = new ArrayList<Aula>();
		for (int i = 0; i < cantAulas; i++) {
			aulas.add(new Aula(String.format("%05d", i)));
		}
		return loadInstanceFCEN(ruta, aulas);
	}
	
	public boolean loadInstanceFCEN(String ruta, List<Aula> aulas) {
		boolean status = false;
		// Se lee el archivo de texto y se generan los grafos de conflicto y
		// relacion.
		FileReader fr = null;
		BufferedReader br = null;
		// Cadena de texto donde se guardara el contenido del archivo
		List<Clase> clases = new ArrayList<Clase>();
		int id = 1;
		try {
			// ruta puede ser de tipo String o tipo File
			fr = new FileReader(ruta);
			br = new BufferedReader(fr);

			String linea;
			// Obtenemos el contenido del archivo linea por linea
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					System.out.println("debug - Linea vacia!");
				else {
					Clase c = null;
					try {
						c = Clase.createClase(id, linea, Clase.Tipo.FCEN);
					} catch (Exception e) {
						System.out.println("debug - Error creando Clase con linea: " + linea);
					}
					if (c != null && c.mPabellon == 1) {
						clases.add(c);
						id++;
					}
				}
			}
		} catch (Exception e) {
		}
		// finally se utiliza para que si todo ocurre correctamente o si ocurre
		// algun error se cierre el archivo que anteriormente abrimos
		finally {
			try {
				br.close();
			} catch (Exception ex) {
			}
		}
		if (!clases.isEmpty()) {
			Instancia instance = new Instancia(clases, aulas);
			for (int i = 0; i < clases.size(); ++i) {
				for (int j = i + 1; j < clases.size(); ++j) {
					Clase c1 = clases.get(i);
					Clase c2 = clases.get(j);
					if (Clase.seSuperponen(c1, c2))
						instance.addConflicto(c1, c2);
					else if (Clase.igualMateria(c1, c2))
						instance.addRelacion(c1, c2);
				}
			}
			mInstance = instance;
			status = true;
		}
		return status;
	}


	public List<Aula> loadAulasFile(String path) {
		// Se lee el archivo de texto y se generan los grafos de conflicto y
		// relacion.
		FileReader fr = null;
		BufferedReader br = null;
		// Cadena de texto donde se guardara el contenido del archivo
		Set<Aula> aulas = new HashSet<Aula>();
		try {
			// ruta puede ser de tipo String o tipo File
			fr = new FileReader(path);
			br = new BufferedReader(fr);

			String linea;
			// Obtenemos el contenido del archivo linea por linea
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					System.out.println("debug - Linea vacia!");
				else {
					aulas.add(new Aula(linea));
				}
			}
		} catch (Exception e) {
		}
		// finally se utiliza para que si todo ocurre correctamente o si ocurre
		// algun error se cierre el archivo que anteriormente abrimos
		finally {
			try {
				br.close();
			} catch (Exception ex) {
			}
		}
		
		List<Aula> ret = new ArrayList<Aula>();
		ret.addAll(aulas);
		return ret;
	}

	public boolean optimize() {
		// TODO Auto-generated method stub
		mInstance.setSolution(  mMicp.findOptimal(mInstance.getConflictGraph(), mInstance.getRelationshipGraph(), mInstance.getAulas()) );
		
		return mInstance.hasSolution();
	}
	
	public boolean saveSolution(String path) {
		
		System.out.println("================================ ");
		System.out.println("SOLUCION ENCONTRADA POR EL MICP: ");
		for(Clase c : mInstance.getClases())
			System.out.println(mInstance.getOptimal(c)+"|"+c.serialize());
			
		return false;
	}



	// -----------------------------------------------------------------------------//
}
