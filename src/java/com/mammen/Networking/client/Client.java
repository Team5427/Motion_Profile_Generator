package com.mammen.Networking.client;


/**
 * READ ME: Everything below the comment line should be copied and pasted to the client
 * 			on the main repository. Anything above here is to allow compatibility without
 * 			changing other parts of the code for the robot.
 */

///////////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import jaci.pathfinder.Trajectory;

public class Client implements Runnable {

	public static final String DEFAULT_IP = "10.54.27.2";
	public static final int DEFAULT_PORT = 25565;
	public static int MAX_BYTE_BUFFER = 256;

	public static String ip;
	public static int port;

	Thread networkThread;
	public ArrayList<Object> inputStreamData = null;


	private Socket clientSocket;
	private ObjectInputStream is;
	private ObjectOutputStream os;

	

	// public GoalData lastRecievedGoal;

	// public Trajectory lastRecievedTrajectory;
	// public boolean newTrajectory;

	public double robotX;
	public double robotY;
	public double robotHeading;

	public Client() {
		ip = DEFAULT_IP;
		port = DEFAULT_PORT;
	}

	public Client(String ip, int port) {
		Client.ip = ip;
		Client.port = port;
		robotX = 5;
		robotY = 2;
	}

	/**
	 * Reconnect to the client to the server
	 *
	 * @return true if connection is a success, false if failed
	 */
	public boolean reconnect() {
		try {
			clientSocket = new Socket(ip, port);
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			//System.out.println(clientSocket.toString());

			inputStreamData = new ArrayList<>();

			System.out.println("Connection to the server has been established successfully.");

			return true;
		} catch (Exception e) {
			// TODO removed due to spam
			// System.out.println("Connection failed to establish.");
			System.out.println("Connection failed to establish.");
			return false;
		}
	}

	/**
	 * Checks if the client is connected to a server
	 *
	 * @return true if server connection is established, false if not.
	 */
	public boolean isConnected() {
		return clientSocket != null && !clientSocket.isClosed();
	}

	public static String getIp() {
		return ip;
	}

	public static void setIp(String ip) {
		Client.ip = ip;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Client.port = port;
	}

	public ArrayList<Object> getInputStreamData() {
		return inputStreamData;
	}

	/**
	 * Sends an object to the server
	 *
	 * @param t
	 *            object to be sent to the server
	 * @return true if the object is sent successfully, false if otherwise.
	 */
	/*
	 * public synchronized boolean send(Task t) {
	 * 
	 * if (networkThread != null && !networkThread.isInterrupted()) { try {
	 * os.writeObject(t); os.reset(); return true; } catch
	 * (NotSerializableException e) { Log.error(getClass() +
	 * ":: send(Serializable o)\n\tThe object to be sent is not serializable.");
	 * } catch (SocketException e) { Log.error("Socket Exception"); } catch
	 * (NullPointerException e) { Log.error(
	 * "\n\tThere was an error connecting to the server."); // This error occurs
	 * when the client attempts to connect to a server, but the running } catch
	 * (Exception e) { Log.error(e.getMessage()); } }
	 * 
	 * return false; }
	 */

	/**
	 * Enables the thread to start receiving data from a network
	 *
	 * @return true if the thread starts successfully, false if otherwise.
	 */
	public synchronized boolean start() {
		if (networkThread == null && (clientSocket == null || !clientSocket.isClosed())) {
			networkThread = new Thread(this);
			networkThread.start();
			return true;
		}

		return false;
	}

	/**
	 * Stops the thread from receiving data from the server
	 *
	 * @return true if the thread is stopped successfully, false if otherwise.
	 */
	public synchronized boolean stop() {
		networkThread.interrupt();

		try {
			clientSocket.close();
			os.close();
			is.close();
		} catch (Exception e) {
		//	System.out.println(e.getMessage());
		}

		clientSocket = null;
		os = null;
		is = null;

		if (!networkThread.isAlive()) { // The thread is found running and is
										// told to stop
			return true;
		} else { // The thread is not running in the first place
			return false;
		}
	}

	

	/**
	 * Running method that receives data from the server.
	 */
	@Override
	public void run() {

		reconnect();

		while (!networkThread.isInterrupted()) {

			if (clientSocket != null && !clientSocket.isClosed() && is != null) {
				try {
					String line = is.readUTF();
					
					robotX = Double.parseDouble(line.split(" ")[0]);
					robotY = Double.parseDouble(line.split(" ")[1]);
					robotHeading = Double.parseDouble(line.split(" ")[2]);

				} catch (SocketException e) {
					reconnect();
				} catch (Exception e) {
					//System.out.println(e.getMessage());
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					//System.out.println("Thread has been interrupted, client thread will stop.");
				} catch (Exception e) {
					//System.out.println(e.getMessage());
				}
			} else {
				//System.out.println("Connection lost, attempting to re-establish with driver station.");
				reconnect();
			}
		}
	}
}