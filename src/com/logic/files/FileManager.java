package com.logic.files;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logic.input.Camera;
import com.logic.input.CircuitState;
import com.logic.input.RevisionManager;
import com.logic.main.LogicSimApp;
import com.logic.ui.CircuitPanel;
import com.logic.ui.LMenuBar;
import com.logic.ui.UserMessage;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * A class that manages file saving and opening operations
 * @author Hank Stennes
 *
 */
public class FileManager {

	/**
	 * The file format version. Should be incremented when the file format is changed in any way.
	 */
	public static final int FILE_FORMAT_VERSION = 6;

	/**
	 * Option that uses readable json formatting for testing the file saving system
	 */
	public static final boolean PRETTY_FILE_OUTPUT = false;

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
				openFile(path, cp.lcomps.size() > 0);
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
			Camera cam = cp.getCamera();
			JSONFile file = new JSONFile(new FileData(FILE_FORMAT_VERSION, cp.lcomps,
					cp.getEditor().getCustomCreator().getCustomTypes(),
					new float[] {cam.getX(), cam.getY(), cam.getZoom()},
					new int[] {cp.getEditor().isSnap() ? 1 : 0, cp.isShowGrid() ? 1 : 0, cp.isHighQuality() ? 1 : 0}));
			if(PRETTY_FILE_OUTPUT) new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get(path).toFile(), file);
			else new ObjectMapper().writeValue(Paths.get(path).toFile(), file);
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
			try {
				JSONFile file = new ObjectMapper().readValue(Paths.get(path).toFile(), JSONFile.class);
				FileData fileData = file.getFileData();
				cp.addLComps(fileData.getLcomps());
				cp.getEditor().getCustomCreator().setCustomTypes(fileData.getCustomTypes());
				float[] camData = fileData.getCamera();
				Camera cam = cp.getCamera();
				cam.setZoom(camData[2]);
				cam.setX(camData[0]);
				cam.setY(camData[1]);
				loadSettings(fileData.getVersion(), fileData.getSettings());
				RevisionManager revision = cp.getEditor().getRevision();
				revision.clearStates();
				revision.saveState(new CircuitState(cp));
				cp.getWindow().setTitle(path);
				cp.repaint();
			} catch (JsonParseException e){
				cp.dispMessage(new UserMessage(cp, "File type not supported", 3000));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadSettings(int version, int[] settings){
		cp.getEditor().setSnap(settings[0] == 1);
		cp.setShowGrid(settings[1] == 1);
		if(version >= 2) cp.setHighQuality(settings[2] == 1);
		((LMenuBar) cp.getWindow().getJMenuBar()).syncViewSettings();
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
