import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Server {

	private static int portNumber;
	private List<ClientThread> clients = new ArrayList<>();

	public static void main(String[] args) {
		new Server();
	}

	/**
	 * constructor that starts the server on portnumber set in the property file and accepts incoming clients
	 * The clients is connected to the server by starting new thread for each new client that is connected
	 */
	public Server() {
		readConfigFile();

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

			System.out.println("Server started on port: " + portNumber + " on: " + new Date());

			while (true){
				Socket socket = serverSocket.accept();

				ClientThread clientThread = new ClientThread(socket);
				clients.add(clientThread);

				new Thread(clientThread).start();
			}
		} catch (IOException ioex) {
			ioex.getMessage();
		}
	}

	/**
	 * reads the propertyfile and setting the fields to its data for further use.
	 */
	private void readConfigFile(){
		Properties props = new Properties();
		InputStream input = null;

		try{
			String filePath = "serverConfig.properties";
			input = Server.class.getClassLoader().getResourceAsStream(filePath);

			if (input == null){
				System.out.println("Unable to read file at " + filePath);
				return;
			}

			props.load(input);

			portNumber = Integer.parseInt(props.getProperty("portNumber"));

		} catch (IOException ioex){
			ioex.getMessage();
		} finally {
			if (input != null){
				try {
					input.close();
				} catch (IOException ioex){
					//tom
				}
			}
		}
	}

	public List<ClientThread> getClients() {
		return clients;
	}
}