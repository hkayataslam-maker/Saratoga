package com.saratoga.dyecalculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText colorCode;
    EditText tankWeight;
    Button calculateButton;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorCode = findViewById(R.id.colorCode);
        tankWeight = findViewById(R.id.tankWeight);
        calculateButton = findViewById(R.id.calculateButton);
        result = findViewById(R.id.result);

        calculateButton.setOnClickListener(v -> {

            String code = colorCode.getText().toString();
            String weight = tankWeight.getText().toString();

            if (code.isEmpty() || weight.isEmpty()) {
                result.setText("من فضلك اكتب كود اللون ووزن الحوض");
                return;
            }

            result.setText(
                    "كود اللون: " + code +
                    "\nوزن الحوض: " + weight + " كجم" +
                    "\n\nالتركيبة هتظهر هنا."
            );
        });
    }
}
