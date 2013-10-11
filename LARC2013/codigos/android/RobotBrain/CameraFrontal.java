package erus.android.robotbrain;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * 
 * @author daniel
 * Primeiro Argumento: Tipo do argumento passado para o metodo doInBackground
 * Segundo Argumento: Tipo do argumento passado para o metodo onProgressUpdate  
 * Terceiro Argumento: Tipo de retorno do metodo doInBackground
 */

public class CameraFrontal extends AsyncTask<Object, Object, Object> {

	protected void onPreExecute()
	{
		
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		Activity atividade = (Activity) arg0[0];
		
		return null;
	}
	
	protected void onPostExecute(Integer result){
	//	textView2.setText(Integer.toString(result));
	}
	
    protected void onProgressUpdate(Integer... arg) {
    //	String aux = textView1.getText().toString();
    //	textView1.setText(Integer.toString((int) ((arg[0] / (float) arg[1] * 100))));
    }
}
