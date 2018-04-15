package ro.pub.cs.systems.eim.lab06.pheasantgame.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Utilities;

public class ServerCommunicationThread extends Thread {

    private Socket socket;
    private TextView serverHistoryTextView;

    private Random random = new Random();

    private String expectedWordPrefix = new String();

    public ServerCommunicationThread(Socket socket, TextView serverHistoryTextView) {
        if (socket != null) {
            this.socket = socket;
            Log.d(Constants.TAG, "[SERVER] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        }
        this.serverHistoryTextView = serverHistoryTextView;
    }

    private String getPh(String message, boolean isFirst) {
        if (message.equals(Constants.END_GAME))
            return message;

        if (!Utilities.wordValidation(message)) {
            Log.d(Constants.TAG, "Server: invalid word " + message + " :(");
            return message;
        }

        if (isFirst)
            expectedWordPrefix = message.substring(0, 2);

        if (!expectedWordPrefix.equals(message.substring(0, 2)))
            return message;

        String prefix = message.substring(message.length() - 2);
        List<String> results = Utilities.getWordListStartingWith(prefix);

        if (results.isEmpty())
            return Constants.END_GAME;

        String result = results.get(random.nextInt(results.size() - 1));
        expectedWordPrefix = result.substring(result.length() - 2);
        Log.d(Constants.TAG, expectedWordPrefix);

        return result;
    }

    public void run() {
        try {
            if (socket == null) {
                return;
            }
            boolean isRunning = true;
            BufferedReader requestReader = Utilities.getReader(socket);
            PrintWriter responsePrintWriter = Utilities.getWriter(socket);
            boolean isFirst = true;

            while (isRunning) {
                // TODO exercise 7a
                final String message = requestReader.readLine();

                serverHistoryTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        serverHistoryTextView.setText("Server received " + message + "\n" + serverHistoryTextView.getText().toString());
                    }
                });
                final String response = getPh(message, isFirst);
                isFirst = false;

                responsePrintWriter.println(response);
                serverHistoryTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        serverHistoryTextView.setText("Server sent " + response + "\n" + serverHistoryTextView.getText().toString());
                    }
                });
                if (message.equals(Constants.END_GAME)) {
                    isRunning = false;
                }
            }
            serverHistoryTextView.post(new Runnable() {
                @Override
                public void run() {
                    serverHistoryTextView.setText("Game ended!\n" + serverHistoryTextView.getText().toString());
                }
            });
            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}
