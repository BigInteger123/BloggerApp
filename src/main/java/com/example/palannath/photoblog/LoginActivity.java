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

public class LoginActivity extends AppCompatActivity {
    private EditText emailtext;
    private EditText passtext;
    private Button loginbut;
    private Button loginRes;
    private FirebaseAuth mAuth;
    private ProgressBar loginprocess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        emailtext=(EditText)findViewById(R.id.ed1);
        passtext=(EditText)findViewById(R.id.ed2);
        loginbut=(Button)findViewById(R.id.bt1);
        loginRes=(Button)findViewById(R.id.bt2);
        loginprocess=(ProgressBar)findViewById(R.id.progressBar);

        loginRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent1);
            }
        });

        loginbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginemail=emailtext.getText().toString();
                String loginpass=passtext.getText().toString();

                if(!TextUtils.isEmpty(loginemail) && !TextUtils.isEmpty(loginpass)){
                    loginprocess.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginemail,loginpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                senttoMain();
                            }
                            else{
                                String errorms=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"error"+errorms,Toast.LENGTH_LONG).show();
                            }
                            loginprocess.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            senttoMain();
        }
    }
    private void senttoMain(){
        Intent maintent =new Intent(LoginActivity.this,MainActivity.class);
        startActivity(maintent);
        finish();
    }
}
