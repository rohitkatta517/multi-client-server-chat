import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Rohit Katta
 * @UID: 1001512896
 *
 * Reference: http://cs.lmu.edu/~ray/notes/javanetexamples/
 *
 * This is main Client Class.
 * Client is SingleThreaded.
 * Client has basic GUI of just textarea.
 *
 * To compile the program, use the following command from the directory:
 * javac Client.java
 * Program doesnt take any command line arguments.
 */

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.
 *
 * The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to give name
 * of the other client wants to chat with. The server will keep
 * sending "SUBMITPART" requests as long as the client submits names that are
 * not in use i.e a valid Client.
 *
 * When the server sends a line beginning with "PARTACCEPTED", it
 * is connected with other client and it can start messaging other Client.
 *
 * When the server sends a "PARTLEFT" message, it means the other client has left
 * or disconnected and this client will not be able to send messages anymore.
 *
 * All communications are sent with HTTP GET header. Timestamp and Content Type
 * Actual message will be prefixed with "MESSAGE " so that server can easily identify message
 *
 *
 * Messages from servers begin with "MESSAGE ", all characters following
 * this string is actual message and will be displayed in Clients message area.
 */
public class Client {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public Client() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                out.println("GET / HTTP/1.1");
                out.println("Date: " + getServerTime());
                out.println("Content-Length: " + textField.getText().length());
                out.println("\r\n");
                out.println("MESSAGE " + textField.getText());
                messageArea.append("Me: " + textField.getText() + "\n");
                textField.setText("");
            }
        });
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Prompt for and return the desired Partner name.
     */
    private String getPartnerName() {
        return JOptionPane.showInputDialog(
                frame,
                "Whom do you want to talk to?",
                "Friend name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = "127.0.0.1"; //getServerAddress();
        Socket socket = new Socket(serverAddress, 8999);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                String name = getName();
                frame.setTitle(name);
                out.println(name);
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("SUBMITPART")) {
                textField.setEditable(false);
                out.println(getPartnerName());
            } else if (line.startsWith("PARTACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("PARTLEFT")) {
                textField.setEditable(false);
                messageArea.append("Other Client disconnected!!! Kindly run program again for new connection.");
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}