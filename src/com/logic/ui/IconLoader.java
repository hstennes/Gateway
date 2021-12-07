package com.logic.ui;

import com.logic.components.*;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Loads and holds all of the images used by the program
 * @author Hank Stennes
 *
 */
public class IconLoader {

	/**
	 * The number of images used for drawing circuits
	 */
	private final int numLogicImages = 14;

	/**
	 * The number of component icons used in the insert toolbar
	 */
	private final int numLogicIcons = 15;

	/**
	 * The number of tool bar icons
	 */ 
	private final int numToolBarIcons = 15;

	/**
	 * Shows the width of each logic image
	 */
	public final int[] imageWidth = new int[] {80, 80, 80, 60, 60, 80, 80, 60, 60, 60, 60, 80, 80, 100};

	/**
	 * Shows the height of each logic image
	 */
	public final int[] imageHeight = new int[] {80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 100};

	/**
	 * The sprite sheet that contains all the images that the program uses
	 */
	public BufferedImage iconSheet;

	/**
	 * The array of logic SVGs
	 */
	public GraphicsNode[] logicSVGs;
	
	/**
	 * The array of logic icons
	 */
	public ImageIcon[] logicIcons;
	
	/**
	 * The array of tool bar icons
	 */
	public ImageIcon[] toolBarIcons;
	
	/**
	 * Constructs a new IconLoader and loads all of the images
	 */
	public IconLoader() {
		iconSheet = loadImage("/icons.png");
		logicSVGs = new GraphicsNode[numLogicImages];
		logicIcons = new ImageIcon[numLogicIcons];
		toolBarIcons = new ImageIcon[numToolBarIcons];
	}
	
	/**
	 * Loads all the BufferedImages and SVG GraphicsNode objects that the program uses
	 */
	public void makeImageIcons() {
		logicSVGs[0] = loadSvg("/buffer.svg");
		logicSVGs[1] = loadSvg("/and.svg");
		logicSVGs[2] = loadSvg("/or.svg");
		logicSVGs[3] = loadSvg("/switch_off.svg");
		logicSVGs[4] = loadSvg("/switch_on.svg");
		logicSVGs[5] = loadSvg("/button_off.svg");
		logicSVGs[6] = loadSvg("/button_on.svg");
		logicSVGs[7] = loadSvg("/light_off.svg");
		logicSVGs[8] = loadSvg("/light_on.svg");
		logicSVGs[9] = loadSvg("/off_const.svg");
		logicSVGs[10] = loadSvg("/on_const.svg");
		logicSVGs[11] = loadSvg("/clock_off.svg");
		logicSVGs[12] = loadSvg("/clock_on.svg");
		logicSVGs[13] = loadSvg("/display.svg");

		toolBarIcons[0] = new ImageIcon(loadImage("/new_file.png"));
		toolBarIcons[1] = new ImageIcon(loadImage("/open_file.png"));
		toolBarIcons[2] = new ImageIcon(loadImage("/save_file.png"));
		toolBarIcons[3] = new ImageIcon(loadImage("/select.png"));
		toolBarIcons[4] = new ImageIcon(loadImage("/pan.png"));
		toolBarIcons[5] = new ImageIcon(loadImage("/insert.png"));
		toolBarIcons[6] = new ImageIcon(loadImage("/undo.png"));
		toolBarIcons[7] = new ImageIcon(loadImage("/redo.png"));
		toolBarIcons[8] = new ImageIcon(loadImage("/cut.png"));
		toolBarIcons[9] = new ImageIcon(loadImage("/copy.png"));
		toolBarIcons[10] = new ImageIcon(loadImage("/paste.png"));
		toolBarIcons[11] = new ImageIcon(loadImage("/delete.png"));
		toolBarIcons[12] = new ImageIcon(loadImage("/rotate_counter.png"));
		toolBarIcons[13] = new ImageIcon(loadImage("/rotate.png"));
		toolBarIcons[14] = new ImageIcon(loadImage("/custom.png"));

		logicIcons[0] = new ImageIcon(renderLogicIcon(new SingleInputGate(0, 0, CompType.BUFFER), 0));
		logicIcons[1] = new ImageIcon(renderLogicIcon(new SingleInputGate(0, 0, CompType.NOT), 1));
		logicIcons[2] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.AND), 2));
		logicIcons[3] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.NAND), 3));
		logicIcons[4] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.OR), 4));
		logicIcons[5] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.NOR), 5));
		logicIcons[6] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.XOR), 6));
		logicIcons[7] = new ImageIcon(renderLogicIcon(new BasicGate(0, 0, CompType.XNOR), 7));
		logicIcons[8] = new ImageIcon(renderLogicIcon(new Clock(0, 0), 8));
		logicIcons[9] = new ImageIcon(renderLogicIcon(new Light(0, 0), 9));
		logicIcons[10] = new ImageIcon(renderLogicIcon(new Switch(0, 0), 10));
		logicIcons[11] = new ImageIcon(renderLogicIcon(new Constant(0, 0, CompType.ZERO), 11));
		logicIcons[12] = new ImageIcon(renderLogicIcon(new Constant(0, 0, CompType.ONE), 12));
		logicIcons[13] = new ImageIcon(renderLogicIcon(new Button(0, 0), 13));
		logicIcons[14] = new ImageIcon(renderLogicIcon(new Display(0, 0), 14));
	}

	/**
	 * Renders the icons for each gate using CompDrawer drawing code set to SVG mode. Does not include connections.
	 * @param model The LComponent that will be rendered to the image (should be placed at 0, 0)
	 * @param index The index the icon will be placed at in the logicIcons array
	 * @return The icon based on the given LComponent
	 */
	private BufferedImage renderLogicIcon(LComponent model, int index){
		/*BufferedImage image = new BufferedImage(70, 70, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		Rectangle bounds = model.getBounds();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		float scale = Math.min((InsertPanel.BUTTON_SIZE - 2 * InsertPanel.paddingValues[index]) / (float) bounds.width,
				(InsertPanel.BUTTON_SIZE - 2 * InsertPanel.paddingValues[index]) / (float) bounds.height);
		g2d.scale(scale, scale);
		g2d.translate((int) ((InsertPanel.BUTTON_SIZE - bounds.width * scale) / 2 / scale),
				(int) ((InsertPanel.BUTTON_SIZE - bounds.height * scale) / 2 / scale));
		model.getDrawer().setUseSVG(true);
		model.render(g2d, null);
		g2d.dispose();*/

		BufferedImage image = new Renderer(null).renderComponentImage(model, 0.40f);
		return image;
	}

	/**
	 * Loads the given SVG file as a GraphicsNode so that it can then be rendered at any resolution
	 * @param path The path to the SVG
	 * @return The GraphicsNode object
	 */
	private GraphicsNode loadSvg(String path){
		GraphicsNode svgIcon = null;
		try {
			String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
			SVGDocument doc = df.createSVGDocument(path, getClass().getResourceAsStream(path));
			UserAgent userAgent = new UserAgentAdapter();
			DocumentLoader loader = new DocumentLoader(userAgent);
			BridgeContext ctx = new org.apache.batik.bridge.BridgeContext(userAgent, loader);
			ctx.setDynamicState(org.apache.batik.bridge.BridgeContext.DYNAMIC);
			GVTBuilder builder = new org.apache.batik.bridge.GVTBuilder();
			svgIcon = builder.build(ctx, doc);
		} catch (Exception excp) {
			svgIcon = null;
			excp.printStackTrace();
		}
		return svgIcon;
	}

	/**
	 * Loads an image from the specified file
	 * @param path The file path to load the image from
	 * @return The BufferedImage
	 */
	private BufferedImage loadImage(String path){
		try {
			return ImageIO.read(getClass().getResource(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
