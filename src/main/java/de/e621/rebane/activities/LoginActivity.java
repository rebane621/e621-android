package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import de.e621.rebane.a621.R;

public class LoginActivity extends DrawerWrapper implements View.OnClickListener {

    EditText username, password;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_login, savedInstanceState);
        //setContentView(R.layout.activity_login);
        onCreateDrawer(this.getClass());   //DrawerWrapper function that requires the layout to be set to prepare the drawer

        username = (EditText) findViewById(R.id.txtUsername);
        password = (EditText) findViewById(R.id.txtPassword);

        password.setTransformationMethod(new FancyPasswordInput());

        findViewById(R.id.bnLogin).setOnClickListener(this);
    }

    @Override public void onClick(View view) {
        if (view.getId() == R.id.bnLogin) {
            login.login(username.getText().toString(), password.getText().toString(), findViewById(R.id.login_progress), this);
        }
    }

    private class FancyPasswordInput extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private char[] lel = new char[]{'◢', '◣', '◥', '◤'};
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return lel[index%4];
            }
            public int length() {
                return mSource.length();
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }
}

