package com.saratoga.dyecalculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText colorCode;
    EditText tankWeight;
    Button calculateButton;
    Button saveButton;
    TextView result;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorCode = findViewById(R.id.colorCode);
        tankWeight = findViewById(R.id.tankWeight);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        result = findViewById(R.id.result);

        preferences = getSharedPreferences("SaratogaRecipes", MODE_PRIVATE);

        calculateButton.setOnClickListener(v -> calculateRecipe());

        saveButton.setOnClickListener(v -> saveRecipe());
    }

    private void calculateRecipe() {

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
                    "التركيبة:\n\n" +
                    "بيج: " + format(beige) + " جم\n" +
                    "بني محروق: " + format(burntBrown) + " جم\n" +
                    "أصفر: " + format(yellow) + " جم"
            );

        } else {
            result.setText("كود اللون غير موجود حاليًا");
        }
    }

    private void saveRecipe() {

        String code = colorCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "اكتب كود اللون أولًا", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!code.equals("557")) {
            Toast.makeText(this, "التركيبة دي غير موجودة حاليًا", Toast.LENGTH_SHORT).show();
            return;
        }

        preferences.edit()
                .putString("code_557", "557")
                .putString("beige_557", "150")
                .putString("burnt_brown_557", "50")
                .putString("yellow_557", "3.8")
                .apply();

        Toast.makeText(this, "تم حفظ تركيبة 557 بنجاح ✅", Toast.LENGTH_SHORT).show();
    }

    private String format(double number) {
        return String.format("%.2f", number);
    }
        }
