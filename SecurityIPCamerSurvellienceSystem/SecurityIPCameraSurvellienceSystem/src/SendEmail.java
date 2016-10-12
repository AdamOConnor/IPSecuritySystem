import java.io.IOException;
import java.util.Properties;  
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;  
import javax.mail.internet.*; 

/**
 * 
 * @author Adam O'Connor, Andrew Connolly, Keith Quinn
 * used for sending emails on detection by the application,
 *
 */

public class SendEmail extends Thread {
	private String sendMailToAddress ="adam-o-connor@hotmail.com";	
	private Properties props = new Properties(); 
	private Session session;
	private String filename;
	String dir;
	FileHandler fh;
	private static Logger logger = Logger.getLogger(SendEmail.class.getName());
	
	/**
	 * setting email address
	 * @param email
	 */
	public void setEmailAddress(String email) {
		sendMailToAddress = email;
	}
	
	/**
	 * setting attachment
	 * @param filename
	 */
	public void setAttachment(String filename) {
		this.filename = filename;
	}
	
	/**
	 * getting the attachment
	 * @return string
	 */
	public String getAttachment() {
		return filename;
	}
	
	/**
	 * get the email address.
	 * @return string
	 */
	public String getEmailAddress() {
		return sendMailToAddress;
	}
		
	/**
	 * running the mail jar to send email's.
	 */
	public void run() {
		props.put("mail.smtp.host", "smtp.gmail.com");  
		props.put("mail.smtp.socketFactory.port", "465");  
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  
		props.put("mail.smtp.auth", "true");  
		props.put("mail.smtp.port", "465"); 
		synchronized (this) {
			//####// LOGIN_TO_ACCOUNT //####
			session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {  
				protected PasswordAuthentication getPasswordAuthentication() {  
				   return new PasswordAuthentication("b00073701itb@gmail.com","b00073701"); 
				}  
			}); 
			
			//#############// send from to	//#######################
			try {  
			   MimeMessage message = new MimeMessage(session);  
			   message.setFrom(new InternetAddress("b00073701itb@gmail.com"));//change accordingly  
			   message.addRecipient(Message.RecipientType.TO,new InternetAddress(getEmailAddress()));  
			   message.setSubject("Alert from Camera");  
			  // message.setText("Motion Detected");  
			  // message.ATTACHMENT.equals(getAttachment());
			   
			   BodyPart messageBodyPart = new MimeBodyPart();

		         // Now set the actual message
		         messageBodyPart.setText("Motion has been detected on your camera!!!");

		         // Create a multipar message
		         Multipart multipart = new MimeMultipart();

		         // Set text message part
		         multipart.addBodyPart(messageBodyPart);

		         // Part two is attachment
		         messageBodyPart = new MimeBodyPart();
		         DataSource source = new FileDataSource(getAttachment());
		         messageBodyPart.setDataHandler(new DataHandler(source));
		         messageBodyPart.setFileName(getAttachment());
		         multipart.addBodyPart(messageBodyPart);

		         // Send the complete message parts
		         message.setContent(multipart);

		         // Send message
	   
	     
			   //send message & log into text file! 
			   Transport.send(message);  		  
			   System.out.println("message sent successfully"); 
			   Logger.getLogger("logfile.txt");// new line
			   fh = new FileHandler(getAttachment()); 
			   logger.addHandler(fh);
			   SimpleFormatter formatter = new SimpleFormatter();  
			   fh.setFormatter(formatter);  
			   logger.addHandler(fh);     
			   logger.log(Level.INFO, "MESSAGE_EMAIL_SENT_STATUS: " + " Message sent successfully");	
			   logger.info("my log info");			   
			  }
			  catch (RuntimeException | MessagingException | IOException e) {
				  
			  } 
				logger.info("my log info");
		}//synchronize
	}
}
