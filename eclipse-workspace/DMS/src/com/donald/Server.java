package com.donald;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * server
 * 
 * @author Donald
 *
 *         July 9, 2019
 */
public class Server {
	private ServerSocket server;
	/**
	 * thread-pool which is managed thread.
	 */
	private ExecutorService threadpool;
	/**
	 * save all of clients' OutputStream
	 */

	// save matched logs from client
	private File serverLogFile;
	// save matched logs in queue
	private BlockingQueue<String> messageQueue;

	/**
	 * constructor
	 * 
	 * @throws IOException
	 */
	public Server() throws IOException {
		try {
			System.out.println("Initializing the server...");
			// initialize server-socket
			server = new ServerSocket(8088);
			// initialize thread-pool
			threadpool = Executors.newFixedThreadPool(50);
			// initialize file
			serverLogFile = new File("server-log.txt");
			// initialize queue
			messageQueue = new LinkedBlockingQueue<String>();
			System.out.println("sever is initialized");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * sever start
	 */
	public void start() {
		try {
			// start write - thread
			WriteLogThread thread = new WriteLogThread();
			thread.start();

			while (true) {
				System.out.println("Waitting for client...");
				Socket socket = server.accept();
				/**
				 * when client is connected, start thread(socket) which interact with client.
				 * And then, we can connect other client.
				 */
				Runnable clienthandler = new ClientHandler(socket);
				/**
				 * use thread-pool assign free thread to execute connected client
				 */
				threadpool.execute(clienthandler);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server;
		try {
			server = new Server();
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("server initialize error...");
		}

	}

	class ClientHandler implements Runnable {
		// current thread handle client
		private Socket socket;

		/**
		 * according to client's socket to create thread
		 * 
		 * @param socket
		 */
		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		/**
		 * thread will get inputStream with socket and read client'e message
		 */
		@Override
		public void run() {
			/**
			 * define pw not in try is aim to use it in finally.
			 */
			PrintWriter pw = null;
			try {
				/**
				 * in order to let server send message to client.We use outputStream
				 */
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
				pw = new PrintWriter(osw, true);
				// inputStream
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, "utf-8");
				BufferedReader read = new BufferedReader(isr);
				/**
				 * recyling read client send matched logs, and save logs to messageQueue which
				 * are waiting for written to file
				 */
				String message = null;
				while ((message = read.readLine()) != null) {
					/*
					 * if client send "over", means it's done. need to stop loop.
					 */
					if ("over".equals(message)) {
						break;
					}
					messageQueue.offer(message);
				}
				/*
				 * when exit loop, means all of logs save in queue. reply client:ok
				 */
				pw.println("OK");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("clent disconeect");
				e.printStackTrace();
				pw.println("ERROR");
			} finally {

				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("client is offline");
			}

		}

	}

	class WriteLogThread extends Thread {
		/**
		 * this thread in server only have one instance. the function is recycling read
		 * logs in queue and write it to file. When queue is empty, sleep a while and
		 * wait for new logs.
		 */
		public void run() {
			try {
				PrintWriter pw = new PrintWriter(serverLogFile);
				while (true) {
					if (messageQueue.size() > 0) {
						pw.println(messageQueue.poll());
					} else {
						pw.flush();
						Thread.sleep(5000);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
