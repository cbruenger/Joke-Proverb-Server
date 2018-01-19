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


/* This file contains 2 classes, the server which accepts a request from a client, and a worker.
/  The server creates an instance of the worker, and has the worker 'do the work', in it's own thread
/  which is trivial since the main focus of this assignment is the client/server communication.
*/

//Import the Java libraries for input/output and working with networks
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

/* This class represents the server and only contains a main method which creates variables
/  representing a queue length, a port number a socket (which is used to accept connections
/  from clients) and a server socket (which is initialized with the given port and queue length).
/  An informative print statement is printed to the screen, and then the program enters a while
/  loop to wait for client requests. Upon receiving a client request, a new Worker is spawned in
/  in a new thread to do the work.
*/
public class JokeServer {
	
	static boolean secondary = false;
	static boolean controlSwitch = true;
	static boolean jokeMode = true;
	static String serverTag = "";
	
	static HashMap<String, Integer> lastJokeSent = new HashMap<String, Integer>();
	static HashMap<String, Integer> lastProverbSent = new HashMap<String, Integer>();
	
	static String[] jokes = {"JA Joke 1", "JB Joke 2", "JC Joke 3", "JD Joke 4"};
	static String[] proverbs = {"PA Proverb 1", "PB Proverb 2", "PC Proverb 3", "PD Proverb 4"};
	
	public static void main(String args[]) throws IOException {
		
		if (args.length > 0 ) {
			secondary = true;
			serverTag = "<S2> ";
		}

		System.out.println(serverTag + "JokeServer starting up");
		
		Collections.shuffle(Arrays.asList(JokeServer.jokes));
		Collections.shuffle(Arrays.asList(JokeServer.proverbs));
		
		AdminAccessor AA = new AdminAccessor();
		Thread t = new Thread(AA);
		t.start();
		
		int q_len = 6;	//Maximum number of client requests to queue
		Socket sock;	//A socket that will be designated to each client request
		int port;
		
		if (secondary)
			port = 4546;	//The default port at which the server will accept requests
		else
			port = 4545;
		
		//This variable represents a Server Socket that is constructed on a given port and with a given queue length
		ServerSocket servsock = new ServerSocket(port, q_len);

		
		System.out.println(serverTag + "Listening for clients at port " + port + ".");	//Informative print statement

		/* This loop runs for the live of the program, waiting for client requests, and then calling the accept() method
		/  on there server socket which returns a new socket to be used. For each request, a new Worker class is started in
		/  its own thread to do the work
		*/
		while (controlSwitch) {
			sock = servsock.accept();	//Assigns the 'sock' var to a new socket to accept a client request
			new Worker(sock).start();	//An instance of Worker is constructed with the given socket and started in its own thread
		}
		servsock.close();
	}
}

class AdminAccessor implements Runnable {
	public static boolean adminControlSwitch = true;
	
	public void run() {
		int q_len = 6;
		Socket sock;
		int port;
		
		if (JokeServer.secondary)
			port = 5051;	//The default port at which the server will accept requests
		else
			port = 5050;
		
		System.out.println(JokeServer.serverTag + "Listening for admin at port " + port + ".");
		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while (adminControlSwitch) {
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}
			servsock.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

class AdminWorker extends Thread {

	Socket sock;
	
	AdminWorker(Socket s) {
		this.sock = s;
	}
	
	public void run() {
		PrintStream out = null;
		//BufferedReader in = null;
		try {
			//in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			
			//String anything = in.readLine();
			JokeServer.jokeMode = !JokeServer.jokeMode;
			String mode;
			if (JokeServer.jokeMode)
				mode = "JOKE";
			else
				mode = "PROVERB";
			System.out.println(JokeServer.serverTag + "Server has been switched into " + mode + " mode by admin.");
			out.println(JokeServer.serverTag + "Server has switched into " + mode + " mode.");
			
			sock.close();
		} catch (IOException ioe2) {
			ioe2.printStackTrace();
			System.out.println(ioe2);
		}
	}
}

/* This Worker class is created by the server with a socket passed as a parameter. Since it runs
/  in its own thread, the 'run()' method is called automatically, which does the 'work'. In this
/  case, the 'work' is setting up a buffered reader through the given socket to recieve a host name
/  or IP address from the client, and also setting up a print stream to send communication back to the
/  client. Then, the Worker retrieves the IP address and host name for the corresponding IP/host specified
/  via the client, and sends it to the client.
*/
class Worker extends Thread {

	Socket sock;	//This socket is a class member, local to the Worker

	//Constructor, takes a socket as an argument and assigns the class member socket to it
	Worker (Socket s) {
		this.sock = s;
	}

	/* Since the class is setup to function in a multi-threaded environment, this method
	/  is automatically called upon invoking the .start() method on an instance of the class.
	/  This method creates and initializes a print stream to send communication through the
	/  given socket, and also creates a buffered reader to accept communication from client
	/  through the given socket. It prints the name of the host that it receives from the client,
	/  calls the 'printRemoteAddress' method before closing the socket.
	*/
	public void run() {
		PrintStream out = null;	//This print stream variable will be used to send communication to the client
		BufferedReader in = null; //This var is a buffer which will receive characters from the client
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); //Initialize buffer reader variable with input stream reader through the given socket
			out = new PrintStream(sock.getOutputStream());	//Initialize the output stream to send communication to the client through the socket

			/* Attempt to retrieve an IP/host name from the buffered reader and call the 'printRemoteAddress' method
			/  Otherwise if there is a problem retrieving the data from the buffer, an IOException is caught
			*/
			try {
				 
				/*
				String name;	//A var to hold the user client specified IP/host name
				name = in.readLine();	//Retrieve the characters from the buffered reader and store in in the 'name' var
				System.out.println("Looking up " + name);	//Print notification to screen including IP/host name
				printRemoteAddress(name, out);	//Call the method to actually retrieve the Ip/host name for the client specified IP/host name
				*/
				String uuid;
				String name;
				uuid = in.readLine();
				name = in.readLine();
				
				handleClient(uuid, name, out);
								
			} catch (IOException x) {
				System.out.println(JokeServer.serverTag + "Server read error");
				x.printStackTrace();
			}
			sock.close();	//Close the socket
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	private void handleClient(String uuid, String name, PrintStream out) {
		if (JokeServer.jokeMode) {
			if (!JokeServer.lastJokeSent.containsKey(uuid))
				initializeClientState(uuid);
			sendJoke(uuid, name, out);
		} else {
			if (!JokeServer.lastProverbSent.containsKey(uuid))
				initializeClientState(uuid);
			sendProverb(uuid, name, out);
		}
	}
	
	private void initializeClientState(String uuid) {
		if (JokeServer.jokeMode) {
			JokeServer.lastJokeSent.put(uuid, -1);
		} else {
			JokeServer.lastProverbSent.put(uuid, -1);
		}
	}

	private void sendJoke(String uuid, String name, PrintStream out) {
		
		int numJokes = JokeServer.jokes.length;
		int joke = JokeServer.lastJokeSent.get(uuid);
		joke++;
		
		out.println(JokeServer.serverTag + JokeServer.jokes[joke].substring(0, 3) + name + ": " + JokeServer.jokes[joke].substring(3));
		System.out.println(JokeServer.serverTag + "Sent " + name + " Joke " + JokeServer.jokes[joke].substring(0, 3));
		
		if (joke == numJokes - 1) {
			out.println(JokeServer.serverTag + "JOKE CYCLE COMPLETED");
			System.out.println(JokeServer.serverTag + "JOKE CYCLE COMPLETED FOR " + name);
			JokeServer.lastJokeSent.replace(uuid, -1);
			Collections.shuffle(Arrays.asList(JokeServer.jokes));
		} else {
			JokeServer.lastJokeSent.replace(uuid, joke);
		}
	}
	
	private void sendProverb(String uuid, String name, PrintStream out) {
		
		int numProverbs = JokeServer.proverbs.length;
		int proverb = JokeServer.lastProverbSent.get(uuid);
		proverb++;
		
		out.println(JokeServer.serverTag + JokeServer.proverbs[proverb].substring(0, 3) + name + ": " + JokeServer.proverbs[proverb].substring(3));
		System.out.println(JokeServer.serverTag + "Sent " + name + " Proverb " + JokeServer.proverbs[proverb].substring(0, 3));
		
		if (proverb == numProverbs - 1) {
			out.println(JokeServer.serverTag + "PROVERB CYCLE COMPLETED");
			System.out.println(JokeServer.serverTag + "PROVERB CYCLE COMPLETED FOR " + name);
			JokeServer.lastProverbSent.replace(uuid, -1);
			Collections.shuffle(Arrays.asList(JokeServer.proverbs));
		} else {
			JokeServer.lastProverbSent.replace(uuid, proverb);
		}
	}


//	/* This method takes the client specified IP/host name and a print stream as args, and then sends
//	/  necessary informative statements through the print stream, including the requested hostName and
//	/  IP address
//	*/

}





