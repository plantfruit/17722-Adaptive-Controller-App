package com.example.microphone;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnector {
    private Thread socketThread;
    private Socket socket;
    private PrintWriter writer;

    private String SERVER_IP = "192.168.1.100";
    private int SERVER_PORT = 5000;

    public ServerConnector() {

    }

    public void connectToServer() {
        SERVER_IP = Constants.ipEt.getText().toString();

        socketThread = new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                writer = new PrintWriter(socket.getOutputStream(), true);

                // Example message
                sendMessage("Hello from Android!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        socketThread.start();
    }

    public void sendMessage(String msg) {
        if (writer != null) {
            writer.println(msg);
        }
    }

    public void sendMessage() {
        if (writer != null) {
            writer.println(Constants.directionLabel.getText());
        }
    }

    public void onDestroy() {
        //super.onDestroy();
        try {
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socketThread != null && socketThread.isAlive()) {
            socketThread.interrupt();
        }
    }
}
