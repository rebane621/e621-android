package de.e621.rebane.activities;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.logging.Logger;

import de.e621.rebane.a621.R;

public class LoginActivity extends DrawerWrapper implements View.OnClickListener {

    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        onCreateDrawer();   //DrawerWrapper function that requires the layout to be set to prepare the drawer
        DrawerWrapper.openActivity = this.getClass();   //block the drawer from reopening this activity while opened

        username = (EditText) findViewById(R.id.txtUsername);
        password = (EditText) findViewById(R.id.txtPassword);

        password.setTransformationMethod(new FancyPasswordInput());

        ((Button)findViewById(R.id.bnLogin)).setOnClickListener(this);
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
    };
}

