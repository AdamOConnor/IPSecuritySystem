import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
/**
 * 
 * @author Adam O'Connor, Andrew Connolly, Keith Quinn
 *
 *	class used to send images that have been detected up to website by ftp.
 */
public class URLConnectionReader {
	public static void main(String[] argv) {
		try {
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
	
