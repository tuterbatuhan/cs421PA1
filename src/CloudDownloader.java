//
//
// http://www.avajava.com/tutorials/lessons/how-do-i-connect-to-a-url-using-basic-authentication.html
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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
		
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine+"\n");
		}
		in.close();
		System.out.println(response.toString());
		
	}

}
