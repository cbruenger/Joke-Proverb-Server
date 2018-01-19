/*--------------------------------------------------------

1. Craig Bruenger / 1-19-2018:

2. Java version used: build 1.8.0_144-b01

3. Precise command-line compilation examples / instructions:

> javac *.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

If two arguments are given for JokeClient / JokeClientAdmin, the first is used
as the default server, and the second is used as the secondary server:

> java JokeClient localHost 140.192.1.22
> java JokeClientAdmin localHost 140.192.1.22

When running the JokeServer, the argument "secondary" can be passed on the
command line in order to launch as the secondary server:

> java JokeServer secondary

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

When passing arguments on the command line and when providing input, make sure
to use EXACTLY the following text, without any spaces or differences in capitalization:

	localHost	----> Command line arg when using the local machine as the host
	secondary	----> Command line arg when launching the secondary server
	s			----> JokeClient / JokeClientAdmin input to switch to secondary server
	quit			----> JokeClient / JokeClientAdmin input to exit the program
	[enter]		----> Simply press enter to request a joke/proverb (in JokeClient program)
					  or to switch the server into Joke/Proverb mode (in JokeClientAdmin program)

----------------------------------------------------------*/

//Import the Java libraries for input/output and working with networks 
import java.io.*;
import java.net.*;


/* This class represents the Client. It contains a main method which takes user input for a server name and
a hostname. Contains a method "getRemoteAddress" which communicates with a server in order to retrieve the IP address
of the user specified hostname and prints it. Contains a method "toText" */
public class JokeClientAdmin {

	static boolean secondaryAvailable = false;
	static String defaultServer;
	static String secondaryServer;
	static String currentServer;
	static int defaultPort = 5050;
	static int secondaryPort = 5051;
	static int currentPort;
	
	public static void main (String args[]) {

		/* Assign the serverName to a default of "localHost" unless an argument is given.
		/ If one or more arguments are present, the serverName is set as the first. 
		*/
		if (args.length < 1) {
			defaultServer = "localHost";
			currentServer = "localHost";
			currentPort = defaultPort;
		} else if (args.length == 1) {
			defaultServer = args[0];
			currentServer = defaultServer;
			currentPort = defaultPort;
		} else {
			secondaryAvailable = true;
			defaultServer = args[0];
			secondaryServer = args[1];
			currentServer = defaultServer;
			currentPort = defaultPort;
		}

		/* Print info to screen (Assignment title, serverName to connect to and 
		/  port number to connect to). I changed the port to 50001.
		*/
		System.out.println("JokeClientAdmin starting up");
		System.out.println("Server one: " + defaultServer + ", port " + defaultPort);
		if (secondaryAvailable)
			System.out.println("Server two: " + secondaryServer + ", port" + secondaryPort);

		//Setup a buffered reader to read characters from input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		//This try block may throw an IOException which will be caught after
		try {

			//Create a variable to store the username of the client
			String input;

			/* Acquire a hostname or IP address from user, exiting the loop only
			/ when user enters 'quit'
			*/
			do {
				//Request input from user and flush the buffer
				System.out.print("Press enter to switch modes, (s) to switch to secondary server, (quit) to exit: ");
				System.out.flush();

				//Store input in 'name' variable
				input = in.readLine();
				
				if (input.equals("s")) {
					if (secondaryAvailable) {
						if (currentServer.equals(defaultServer) && currentPort == defaultPort) {
							currentServer = secondaryServer;
							currentPort = secondaryPort;
						} else {
							currentServer = defaultServer.toString();
							currentPort = defaultPort;
						}
						System.out.println("Now communicating with: " + currentServer + ", port " + currentPort);
					} else {
						System.out.println("No secondary server being used.");
					}
				}
				if (!input.equals("quit")) {
					//Call helper method 'getRemoteAddress' which communicates with server
					switchServerMode(currentServer);
				}
				
			} while (!input.equals("quit"));
			System.out.println("Cancelled by admin.");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
	
	/* This method takes 2 arguments, 'name' holds the name of a host or IP address
	/  from user input, 'serverName' is either the local host or IP address of another
	/  server. The method communicates the 'name' with the given server and receives
	/  a corresponding IP address which is printed to the screen, unless there was a
	/  socket error.
	*/
	static void switchServerMode(String serverName) {
		Socket sock;
		BufferedReader fromServer;
		String textFromServer;
		
		try {
			sock = new Socket(serverName, currentPort);	//Attempt to connect the socket to the server at the given port
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //Initialize the buffer reader with an input stream through the socket
			
			//Iterate through the buffered reader of response from the server, printing up to 2 lines
			for (int i = 1; i <= 3; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}

			sock.close();	//Close the socket
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}