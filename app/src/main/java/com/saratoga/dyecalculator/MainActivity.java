package com.saratoga.dyecalculator;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText colorCode;
    EditText tankWeight;
    Button calculateButton;
    Button saveButton;
    Button searchButton;
    Button addRecipeButton;
    TextView result;

    SharedPreferences preferences;

    // لمنع إعادة الحساب أثناء تحديث واجهة النتيجة
    private boolean isUpdating = false;

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

        preferences = getSharedPreferences(
                "SaratogaRecipes",
                MODE_PRIVATE
        );

        // زر الحساب ما زال موجود ويعمل
        calculateButton.setOnClickListener(v -> calculateRecipe());

        saveButton.setOnClickListener(v -> saveCurrentRecipe());

        searchButton.setOnClickListener(v -> searchRecipe());

        addRecipeButton.setOnClickListener(v -> showAddRecipeDialog());

        // ==========================================
        // الحساب التلقائي عند تغيير وزن الحوض
        // ==========================================

        tankWeight.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                if (!isUpdating) {
                    calculateAutomatically();
                }
            }

            @Override
            public void afterTextChanged(
                    Editable s
            ) {
            }
        });

        // ==========================================
        // الحساب التلقائي عند تغيير كود اللون
        // ==========================================

        colorCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                if (!isUpdating) {
                    calculateAutomatically();
                }
            }

            @Override
            public void afterTextChanged(
                    Editable s
            ) {
            }
        });
    }

    // =====================================================
    // الحساب التلقائي
    // =====================================================

    private void calculateAutomatically() {

        String code = colorCode.getText()
                .toString()
                .trim();

        String weightText = tankWeight.getText()
                .toString()
                .trim();

        // لو إحدى الخانتين فاضية، لا تعمل حساب
        if (code.isEmpty() || weightText.isEmpty()) {
            return;
        }

        double tank;

        try {

            tank = Double.parseDouble(
                    weightText.replace(",", ".")
            );

        } catch (Exception e) {
            return;
        }

        if (tank <= 0) {
            return;
        }

        String recipe = preferences.getString(
                "recipe_" + code,
                ""
        );

        // تركيبة 557 الافتراضية
        if (recipe.isEmpty() && code.equals("557")) {
            recipe = createDefault557();
        }

        if (recipe.isEmpty()) {

            result.setText(
                    "كود اللون " + code +
                            " غير موجود حاليًا"
            );

            return;
        }

        try {

            JSONArray colors =
                    new JSONArray(recipe);

            showCalculatedResult(
                    code,
                    tank,
                    colors
            );

        } catch (Exception e) {

            result.setText(
                    "حدث خطأ في قراءة التركيبة"
            );
        }
    }

    // =====================================================
    // حساب التركيبة بالزر
    // =====================================================

    private void calculateRecipe() {

        String code = colorCode.getText()
                .toString()
                .trim();

        String weightText = tankWeight.getText()
                .toString()
                .trim();

        if (code.isEmpty()) {

            result.setText(
                    "اكتب كود اللون أولًا"
            );

            return;
        }

        if (weightText.isEmpty()) {

            result.setText(
                    "اكتب وزن الحوض أولًا"
            );

            return;
        }

        double tank;

        try {

            tank = Double.parseDouble(
                    weightText.replace(",", ".")
            );

        } catch (Exception e) {

            result.setText(
                    "وزن الحوض غير صحيح"
            );

            return;
        }

        if (tank <= 0) {

            result.setText(
                    "وزن الحوض يجب أن يكون أكبر من صفر"
            );

            return;
        }

        String recipe = preferences.getString(
                "recipe_" + code,
                ""
        );

        // تركيبة 557 الافتراضية
        if (recipe.isEmpty() && code.equals("557")) {
            recipe = createDefault557();
        }

        if (recipe.isEmpty()) {

            result.setText(
                    "كود اللون " + code +
                            " غير موجود حاليًا"
            );

            return;
        }

        try {

            JSONArray colors =
                    new JSONArray(recipe);

            showCalculatedResult(
                    code,
                    tank,
                    colors
            );

        } catch (Exception e) {

            result.setText(
                    "حدث خطأ في قراءة التركيبة"
            );
        }
    }

    // =====================================================
    // عرض النتيجة
    // =====================================================

    private void showCalculatedResult(
            String code,
            double tank,
            JSONArray colors
    ) throws Exception {

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                20,
                20,
                20,
                20
        );

        TextView title =
                new TextView(this);

        title.setText(
                "النتيجة النهائية\n\n" +
                        "كود اللون: " + code +
                        "\n" +
                        "وزن الحوض: " +
                        format(tank) +
                        " كجم"
        );

        title.setTextSize(21);

        title.setTextColor(
                Color.rgb(30, 30, 30)
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                10,
                10,
                25
        );

        container.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "الكميات المطلوبة"
        );

        subtitle.setTextSize(18);

        subtitle.setGravity(
                Gravity.CENTER
        );

        subtitle.setPadding(
                5,
                5,
                5,
                15
        );

        container.addView(subtitle);

        for (
                int i = 0;
                i < colors.length();
                i++
        ) {

            JSONObject item =
                    colors.getJSONObject(i);

            String name =
                    item.getString("name");

            double baseWeight =
                    item.getDouble("weight");

            // ==========================================
            // الحساب:
            // التركيبة الأصلية لكل 100 كجم
            // الكمية المطلوبة = الوزن الأصلي × وزن الحوض ÷ 100
            // ==========================================

            double calculated =
                    baseWeight * tank / 100.0;

            addColorCard(
                    container,
                    name,
                    calculated
            );
        }

        // استبدال النتيجة القديمة بالنتيجة الجديدة
        result.setText("");

        ViewGroup parent =
                (ViewGroup) result.getParent();

        int index =
                parent.indexOfChild(result);

        parent.removeView(result);

        parent.addView(
                container,
                index
        );
    }

    // =====================================================
    // كارت الصبغة
    // =====================================================

    private void addColorCard(
            LinearLayout container,
            String name,
            double weight
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                22,
                20,
                22,
                20
        );

        int backgroundColor =
                getColorFromName(name);

        card.setBackgroundColor(
                backgroundColor
        );

        TextView nameView =
                new TextView(this);

        nameView.setText(name);

        nameView.setTextSize(19);

        nameView.setTextColor(
                getTextColor(backgroundColor)
        );

        nameView.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView weightView =
                new TextView(this);

        weightView.setText(
                format(weight) + " جم"
        );

        weightView.setTextSize(20);

        weightView.setTextColor(
                getTextColor(backgroundColor)
        );

        weightView.setGravity(
                Gravity.CENTER_VERTICAL |
                        Gravity.END
        );

        LinearLayout.LayoutParams
                nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams
                                .WRAP_CONTENT,
                        1
                );

        LinearLayout.LayoutParams
                weightParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams
                                .WRAP_CONTENT,
                        1
                );

        card.addView(
                nameView,
                nameParams
        );

        card.addView(
                weightView,
                weightParams
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams
                                .MATCH_PARENT,
                        LinearLayout.LayoutParams
                                .WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                14
        );

        container.addView(
                card,
                cardParams
        );
    }

    // =====================================================
    // ألوان الصبغات
    // =====================================================

    private int getColorFromName(
            String originalName
    ) {

        String name =
                originalName
                        .toLowerCase(Locale.ROOT)
                        .trim();

        if (name.contains("احمر") ||
                name.contains("أحمر")) {

            return Color.rgb(
                    220,
                    50,
                    50
            );
        }

        if (name.contains("اصفر") ||
                name.contains("أصفر") ||
                name.contains("yellow")) {

            return Color.rgb(
                    245,
                    205,
                    35
            );
        }

        if (name.contains("اخضر") ||
                name.contains("أخضر") ||
                name.contains("green")) {

            return Color.rgb(
                    45,
                    170,
                    75
            );
        }

        if (name.contains("ازرق") ||
                name.contains("أزرق") ||
                name.contains("blue")) {

            return Color.rgb(
                    45,
                    100,
                    210
            );
        }

        if (name.contains("تركواز") ||
                name.contains("turquoise")) {

            return Color.rgb(
                    40,
                    190,
                    190
            );
        }

        if (name.contains("بني") ||
                name.contains("brown")) {

            return Color.rgb(
                    125,
                    75,
                    40
            );
        }

        if (name.contains("بيج") ||
                name.contains("beige")) {

            return Color.rgb(
                    210,
                    190,
                    145
            );
        }

        if (name.contains("اسود") ||
                name.contains("أسود") ||
                name.contains("black")) {

            return Color.rgb(
                    35,
                    35,
                    35
            );
        }

        if (name.contains("ابيض") ||
                name.contains("أبيض") ||
                name.contains("white")) {

            return Color.rgb(
                    235,
                    235,
                    235
            );
        }

        if (name.contains("موف") ||
                name.contains("mauve") ||
                name.contains("purple")) {

            return Color.rgb(
                    145,
                    75,
                    180
            );
        }

        if (name.contains("فوشيا") ||
                name.contains("fuchsia")) {

            return Color.rgb(
                    220,
                    50,
                    150
            );
        }

        if (name.contains("وردي") ||
                name.contains("pink")) {

            return Color.rgb(
                    235,
                    120,
                    160
            );
        }

        if (name.contains("برتقالي") ||
                name.contains("orange")) {

            return Color.rgb(
                    240,
                    125,
                    35
            );
        }

        if (name.contains("رمادي") ||
                name.contains("رصاصي") ||
                name.contains("grey") ||
                name.contains("gray")) {

            return Color.rgb(
                    125,
                    125,
                    125
            );
        }

        if (name.contains("كحلي") ||
                name.contains("navy")) {

            return Color.rgb(
                    35,
                    55,
                    110
            );
        }

        if (name.contains("زيتي") ||
                name.contains("olive")) {

            return Color.rgb(
                    105,
                    115,
                    45
            );
        }

        return Color.rgb(
                90,
                90,
                90
        );
    }

    // =====================================================
    // لون الكتابة
    // =====================================================

    private int getTextColor(
            int background
    ) {

        int red =
                Color.red(background);

        int green =
                Color.green(background);

        int blue =
                Color.blue(background);

        double brightness =
                (red * 0.299) +
                (green * 0.587) +
                (blue * 0.114);

        if (brightness > 170) {

            return Color.BLACK;

        } else {

            return Color.WHITE;
        }
    }

    // =====================================================
    // إضافة تركيبة
    // =====================================================

    private void showAddRecipeDialog() {

        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        mainLayout.setPadding(
                35,
                10,
                35,
                10
        );

        EditText codeInput =
                new EditText(this);

        codeInput.setHint(
                "كود اللون"
        );

        codeInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        mainLayout.addView(
                codeInput
        );

        EditText[] names =
                new EditText[4];

        EditText[] weights =
                new EditText[4];

        for (int i = 0; i < 4; i++) {

            TextView number =
                    new TextView(this);

            number.setText(
                    "الصبغة رقم " +
                            (i + 1)
            );

            number.setTextSize(17);

            number.setPadding(
                    0,
                    15,
                    0,
                    5
            );

            mainLayout.addView(
                    number
            );

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            names[i] =
                    new EditText(this);

            names[i].setHint(
                    "اسم الصبغة"
            );

            names[i].setSingleLine(
                    true
            );

            weights[i] =
                    new EditText(this);

            weights[i].setHint(
                    "الوزن / 100 كجم"
            );

            weights[i].setInputType(
                    InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            );

            weights[i].setSingleLine(
                    true
            );

            LinearLayout.LayoutParams
                    nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams
                                    .WRAP_CONTENT,
                            1
                    );

            nameParams.setMargins(
                    0,
                    0,
                    10,
                    0
            );

            LinearLayout.LayoutParams
                    weightParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams
                                    .WRAP_CONTENT,
                            1
                    );

            row.addView(
                    names[i],
                    nameParams
            );

            row.addView(
                    weights[i],
                    weightParams
            );

            mainLayout.addView(
                    row
            );
        }

        new AlertDialog.Builder(this)

                .setTitle(
                        "إضافة تركيبة جديدة"
                )

                .setView(
                        mainLayout
                )

                .setPositiveButton(
                        "حفظ",
                        (dialog, which) -> {

                            String code =
                                    codeInput
                                            .getText()
                                            .toString()
                                            .trim();

                            if (code.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "اكتب كود اللون",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            try {

                                JSONArray recipe =
                                        new JSONArray();

                                for (
                                        int i = 0;
                                        i < 4;
                                        i++
                                ) {

                                    String name =
                                            names[i]
                                                    .getText()
                                                    .toString()
                                                    .trim();

                                    String weightText =
                                            weights[i]
                                                    .getText()
                                                    .toString()
                                                    .trim();

                                    if (
                                            name.isEmpty()
                                                    &&
                                            weightText.isEmpty()
                                    ) {

                                        continue;
                                    }

                                    if (
                                            name.isEmpty()
                                                    ||
                                            weightText.isEmpty()
                                    ) {

                                        Toast.makeText(
                                                this,
                                                "أكمل بيانات الصبغة رقم " +
                                                        (i + 1),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    double weight =
                                            Double.parseDouble(
                                                    weightText
                                                            .replace(
                                                                    ",",
                                                                    "."
                                                            )
                                            );

                                    if (weight < 0) {

                                        Toast.makeText(
                                                this,
                                                "الوزن غير صحيح",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    JSONObject item =
                                            new JSONObject();

                                    item.put(
                                            "name",
                                            name
                                    );

                                    item.put(
                                            "weight",
                                            weight
                                    );

                                    recipe.put(
                                            item
                                    );
                                }

                                if (
                                        recipe.length()
                                                == 0
                                ) {

                                    Toast.makeText(
                                            this,
                                            "أضف صبغة واحدة على الأقل",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                preferences.edit()
                                        .putString(
                                                "recipe_" + code,
                                                recipe.toString()
                                        )
                                        .apply();

                                Toast.makeText(
                                        this,
                                        "تم حفظ التركيبة " +
                                                code +
                                                " ✅",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } catch (Exception e) {

                                Toast.makeText(
                                        this,
                                        "حدث خطأ أثناء حفظ التركيبة",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .setNegativeButton(
                        "إلغاء",
                        null
                )

                .show();
    }

    // =====================================================
    // حفظ التركيبة الحالية
    // =====================================================

    private void saveCurrentRecipe() {

        String code =
                colorCode
                        .getText()
                        .toString()
                        .trim();

        if (code.isEmpty()) {

            Toast.makeText(
                    this,
                    "اكتب كود اللون أولًا",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String recipe =
                preferences.getString(
                        "recipe_" + code,
                        ""
                );

        if (
                recipe.isEmpty()
                        &&
                code.equals("557")
        ) {

            recipe =
                    createDefault557();

            preferences.edit()
                    .putString(
                            "recipe_557",
                            recipe
                    )
                    .apply();

            Toast.makeText(
                    this,
                    "تم حفظ تركيبة 557 ✅",
                    Toast.LENGTH_SHORT
            ).show();

        } else if (!recipe.isEmpty()) {

            Toast.makeText(
                    this,
                    "التركيبة محفوظة بالفعل ✅",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "التركيبة غير موجودة",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // البحث
    // =====================================================

    private void searchRecipe() {

        String code =
                colorCode
                        .getText()
                        .toString()
                        .trim();

        if (code.isEmpty()) {

            result.setText(
                    "اكتب كود اللون للبحث"
            );

            return;
        }

        String recipe =
                preferences.getString(
                        "recipe_" + code,
                        ""
                );

        if (
                recipe.isEmpty()
                        &&
                code.equals("557")
        ) {

            recipe =
                    createDefault557();
        }

        if (recipe.isEmpty()) {

            result.setText(
                    "لم يتم العثور على التركيبة"
            );

            return;
        }

        try {

            JSONArray array =
                    new JSONArray(recipe);

            StringBuilder text =
                    new StringBuilder();

            text.append(
                    "التركيبة المحفوظة\n\n"
            );

            text.append(
                    "كود اللون: "
            );

            text.append(code);

            text.append("\n\n");

            for (
                    int i = 0;
                    i < array.length();
                    i++
            ) {

                JSONObject item =
                        array.getJSONObject(i);

                text.append(
                        item.getString("name")
                );

                text.append(
                        " : "
                );

                text.append(
                        format(
                                item.getDouble(
                                        "weight"
                                )
                        )
                );

                text.append(
                        " جم / 100 كجم\n"
                );
            }

            result.setText(
                    text.toString()
            );

        } catch (Exception e) {

            result.setText(
                    "حدث خطأ في قراءة التركيبة"
            );
        }
    }

    // =====================================================
    // التركيبة الافتراضية 557
    // =====================================================

    private String createDefault557() {

        try {

            JSONArray recipe =
                    new JSONArray();

            JSONObject beige =
                    new JSONObject();

            beige.put(
                    "name",
                    "بيج"
            );

            beige.put(
                    "weight",
                    150
            );

            recipe.put(
                    beige
            );

            JSONObject brown =
                    new JSONObject();

            brown.put(
                    "name",
                    "بني محروق"
            );

            brown.put(
                    "weight",
                    50
            );

            recipe.put(
                    brown
            );

            JSONObject yellow =
                    new JSONObject();

            yellow.put(
                    "name",
                    "أصفر GL"
            );

            yellow.put(
                    "weight",
                    3.8
            );

            recipe.put(
                    yellow
            );

            return recipe.toString();

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // تنسيق الأرقام
    // =====================================================

    private String format(
            double number
    ) {

        if (
                number ==
                        (long) number
        ) {

            return String.format(
                    Locale.US,
                    "%d",
                    (long) number
            );
        }

        return String.format(
                Locale.US,
                "%.2f",
                number
        );
    }
    }
