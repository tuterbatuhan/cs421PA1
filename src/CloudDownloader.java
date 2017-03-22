import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
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
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));	
		int totalBytes=Integer.parseInt(inFromServer.readLine());
		int byteCounter=0;
		Socket tempSocket = null;
		
		System.out.println("URL of the index file: " + args[0]);
		System.out.println("File size is " + totalBytes + " Bytes");

		int maxServerNumber = 10;
		ArrayList<String> urlString = new ArrayList<>(); 
		ArrayList<String> auth = new ArrayList<>();
		ArrayList<Integer> bytesFrom = new ArrayList<>();
		ArrayList<Integer> bytesTo = new ArrayList<>();
		int fileCounter = 0;
		while(true){
			urlString.add(inFromServer.readLine());
			auth.add(Base64.getEncoder().encodeToString(inFromServer.readLine().getBytes()));
			String[] str = inFromServer.readLine().split("-");
			bytesFrom.add(Integer.parseInt(str[0]));
			bytesTo.add(Integer.parseInt(str[1]));
			
			if(bytesTo.get(fileCounter)==totalBytes)
				break;
			fileCounter++;
		}
		System.out.println("Index file is downloaded");
		System.out.println("There are " + (fileCounter+1) + " servers in the index");
		for (int i=0;i<=fileCounter;i++){
			tempSocket = GET(urlString.get(i),auth.get(i),byteCounter+1-bytesFrom.get(i),bytesTo.get(i),true);
			System.out.println("Connected to " + urlString.get(i));
			BufferedReader tempReader = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
			String tempAnswer = tempReader.readLine();
			while((tempAnswer = tempReader.readLine())!=null){
				if(tempAnswer.contains("Content-Type:")){
					tempReader.readLine();
					break;
				}
			}			
			int bite = tempReader.read();
			while(bite != -1){
				fos.write(bite);
				bite = tempReader.read();
			}	
			System.out.println("Downloaded bytes "+(byteCounter+1)+" to "+bytesTo.get(i) + " (size = " + (bytesTo.get(i)-(byteCounter)) + ")");
			byteCounter = bytesTo.get(i);
			if(byteCounter==totalBytes){
				System.out.println("Download of the file is complete (size = " + totalBytes + ")");
			}
		}
		bw.close();
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