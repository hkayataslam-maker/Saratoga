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

            String code = colorCode.getText().toString().trim();
            String weightText = tankWeight.getText().toString().trim();

            if (code.isEmpty() || weightText.isEmpty()) {
                result.setText("من فضلك اكتب كود اللون ووزن الحوض");
                return;
            }

            double weight;

            try {
                weight = Double.parseDouble(weightText);
            } catch (Exception e) {
                result.setText("وزن الحوض غير صحيح");
                return;
            }

            if (code.equals("557")) {

                double beige = 150 * weight / 100;
                double burntBrown = 50 * weight / 100;
                double yellow = 3.8 * weight / 100;

                result.setText(
                        "كود اللون: 557\n" +
                        "وزن الحوض: " + weight + " كجم\n\n" +
                        "التركيبة المطلوبة:\n\n" +
                        "بيج: " + format(beige) + " جم\n" +
                        "بني محروق: " + format(burntBrown) + " جم\n" +
                        "أصفر: " + format(yellow) + " جم"
                );

            } else {
                result.setText("كود اللون غير موجود حاليًا");
            }
        });
    }

    private String format(double number) {
        return String.format("%.2f", number);
    }
}
