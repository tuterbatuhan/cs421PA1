//
//
// http://www.avajava.com/tutorials/lessons/how-do-i-connect-to-a-url-using-basic-authentication.html
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class CloudDownloader
{

	public static void main(String[] args) throws IOException
	{
		String urlString = args[0];
		String userName = args[1];
		String password = args[2];
		String authString = userName + ":" + password;
		String asB64 = Base64.getEncoder().encodeToString(authString.getBytes());
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic " + asB64);
		int responseCode = connection.getResponseCode();
		if(responseCode!=200){
			System.out.println("Response Code : " + responseCode);
			return;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		File file=null;
		FileOutputStream fos=null;
		int totalBytes=0;
		Boolean finished = false;
		if((inputLine = in.readLine()) != null)
			file = new File(inputLine);
		if((inputLine = in.readLine()) != null)
			totalBytes = Integer.parseInt(inputLine);
		//System.out.println(totalBytes);
		int byteCounter=0;
		fos = new FileOutputStream(file);
		while(!finished){
			String urlString2 = in.readLine();
			String asB642 = Base64.getEncoder().encodeToString(in.readLine().getBytes());
			Scanner s = new Scanner(in.readLine());
			s.useDelimiter("-");
			int bytesFrom = s.nextInt();
			int bytesTo = s.nextInt();
			URL url2 = new URL(urlString);
			HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
			connection2.setRequestMethod("GET");
			connection2.setRequestProperty("Authorization", "Basic " + asB642);
			connection2.setRequestProperty("Range", "Bytes="+byteCounter+1+"-"+bytesTo);
			int responseCode2 = connection.getResponseCode();
			if(responseCode2!=200){
				System.out.println("Response Code 2 : " + responseCode2);
				return;
			}
			
			byteCounter = bytesTo;
			
			InputStream inputStream = connection2.getInputStream();
			int data = inputStream.read();
			while(data != -1) {
				  //do something with data...
				  fos.write(data);
				  data = inputStream.read();
				}
			inputStream.close();
			if(byteCounter==totalBytes)
				finished = true;
		}
		fos.close();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine+	"\n");
		}
		in.close();
		System.out.println(response.toString());
		
	}

}
