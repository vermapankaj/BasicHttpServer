package http.server;
/*
* BasicHttpServer.java
* Author: S.Prasanna
* @version 1.00
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class BasicHttpServer extends Thread {

    static final String HTML_START =
            "<html>" +
                    "<title>HTTP Server in java</title>" +
                    "<body>";

    static final String HTML_END =
            "</body>" +
                    "</html>";

    Socket connectedClient = null;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;


    public BasicHttpServer(Socket client) {
        connectedClient = client;
    }

    public void run() {

        try {

            System.out.println( "The Client "+
                    connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");

            inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            String requestString = inFromClient.readLine();

            StringBuilder requestStringBuilder = new StringBuilder("");
            requestStringBuilder.append(requestString + "\n");

            //First line is header line
            String headerLine = requestString;

            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();

            StringBuffer responseBuffer = new StringBuffer();
            responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");

            System.out.println("The HTTP request string is ....");
            while (inFromClient.ready())
            {
                // Read the HTTP complete HTTP Query
                requestString = inFromClient.readLine();
                requestStringBuilder.append(requestString + "\n");
            }

            responseBuffer.append("your request is as below : \n "+ requestStringBuilder.toString() + "<BR>");
            System.out.println(requestStringBuilder.toString());

            if (httpMethod.equals("GET")) {
                if (httpQueryString.equals("/")) {
                    // The default home page
                    sendResponse(200, responseBuffer.toString(), false);
                } else {
                    //This is interpreted as a file name
                    String query = httpQueryString.replaceFirst("/", "");
                    query = URLDecoder.decode(query, "UTF8");
                    if (new File(query).isFile()){
                        sendResponse(200, query, true);
                    }
                    else {
                        sendResponse(200, responseBuffer.toString(), false);
                    }
                }
            }
            else if (httpMethod.equals("POST")) {

                sendResponse(200, responseBuffer.toString() + "This is a post call", false);
            }
            else {
                sendResponse(404, "<b>method not supported ...." +
                        "Usage: only http GET and POST methods</b>", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {

        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer"+"\r\n";
        String contentLengthLine = null;
        String fileName = null;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        FileInputStream fin = null;

        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

        if (isFile) {
            fileName = responseString;
            fin = new FileInputStream(fileName);
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
            if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
                contentTypeLine = "Content-Type: \r\n";
        }
        else {
            responseString = BasicHttpServer.HTML_START + responseString + BasicHttpServer.HTML_END;
            contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
        }

        StringBuilder responseStringBuilder = new StringBuilder("");
        responseStringBuilder.append(statusLine);
        responseStringBuilder.append(serverdetails);
        responseStringBuilder.append(contentTypeLine);
        responseStringBuilder.append(contentLengthLine);
        responseStringBuilder.append("Connection: close\r\n");
        responseStringBuilder.append("\r\n");

        //Serving html files from here
        if (isFile) {
            outToClient.writeBytes(responseStringBuilder.toString());
            sendFile(fin, outToClient);
        }
        else {
            responseStringBuilder.append(responseString);
            outToClient.writeBytes(responseStringBuilder.toString());
        }

        System.out.println("response = "+ responseStringBuilder.toString());

        outToClient.close();
    }

    public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
        byte[] buffer = new byte[1024] ;
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1 ) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public static void main (String args[]) throws Exception {

        ServerSocket Server = new ServerSocket (5000);
        System.out.println ("TCPServer Waiting for client on port 5000");

        while(true) {
            Socket connected = Server.accept();
            (new BasicHttpServer(connected)).start();
        }
    }
}