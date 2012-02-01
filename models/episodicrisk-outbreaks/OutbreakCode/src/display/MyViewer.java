package display;


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


import cluster.Edge;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import episodicriskmodel.Person;

public class MyViewer extends VisualizationViewer<Person, Edge>{
	private static final long serialVersionUID = 1L;
	private Dimension dim; 

	public MyViewer(Layout<Person, Edge> layout, Dimension preferredSize) {
		super(layout, preferredSize);
		dim = preferredSize; 
	}

	
	public Image getImage(Point2D center, Dimension d){
		int width = getWidth();
		int height = getHeight();

		float scalex = (float)width/d.width;
		float scaley = (float)height/d.height;

		scalex = d.width;
		scaley = d.height;
		try
		{
			renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).scale(scalex, scaley, center);

			BufferedImage bi = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = bi.createGraphics();
			//graphics.setRenderingHints(renderingHints);
			paint(graphics);
			graphics.dispose();
			return bi;
		} finally {
			renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
		}
	}
}