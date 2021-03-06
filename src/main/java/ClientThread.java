import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class ClientThread implements Runnable {

	private String choice;

	private boolean flag = true;

	private PrintWriter outputToClient;
	private BufferedReader clientInput;
	private Socket threadSocket;
	private DBHandler dbHandler;
	private InputHandler inputHandler;
	private DBConnection dbCon = new DBConnection();
	private String username;

	public ClientThread(Socket socket) {
		threadSocket = socket;
	}

	/**
	 * This method will run every time a new client is connected,
	 * it uses a PrintWriter to read incoming text sent from the server, in this case the menus
	 * and a BufferedReader to read the clients text, in this case answers to the menus the server provides.
	 * the while loop uses a flag that will turn to fale if the client options to exit the menus.
	 */
	public void run() {
		try(BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in))) {

			outputToClient = new PrintWriter(threadSocket.getOutputStream(), true);
			clientInput = new BufferedReader(new InputStreamReader(threadSocket.getInputStream()));

			while (flag) {

				//Set username
				outputToClient.println("Velg brukernavn:");
				username = clientInput.readLine();
				outputToClient.println(" ");

				System.out.println(username + " has connected on: " + new Date());

				startMenu();

				//Lese input fra klineten
				if (clientInput.ready()) {
					String clientMsg = clientInput.readLine();
					System.out.println(clientMsg);
				}

				//Skrive tekst til klienten
				if (serverInput.ready()) {
					String input = serverInput.readLine();
					outputToClient.println(input);
				}
			}
		} catch (Exception e){
			e.getMessage();
		} finally {
			outputToClient.close();

			if (clientInput != null){
				try {
					clientInput.close();
				} catch (IOException ioex){
					//empty on purpose
				}
			}
		}
	}

	//region UserMenu

	/**
	 * Welcome text and initialization of the database.
	 * All of the menus is written in norwegian
	 */
	private void startMenu() {

		outputToClient.println(
				"Velkommen til serveren " + username + "!\n" +
				"Skriv inn ønsket valg og avslutt med enter\n\n" +
				"------------------------------------------------------------------------------------------------- \n" +
				"1: Koble til og opprett databasen\n" +
				"0: Koble fra serveren\n" +
				"-------------------------------------------------------------------------------------------------");

		try {
			choice = clientInput.readLine();

			switch (choice){
				case "1":
					outputToClient.print("Kobler til databasen...");
					DBConnection dbConnection = new DBConnection();
					dbConnection.setupCheck();
					outputToClient.println("Vellykket \n");

					//neste meny
					serverInitMenu();
					break;

				case "0":
					outputToClient.print("Serveren kobles fra...");
					flag = false;
					outputToClient.println("Vellykket");
					System.out.println(username + " has disconnected on: " + new Date());
					break;

				default:
					outputToClient.println("Feil svar! Prøv igjen... \n");

					//rekursivt kall hvis ikke klienten svarer som forventet
					startMenu();
					break;
			}
		} catch (IOException ioex){
			System.out.println("Feil ved lesing av input: " + ioex.getMessage());
		}
	}

	/**
	 * database table creation/deletion
	 */
	private void serverInitMenu() {
		outputToClient.println(
				"------------------------------------------------------------------------------------------------- \n" +
				"1: Drop tabellen 'subject' hvis den eksisterer \n" +
				"2: Opprett tabeller \n" +
				"9: Tilbake \n" +
				"0: Koble fra serveren\n" +
				"-------------------------------------------------------------------------------------------------");

		try {
			choice = clientInput.readLine();

			switch (choice){
				case "1":
					dbHandler = new DBHandler();
					dbHandler.dropTablesIfExists(dbCon.connect());
					outputToClient.print("Dropper tabellen hvis den eksisterer...");
					outputToClient.println("Vellykket \n");

					//rekursivt kall til menyen fordi
					serverInitMenu();
					break;

				case "2":
					outputToClient.println("Oppretter tabellen 'subject'...");
					outputToClient.println(dbHandler.createSubjectTable(dbCon.connect()));
					outputToClient.println("Vellykket! \n");

					//neste meny
					insertDataToTables();
					break;

				case "9":
					outputToClient.println("Går tilbake... \n");
					startMenu();
					break;

				case "0":
					outputToClient.print("Serveren kobles fra...");
					flag = false;
					outputToClient.println("Vellykket");
					System.out.println(username + " has disconnected on: " + new Date());
					break;

				default:
					outputToClient.println("Feil svar! Prøv igjen... \n");

					//rekursivt kall hvis ikke klienten svarer som forventet
					serverInitMenu();
					break;
			}
		} catch (IOException ioex){
			System.out.println("Feil ved lesing av input: " + ioex.getMessage());
		}
	}

	/**
	 * populate the tables
	 */
	private void insertDataToTables() {
		outputToClient.println(
				"------------------------------------------------------------------------------------------------- \n" +
				"1: Fyll inn tabellen med data fra fil \n" +
				"9: Tilbake \n" +
				"0: Koble fra serveren\n" +
				"-------------------------------------------------------------------------------------------------");

		try {
			choice = clientInput.readLine();

			switch (choice) {
				case "1":
					outputToClient.println("Oppretter 'subject' table...'");
					inputHandler = new InputHandler();
					outputToClient.println(inputHandler.addSubjectDataFromFile(dbCon.connect()));
					outputToClient.println("Vellykket");

					//neste meny
					printDataFromDatabaseMenu();
					break;

				case "9":
					outputToClient.println("Går tilbake... \n");
					serverInitMenu();
					break;

				case "0":
					outputToClient.print("Serveren kobles fra...");
					flag = false;
					outputToClient.println("Vellykket");
					System.out.println(username + " has disconnected");
					break;

				default:
					outputToClient.println("Feil svar! Prøv igjen... \n");

					//rekursivt kall hvis ikke klienten svarer som forventet
					insertDataToTables();
					break;
			}
		} catch (IOException ioex){
			System.out.println("Feil ved lesing av input: " + ioex.getMessage());
		}
	}

	/**
	 * print data from the table, either by entering a specific subject code or print all the rows in the table.
	 */
	private void printDataFromDatabaseMenu() {
		outputToClient.println(
				"------------------------------------------------------------------------------------------------- \n" +
				"1: Print data fra tabellen 'subject'\n" +
				"2: Print ett emne fra emnekode\n" +
				"9: Tilbake\n" +
				"0: Koble fra serveren\n" +
				"-------------------------------------------------------------------------------------------------");

		try {
			choice = clientInput.readLine();

			switch (choice){
				case "1":
					outputToClient.println("\n------------------------------------------- SUBJECTS --------------------------------------------");
					inputHandler = new InputHandler();
					outputToClient.println(inputHandler.printAllSubjects(dbCon.connect()) + "\n");

					//slutten av menyen, rekursivt kall
					printDataFromDatabaseMenu();
					break;

				case "2":
					outputToClient.println("Skriv inn emnekode:");
					String emnekode = clientInput.readLine();
					outputToClient.println("\n--------------------------------------- PRINTING " + emnekode.toUpperCase() + " ---------------------------------------");
					inputHandler = new InputHandler();
					outputToClient.println(inputHandler.printSingleSubject(dbCon.connect(), emnekode) + "\n");

					//slutten av menyen, rekursivt kall
					printDataFromDatabaseMenu();
					break;

				case "9":
					outputToClient.println("Går tilbake...");
					outputToClient.println("");

					insertDataToTables();
					break;

				case "0":
					outputToClient.print("Serveren kobles fra...");
					flag = false;
					outputToClient.println("Vellykket");
					System.out.println(username + " has disconnected");
					break;

				default:
					outputToClient.println("Feil svar! Prøv igjen...\n");

					//rekursivt kall hvis ikke klienten svarer som forventet
					printDataFromDatabaseMenu();
					break;
			}
		} catch (IOException ioex){
			System.out.println("Feil ved lesing av input: " + ioex.getMessage());
		}
	}
	//endregion
}