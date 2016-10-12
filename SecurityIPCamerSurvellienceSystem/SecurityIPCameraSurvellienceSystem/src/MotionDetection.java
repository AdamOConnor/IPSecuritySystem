import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 *	This application is for the intented use of a Third Year Project 
 *	studying computer science for a Bachelors Degree in computing,
 *	Project is a security camera system that detects unwanted visitors or movement,
 *	that connects to any streaming IP camera. 
 *	sends email notifications to the user when they enter their email address.
 *	
 *	Website to view : http://mgp2016.netau.net/index.html
 *
 *	Streaming URL's :
 *
 *			Stream always accessible - http://c-cam.uchicago.edu/mjpg/video.mjpg");
 *			Locally accessible if opencv installed - C:\\OpenCv\\opencv\\sources\\samples\\gpu\\768x576.avi
 *			Local network Adams cam - http://192.168.192.27:8080/?action=stream?resolution=640x480&.mjpg
 *			Outside network Adams cam - http://80.111.38.87:8080/?action=stream?resolution=640x480&.mjpg
 *			
 *
 *	Project Supervisor Dr Simon Mcloughlin
 *
 * 	@author Adam O'Connor, Andrew Connolly, Keith Quinn
 */
@SuppressWarnings("serial")
public class MotionDetection extends javax.swing.JFrame {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	static Mat imag = null;
	@SuppressWarnings("unused")
	private VideoCapture camera = null;
	private static boolean notify = false;
	static boolean done;
	private static String emailAddress;
	private static File file = null;
	private static File dir = null;
	private static int imageSave = 0;
	private static int sendEmail = 0;
	private static int setSaveImage = 10;
	private static int setSendEmail = 15;
	private static int sensitivity = 3000;


	/**
	 * Creates new form MotionFrame
	 */
	public MotionDetection() {
		initComponents();
		setSize(1000, 600);
		setVisible(true); 

		String ipCameraStream = JOptionPane.showInputDialog(
				this, 
				"enter the stream URL Please", 
				"Motion Detection Detector", 
				JOptionPane.INFORMATION_MESSAGE
				);

		JOptionPane.showMessageDialog(this, "Please wait while the stream loads \n"
				+ "thanks for your patience.");

		if(ipCameraStream == null || ipCameraStream.length() == 0) {
			System.exit(0);
			JOptionPane.getRootFrame().dispose();   
		}
		else {
			streamCamDetection(ipCameraStream);
		}

	}

	/**
	 * method for getting the video stream and to display.
	 * @param streamUrl
	 */
	private void streamCamDetection(String streamUrl) {
		try {
			//current frame
			Mat frame = new Mat();
			// changing the colour's of frame display
			Mat change_frame = new Mat();
			// different frame to check
			Mat diff_frame = null;
			// temporary frame to distinguish between detections
			Mat temporary_frame = null;
			ArrayList<Rect> array = new ArrayList<Rect>();
			
			// access the stream url of camera to show stream of camera.
			VideoCapture camera = new VideoCapture(streamUrl);
			
			Size setSize = new Size(640, 480);
			int i = 0;

			while (true) {
				if (camera.read(frame)) {
					// resize the current frame to display
					Imgproc.resize(frame, frame, setSize);
					// clone the frame and sent to static imag used for saving the images.
					imag = frame.clone();
					//errors compiling because size not set CV_8UC1 is for setting the channels of the
					//matrix that is to be computed
					change_frame = new Mat(frame.size(), CvType.CV_8UC1);
					// changes the colour's of current frame and saves to change_frame
					//colour's from color to gray.
					Imgproc.cvtColor(frame, change_frame, Imgproc.COLOR_BGR2GRAY);
					// blur image as larger space of detection
					Imgproc.GaussianBlur(change_frame, change_frame, new Size(3, 3), 0);

					if (i == 0) {
						// start the frame detection print frame to screen.
						setSize(frame.width(), frame.height());
						// diff_frame is the compare to frame
						diff_frame = new Mat(change_frame.size(), CvType.CV_8UC1);
						// temporary_frame is the compared against
						temporary_frame = new Mat(change_frame.size(), CvType.CV_8UC1);
						// set the compare to frame as the same as the change frame.
						diff_frame = change_frame.clone();
					}

					if (i == 1) {
						// check the frames in question to the temporary_frame
						Core.subtract(change_frame, temporary_frame, diff_frame);
						// change to the binary version of the images.
						Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,
								Imgproc.ADAPTIVE_THRESH_MEAN_C,
								Imgproc.THRESH_BINARY_INV, 5, 2);
						// check to see if the binary version is greater than 0
						// if > 0 something is different in the frame.
						array = detection_contours(diff_frame);
						if (array.size() > 0) {
							//if same image is true start saving images.
							if(saveImage == true) {
								imageSave++;
								// save the image
								savingFile(imag);
								// upload image to website
								uploadFile(imag);

							}
							if(notify == true) {
								sendEmail++;
								// send email.
								sendEmail();
							}
							Iterator<Rect> checkFrame = array.iterator();
							while (checkFrame.hasNext()) {
								Rect obj = checkFrame.next();
								// draw rect around the detection
								Core.rectangle(imag, obj.br(), obj.tl(),
										new Scalar(0, 255, 0), 1);
							}
						}
					}

					i = 1;
					// display the stream to the user
					ImageIcon image = new ImageIcon(Mat2bufferedImage(imag));
					videoLabel.setIcon(image);
					videoLabel.repaint();
					temporary_frame = change_frame.clone();
				}		
				setSize(1000, 600);
			}  
		}catch(IOException e)
		{
			JOptionPane.showMessageDialog(this, "This is not a correct url stream.");
			System.exit(0);
		}
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 * made using netbeans gui creator
	 */

	private void initComponents() {

		videoPanel = new javax.swing.JPanel();
		videoLabel = new javax.swing.JLabel();
		buttonPanel = new javax.swing.JPanel();
		saveToggleButton = new javax.swing.JToggleButton();
		savingImage = new javax.swing.JLabel();
		showImagesButton = new javax.swing.JButton();
		emailToggle = new javax.swing.JToggleButton();
		menuBar = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		settingsItem = new javax.swing.JMenuItem();
		imagesDestinationItem = new javax.swing.JMenuItem();
		informationMenu = new javax.swing.JMenu();
		informationItem = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		videoPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout videoPanelLayout = new javax.swing.GroupLayout(videoPanel);
		videoPanel.setLayout(videoPanelLayout);
		videoPanelLayout.setHorizontalGroup(
				videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(videoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
				);
		videoPanelLayout.setVerticalGroup(
				videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(videoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);

		buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		saveToggleButton.setText("Save Images");
		saveToggleButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveToggleButtonActionPerformed(evt);
			}
		});

		showImagesButton.setText("Show Images");
		showImagesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				showImagesButton(evt);
			}
		});

		emailToggle.setText("Email Notifications");
		emailToggle.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				emailToggleActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setHorizontalGroup(
				buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(buttonPanelLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(buttonPanelLayout.createSequentialGroup()
										.addComponent(saveToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(37, 37, 37)
										.addComponent(savingImage, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addComponent(showImagesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(emailToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap(53, Short.MAX_VALUE))
				);

		buttonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {emailToggle, saveToggleButton, showImagesButton});

		buttonPanelLayout.setVerticalGroup(
				buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(buttonPanelLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(saveToggleButton)
								.addComponent(savingImage, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(showImagesButton)
						.addGap(29, 29, 29)
						.addComponent(emailToggle)
						.addContainerGap(335, Short.MAX_VALUE))
				);

		fileMenu.setText("File");

		settingsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		settingsItem.setText("Settings");
		settingsItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				settingsItemActionPerformed(evt);
			}
		});
		fileMenu.add(settingsItem);

		imagesDestinationItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
		imagesDestinationItem.setText("Images destination");
		imagesDestinationItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				imagesDestinationItemActionPerformed(evt);
			}
		});
		fileMenu.add(imagesDestinationItem);

		menuBar.add(fileMenu);

		informationMenu.setText("Information");

		informationItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.SHIFT_MASK));
		informationItem.setText("Information");
		informationItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				informationItemActionPerformed(evt);
			}
		});
		informationMenu.add(informationItem);

		menuBar.add(informationMenu);

		setJMenuBar(menuBar);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(videoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGap(23, 23, 23))
				);

		pack();
	}// </editor-fold>                        

	/**
	 * saving images button make sure directory is set by the user.
	 * @param evt
	 */
	private void saveToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
		if(saveToggleButton.isSelected()){
			if(dir == null) {
				JFileChooser chooser;
				String choosertitle = "Choose Folder";
				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(dir);
				chooser.setDialogTitle(choosertitle);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
			
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 

					dir = chooser.getCurrentDirectory();
					file = chooser.getSelectedFile();

					saveImage = true;
					ImageIcon loadingIcon = new ImageIcon("loading.png");
					savingImage.setIcon(loadingIcon);
				}
				else {
					JOptionPane.showMessageDialog(this, "No selection has been chosen.");
					saveToggleButton.setSelected(false);
				}
			}
			else{
				saveImage = true;
				ImageIcon loadingIcon = new ImageIcon("loading.png");
				savingImage.setIcon(loadingIcon);
			}

		}
		else
			if(!saveToggleButton.isSelected()) {
				saveImage = false;
				savingImage.setIcon(null);
			}

	}                                              

	/**
	 * set the image destination for the detected images.
	 * @param evt
	 */
	private void imagesDestinationItemActionPerformed(java.awt.event.ActionEvent evt) {                                                      
		// TODO add your handling code here:
		JFileChooser chooser;
		String choosertitle = "Choose Folder";
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(dir);
		chooser.setDialogTitle(choosertitle);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		    
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			dir = chooser.getCurrentDirectory();
			file = chooser.getSelectedFile();
		}
		else {
			JOptionPane.showMessageDialog(this, "No selection has been chosen.");
		}
	}  

	/**
	 * check to see if users email address is valid
	 * return true if is correct.
	 * @param email
	 * @return boolean
	 */
	
	public static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	/**
	 * method is to instantiate the users email address 
	 * so that it can send the detection and image.
	 * @param evt
	 */
	private void emailToggleActionPerformed(java.awt.event.ActionEvent evt) {                                            
		// TODO add your handling code here:
		
		if(emailToggle.isSelected() ) {
			if(emailAddress == null)
			{
				emailAddress = JOptionPane.showInputDialog(
						this, 
						"please enter your email address", 
						"Email Address", 
						JOptionPane.INFORMATION_MESSAGE
						);
				notify = false;
			}
			if(emailAddress != null){
				// check if email address is valid.
				boolean checkEmail = isValidEmailAddress(emailAddress);
				if(checkEmail == false) {
					JOptionPane.showMessageDialog(this, "This is not a valid email Address.");
					emailAddress = JOptionPane.showInputDialog(
							this, 
							"please enter your email address", 
							"Email Address", 
							JOptionPane.INFORMATION_MESSAGE
							);
				}
				if(checkEmail == true && saveImage == true)
				{

					JOptionPane.showMessageDialog(this, "Email Address is valid \n"
							+ "notifications will now be \n"
							+ "sent upon detection.");
					notify = true;
				}
				else if(saveImage == false && checkEmail == false){
					notify = false;
					JOptionPane.showMessageDialog(this, "This is not a valid email Address \n"
							+ "and images are not set to save.");
					emailToggle.setSelected(false);
				}
				else 
					if(saveImage == false) {
						JOptionPane.showMessageDialog(this, "images are not set to save. \n"
								+ "Please save images");
						emailToggle.setSelected(false);
						notify = false;
					}
					else
						if(checkEmail == false) {
							JOptionPane.showMessageDialog(this, "This is not a valid email Address.");
							emailToggle.setSelected(false);
							notify = false;
						}
			}
		}
		if(!emailToggle.isSelected()) {
			notify = false;
		}
	}   
	
	/**
	 * display settings to control the sensitivity of the motion detection
	 * and the send email and take image images settings.
	 * @param evt
	 */

	private void settingsItemActionPerformed(java.awt.event.ActionEvent evt) {                                             
		// TODO add your handling code here:
		SampleDialog thisdialog = new SampleDialog();
		thisdialog.setModal(true);
		thisdialog.setVisible(true);
	}                                                                                        

	/**
	 * information about the project that has been created
	 * for the degree in computer science
	 * @param evt
	 */
	
	private void informationItemActionPerformed(java.awt.event.ActionEvent evt) {                                                
		// TODO add your handling code here:
		ImageIcon icon = new ImageIcon("information.gif");
		JOptionPane.showMessageDialog(this,
				"information about this program.\n"
						+"Is intented for year 3 studying for\n"
						+ "a Bsc in computing. The project \n"
						+ "is used for the the security of \n"
						+ " the users home and to retrieve \n"
						+ "updates of any detections that  \n"
						+ "have been seen in the area of \n"
						+ "operation.\n"
						+ "\n"
						+ "       Major Group project \n"
						+ "               3rd Year \n"
						+ " Adam O'Connor-B00066540    \n"
						+ "Andrew Connolly-B00069517    \n"
						+ "    Keith Quinn-B00073701 ",
						"Information",
						JOptionPane.INFORMATION_MESSAGE,
						icon);
	}                                               

	/**
	 * invoke the show images class to display all the images 
	 * that have been sent into the folder specified by the user
	 * @param evt
	 */
	private void showImagesButton(java.awt.event.ActionEvent evt) {                                         
		// TODO add your handling code here:

		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				ImageList imageList = new ImageList();
				imageList.myDirectory(file);
				JFrame f = new JFrame("Image Browser");
				f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				f.add(imageList.getGui());
				f.setJMenuBar(imageList.getMenuBar());
				f.setLocationByPlatform(true);
				f.pack();
				f.setSize(1000,600);
				f.setVisible(true);
			}
		});        // TODO 
	}                                        
	
	/**
	 * get the bytes of the images stream in bytes and display on the 
	 * gui for user's to view.	
	 * @param image
	 * @return
	 */
	public static BufferedImage Mat2bufferedImage(Mat image) {
		// get the byte mat for stream
		MatOfByte bytemat = new MatOfByte();
		// encode to .jpg
		Highgui.imencode(".jpg", image, bytemat);
		// set byte array of bytes
		byte[] bytes = bytemat.toArray();
		// get the byte stream
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		try {
			// send the image to display.
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}

	/**
	 * gets the detection and compute's the information to
	 * display the contours and send the mat object back out
	 * when the detection is of a certain size.
	 * 
	 * @param outmat
	 * @return
	 */
	public static ArrayList<Rect> detection_contours(Mat outmat) {
		// clone the mat to see if anything has changed.
		Mat v = new Mat();
		Mat vv = outmat.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		//apply detects objects moving and produces a foreground mask
		Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

		double maxArea = 100;
		int maxAreaIdx = -1;
		Rect r = null;
		//array to hold detections
		ArrayList<Rect> rect_array = new ArrayList<Rect>();

		for (int idx = 0; idx < contours.size(); idx++) {
			Mat contour = contours.get(idx);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				maxAreaIdx = idx;
				//on detection draw green box around detection
				r = Imgproc.boundingRect(contours.get(maxAreaIdx));

				// user specified sensitivity of detection only detects objects of the size 
				// indicated
				if(r.area() > sensitivity ) {
					// add detection to list
					rect_array.add(r);
				}
			}
		}

		v.release();

		return rect_array;
	}
	
	/**
	 * time stamp for each image name.
	 * @return
	 */
	
	public static String getCurrentTimeStamp()
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}
	
	/**
	 * saving specific image upon detection
	 * save in file specified by user and set the current time and date.
	 * @param saveImage
	 * @throws IOException
	 */

	public static void savingFile(Mat saveImage) throws IOException {
		if(imageSave == setSaveImage)
		{
			String timeStamp = getCurrentTimeStamp();   
			System.out.println(dir);
			String filename = file + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg";
			System.out.println("Saving results in: " + filename); 
			System.out.println(setSaveImage);
			// set the timestamp on the images
			Core.putText(saveImage, ""+timeStamp, new Point(saveImage.rows()/4,saveImage.cols()/14), Core.FONT_HERSHEY_TRIPLEX,new Double(1),new  Scalar(255));
			Highgui.imwrite(filename, saveImage);
			//reset image saving
			imageSave = 0;
			
			String server = "mgp2016.netau.net";//my server
			int port = 21;
			String user = "a5047433";//server usernae
			String pass = "oldhead12";//password

			FTPClient ftpClient = new FTPClient();
			try {

				ftpClient.connect(server, port);//connest to the server
				ftpClient.login(user, pass);//provide login info from above
				ftpClient.enterLocalPassiveMode();

				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				//fileto be uploaded
				File firstLocalFile = new File(file + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg");

				String firstRemoteFile = "public_html/"+ getCurrentTimeStamp()+ ".jpg";//uploaded there
				InputStream inputStream = new FileInputStream(firstLocalFile);
				//uploading files
				System.out.println("Start uploading first file" + getCurrentTimeStamp());
				try{
					done = ftpClient.storeFile(firstRemoteFile, inputStream);
					inputStream.close();
				}catch(Exception e) {

				}

				if (done) {
					System.out.println("The first file is uploaded successfully From here.");
					new URLConnectionReader();
				}

			} catch (Exception ex) {
				System.out.println("Error: " + ex.getMessage());
				ex.printStackTrace();
			} finally {
				try {
					if (ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				try {
					System.out.println("HIT HERE");
					  URL phpUrl = new URL("http://mgp2016.netau.net/pushtest.php");
					  URLConnection urlCon = phpUrl.openConnection();
					  BufferedReader br = new BufferedReader(
					                          new InputStreamReader(
					                          urlCon.getInputStream()));
					  String line;

					  while ((line = br.readLine()) != null) 
					      System.out.println(line);
					  br.close();
					} catch(Exception e) {
					  // handle errors here...
					}
			}
		}
			

	}


	
	/**
	 * sending email method sends notification to the email address entered when the program 
	 * is being run.
	 * 
	 * Saving images must be true as well as the certain counter for the sending email.
	 * @throws IOException
	 */

	public void sendEmail() throws IOException {

		if(sendEmail == setSendEmail & saveImage == true) {
			SendEmail alertEmail = new SendEmail();
			alertEmail.setEmailAddress(emailAddress);
			// gets the email address to send message to.
			// gets the current filename and sends to the SendEmail class.
			String filename = file + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg";
			alertEmail.setAttachment(filename);
			alertEmail.start();
			// reset counter
			sendEmail = 0;
		}
	}

	/**
	 * main to run the project.
	 * @param args the command line arguments
	 */
	public static void main(String args[]) throws FileNotFoundException, IOException {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(MotionDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(MotionDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(MotionDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MotionDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		@SuppressWarnings("unused")
		MotionDetection thisFrame =  new MotionDetection();

	}

	/**
	 * uploads the detected images by ftp to a website that the user can 
	 * view. live stream can be viewed here also.
	 * @param upload
	 * @throws FileNotFoundException
	 */
	public static void uploadFile(Mat upload) throws FileNotFoundException
	{
		if(imageSave == setSaveImage)
		{
			String server = "mgp2016.netau.net";//my server
			int port = 21;
			String user = "a5047433";//server username
			String pass = "oldhead12";//password

			FTPClient ftpClient = new FTPClient();
			try {

				ftpClient.connect(server, port);//connest to the server
				ftpClient.login(user, pass);//provide login info from above
				ftpClient.enterLocalPassiveMode();

				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				//fileto be uploaded
				File firstLocalFile = new File(file + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg");

				String firstRemoteFile = "public_html/admin/"+ getCurrentTimeStamp()+ ".jpg";//uploaded there
				InputStream inputStream = new FileInputStream(firstLocalFile);
				//uploading files
				System.out.println("Start uploading first file" + getCurrentTimeStamp());
				try{
					done = ftpClient.storeFile(firstRemoteFile, inputStream);
					inputStream.close();
				}catch(Exception e) {

				}

				if (done) {
					System.out.println("The first file is uploaded successfully.");
				}

			} catch (Exception ex) {
				System.out.println("Error: " + ex.getMessage());
				ex.printStackTrace();
			} finally {
				try {
					if (ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	/**
	 * 
	 * @author Adam O'Connor
	 * method displays the settings that the user might
	 * want to change.
	 * 
	 * such as sensitivity, email notifications, images save.
	 */
	private class SampleDialog extends JDialog
	{
		private SampleDialog()
		{
			setTitle("Settings");
			initComponents();
		}
		// setting up the gui
		private void initComponents() {
			settingsPanel = new javax.swing.JPanel();
			objectSizeChanger = new javax.swing.JSlider();
			objectSizeLabel = new javax.swing.JLabel();
			saveImageLabel = new javax.swing.JLabel();
			sendEmailLabel = new javax.swing.JLabel();
			saveImageSpinner = new javax.swing.JSpinner();
			sendEmailSpinner = new javax.swing.JSpinner();
			applyButton = new javax.swing.JButton();
			defaultButton = new javax.swing.JButton();

			settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));

			objectSizeChanger.setMaximum(8000);
			objectSizeChanger.setToolTipText("");
			objectSizeChanger.setValue(sensitivity);

			objectSizeLabel.setText("Sensitivity of detection");

			saveImageLabel.setText("save image after");

			sendEmailLabel.setText("send email after ");

			saveImageSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 50, 1));

			sendEmailSpinner.setModel(new javax.swing.SpinnerNumberModel(15, 0, 50, 1));

			javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
			settingsPanel.setLayout(settingsPanelLayout);
			settingsPanelLayout.setHorizontalGroup(
					settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(settingsPanelLayout.createSequentialGroup()
							.addContainerGap(13, Short.MAX_VALUE)
							.addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPanelLayout.createSequentialGroup()
											.addComponent(objectSizeLabel)
											.addGap(18, 18, 18)
											.addComponent(objectSizeChanger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addGap(56, 56, 56))
									.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPanelLayout.createSequentialGroup()
											.addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
													.addGroup(settingsPanelLayout.createSequentialGroup()
															.addComponent(sendEmailLabel)
															.addGap(18, 18, 18)
															.addComponent(sendEmailSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
													.addGroup(settingsPanelLayout.createSequentialGroup()
															.addComponent(saveImageLabel)
															.addGap(18, 18, 18)
															.addComponent(saveImageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
											.addGap(208, 208, 208))))
					);
			settingsPanelLayout.setVerticalGroup(
					settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(settingsPanelLayout.createSequentialGroup()
							.addGap(31, 31, 31)
							.addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
									.addComponent(objectSizeChanger, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(objectSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(saveImageLabel)
									.addComponent(saveImageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
							.addGap(18, 18, 18)
							.addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(sendEmailLabel)
									.addComponent(sendEmailSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
							.addContainerGap(59, Short.MAX_VALUE))
					);

			applyButton.setText("Apply");
			applyButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					applyButtonActionPerformed(evt);
				}
			});

			defaultButton.setText("Default");
			defaultButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					defaultButtonActionPerformed(evt);
				}
			});

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addContainerGap())
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
							.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(applyButton)
							.addGap(18, 18, 18)
							.addComponent(defaultButton)
							.addGap(30, 30, 30))
					);
			layout.setVerticalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addGap(24, 24, 24)
							.addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addGap(18, 18, 18)
							.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(applyButton)
									.addComponent(defaultButton))
							.addContainerGap(27, Short.MAX_VALUE))
					);

			pack();
		}// </editor-fold>                            

		/**
		 * gets the values of the spinners and slider and saves to specific value.
		 * @param evt
		 */
		private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {    

			Object sensitivitySlider;
			sensitivitySlider = objectSizeChanger.getValue();
			sensitivity = (int) sensitivitySlider;

			Object imageSpinner;
			imageSpinner = saveImageSpinner.getValue();
			setSaveImage = (int) imageSpinner;

			try {
				Object emailSpinner;
				emailSpinner = sendEmailSpinner.getValue();
				setSendEmail = (int) emailSpinner;
			}catch(Exception e){

			}
			setVisible(false);
			JOptionPane.showMessageDialog(this, "Settings have now been changed");
		}    

		/**
		 * setting the default values for each of the setting types.
		 * @param evt
		 */
		private void defaultButtonActionPerformed(java.awt.event.ActionEvent evt) { 

			sensitivity = 3000;
			objectSizeChanger.setValue(sensitivity);

			setSaveImage = 10;
			saveImageSpinner.setValue(setSaveImage);

			try {
				setSendEmail = 15;
				sendEmailSpinner.setValue(setSendEmail);
			}catch(Exception e){

			}
			setVisible(false);
			JOptionPane.showMessageDialog(this, "Settings have now been changed");
		}                                           

		/**
		 * @param args the command line arguments
		 */
		// Variables declaration - do not modify                     
		private javax.swing.JButton applyButton;
		private javax.swing.JButton defaultButton;
		private javax.swing.JSlider objectSizeChanger;
		private javax.swing.JLabel objectSizeLabel;
		private javax.swing.JLabel saveImageLabel;
		private javax.swing.JSpinner saveImageSpinner;
		private javax.swing.JLabel sendEmailLabel;
		private javax.swing.JSpinner sendEmailSpinner;
		private javax.swing.JPanel settingsPanel;


	}

















	// Variables declaration - do not modify                     
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenuItem imagesDestinationItem;
	private javax.swing.JMenuItem informationItem;
	private javax.swing.JMenu informationMenu;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JToggleButton saveToggleButton;
	private javax.swing.JLabel savingImage;
	private javax.swing.JMenuItem settingsItem;
	private javax.swing.JButton showImagesButton;
	private javax.swing.JPanel videoPanel;
	private javax.swing.JLabel videoLabel;
	private javax.swing.JToggleButton emailToggle;
	// End of variables declaration                   
	private Boolean saveImage = false;
}
