# multi-client-server-chat
Server:
Server is Multithreaded. New thread is created for each CLient.
It now listens on 8999 port. 

Server maintains a list of all users online and their availabilty to chat.
Server also maintains a Key-value pair for Client name and its printWriter.
Server also maintains a list map of who is chatting with who.

When a new client connects to server, server keeps requesting client for
name until that name is not in our user list.

When a Client sends a message to Server, server from the available user map list checks the client
it is chatting with. Based on this name, servers fetches the PrintWriter from User Writer Key Value Map.

Server then sends the message to other Client(Printwriter we fetched from last step.)

Server handles user leaving chat. It notifies other user that first user disconneted.
upon user disconnecting, all data relating to that user will be removed.

Server logs all incoming messages in command line.

Client:
A simple Swing-based client for the chat server.  Graphically
it is a frame with a text field for entering messages and a
textarea to see the whole dialog.

The client follows the Chat Protocol which is as follows.
When the server sends "SUBMITNAME" the client replies with the
desired screen name.  
 
The server will keep sending "SUBMITNAME"
requests as long as the client submits screen names that are
already in use.  When the server sends a line beginning
with "NAMEACCEPTED" the client is now allowed to give name
of the other client wants to chat with. The server will keep
sending "SUBMITPART" requests as long as the client submits names that are
not in use i.e a valid Client.  
 
When the server sends a line beginning with "PARTACCEPTED", it
is connected with other client and it can start messaging other Client.

When the server sends a "PARTLEFT" message, it means the other client has left
or disconnected and this client will not be able to send messages anymore.
 
All communications are sent with HTTP GET header. Timestamp and Content Type
Actual message will be prefixed with "MESSAGE " so that server can easily identify message 

 
Messages from servers begin with "MESSAGE ", all characters following
this string is actual message and will be displayed in Clients message area.

How to Run the code:
javac Server.java Client.java
OR
use makefile for compiling (makefile submitted along with code files and readme files)

java Server
java Client
(Run Client program for wach new instance of Client)

NOTE:
Server is implemented using MultiThreading.
