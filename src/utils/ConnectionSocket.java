/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dawid
 */
public class ConnectionSocket {

    private Logger logger = Logger.getLogger("connectionSocket");
    private ServerSocket serverSocket;
    private Socket socket;
    
    private PrintWriter printWriter;

    private int port = 4333;


    public ConnectionSocket() throws IOException {
        serverSocket = new ServerSocket(port, 1);        
    }

    public void initReader(OnGetMessage callback) throws IOException {
        while (true) {
            socket = serverSocket.accept();
            printWriter = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
            
            System.out.println("The following client has connected:" + socket.getInetAddress().getCanonicalHostName());
            //A client has connected to this server. Send welcome message
            Thread thread = new Thread(new SocketClientHandler(socket, callback));
            thread.start();
        }

    }

    public void sendMessage(String message){
            printWriter.println(message);
            printWriter.flush();        
    }

    public interface OnGetMessage {

        public void onGetMessage(String message);
    }

    public class SocketClientHandler implements Runnable {

        private Socket client;
        private OnGetMessage callback;

        public SocketClientHandler(Socket client, OnGetMessage callback) {
            this.client = client;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String userInput;    
                while ((userInput = stdIn.readLine()) != null) {
                    callback.onGetMessage(userInput);
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ConnectionSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
