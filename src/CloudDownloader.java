
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class CloudDownloader
{

	public static void main(String[] args) throws Exception
	{
		String authString = args[1];
		String asB64 = Base64.getEncoder().encodeToString(authString.getBytes());
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
		Boolean finished = false;
		int byteCounter=0;
		Socket tempSocket = null;
		String inputLine;
		while(!finished){
			String urlString2 = inFromServer.readLine();
			//System.out.println("ANAAAAN "+urlString2);
			String asB642 = Base64.getEncoder().encodeToString(inFromServer.readLine().getBytes());
			int bytesTo = Integer.parseInt(inFromServer.readLine().split("-")[1]);
			System.out.println("\n BytesFrom: " + (byteCounter+1) + " BytesTo: " + bytesTo + "\n");
			tempSocket = GET(urlString2,asB642,byteCounter+1,bytesTo,true);
			BufferedReader tempReader = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
			String tempAnswer = tempReader.readLine();
			if(!tempAnswer.contains("200")){
				//System.out.println("Response Code : " + tempAnswer);
			}
			System.out.println("Writing the bytes from "+(byteCounter+1)+" to "+bytesTo);
			byteCounter = bytesTo;
			while((tempAnswer = tempReader.readLine())!=null){
				System.out.println(tempAnswer);
			}
//			InputStream inputStream = tempSocket.getInputStream();
//			int data = inputStream.read();
//			
//			while(data != -1) {
//				  //do something with data...
//				  fos.write(data);
//				  data = inputStream.read();
//				}
//			inputStream.close();
			if(byteCounter==totalBytes)
				finished = true;
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
			outToServer.writeBytes("Range: bytes=" + lowerBound +"-" +upperBound +"\n");
		outToServer.writeBytes("\r\n");
		return clientSocket;
	}
}
