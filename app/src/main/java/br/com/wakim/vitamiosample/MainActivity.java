package br.com.wakim.vitamiosample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import io.vov.vitamio.LibsChecker;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	EditText mURI;
	Spinner mSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (! LibsChecker.checkVitamioLibs(this)) {
			return;
		}

		mURI = (EditText) findViewById(R.id.edit_uri);
		mSpinner = (Spinner) findViewById(R.id.spinner);

		findViewById(R.id.btn_open).setOnClickListener(this);

		configureSpinner();
	}

	void configureSpinner() {
		mSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.rmtp_uris)) {
			@Override
			public boolean isEnabled(int position) {
				return !isGroup(position);
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				TextView view = (TextView) super.getDropDownView(position, convertView, parent);

				if (isGroup(position)) {
					view.setTextColor(getResources().getColor(R.color.colorAccent));
				} else {
					view.setTextColor(getResources().getColor(R.color.textColor));
				}

				return view;
			}

			boolean isGroup(int position) {
				return isGroup(getItem(position));
			}

			boolean isGroup(String item) {
				return !item.startsWith("rtmp");
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_open) {
			String uriText = mURI.getText().toString();

			if(! uriText.isEmpty()) {
				playVideo(uriText);
			} else {
				String selectedUri = (String) mSpinner.getSelectedItem();
				playVideo(selectedUri);
			}
		}
	}

	public void playVideo(String uriText) {
		Uri uri = Uri.parse(uriText);
		Intent i = new Intent(this, PlayerActivity.class)
					.putExtra(PlayerActivity.URI_EXTRA, uri);

		startActivity(i);
	}
}
