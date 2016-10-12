

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * class is used for displaying images that have been
 * saved to the folder user specifies.
 * @author Adam O'Connor, Keith Quinn, Andrew Connolly
 *
 */
public class ImageList {

    private static JPanel gui;
    private static JFileChooser fileChooser;
    static FilenameFilter fileNameFilter;
    private static JMenuBar menuBar;
    @SuppressWarnings("rawtypes")
	static DefaultListModel model; 

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * setting up the gui for the user.
     */
	public ImageList() {
        gui = new JPanel(new GridLayout());

        JPanel imageViewContainer = new JPanel(new GridBagLayout());
        final JLabel imageView = new JLabel();
        imageViewContainer.add(imageView);

        model = new DefaultListModel(); 
        final JList imageList = new JList(model);
        imageList.setCellRenderer(new IconCellRenderer());
        ListSelectionListener listener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                Object o = imageList.getSelectedValue();
                if (o instanceof BufferedImage) {
                    imageView.setIcon(new ImageIcon((BufferedImage)o));
                }
            }

        };
        imageList.addListSelectionListener(listener);
        
       // myDirectory("sas");
        
        gui.add(new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, 
                new JScrollPane(
                        imageList, 
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), 
                new JScrollPane(imageViewContainer)));
    }
    
    /**
     * access to the directory that has been used to save images.
     * @param dir
     */
    public void myDirectory(File dir) {
    	
    	fileChooser = new JFileChooser();
        String[] imageTypes = ImageIO.getReaderFileSuffixes();
        FileNameExtensionFilter fnf = new FileNameExtensionFilter("Images", imageTypes);
        fileChooser.setFileFilter(fnf);
        fileChooser.setCurrentDirectory(dir);

        fileNameFilter = new FilenameFilter() {
            @Override 
            public boolean accept(File file, String name) {
                return true;
            }
        };

        menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem browse = new JMenuItem("Browse");
        menu.add(browse);
        browse.addActionListener(new ActionListener(){
            @Override 
            public void actionPerformed(ActionEvent ae) {
                int result = fileChooser.showOpenDialog(gui);
                if (result==JFileChooser.APPROVE_OPTION) {
                    File eg = fileChooser.getSelectedFile();
                    // this will be an image, we want the parent directory
                    File dir = eg.getParentFile();
                    try {
                        loadImages(dir);
                    } catch(Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                gui, 
                                e, 
                                "Load failure!", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
      
    }

    @SuppressWarnings("unchecked")
    /**
     * loading the images to display on the main gui.
     * @param directory
     * @throws IOException
     */
	public static void loadImages(File directory) throws IOException {
        File[] imageFiles = directory.listFiles(fileNameFilter);
        BufferedImage[] images = new BufferedImage[imageFiles.length];
        model.removeAllElements();
        for (int ii=0; ii<images.length; ii++) {
            model.addElement(ImageIO.read(imageFiles[ii]));
        }
    }

    public Container getGui() {
        return gui;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * used to run the ImageList class
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                ImageList imageList = new ImageList();

                JFrame f = new JFrame("Image Browser");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.add(imageList.getGui());
                f.setJMenuBar(imageList.getMenuBar());
                f.setLocationByPlatform(true);
                f.pack();
                f.setSize(800,600);
                f.setVisible(true);
            }
        });
    }
}

class IconCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    private int size;
    private BufferedImage icon;

    IconCellRenderer() {
        this(100);
    }

    IconCellRenderer(int size) {
        this.size = size;
        icon = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public Component getListCellRendererComponent(
            @SuppressWarnings("rawtypes") 
            JList list, 
            Object value, 
            int index, 
            boolean isSelected, 
            boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (c instanceof JLabel && value instanceof BufferedImage) {
            JLabel l = (JLabel)c;
            l.setText("");
            BufferedImage i = (BufferedImage)value;
            l.setIcon(new ImageIcon(icon));

            Graphics2D g = icon.createGraphics();
            g.setColor(new Color(0,0,0,0));
            g.clearRect(0, 0, size, size);
            g.drawImage(i,0,0,size,size,this);

            g.dispose();
        }
        return c;
    }

    @Override 
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }
}

