package org.shirakumo.ocelot;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

public class FirstTimeSetup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_setup);
        EditText username = ((EditText)findViewById(R.id.setup_username));
        EditText password = ((EditText)findViewById(R.id.setup_password));
        EditText hostname = ((EditText)findViewById(R.id.setup_hostname));
        EditText port = ((EditText)findViewById(R.id.setup_port));
        CheckBox registered = ((CheckBox)findViewById(R.id.setup_registered));

        registered.setOnCheckedChangeListener((vw, ticked)->{
            password.setVisibility(ticked? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.setup_complete).setOnClickListener((vw)->{
            boolean error = false;

            if(32 < username.getText().length()){
                username.setError("The username must be less than 32 characters long.");
                error = true;
            }

            if(password.getText().length() < 6 && registered.isChecked()){
                password.setError("Registered accounts need a password with 6 to 32 characters.");
                error = true;
            }

            if(65535 < Integer.parseInt(port.getText().toString())){
                port.setError("There are not ports above 65535.");
                error = true;
            }

            if(!error) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("setup", true)
                        .putString("username", username.getText().toString())
                        .putString("password", password.getText().toString())
                        .putString("hostname", hostname.getText().toString())
                        .putString("port", port.getText().toString())
                        .putBoolean("autoconnect", ((Switch) findViewById(R.id.setup_autoconnect)).isChecked())
                        .apply();
                finish();
            }
        });
    }
}
