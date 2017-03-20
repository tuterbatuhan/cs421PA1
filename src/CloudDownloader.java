import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Base64;

public class CloudDownloader
{

	public static void main(String[] args) throws Exception
	{
		String authString_index = args[1];
		String asB64 = Base64.getEncoder().encodeToString(authString_index.getBytes());
		Socket clientSocket = GET(args[0],asB64,0,0,false);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String answer = inFromServer.readLine();
		if(!answer.contains("200")){
			System.out.println("Error! \nResponse Code : " + answer);
			return;
		}
		while((answer = inFromServer.readLine())!=null){
			if(answer.contains("Content-Type:")){
				answer = inFromServer.readLine();
				break;
			}
		}
		String fileName = inFromServer.readLine();
		File file= new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		int totalBytes=Integer.parseInt(inFromServer.readLine());
		int byteCounter=0;
		Socket tempSocket = null;
		String inputLine;
		
		System.out.println("URL of the index file: " + args[0]);
		System.out.println("File size is " + totalBytes + " Bytes");

		//baþtan indexteki her deðeri bi kaydediyorum. sýrf "There are 2 servers in the index" yazýsýný istediði için.
		int maxServerNumber = 10;//2den fazla olursa diye 10 dedim garanti olsun diye. daha az rastgele biþey de yapabiliriz
		String[] urlString = new String[maxServerNumber];
		String[] auth = new String[maxServerNumber];
		int[] bytesFrom = new int[maxServerNumber];
		int[] bytesTo = new int[maxServerNumber];
		int i = 0;
		while(true){
			urlString[i] = inFromServer.readLine();
			auth[i] = Base64.getEncoder().encodeToString(inFromServer.readLine().getBytes());
			String[] str = inFromServer.readLine().split("-");
			bytesFrom[i] = Integer.parseInt(str[0]);
			bytesTo[i] = Integer.parseInt(str[1]);
			
			if(bytesTo[i]==totalBytes)
				break;
			i++;
		}
		i = 0;
		System.out.println("Index file is downloaded");
		boolean finished = false;
		while(true){
			if(bytesFrom[i]!=0)
				i++;
			
			break;
		}
		System.out.println("There are " + (i+1) + " servers in the index");
		i=0;

		while(!finished){
			tempSocket = GET(urlString[i],auth[i],byteCounter+2-bytesFrom[i],bytesTo[i],true);
			System.out.println("Connected to " + urlString[i]);
			BufferedReader tempReader = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
			String tempAnswer = tempReader.readLine();
			if(!tempAnswer.contains("200")){
				//System.out.println("Response Code : " + tempAnswer);
			}
			
			while((tempAnswer = tempReader.readLine())!=null){
				if(tempAnswer.contains("Content-Type:")){
					answer = tempReader.readLine();
					break;
				}
			}
			
			InputStream inputStream = tempSocket.getInputStream();
			int data = inputStream.read();
			
			
			//---------yapýlacak------------
/*			while(data != -1) {
				  //do something with data...
				  fos.write(data);
				  data = inputStream.read();
				}
			inputStream.close();	*/
			while((tempAnswer = tempReader.readLine())!=null){
				System.out.println(tempAnswer);
			}
			//-------------------------------
			
			System.out.println("Downloaded bytes "+(byteCounter+1)+" to "+bytesTo[i] + " (size = " + (bytesTo[i]-(byteCounter)) + ")");
			byteCounter = bytesTo[i];
			i++;
			if(byteCounter==totalBytes){
				finished = true;
				System.out.println("Download of the file is complete (size = " + totalBytes + ")");
			}
		}
		fos.close();
		clientSocket.close();
		return;
	}
	private static Socket GET(String URL, String auth, int lowerBound, int upperBound, Boolean boundCheck) throws Exception{
		String IP = URL.split("/")[0];
		String FileName = URL.split("/")[1];
		Socket clientSocket = new Socket(IP, 80);
		
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes("GET /"+FileName+" HTTP/1.1\n");
		outToServer.writeBytes("Host: "+IP+"\n");
		outToServer.writeBytes("Authorization: basic " + auth+"\n");
		if(boundCheck)
			outToServer.writeBytes("Range: bytes=" + lowerBound +"-" +upperBound +"\r\n");
		outToServer.writeBytes("\r\n");
		return clientSocket;
	}
}
