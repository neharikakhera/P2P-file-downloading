package Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import Chunk.ChunkFileObject;

/*
* Main class who breaks the file that needs to be distributed among its client's based on config file
* Once distribution is done it will exit from p2p network
*/
public class Server {
	private static final int sPort = 9000;
	ServerSocket recSock;
	Socket connSock;

	static String base = "C:/Users/nehar/desktop/P2P/Server" ;
;
	//int sizeOfEachChunk = 100000;
//	private int mainServport;
	// have both these in sync
	static Map<Integer, ArrayList<Integer>> cliMap;
	//static Map<Integer, String> clientMapConf;

//	public static void main(String[] args) {
	public void run() {
	//	Server s = new Server();
		try {

			// break the input file into chunks
			divideFiletoChunks();
			// read conf file

			String str;
			int clients = 5;

            System.out.println(" base location:" + base);
			String chunksLoc = base + "/chunks";
			File[] files = new File(chunksLoc).listFiles();

			int chunkCount = files.length;
			System.out.println("No. of Chunks:" + chunkCount);

			cliMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
			for (int i = 1; i <= clients; i++) {
				ArrayList<Integer> arr = new ArrayList<Integer>();
				for (int j = i; j <= chunkCount; j += clients) {
					arr.add(j);
				}
				cliMap.put(i, arr);

			}
			System.out.println(cliMap);

			if (chunkCount > 0) {
				// wait for the connections to initiate

				   TCPServconnect(files);

			} else
				System.out.println("There are no files in chunks folder!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[])
	{
		Server s = new Server();
		s.run();
	}
// may be I can break this code??
	public void TCPServconnect(File[] files) {
		try {
			int client = 0; // initialized to 0
			recSock = new ServerSocket(sPort);
			System.out.println("Main Server socket created, accepting connections...");
			while (true) {
				System.out.println(cliMap);
				client++;
				if (client <= cliMap.size()) {
					connSock = recSock.accept();
					System.out.println("new client connection accepted :-" + connSock);
					// create thread and pass the socket n files to handle
					new ServerThread(connSock, files, cliMap.get(client)).start();

				} else {
					System.out.println("Cannot serve more clients, i am done!");
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void divideFiletoChunks() {

		try {
			//xyz.mp4
			// main file that is to be broken into pieces

			 //"FLR.pdf"
			File inputFile = new File(base +"/"+ "test.pdf");
			Long fileLength = inputFile.length();

			System.out.println("Input File size :" + fileLength);

			String newdir = inputFile.getParent() + "/chunks/";
			File outFolder = new File(newdir);
			if (outFolder.mkdirs())
				System.out.println("Chunks Folder created");
			else
				System.out.println("Chunks folder already exits or unable to create folder for chunks");

			byte[] chunk = new byte[1024*100];

			FileInputStream fileInStream = new FileInputStream(inputFile);

			BufferedInputStream bufferStream = new BufferedInputStream(fileInStream);
			int index = 1;
			int bytesRead;
			// chuck will be populated with data
			while ((bytesRead = bufferStream.read(chunk)) > 0) {
				FileOutputStream fileOutStream = new FileOutputStream(
						new File(newdir, String.format("%04d", index) + "_" + inputFile.getName()));
				BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
				bufferOutStream.write(chunk, 0, bytesRead);
				bufferOutStream.close();
				index++;
			}
			bufferStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class ServerThread extends Thread {

	private Socket socket;
	File[] files;
	ObjectOutputStream outStream;
	ArrayList<Integer> chunkList;
//	String configClient;

	ServerThread(Socket s, File[] files, ArrayList<Integer> cl) {
		this.socket = s;
		this.files = files;
		this.chunkList = cl;

	}

	public void run() {
		try {
			// get output stream
			outStream = new ObjectOutputStream(socket.getOutputStream());

			/*
			 * Total no of files clients need to have... info will be passed to
			 * each client from here
			 */


			outStream.writeObject(files.length);

			outStream.writeObject(chunkList.size());
			Arrays.sort(files);
			for (int i = 0; i < chunkList.size(); i++) {
				// construct the chunk object
				ChunkFileObject sChunkObj = constructChuckFileObject(files[chunkList.get(i) - 1], chunkList.get(i));
				// send file
			//	sendChunkObject(sChunkObj);
				try {

					outStream.writeObject(sChunkObj);
					outStream.flush();
					System.out.println("send object done...");
				} catch (Exception e) {
					e.printStackTrace();
				}
				// let's intro sleep
				Thread.sleep(1000);
			}
			// disconnect
		//	TCPServDisconnect();
			try {
				outStream.close();
				//System.out.println("file out stream closed...");
				socket.close();
				System.out.println("Main Server socket closed:" + socket);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ChunkFileObject constructChuckFileObject(File file, int chunkNum) throws IOException {
		byte[] chunk = new byte[100000]; // should be 100kb, see demo
		System.out.println("construct object - " + file.getName());
		ChunkFileObject chunkObj = new ChunkFileObject();

		chunkObj.setFileNum(chunkNum);

		chunkObj.setFileName(file.getName());
		FileInputStream fileInStream = new FileInputStream(file);

		BufferedInputStream bufferInStream = new BufferedInputStream(fileInStream);

		int bytesRead = bufferInStream.read(chunk);

		chunkObj.setChunksize(bytesRead);

		chunkObj.setFileData(chunk);

		bufferInStream.close();
		fileInStream.close();

		return chunkObj;
	}

//	public void sendChunkObject(ChunkFileObject sChunkObj) {
//		try {

//			outStream.writeObject(sChunkObj);
//			outStream.flush();
//			System.out.println("send object done...");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	public void TCPServDisconnect() {
//		try {
//			outStream.close();
			//System.out.println("file out stream closed...");
	//		socket.close();
	//		System.out.println("Main Server socket closed:" + socket);
	//	} catch (Exception e) {
	//		e.printStackTrace();
	//	}
	//}
}
