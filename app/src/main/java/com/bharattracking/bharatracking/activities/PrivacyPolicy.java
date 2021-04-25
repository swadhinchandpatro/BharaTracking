package com.bharattracking.bharatracking.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PrivacyPolicy extends AppCompatActivity {

    StringBuilder sb = new StringBuilder();
    private TextView policyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        initView();

        displayPolicy("policy.txt");
        policyTextView.setText(Html.fromHtml(sb.toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        policyTextView = findViewById(R.id.policy_content);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayPolicy(String filename) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));

            String mLine;
            while ((mLine = reader.readLine()) != null){
                sb.append(mLine);
            }
        }catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Error Reading file!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (reader !=null){
                try{
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
