package knock.knock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by pankajverma on 23/11/15.
 */
class KKMultiServerRunnable implements Runnable {

    Socket clientSocket;
    KKMultiServerRunnable(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }
    @Override
    public void run() {
        try {
        	System.out.println("entered into runnable " + Thread.currentThread().getName());
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("opened output streadm");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            // Initiate conversation with client
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
            System.out.println("exited into runnable ");
        }
        catch (IOException e)
        {
            System.out.println("Exception caught when trying to listen on port "
                    + " or listening for a connection");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}