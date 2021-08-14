package com.logic.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logic.input.Camera;
import com.logic.input.CircuitState;
import com.logic.input.RevisionManager;
import com.logic.main.LogicSimApp;
import com.logic.ui.CircuitPanel;
import com.logic.ui.LMenuBar;
import com.logic.ui.UserMessage;
import org.apache.commons.io.FilenameUtils;

/**
 * A class that manages file saving and opening operations
 * @author Hank Stennes
 *
 */
public class FileManager {

	/**
	 * A filter that only allows for the selection of files that are compatible with Gateway
	 */
	private final FileNameExtensionFilter filter = new FileNameExtensionFilter("Gateway files", "gtw");
	
	/**
	 * The CircuitPanel whose components will be saved and that will be edited if a file is opened
	 */
	private CircuitPanel cp;
	
	/**
	 * The path of the file that is currently contained in the CircuitPanel, which is used for the save function (as opposed to save as, which
	 * prompts the user for a new path)
	 */
	private String currentFile;
	
	/**
	 * The JFileChooser that is used to display save and open dialogs
	 */
	private JFileChooser fc;
	
	/**
	 * Constructs a new FileManager
	 * @param cp The CircuitPanel
	 */
	public FileManager(CircuitPanel cp) {
		this.cp = cp;
		fc = new JFileChooser();
		fc.setFileFilter(filter);
	}
	
	/**
	 * Constructs a new FileManager for a file that has already been saved
	 * @param cp The CircuitPanel
	 * @param path The path of the file that this FileManager is being created for
	 */
	public FileManager(CircuitPanel cp, String path) {
		this.cp = cp;
		this.currentFile = path;
		fc = new JFileChooser();
		fc.setFileFilter(filter);
	}
	
	/**
	 * Saves the current file to its previous location, or prompts the user for a file location if this file has not yet been saved
	 */
	public void save() {
		if(currentFile == null) saveAs();
		else saveFile(currentFile);
	}
	
	/**
	 * Prompts the user for a file location and saves the file to this location (equivalent to save() when the file has not previously been
	 * saved)
	 */
	public void saveAs() {
		if(fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String path = getSanitizedPath(fc.getSelectedFile());
			currentFile = path;
			saveFile(path);
		}
	}
	
	/**
	 * Asks the user for a file path and opens the given file
	 */
	public void open() {
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String path = file.getAbsolutePath();
			if(!path.equals(currentFile)) {
				openFile(path, cp.getEditor().getRevision().hasEdits());
				currentFile = path;
			}
		}
	}
	
	/**
	 * A utility method for saving the contents of the CircuitPanel to the given path. This method will work regardless of if the given 
	 * file already exists (it will overwrite it if it does)
	 * @param path The path to save the file to
	 */
	private void saveFile(String path) {
		cp.getWindow().setTitle(path);
		UserMessage message = new UserMessage(cp, "Circuit saved", 3000);
		cp.dispMessage(message);
		try {
			/*
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			GatewayFile gatewayFile = new GatewayFile(cp.lcomps, cp.getCamera());
			oos.writeObject(gatewayFile);
			oos.close();
			fos.close();
			 */
			Camera cam = cp.getCamera();
			JSONFile file = new JSONFile(new FileData(cp.lcomps,
					cp.getEditor().getCustomCreator().getCustoms(),
					new double[] {cam.getX(), cam.getY(), cam.getZoom()},
					new int[] {cp.getEditor().isSnap() ? 1 : 0, cp.isShowGrid() ? 1 : 0}));
			new ObjectMapper().writeValue(Paths.get(path).toFile(), file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens a file either in the current window or in a new window depending on the newWindow parameter
	 * @param path The path of the file to open
	 * @param newWindow If true, the file opens in a new window by calling openFile(path, false) in the new window. If false, the file opens
	 * in the current window by replacing the contents of the CircuitPanel
	 */
	public void openFile(String path, boolean newWindow) {
		if(newWindow) {
			LogicSimApp.newWindow(path);
		}
		else {
			cp.getWindow().setTitle(path);
			try {
				/*
				FileInputStream fis = new FileInputStream(path);
				ObjectInputStream ois = new ObjectInputStream(fis); 
				GatewayFile gatewayFile = (GatewayFile) ois.readObject();
				gatewayFile.setupCircuitPanel(cp);
				gatewayFile.setupCamera(cp.getCamera());
				RevisionManager revision = cp.getEditor().getRevision();
				revision.clearStates();
				revision.saveState(new CircuitState(cp));
				ois.close();
				fis.close();
				 */

				JSONFile file = new ObjectMapper().readValue(Paths.get(path).toFile(), JSONFile.class);
				FileData fileData = file.getFileData();
				cp.addLComps(fileData.getLcomps());
				cp.getEditor().getCustomCreator().setCustoms(fileData.getCustoms());
				double[] camData = fileData.getCamera();
				Camera cam = cp.getCamera();
				cam.setZoom(camData[2]);
				cam.setX(camData[0]);
				cam.setY(camData[1]);
				int[] settings = fileData.getSettings();
				cp.getEditor().setSnap(settings[0] == 1);
				cp.setShowGrid(settings[1] == 1);
				((LMenuBar) cp.getWindow().getJMenuBar()).syncViewSettings();
				RevisionManager revision = cp.getEditor().getRevision();
				revision.clearStates();
				revision.saveState(new CircuitState(cp));
				cp.repaint();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the path of the given file with the correct extension
	 * @param file The file
	 * @return The sanitized path
	 */
	private String getSanitizedPath(File file){
		if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("gtw"))
			return file.getAbsolutePath();
		else
			return new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".gtw").getAbsolutePath();
	}
}
