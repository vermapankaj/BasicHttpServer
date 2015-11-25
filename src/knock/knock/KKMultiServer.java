package knock.knock;

import java.net.*;
import java.io.*;

public class KKMultiServer {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java knock.knock.KnockKnockServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

		System.out.println("entered into main");
        ServerSocket serverSocket = new ServerSocket(portNumber);

        int count = 1;
        while (true) {
        	System.out.println("just before socket accept call");
            try {
            	Socket clientSocket = serverSocket.accept();
            	System.out.println("just after socket accept.");
				System.out.println("starting new thread.....");
                new Thread(new KKMultiServerRunnable(clientSocket), "t"+ (count++)).start();
				System.out.println("started new thread...");

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }
}

