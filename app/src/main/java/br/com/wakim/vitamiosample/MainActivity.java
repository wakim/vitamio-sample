package br.com.wakim.vitamiosample;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.vov.vitamio.LibsChecker;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	EditText mURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (! LibsChecker.checkVitamioLibs(this)) {
			return;
		}

		mURI = (EditText) findViewById(R.id.edit_uri);

		findViewById(R.id.btn_open).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_open) {
			String uriText = mURI.getText().toString();

			try {
				Uri uri = Uri.parse(uriText);
			} catch(Exception e) {
				Toast.makeText(this, "URI Invalida!", Toast.LENGTH_LONG).show();
			}
		}
	}
}
