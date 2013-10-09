package com.example.servidor;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.widget.TextView;

public class Servidor extends Activity {

	private ServerSocket serverSocket;
	Handler updateConversationHandler;
	Thread serverThread = null;
	
	Server server = null;
	
	int cont = 0;

	private TextView text;

	public static final int SERVERPORT = 7777;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servidor);

		text = (TextView) findViewById(R.id.text2);

		updateConversationHandler = new Handler();
		
		try
        {
            server = new Server(4568); //Port
            server.start();         
        } catch (IOException e)
        {
            Log.e("arduino52", "Unable to start TCP server", e);
            System.exit(-1);
        }

		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();

	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			try {
				text.setText("Aguardando conexão..." + "\n");
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					socket = serverSocket.accept();
					CommunicationThread commThread = new CommunicationThread(socket);
					new Thread(commThread).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class CommunicationThread implements Runnable {

		private Socket clientSocket;
		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;
			try {
				
				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				try {

					String read = input.readLine();

					updateConversationHandler.post(new updateUIThread(read));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			
			if (msg.charAt(0) == '0'){
				try{ 
					server.send(new byte[] {(byte) 0});
					text.setText(text.getText().toString() + "Envio LED: " + msg.charAt(1) + "\n\n");
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			if (msg.charAt(0) == '1'){
				try{ 
					int parametro1 = msg.charAt(1) - 48;
					int parametro2 = msg.charAt(2) - 48;
					int parametro3 = msg.charAt(3) - 48;
					int parametro4 = msg.charAt(4) - 48;
					int parametro5 = msg.charAt(5) - 48;
						server.send(new byte[] {(byte) 1,(byte) parametro1,(byte) parametro2,(byte) parametro3,(byte) parametro4,(byte) parametro5});
					text.setText("Estado: " + parametro1 + "\n" + "Velocidade: " + parametro2  + "\n" + "Vassoura: " + parametro3   +  "\n" + "Despejo: " + parametro4 + "\n" + "Vibrador: " + parametro5 + "\n" );
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			//text.setText(text.getText().toString() + " Client Says: "+ msg + "\n");
		}
	}
	
    @Override
    protected void onDestroy (){
        super.onDestroy();
        server.stop();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
