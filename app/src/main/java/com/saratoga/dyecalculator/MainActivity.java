package com.saratoga.dyecalculator;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("Saratoga");
        textView.setTextSize(28);
        textView.setPadding(40, 100, 40, 40);

        setContentView(textView);
    }
}
