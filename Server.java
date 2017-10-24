import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;

/**
 *
 * @author Rohit Katta
 * @UID: 1001512896
 *
 * Reference: http://cs.lmu.edu/~ray/notes/javanetexamples/
 *
 * This is main Server Class.
 * Server is MultiThreaded. It creates new thread for each client connection
 * Server has no GUI.
 *
 * To compile the program, use the following command from the directory:
 * javac Server.java
 * Program doesnt take any command line arguments.
 */

public class Server {
    /**
     * The port that the server listens on.
     */
    public static final int port = 8999;
    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     * Boolean value is to check whether client is already communicating
     * with another client.
     * true=busy communicating
     * false=available
     */
    private static HashMap<String, Boolean> names = new HashMap<String, Boolean>();
    /**
     * The set of all pair of names of clients in the chat room who are
     * chatting with each other.
     * This is used to identify who the user is communicating with and send
     * messages to and from each other only
     */
    private static HashMap<String, String> clientCombos = new HashMap<String, String>();
    /**
     * The set of all the print writers for all the clients mapped to respective client.  This
     * set is kept so we can know whom to send message from coboma.
     */
    private static HashMap<String, PrintWriter> ClientWriterMap = new HashMap<String, PrintWriter>();


    public static void main(String[] args) throws IOException {

        ServerSocket ss = null;
        try {
            System.out.println("Connecting to port " + port + " ....");
            ss = new ServerSocket(port); //Server socket will be created here,
            System.out.println("Server started on port " + port);
        } catch (IOException e) { //If exception occurs, stack trace is printed for debugging and relevant user msg shown
            System.out.println("Cannot start Server on port " + port);
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            while (true) {
                //For each client a new thread is crated to handle that user.
                new Thread(new ClientHandler(ss.accept())).start();
            }
        } finally {
            ss.close(); //Closes in case of exception
        }
    }

    /**
     * A handler Runnable class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client.
     */
    private static class ClientHandler implements Runnable {
        private String name; //Name of this Client
        private String partner; //Name of Client this client wants to connect to
        private Socket socket; //Socket from this client to server.
        private BufferedReader in; // Reads stream  from Client.
        private PrintWriter out; // Printwiter to send messages to this client.
        private PrintWriter partOut; // To send messages to other communicating Client.

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and requests for name of other client
         * he wants to connect to, after connecting we map the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * sends them.
         */
        public void run(){
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        continue;
                    }
                    synchronized (names) {
                        if (!names.containsKey(name)) {
                            names.put(name, false);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                ClientWriterMap.put(name, out);

                // Request a name with whom he wants to talk from this client.
                // Keep requesting until a name is submitted that is not already chatting.
                // Note that checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITPART");
                    String nm = in.readLine();
                    if (nm == null) {
                        continue;
                    }
                    synchronized (names) {
                        //If user is not in our users list, send back request again
                        if (!names.containsKey(nm)) {
                            continue;
                        } else {
                            //If user in our list. We check whether user is busy with another Client
                            //If Busy we ask user to give a new name
                            //If not busy we connect
                            if (!names.get(nm)) { //Checking here if user is busy. Will go inside only if user is not busy.
                                partner = nm; //
                                names.replace(nm, false, true);
                                clientCombos.put(name, nm);
                                clientCombos.put(nm,name);
                                break;
                            }
                            else { //If user is busy we continue and ask for new name.
                                continue;
                            }
                        }
                    }
                }

                //If valid partner, they will be connected
                //Below Acknowledgement will be sent to this client.
                out.println("PARTACCEPTED");

                //Other Clients WriterMap will be assigned
                //All future communications happen from this writer
                partOut = ClientWriterMap.get(partner);

                // Accept messages from this client and send them.
                // Send only to requested Client.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    // This is to display and log all messages from this client
                    // Messages from Cient start with "MESSAGE "
                    // HTTP header information is logged and printed dirctly.
                    // In else(i.e if it is message from client) we reformat it and
                    // send it to the other client
                    if(!input.startsWith("MESSAGE ")){
                        System.out.println(input);
                        partOut.println(input);
                    }
                    else {
                        input = input.substring(8);
                        partOut.println("MESSAGE " + name + ": " + input); // Message sent to other cleint
                        //out.println("MESSAGE " + name + ": " + input);
                        //Message is Displayed in Servers Console.
                        System.out.println("Message from " + name + " to " + partner + " :: " + input);
                        System.out.println("\r\n");
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name); //Name is removed from our Users List
                }
                if (out != null) {
                    ClientWriterMap.remove(out); //Mapwriter object is removed
                }
                if(partOut != null) {
                    partOut.println("PARTLEFT"); //Other Client is notified of this clients leaving.
                }
                if(partner != null){
                    clientCombos.remove(partner);
                    clientCombos.remove(name, partner);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}