package http.server;
/*
* BasicHttpServer.java
* http://www.prasannatech.net/2008/10/simple-http-server-java.html
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

            System.out.println("The Client " +
                    connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");

            inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            String requestString = inFromClient.readLine();

            //First line is header line
            String headerLine = requestString;

            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();

            if (httpMethod.equals("GET")) {
                handleGetCall(headerLine, httpQueryString);
            } else if (httpMethod.equals("POST")) {
                handlePostCall(headerLine);
                //sendResponse(404, "<b>method not supported ...." +
                //        "Usage: only http GET and POST methods</b>", false);
            } else {
                sendResponse(404, "<b>method not supported ...." +
                        "Usage: only http GET and POST methods</b>", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGetCall(String headerLine, String httpQueryString) throws Exception {
        String requestString = headerLine;
        StringBuilder requestStringBuilder = new StringBuilder("");
        requestStringBuilder.append(requestString + "\n");

        StringBuffer responseBuffer = new StringBuffer();
        responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");


        System.out.println("The HTTP request string is ....");
        while (inFromClient.ready()) {
            // Read the HTTP complete HTTP Query
            requestString = inFromClient.readLine();
            requestStringBuilder.append(requestString + "\n");
        }

        responseBuffer.append("your request is as below : \n " + requestStringBuilder.toString() + "<BR>");
        System.out.println(requestStringBuilder.toString());


        if (httpQueryString.equals("/")) {
            // The default home page
            sendResponse(200, responseBuffer.toString(), false);
        } else {
            //This is interpreted as a file name
            String query = httpQueryString.replaceFirst("/", "");
            query = URLDecoder.decode(query, "UTF8");
            if (new File(query).isFile()) {
                sendResponse(200, query, true);
            } else {
                sendResponse(200, responseBuffer.toString(), false);
            }
        }
    }

    /**
     * POST /users HTTP/1.1
     * Host: localhost:5000
     * Connection: keep-alive
     * Content-Length: 230
     * Cache-Control: no-cache
     * Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop
     * Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydopWcI1OByByO0oF
     * User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36
     * Postman-Token: 213df71a-5250-825b-fbbf-89ee9d867986
     * Accept:
     * Accept-Encoding: gzip, deflate
     * Accept-Language: en-US,en;q=0.8
     * ------WebKitFormBoundarydopWcI1OByByO0oF
     * Content-Disposition: form-data; name="marks"
     * 67
     * ------WebKitFormBoundarydopWcI1OByByO0oF
     * Content-Disposition: form-data; name="user"
     * pankaj
     * ------WebKitFormBoundarydopWcI1OByByO0oF--
     */
    public void handlePostCall(String headerLine) throws Exception {
        //POST request

        StringBuilder requestStringBuilder = new StringBuilder("");
        requestStringBuilder.append(headerLine + "\n");

        System.out.println("POST request");
        String contentLength = null;
        do {
            String requestString = inFromClient.readLine();
            requestStringBuilder.append(requestString + "\n");

            String filename = null;

            if (requestString.indexOf("Content-Type: multipart/form-data") != -1) {
                String boundary = requestString.split("boundary=")[1];
                // The POST boundary

                while (!requestString.contains(boundary) && inFromClient.ready()) {
                    requestString = inFromClient.readLine();
                    requestStringBuilder.append(requestString + "\n");
                }
                while (inFromClient.ready()) {
                    requestString = inFromClient.readLine();
                    requestStringBuilder.append(requestString + "\n");

                    if (requestString.indexOf("Content-Disposition: form-data") != -1) {
                        String name = requestString.split("name=")[1];
                        name = name.replace("\"", "");
                        String value = "";
                        while (true) {
                            requestString = inFromClient.readLine();
                            requestStringBuilder.append(requestString + "\n");
                            if (!requestString.contains(boundary)) {
                                value += requestString;
                            } else {
                                break;
                            }
                        }

                        value = value.trim();
                        System.out.println("post params " + name + "=" + value);
                    }
                }

                System.out.println(requestStringBuilder.toString());
                sendResponse(200, " Uploaded..", false);
            } //if

        } while (inFromClient.ready()); //End of do-while
    }

    public void sendResponse(int statusCode, String responseString, boolean isFile) throws Exception {

        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer" + "\r\n";
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
        } else {
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
        } else {
            responseStringBuilder.append(responseString);
            outToClient.writeBytes(responseStringBuilder.toString());
        }

        System.out.println("response = " + responseStringBuilder.toString());

        outToClient.close();
    }

    public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public static void main(String args[]) throws Exception {

        ServerSocket Server = new ServerSocket(3000);
        System.out.println("TCPServer Waiting for client on port 5000");

        while (true) {
            Socket connected = Server.accept();
            (new BasicHttpServer(connected)).start();
        }
    }
}