package vlms;
  
/* From http://java.sun.com/docs/books/tutorial/index.html */
/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
/**
 * A 1.4 application that requires the following additional files:
 *   TreeDemoHelp.html
 *    arnold.html
 *    bloch.html
 *    chan.html
 *    jls.html
 *    swingtutorial.html
 *    tutorial.html
 *    tutorialcont.html
 *    vm.html
 */

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.ImageIcon;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;

import ij.plugin.PlugIn;
import ij.*;

import java.io.File;


public class ExperimentView extends JPanel
                          implements TreeSelectionListener {
    //private JEditorPane htmlPane;
    public JTree tree;
	public Experiment exper;
	
	
	//public File path;
	
	

    public URL helpURL;
    public static boolean DEBUG = false;

    public ExperimentView(Experiment experiment) {
        super(new GridLayout(1,0));
		exper = experiment;

		//String path = "C:\\Users\\localuser\\Desktop\\Code Laboratory\\Steffi\\Steffi NMJ datasets\\150729_w1118";
		
		//File path = new File();
		//path = exp.path.getPath();
		tree = createFileTree(exper.path.getPath());
		
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        //Set the icon for leaf nodes.
        ImageIcon hemisegIcon = createImageIcon("hemiseg.gif");
		
        if (hemisegIcon != null) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            //renderer.setLeafIcon(hemisegIcon);
			renderer.setOpenIcon(hemisegIcon);
            tree.setCellRenderer(renderer);
        } else {
            System.err.println("Leaf icon missing; using default.");
        }

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);		

        Dimension minimumSize = new Dimension(100, 50);
        treeView.setMinimumSize(minimumSize);
		treeView.setPreferredSize(new Dimension(500, 300));
   
        add(treeView);
		runGui();
    }
	
	public static JTree createFileTree(String path) {
		JTree jt = new JTree(scan(new MyFile(path)));
		return jt;
	}

	private static MutableTreeNode scan(MyFile path) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(path);
	
		if (path.isDirectory()) {
		 for (MyFile child: path.listFiles())
			node.add(scan(child));
		}
		return node;
	}
	
	private static class MyFile extends File {
		public MyFile(String path) {
			super(path);
		}
		public String toString() {
			return this.getName();
		}
		public MyFile[] listFiles() {
			File[] fs = super.listFiles();
			MyFile[] mfs = new MyFile[fs.length];
			for(int i=0; i<fs.length; i++) {
				mfs[i] = new MyFile(fs[i].getPath());
			}
			return mfs;
		}

		
	}

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
		// *** 
        if (node == null) return;
		MyFile f = (MyFile)node.getUserObject();
		//System.out.println(nodeInfo.getPath());
		if (f.getName().endsWith(".tif")) {
			IJ.log(f.getPath());
			ImagePlus imp = IJ.openImage(f.getPath());
			imp.show();
		}
    }

    // private void initHelp() {
        // String s = "TreeDemoHelp.html";
        // helpURL = ExperimentView.class.getResource(s);
        // if (helpURL == null) {
            // System.err.println("Couldn't open help file: " + s);
        // } else if (DEBUG) {
            // System.out.println("Help URL is " + helpURL);
        // }

        // displayURL(helpURL);
    // }

    //private void displayURL(URL url) {
       /*  try {
            if (url != null) {
                //htmlPane.setPage(url);
            } else { //null url
    //htmlPane.setText("File Not Found");
                if (DEBUG) {
                    System.out.println("Attempted to display a null URL.");
                }
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        } */
    //}

    // private void createNodes(DefaultMutableTreeNode top) {
        // DefaultMutableTreeNode category = null;
        // DefaultMutableTreeNode book = null;

        // category = new DefaultMutableTreeNode("Books for Java Programmers");
        // top.add(category);

        // // original Tutorial
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Tutorial: A Short Course on the Basics",
            // "tutorial.html"));
        // category.add(book);

        // // Tutorial Continued
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Tutorial Continued: The Rest of the JDK",
            // "tutorialcont.html"));
        // category.add(book);

        // // JFC Swing Tutorial
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The JFC Swing Tutorial: A Guide to Constructing GUIs",
            // "swingtutorial.html"));
        // category.add(book);

        // // Bloch
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("Effective Java Programming Language Guide",
       // "bloch.html"));
        // category.add(book);

        // // Arnold/Gosling
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Programming Language", "arnold.html"));
        // category.add(book);

        // // Chan
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Developers Almanac",
             // "chan.html"));
        // category.add(book);

        // category = new DefaultMutableTreeNode("Books for Java Implementers");
        // top.add(category);

        // // VM
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Virtual Machine Specification",
             // "vm.html"));
        // category.add(book);

        // // Language Spec
        // book = new DefaultMutableTreeNode(new BookInfo
            // ("The Java Language Specification",
             // "jls.html"));
        // category.add(book);
    // }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ExperimentView.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("ExperimentView");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        //ExperimentView newContentPane = new ExperimentView();
        this.setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
		
		
		
		
    }

    public void runGui() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}