package com.huettner.se2_0;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Observable;

public class MainActivity extends AppCompatActivity {


    private EditText inputField;

    private TextView outputField;

    private Button serverButton;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private PrintStream ps;

    private static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputField = (EditText) findViewById(R.id.editInputField);
        outputField = (TextView) findViewById(R.id.answerView);
        serverButton = (Button) findViewById(R.id.button);
        NetworkThread network = new NetworkThread();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.getData().containsKey("answer")) {
                    outputField.setText(msg.getData().get("answer").toString());
                    serverButton.setEnabled(true);
                }else  if(msg.getData().containsKey("calculationAnswer")) {
                    outputField.setText(msg.getData().get("calculationAnswer").toString());
                }
            }
        };


    }


    public class NetworkThread extends Thread {

        public NetworkThread() {

        }

        public void run() {
            if (socket == null) {
                try {
                    Socket socket = new Socket("se2-isys.aau.at", 53212);
                    OutputStream out = socket.getOutputStream();
                    ps = new PrintStream(out, true);
                    writer = new BufferedWriter(new OutputStreamWriter(ps));
                    InputStream in = socket.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));

                } catch (IOException e) {

                    throw new RuntimeException(e);
                }
            }
            String sendToServer = inputField.getText().toString();
            try {
                ps.println(sendToServer);

                Log.i("SE_2", "Send " + sendToServer);

                while (!reader.ready()) {
                    Thread.sleep(10);
                    Log.i("SE_2", "Waiting for an answer...");
                }
                String answer = "";
                while (reader.ready()) {
                    answer += reader.readLine();

                }
                Log.i("SE_2", "Answered " + answer);

                sendAnswer(answer,"answer");

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendToServer(View view) {
        serverButton.setEnabled(false);
        new NetworkThread().start();

    }

    public void calculate(View view) {

        String input = inputField.getText().toString();
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            sum += Integer.parseInt(input.charAt(i) + "");
        }


        sendAnswer("" +  Integer.toBinaryString(sum),"calculationAnswer");


    }

    public void sendAnswer(String answer, String type) {

        Message handleMessage = new Message();
        Bundle b = new Bundle();
        b.putString(type, answer);
        handleMessage.setData(b);
        handler.sendMessage(handleMessage);
    }


}