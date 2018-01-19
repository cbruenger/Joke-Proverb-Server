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
import java.util.UUID;

/* This class represents the Client. It contains a main method which takes user input for a server name and
a hostname. Contains a method "getRemoteAddress" which communicates with a server in order to retrieve the IP address
of the user specified hostname and prints it. Contains a method "toText" */
public class JokeClient {

	static String uuid = UUID.randomUUID().toString();
	static boolean secondaryAvailable = false;
	static String defaultServer;
	static String secondaryServer;
	static String currentServer;
	static int defaultPort = 4545;
	static int secondaryPort = 4546;
	static int currentPort;
	
	public static void main (String args[]) {

		/* Assign the serverName to a default of "localHost" unless an argument is given.
		/ If one or more arguments are present, the serverName is set as the first. 
		*/
//		String serverOneName = null;
//		String serverTwoName = null;
		
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
		/  port number to connect to).
		*/
		System.out.println("JokeClient starting up");
		System.out.println("Server one: " + defaultServer + ", port " + defaultPort);
		if (secondaryAvailable)
			System.out.println("Server two: " + secondaryServer + ", port " + secondaryPort);

		//Setup a buffered reader to read characters from input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		//This try block may throw an IOException which will be caught after
		try {

			//Create a variable to store a hostname or IP Address as a string
			String name;
			String input;
			
			//Request input from user and flush the buffer
			System.out.print("Enter your name: ");
			System.out.flush();
			name = in.readLine();

			/* Acquire a hostname or IP address from user, exiting the loop only
			/ when user enters 'quit'
			*/
			do {
				System.out.print("Press enter for joke/proverb, (s) to switch to secondary server, (quit) to exit:" );
				System.out.flush();
				
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
					getServerResponse(uuid, name, currentServer);
				}
				
			} while (!input.equals("quit"));
			System.out.println("Cancelled by client.");
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
	static void getServerResponse(String identifier, String name, String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			sock = new Socket(serverName, currentPort);
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			toServer.println(identifier + "\n" + name);
			toServer.flush();
			
			for (int i = 1; i <= 3; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}

			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}

	/* This method is not called in this class. It could be useful if we were required to make it portable for
	128 bit systems, but as the code in this class is currently structured, this method could be removed and
	everything would still work. It takes an array of bytes, representing the IP address retreived from the 
	server, and converts it is to characters for printing. Returns the array.
	*/
	static String toText (byte ip[]) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; ++i) {
			if (i > 0)
				result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
}