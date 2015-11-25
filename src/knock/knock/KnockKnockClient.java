package knock.knock;

import java.io.*;
import java.net.*;

// Implements the client program that speaks to the knock.knock.KnockKnockServer.
//When you start the client program, the server should already be running and listening to the port waiting for a client to request a connection.

public class KnockKnockClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
        // The first thing the client program does is to open a socket that is connected to the server running on a specified host name and port
        // When creating its socket, the knock.knock.KnockKnockClient uses the host name of the first CL argument, the name of the comp on your network that is running the server program knock.knock.KnockKnockServer
        // The knock.knock.KnockKnockClient example uses the second command-line argument as the port number when creating its socket -- this is the remote port number, the number of a port on the server computer and is the port to which knock.knock.KnockKnockServer is listening
        // The following command runs the knock.knock.KnockKnockClient with knockknockserver.example.com as the name of the computer that is running the server program knock.knock.KnockKnockServer and 4444 as the remote port number
        // java knock.knock.KnockKnockClient knockknockserver.example.com 4444
        // The client's socket is bound to any available local port -- a port on the client computer. Remember that the server gets a new socket as well.
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket kkSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(kkSocket.getInputStream()));
        ) {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            // Implements the communication between the client and the server.
            // The server speaks first, so the client must listen first
            // The client does this by reading from the input stream attached to the socket.
            // If the server does speak, it says "Bye." and the client exits the loop.
            // Otherwise the client displays the text to the standard output and then reads the response from the user, who types into the standard input
            // After the user types a carriage return the client sends the text to the server through the output stream attached to the socket
            // The communication ends when the server asks if the client wishes to hear another joke, the client says no, and the server says "Bye."
            // The client automatically closes its input and output streams and the socket because they were created in the try-with-resources statement.
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

    }
}