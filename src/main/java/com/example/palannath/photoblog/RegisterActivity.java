package com.example.palannath.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText reg_email;
    private EditText reg_pass;
    private EditText reg_confirm_pass;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();

        reg_email=(EditText)findViewById(R.id.reg_ed1);
        reg_pass=(EditText)findViewById(R.id.reg_ed2);
        reg_confirm_pass=(EditText)findViewById(R.id.reg_ed3);
        reg_btn=(Button)findViewById(R.id.reg_bt1);
        reg_login_btn=(Button)findViewById(R.id.reg_bt2);
        progressBar=(ProgressBar)findViewById(R.id.reg_progressBar);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=reg_email.getText().toString();
                String pass=reg_pass.getText().toString();
                String conpass=reg_confirm_pass.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(conpass)){

                    if(pass.equals(conpass)){
                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent setintent =new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setintent);
                                    finish();
                                }
                                else{
                                    String errormessage =task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error"+errormessage,Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else{
                        Toast.makeText(RegisterActivity.this,"Password doesn't Matches",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent i=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(i);
        finish();
    }
}
