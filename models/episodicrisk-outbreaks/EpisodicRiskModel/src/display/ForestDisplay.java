package display;

/**
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */


import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;


import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import basemodel.ParametersInterface.ACT_TYPE;
import basemodel.ParametersInterface.STAGE;

import cluster.Edge;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;
import episodicriskmodel.Person;

/**
 * Modified version of Jung Library's class file "TreeCollapseDemo"
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 * @author Tom Nelson
 * 
 */
@SuppressWarnings("serial")
public class ForestDisplay extends JFrame {	
	/** The graph */
	Forest<Person,Edge> graph;
	/** The visual component and renderer for the graph */
	MyViewer vv;
	VisualizationServer.Paintable rings;
	String root;

	TreeLayoutExt<Person, Edge> layout;
	@SuppressWarnings("rawtypes")
	FRLayout frLayout;
	TreeCollapser collapser;
	RadialTreeLayout<Person, Edge> radialLayout;

	@SuppressWarnings({ "unchecked", "rawtypes"})
	public ForestDisplay(Forest<Person, Edge> forest, ArrayList<Person> sortedRoots) {
		graph = forest;
		layout = new TreeLayoutExt<Person, Edge>(graph, sortedRoots);
		collapser = new TreeCollapser();
		radialLayout = new RadialTreeLayout<Person, Edge>(graph);
		radialLayout.setSize(new Dimension(600,600));
		frLayout = new FRLayout<Person, Edge>(graph);

		// set default layout for display
		//vv = new MyViewer(layout, new Dimension(1200,4800));
		vv = new MyViewer(layout, new Dimension(600,600));
		//		vv = new MyViewer(layout, new Dimension(300,200));
		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		//vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction());
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		// add a listener for ToolTips
		//vv.setvVertexToolTipTransformer(new ToStringLabeller());
		vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
		rings = new Rings();		

		Container content = getContentPane();
		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		content.add(panel);

		final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		vv.setGraphMouse(graphMouse);
		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});

		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1/1.1f, vv.getCenter());
			}
		});

		JToggleButton radial = new JToggleButton("Radial");
		radial.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					//					layout.setRadial(true);
					vv.setGraphLayout(radialLayout);
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.addPreRenderPaintable(rings);
				} else {
					//					layout.setRadial(false);
					vv.setGraphLayout(layout);
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.removePreRenderPaintable(rings);
				}
				vv.repaint();
			}});

		JButton collapse = new JButton("Collapse");
		collapse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Collection picked =new HashSet(vv.getPickedVertexState().getPicked());
				if(picked.size() == 1) {
					Object root = picked.iterator().next();
					Forest inGraph = (Forest)layout.getGraph();
					try {
						collapser.collapse(vv.getGraphLayout(), inGraph, root);
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					}

					vv.getPickedVertexState().clear();
					vv.repaint();
				}
			}});

		JButton expand = new JButton("Expand");
		expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection picked = vv.getPickedVertexState().getPicked();
				for(Object v : picked) {
					if(v instanceof Forest) {
						Forest inGraph = (Forest)layout.getGraph();
						collapser.expand(inGraph, (Forest)v);
					}
					vv.getPickedVertexState().clear();
					vv.repaint();
				}
			}});

		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
/*				BufferedImage bi = ScreenImage.createImage(panel);
				try{
					File outputfile = new File("saved.png");
					ImageIO.write(bi, "png", outputfile);
				} catch (IOException exp) {}
*/			}		
		});

		/*		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//vv.writeJPEGImage("myjpeg");
				System.out.println("Num components: " + vv.getComponentCount());
				Container content = getContentPane();
//				writeJPEGImage("abc.jpg", content.getComponent(0));
				try {
					writeJPEGImage("abc.jpg", panel);
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					Container content = getContentPane();
					EPSDump dumper = new EPSDump();
					dumper.dumpComponent(new File("myfile.jpg"), content.getComponent(0));
				} catch (Exception e) {e.printStackTrace();}


			}
		});
		 */
		JPanel scaleGrid = new JPanel(new GridLayout(1,0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(radial);
		controls.add(scaleGrid);
		controls.add(modeBox);
		controls.add(collapse);
		controls.add(expand);
		controls.add(save);
		content.add(controls, BorderLayout.SOUTH);
	}

	//	Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
	//		public Paint transform(Edge edge) {
	//			Color color = Color.BLACK;
	//			RISK_STATE infectorRiskState = edge.getTransmission().getInfectorRiskState();
	//			if (infectorRiskState.equals(RISK_STATE.HIGH)) {
	//				color = Color.ORANGE;
	//			}
	//			else if (infectorRiskState.equals(RISK_STATE.LOW)) {
	//				color = Color.GREEN;
	//			}
	//			return color;
	//		}
	//	};

	Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
		public Paint transform(Edge edge) {
			Color color = Color.BLACK;
			ACT_TYPE actType = edge.getTransmission().getActType();
			if (actType.equals(ACT_TYPE.AHI)) {
				color = Color.RED;
			}
			//			RISK_STATE infectorRiskState = edge.getTransmission().getInfectorRiskState();
			//			if (infectorRiskState.equals(RISK_STATE.HIGH)) {
			//				color = Color.ORANGE;
			//			}
			//			else if (infectorRiskState.equals(RISK_STATE.LOW)) {
			//				color = Color.GREEN;
			//			}
			return color;
		}
	};

	Transformer<Person,Paint> vertexPaint = new Transformer<Person,Paint>() {
		public Paint transform(Person individual) {
			Color color;
			if (individual.getID() == -1) {
				color = Color.YELLOW;
				return color;
			}

			if (individual.getInfectorStatus().equals(STAGE.ACUTE)) {
				color = Color.RED;
			}
			else if (individual.getInfectorStatus().equals(STAGE.CHRONIC)) {
				color = Color.BLACK;
			}
			else {
				color = Color.GRAY;
			}
			//			Color color = Color.LIGHT_GRAY;
			//			if (individual.getInfectedMixingSite().equals(MIXING_SITE.HIGH_RISK)) {
			//				color = Color.RED;
			//			}
			//			else {
			//				if (individual.getInfectedRiskState().equals(RISK_STATE.HIGH)) {
			//					color = Color.YELLOW;
			//				}
			//				else if (individual.getInfectedRiskState().equals(RISK_STATE.LOW)) {
			//					color = Color.BLUE;
			//				}
			//			}
			return color;
		}
	};

	class Rings implements VisualizationServer.Paintable {
		Collection<Double> depths;

		public Rings() {
			depths = getDepths();
		}

		private Collection<Double> getDepths() {
			Set<Double> depths = new HashSet<Double>();
			Map<Person, PolarPoint> polarLocations = radialLayout.getPolarLocations();
			for(Person v : graph.getVertices()) {
				PolarPoint pp = polarLocations.get(v);
				depths.add(pp.getRadius());
			}
			return depths;
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);

			Graphics2D g2d = (Graphics2D)g;
			Point2D center = radialLayout.getCenter();

			Ellipse2D ellipse = new Ellipse2D.Double();
			for(double d : depths) {
				ellipse.setFrameFromDiagonal(center.getX()-d, center.getY()-d, 
						center.getX()+d, center.getY()+d);
				Shape shape = vv.getRenderContext().
				getMultiLayerTransformer().getTransformer(Layer.LAYOUT).transform(ellipse);
				g2d.draw(shape);
			}
		}

		public boolean useTransform() {
			return true;
		}
	}

	/**
	 * a demo class that will create a vertex shape that is either a
	 * polygon or star. The number of sides corresponds to the number
	 * of vertices that were collapsed into the vertex represented by
	 * this shape.
	 * 
	 * @author Tom Nelson
	 *
	 * @param <V>
	 */
	class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {
		ClusterVertexShapeFunction() {
			setSizeTransformer(new ClusterVertexSizeFunction<V>(20));
		}
		@SuppressWarnings("rawtypes")
		@Override
		public Shape transform(V v) {
			if(v instanceof Graph) {
				int size = ((Graph)v).getVertexCount();
				if (size < 8) {   
					int sides = Math.max(size, 3);
					return factory.getRegularPolygon(v, sides);
				}
				else {
					return factory.getRegularStar(v, size);
				}
			}
			return super.transform(v);
		}
	}

	/**
	 * A demo class that will make vertices larger if they represent
	 * a collapsed collection of original vertices
	 * @author Tom Nelson
	 *
	 * @param <V>
	 */
	class ClusterVertexSizeFunction<V> implements Transformer<V,Integer> {
		int size;
		public ClusterVertexSizeFunction(Integer size) {
			this.size = size;
		}

		public Integer transform(V v) {
			if(v instanceof Graph) {
				return 30;
			}
			return size;
		}
	}

	/*	public void writeJPEGImage(String filename, GraphZoomScrollPane panel) throws AWTException {
		//Dimension dim = component.getSize();
		//Rectangle r = component.getBounds();
		//int width = r.width;//dim.width;
		//int height = r.height;//dim.height;
//		Color bg = getBackground();
		Color bg = Color.WHITE;

		//BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		JFrame frame = new JFrame();
		frame.setContentPane( panel);
		frame.pack();
		//ScreenImage.createImage( someComponent );
		//BufferedImage bi = captureComponentUsingImageBuffer(panel);//(BufferedImage)vv.getImage(new Point2D.Double(0,0), new Dimension(2300,2300));//ScreenImage.createImage(panel);
		vv.setDoubleBuffered(true);
		vv.repaint();
		Rectangle region = new Rectangle(0, 0, panel.getWidth()*2, panel.getHeight()*2);
		BufferedImage bi = ScreenImage.createImage((JComponent)panel.getComponent(0), region);
		Graphics2D graphics = bi.createGraphics();
		graphics.setColor(bg);
		panel.paint(graphics);
		//component.paint(graphics);
		//graphics.drawImage(bi, 0, 0, component);
		//graphics.fillRect(0,0, width, height);

		try{
			//FileOutputStream out = new FileOutputStream(filename);
			ImageIO.write(bi,"bmp",new File(filename));
			//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			//encoder.encode(bi);
			//out.close();
		}catch(Exception e){e.printStackTrace();}

	}
	 */
	/*
	 *    BufferedImage myImage = 
         new BufferedImage(size.width, size.height,
         BufferedImage.TYPE_INT_RGB);
       Graphics2D g2 = myImage.createGraphics();
       myComponent.paint(g2);
       try {
         OutputStream out = new FileOutputStream(filename);
         JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
         encoder.encode(myImage);
         out.close();
       } catch (Exception e) {
         System.out.println(e); 
       }
	 */

	/*	public BufferedImage captureComponentUsingImageBuffer(Component c) {
	    Dimension preferredSize = c.getPreferredSize();
	    BufferedImage img = new BufferedImage(preferredSize.width, preferredSize.height, BufferedImage.TYPE_INT_ARGB);
	    c.paint(img.getGraphics());
	    return img;
	}
	 */
}