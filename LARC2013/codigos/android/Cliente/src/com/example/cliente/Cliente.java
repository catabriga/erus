package com.example.cliente;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Cliente extends Activity {

private Socket socket;

	Estado estado;

	int velocidade = 0;
	int sentido;//1 horario, 0 antihorario

	private static final int SERVERPORT = 7777;
	//private static final String SERVER_IP = "192.168.2.254";
	//private static final String SERVER_IP = "192.168.2.103";
	private static final String SERVER_IP = "192.168.43.255";

	ToggleButton tbtnUp;
	ToggleButton tbtnDown;
	ToggleButton tbtnLeft;
	ToggleButton tbtnRight;
	ToggleButton tbtnBroom;
	ToggleButton tbtnCloseGate;
	ToggleButton tbtnOpenGate;
	ToggleButton tbtnVibrate;
	ToggleButton tbtnOnOff;
	RadioGroup radioGroup1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cliente);
		estado = new Estado();
		
		tbtnUp = (ToggleButton)findViewById(R.id.tbtnUp);
		tbtnDown = (ToggleButton)findViewById(R.id.tbtnDown);
		tbtnLeft = (ToggleButton)findViewById(R.id.tbtnLeft);
		tbtnRight = (ToggleButton)findViewById(R.id.tbtnRight);
		tbtnBroom = (ToggleButton)findViewById(R.id.tbtnBroom);
		tbtnCloseGate = (ToggleButton)findViewById(R.id.tbtnCloseGate);
		tbtnOpenGate = (ToggleButton)findViewById(R.id.tbtnOpenGate);
		tbtnVibrate = (ToggleButton)findViewById(R.id.tbtnVibrate);
		tbtnOnOff = (ToggleButton)findViewById(R.id.tbtnOnOff);
		radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
		
		tbtnUp.setOnCheckedChangeListener(listenerUp);
		tbtnDown.setOnCheckedChangeListener(listenerDown);
		tbtnLeft.setOnCheckedChangeListener(listenerLeft);
		tbtnRight.setOnCheckedChangeListener(listenerRight);
		tbtnBroom.setOnCheckedChangeListener(listenerBroom);
		tbtnOpenGate.setOnCheckedChangeListener(listenerOpenGate);
		tbtnCloseGate.setOnCheckedChangeListener(listenerCloseGate);
		tbtnVibrate.setOnCheckedChangeListener(listenerVibrate);
		tbtnOnOff.setOnCheckedChangeListener(listenerOnOff);
        
        

	
		new Thread(new ClientThread()).start();
	}
	
	OnCheckedChangeListener listenerUp = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		        	tbtnUp.setBackgroundResource(R.drawable.up_on);
		        	tbtnDown.setChecked(false);
		        	
		        	//define tipo de movimento
		        	if(tbtnLeft.isChecked())
		        		estado.robo[1] = 3;
		        	else if (tbtnRight.isChecked())
		        		estado.robo[1] = 4;
		        	else 
		        		estado.robo[1] = 1;
		        	
		        	
		        	//define velocidade
		        	int radioButtonID = radioGroup1.getCheckedRadioButtonId();
		        	View radioButton = radioGroup1.findViewById(radioButtonID);
		        	int velocidade = radioGroup1.indexOfChild(radioButton); 
		        	estado.robo[2] = velocidade;
		        	
		        	
		        	//define vassoura
		        	if(tbtnBroom.isChecked())
		        		estado.robo[4] = 1;
		        	else
		        		estado.robo[4] = 0;
		        	
		        	
		        	
		        	//define despejo de latas
		        	if(tbtnOpenGate.isChecked())
		        		estado.robo[4] = 1;
		        	else if (tbtnCloseGate.isChecked())
		        		estado.robo[4] = 2;
		        	else 
		        		estado.robo[4] = 0;

		        	
		        	//define vibrador
		        	if(tbtnVibrate.isChecked())
		        		estado.robo[5] = 1;
		        	else
		        		estado.robo[5] = 0;
		        }
		        else
		        {
		        	tbtnUp.setBackgroundResource(R.drawable.up_off);
		        	
		        	if(tbtnLeft.isChecked())
		        		estado.robo[1] = 5;
		        	else if (tbtnRight.isChecked())
		        		estado.robo[1] = 6;
		        	else
		        		estado.robo[1] = 0;
		        }
	        	enviaEstado();
			}
		}
	};
	
	OnCheckedChangeListener listenerDown = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		
			if(tbtnOnOff.isChecked()){
				if(isChecked)
			    {
					tbtnDown.setBackgroundResource(R.drawable.down_on);
					tbtnUp.setChecked(false);
					
					//define tipo de movimento
		        	if(tbtnLeft.isChecked())
		        		estado.robo[1] = 7;
		        	else if (tbtnRight.isChecked())
		        		estado.robo[1] = 8;
		        	else 
		        		estado.robo[1] = 2;
		        	
		        	//define velocidade
		        	int radioButtonID = radioGroup1.getCheckedRadioButtonId();
		        	View radioButton = radioGroup1.findViewById(radioButtonID);
		        	int velocidade = radioGroup1.indexOfChild(radioButton); 
		        	estado.robo[2] = velocidade;
		        	
		        	//define vassoura
		        	if(tbtnBroom.isChecked())
		        		estado.robo[4] = 1;
		        	else
		        		estado.robo[4] = 0;
		        	
		        	
		        	//define despejo de latas
		        	if(tbtnOpenGate.isChecked())
		        		estado.robo[4] = 1;
		        	else if (tbtnCloseGate.isChecked())
		        		estado.robo[4] = 2;
		        	else 
		        		estado.robo[4] = 0;

		        	//define vibrador
		        	if(tbtnVibrate.isChecked())
		        		estado.robo[5] = 1;
		        	else
		        		estado.robo[5] = 0;		        
			    }
			    else
			    {
			    	tbtnDown.setBackgroundResource(R.drawable.down_off);
		        	if(tbtnLeft.isChecked())
		        		estado.robo[1] = 5;
		        	else if (tbtnRight.isChecked())
		        		estado.robo[1] = 6;
		        	else
		        		estado.robo[1] = 0;
		        }
	        	enviaEstado();
		    }
		}
	};

	OnCheckedChangeListener listenerLeft = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		            //led("1");
		        	tbtnLeft.setBackgroundResource(R.drawable.left_on);
		        	tbtnRight.setChecked(false);
		        	
		    		//define tipo de movimento
		        	if(tbtnUp.isChecked())
		        		estado.robo[1] = 3;
		        	else if (tbtnDown.isChecked())
		        		estado.robo[1] = 7;
		        	else 
		        		estado.robo[1] = 5;
		        	
		        	//define velocidade
		        	int radioButtonID = radioGroup1.getCheckedRadioButtonId();
		        	View radioButton = radioGroup1.findViewById(radioButtonID);
		        	int velocidade = radioGroup1.indexOfChild(radioButton); 
		        	estado.robo[2] = velocidade;
		        	
		        	//define vassoura
		        	if(tbtnBroom.isChecked())
		        		estado.robo[4] = 1;
		        	else
		        		estado.robo[4] = 0;
		        	
		        	
		        	//define despejo de latas
		        	if(tbtnOpenGate.isChecked())
		        		estado.robo[4] = 1;
		        	else if (tbtnCloseGate.isChecked())
		        		estado.robo[4] = 2;
		        	else 
		        		estado.robo[4] = 0;

		        	//define vibrador
		        	if(tbtnVibrate.isChecked())
		        		estado.robo[5] = 1;
		        	else
		        		estado.robo[5] = 0;
		        }
		        else
		        {
		        	tbtnLeft.setBackgroundResource(R.drawable.left_off);
		        	
		        	if(tbtnUp.isChecked())
		        		estado.robo[1] = 1;
		        	else if (tbtnDown.isChecked())
		        		estado.robo[1] = 2;
		        	else
		        		estado.robo[1] = 0;
		        }
	        	enviaEstado();
			}
		}
	};

	OnCheckedChangeListener listenerRight = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		            //led("1");
		        	tbtnRight.setBackgroundResource(R.drawable.right_on);
		        	tbtnLeft.setChecked(false);
		    
		        	//define tipo de movimento
		        	if(tbtnUp.isChecked())
		        		estado.robo[1] = 4;
		        	else if (tbtnDown.isChecked())
		        		estado.robo[1] = 8;
		        	else 
		        		estado.robo[1] = 6;
		        	
		        	//define velocidade
		        	int radioButtonID = radioGroup1.getCheckedRadioButtonId();
		        	View radioButton = radioGroup1.findViewById(radioButtonID);
		        	int velocidade = radioGroup1.indexOfChild(radioButton); 
		        	estado.robo[2] = velocidade;
		        	
		        	//define vassoura
		        	if(tbtnBroom.isChecked())
		        		estado.robo[4] = 1;
		        	else
		        		estado.robo[4] = 0;
		        	
		        	
		        	//define despejo de latas
		        	if(tbtnOpenGate.isChecked())
		        		estado.robo[4] = 1;
		        	else if (tbtnCloseGate.isChecked())
		        		estado.robo[4] = 2;
		        	else 
		        		estado.robo[4] = 0;

		        	//define vibrador
		        	if(tbtnVibrate.isChecked())
		        		estado.robo[5] = 1;
		        	else
		        		estado.robo[5] = 0;

		        }
		        else
		        {
		        	tbtnRight.setBackgroundResource(R.drawable.right_off);
		        	if(tbtnUp.isChecked())
		        		estado.robo[1] = 1;
		        	else if (tbtnDown.isChecked())
		        		estado.robo[1] = 2;
		        	else
		        		estado.robo[1] = 0;
		        }
	        	enviaEstado();
			}
		}
	};

	OnCheckedChangeListener listenerBroom= new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		            //led("1");
		        	tbtnBroom.setBackgroundResource(R.drawable.broom_on);
		        	estado.robo[3] = 1;
		        }
		        else
		        {
		        	tbtnBroom.setBackgroundResource(R.drawable.broom_off);
		        	estado.robo[3] = 0;
		        }
		      	enviaEstado();
			}
		}
	};	

	OnCheckedChangeListener listenerOpenGate= new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		        	tbtnOpenGate.setBackgroundResource(R.drawable.open_gate_on);
		        	tbtnCloseGate.setChecked(false);
		        	estado.robo[4] = 1;
		        }
		        else
		        {
		        	tbtnOpenGate.setBackgroundResource(R.drawable.open_gate_off);
		        	estado.robo[4] = 0;
		        }
		      	enviaEstado();
			}
		}
	};	
	
	OnCheckedChangeListener listenerCloseGate= new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		        	tbtnCloseGate.setBackgroundResource(R.drawable.close_gate_on);
		        	tbtnOpenGate.setChecked(false);
		        	estado.robo[4] = 2;
		        }
		        else
		        {
		        	tbtnCloseGate.setBackgroundResource(R.drawable.close_gate_off);
		        	estado.robo[4] = 0;
		        }
		      	enviaEstado();
			}
		}
	};
	
	OnCheckedChangeListener listenerVibrate= new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			if(tbtnOnOff.isChecked()){
		        if(isChecked)   // If button checked
		        {
		        	tbtnVibrate.setBackgroundResource(R.drawable.vibrate_on);
		           	estado.robo[5] = 1;
		        }
		        else
		        {
		        	tbtnVibrate.setBackgroundResource(R.drawable.vibrate_off);
		           	estado.robo[5] = 0;
		        }
			}
	      	enviaEstado();
		}
	};
	
	OnCheckedChangeListener listenerOnOff= new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		
		        if(isChecked)   // If button checked
		        {
		        	tbtnOnOff.setBackgroundResource(R.drawable.on);
		        	estado.robo[0] = 1;
		        }
		        else
		        {
		        	for (int i = 0; i < 6; i++)
		        		estado.robo[1] = 0;
		        	
		        	tbtnOnOff.setBackgroundResource(R.drawable.off);
		        	tbtnUp.setBackgroundResource(R.drawable.up_off);
		        	tbtnDown.setBackgroundResource(R.drawable.down_off);
		        	tbtnLeft.setBackgroundResource(R.drawable.left_off);
		        	tbtnRight.setBackgroundResource(R.drawable.right_off);
		        	tbtnBroom.setBackgroundResource(R.drawable.broom_off);
		        	tbtnVibrate.setBackgroundResource(R.drawable.vibrate_off);
		        	tbtnOpenGate.setBackgroundResource(R.drawable.open_gate_off);
		        	tbtnCloseGate.setBackgroundResource(R.drawable.close_gate_off);
		        	
		        	
		        }
		      	enviaEstado();
		}
	};	
	
	
	void enviaEstado()
	{
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
			out.println(Integer.toString(estado.robo[0]) + Integer.toString(estado.robo[1]) + Integer.toString(estado.robo[2]) + Integer.toString(estado.robo[3]) + Integer.toString(estado.robo[4]) + Integer.toString(estado.robo[5]));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void led(String acao)
	{
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
			out.println("0" + acao);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class listenerSeekbar implements SeekBar.OnSeekBarChangeListener {

	        public void onProgressChanged(SeekBar seekBar, int progress,
	                boolean fromUser) {
	        	
	        	rotacao();
	        	//textView2.setText(Integer.toString(velocidade));

	            	
	            	if (velocidade >= 80 && velocidade <= 100)
	            		velocidade = 1; //Delay de 5
	            	else if (velocidade >= 60 && velocidade < 80)
	            		velocidade = 2; //Delay de 10
	            	else if (velocidade >= 40 && velocidade < 60)
	            		velocidade = 3; //Delay de 20
	            	else if (velocidade >= 20 && velocidade < 40)
	            		velocidade = 4; //Delay de 30
	            	else if (velocidade >= 10 && velocidade < 20)
	            		velocidade = 5; //Delay de 50
	            	else if (velocidade > 0 && velocidade < 10)
	            		velocidade = 6; //Delay de 100
	            	else if (velocidade == 0)
	            		velocidade = 0;
	            	
	            	
	            	try {
		    			PrintWriter out = new PrintWriter(new BufferedWriter(
		    					new OutputStreamWriter(socket.getOutputStream())),
		    					true);
		    			out.println("1" + sentido + velocidade);
		    		} catch (UnknownHostException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    		}
	        }

	        public void onStartTrackingTouch(SeekBar seekBar) {}

	        public void onStopTrackingTouch(SeekBar seekBar) {}

	    }
	 	 
	public void rotacao()
	    {
	    	/*int progress = bar.getProgress();
	    	if (progress > 50){
	    		sentido = 0;
	    		velocidade = 2*(progress-50);
	    	}
	    	else
	    	{
	    		sentido = 1;
	    		velocidade = 100 - 2*progress;
	    	} */       	
	    }

	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

				socket = new Socket(serverAddr, SERVERPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cliente, menu);
		return true;
	}
}

