package com.saratoga.dyecalculator;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText colorCode;
    EditText tankWeight;
    Button calculateButton;
    Button saveButton;
    Button searchButton;
    Button addRecipeButton;
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
        addRecipeButton = findViewById(R.id.addRecipeButton);
        result = findViewById(R.id.result);

        preferences = getSharedPreferences("SaratogaRecipes", MODE_PRIVATE);

        calculateButton.setOnClickListener(v -> calculateRecipe());
        saveButton.setOnClickListener(v -> saveRecipe());
        searchButton.setOnClickListener(v -> searchRecipe());
        addRecipeButton.setOnClickListener(v -> showAddRecipeDialog());
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

        if (weight <= 0) {
            result.setText("وزن الحوض لازم يكون أكبر من صفر");
            return;
        }

        String recipe = preferences.getString("recipe_" + code, "");

        if (recipe.isEmpty() && code.equals("557")) {
            recipe =
                    "بيج: 150\n" +
                    "بني محروق: 50\n" +
                    "أصفر: 3.8";
        }

        if (recipe.isEmpty()) {
            result.setText("كود اللون غير موجود حاليًا");
            return;
        }

        String[] lines = recipe.split("\\n");

        StringBuilder output = new StringBuilder();

        output.append("كود اللون: ")
                .append(code)
                .append("\n");

        output.append("وزن الحوض: ")
                .append(format(weight))
                .append(" كجم\n\n");

        output.append("الكميات المطلوبة:\n\n");

        boolean found = false;

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(":", 2);

            if (parts.length < 2) {
                continue;
            }

            String dyeName = parts[0].trim();

            String amountText = parts[1]
                    .replace("جم", "")
                    .replace("/ 100 كجم", "")
                    .replace("/100 كجم", "")
                    .trim();

            try {

                double amountPer100Kg =
                        Double.parseDouble(amountText);

                double requiredAmount =
                        amountPer100Kg * weight / 100.0;

                output.append(dyeName)
                        .append(": ")
                        .append(format(requiredAmount))
                        .append(" جم\n");

                found = true;

            } catch (NumberFormatException e) {
                // تجاهل السطر غير الصحيح
            }
        }

        if (!found) {
            result.setText(
                    "صيغة التركيبة غير صحيحة.\n\n" +
                    "مثال صحيح:\n" +
                    "بيج: 150\n" +
                    "بني محروق: 50\n" +
                    "أصفر: 3.8"
            );
            return;
        }

        result.setText(output.toString());
    }

    private void saveRecipe() {

        String code = colorCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(
                    this,
                    "اكتب كود اللون أولًا",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!code.equals("557")) {
            Toast.makeText(
                    this,
                    "استخدم زر إضافة تركيبة جديدة لإضافة كود جديد",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        preferences.edit()
                .putString(
                        "recipe_557",
                        "بيج: 150\n" +
                        "بني محروق: 50\n" +
                        "أصفر: 3.8"
                )
                .apply();

        Toast.makeText(
                this,
                "تم حفظ تركيبة 557 بنجاح ✅",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void searchRecipe() {

        String code = colorCode.getText().toString().trim();

        if (code.isEmpty()) {
            result.setText("اكتب كود اللون للبحث");
            return;
        }

        String recipe = preferences.getString(
                "recipe_" + code,
                ""
        );

        if (recipe.isEmpty() && code.equals("557")) {
            recipe =
                    "بيج: 150\n" +
                    "بني محروق: 50\n" +
                    "أصفر: 3.8";
        }

        if (!recipe.isEmpty()) {

            result.setText(
                    "🔍 التركيبة المحفوظة\n\n" +
                    "كود اللون: " + code + "\n\n" +
                    recipe
            );

        } else {

            result.setText("لم يتم العثور على التركيبة");
        }
    }

    private void showAddRecipeDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText codeInput = new EditText(this);
        codeInput.setHint("كود اللون");
        codeInput.setInputType(2);

        EditText recipeInput = new EditText(this);
        recipeInput.setHint(
                "اكتب الصبغات كل واحدة في سطر\n\n" +
                "مثال:\n" +
                "بيج: 150\n" +
                "بني محروق: 50\n" +
                "أصفر: 3.8"
        );

        recipeInput.setMinLines(6);
        recipeInput.setGravity(48);

        layout.addView(codeInput);
        layout.addView(recipeInput);

        new AlertDialog.Builder(this)
                .setTitle("➕ إضافة تركيبة جديدة")
                .setView(layout)
                .setPositiveButton("حفظ", (dialog, which) -> {

                    String code =
                            codeInput.getText().toString().trim();

                    String recipe =
                            recipeInput.getText().toString().trim();

                    if (code.isEmpty() || recipe.isEmpty()) {

                        Toast.makeText(
                                this,
                                "اكتب الكود والتركيبة",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    preferences.edit()
                            .putString("recipe_" + code, recipe)
                            .apply();

                    Toast.makeText(
                            this,
                            "تم حفظ التركيبة " + code + " ✅",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private String format(double number) {
        return String.format("%.2f", number);
    }
    }
