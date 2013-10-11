package erus.android.robotbrain;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RobotBrain extends Activity {

	Button Button1;
	EditText editText1;
	TextView textView1;
	TextView textView2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_robot_brain);
		
		Button1 = (Button) findViewById(R.id.button1);
		editText1 = (EditText) findViewById(R.id.editText1);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		
		Button1.setOnClickListener(new View.OnClickListener() {
			@Override
			
			public void onClick(View arg0) {
				
				Editable a = editText1.getText();
				String b = a.toString();
				new CameraFrontal().execute(Integer.parseInt(b),1,3);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bobot_brain, menu);
		return true;
	}
}