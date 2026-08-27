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
    Button searchButton;
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
        searchButton = findViewById(R.id.searchButton);
        result = findViewById(R.id.result);

        preferences = getSharedPreferences("SaratogaRecipes", MODE_PRIVATE);

        calculateButton.setOnClickListener(v -> calculateRecipe());
        saveButton.setOnClickListener(v -> saveRecipe());
        searchButton.setOnClickListener(v -> searchRecipe());
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

    private void searchRecipe() {

        String code = colorCode.getText().toString().trim();

        if (code.isEmpty()) {
            result.setText("اكتب كود اللون للبحث");
            return;
        }

        if (code.equals("557")) {

            String savedCode = preferences.getString("code_557", "");

            if (savedCode.isEmpty()) {
                result.setText("التركيبة 557 غير محفوظة");
                return;
            }

            result.setText(
                    "🔍 التركيبة المحفوظة\n\n" +
                    "كود اللون: 557\n\n" +
                    "بيج: 150 جم / 100 كجم\n" +
                    "بني محروق: 50 جم / 100 كجم\n" +
                    "أصفر: 3.8 جم / 100 كجم"
            );

        } else {
            result.setText("لم يتم العثور على التركيبة");
        }
    }

    private String format(double number) {
        return String.format("%.2f", number);
    }
    }
