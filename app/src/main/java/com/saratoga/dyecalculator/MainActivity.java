package com.saratoga.dyecalculator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText colorCode;
    EditText tankWeight;

    Button calculateButton;
    Button saveButton;
    Button searchButton;
    Button addRecipeButton;
    Button cameraButton;

    LinearLayout resultsContainer;

    SharedPreferences preferences;

    private static final int CAMERA_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_REQUEST = 1002;

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
        cameraButton = findViewById(R.id.cameraButton);

        resultsContainer =
                findViewById(R.id.resultsContainer);

        preferences =
                getSharedPreferences(
                        "SaratogaRecipes",
                        MODE_PRIVATE
                );

        calculateButton.setOnClickListener(
                v -> calculateRecipe()
        );

        saveButton.setOnClickListener(
                v -> saveCurrentRecipe()
        );

        searchButton.setOnClickListener(
                v -> searchRecipe()
        );

        addRecipeButton.setOnClickListener(
                v -> showAddRecipeDialog()
        );

        cameraButton.setOnClickListener(
                v -> openCamera()
        );

        // تحديث تلقائي عند تغيير وزن الحوض
        tankWeight.addTextChangedListener(
                new TextWatcher() {

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

                        calculateAutomatically();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        // تحديث تلقائي عند تغيير الكود
        colorCode.addTextChangedListener(
                new TextWatcher() {

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

                        calculateAutomatically();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    // =====================================================
    // الكاميرا
    // =====================================================

    private void openCamera() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_PERMISSION_REQUEST
            );

            return;
        }

        Intent intent =
                new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE
                );

        if (
                intent.resolveActivity(
                        getPackageManager()
                ) != null
        ) {

            startActivityForResult(
                    intent,
                    CAMERA_REQUEST
            );

        } else {

            Toast.makeText(
                    this,
                    "لا توجد كاميرا متاحة",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (
                requestCode ==
                        CAMERA_PERMISSION_REQUEST
        ) {

            if (
                    grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED
            ) {

                openCamera();

            } else {

                Toast.makeText(
                        this,
                        "لازم تسمح للتطبيق باستخدام الكاميرا",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == CAMERA_REQUEST &&
                resultCode == RESULT_OK &&
                data != null
        ) {

            Bundle extras =
                    data.getExtras();

            if (extras != null) {

                Bitmap bitmap =
                        (Bitmap) extras.get("data");

                if (bitmap != null) {

                    readTextFromImage(bitmap);
                }
            }
        }
    }

    // =====================================================
    // OCR
    // =====================================================

    private void readTextFromImage(
            Bitmap bitmap
    ) {

        Toast.makeText(
                this,
                "جاري قراءة ورقة التركيبة...",
                Toast.LENGTH_SHORT
        ).show();

        InputImage image =
                InputImage.fromBitmap(
                        bitmap,
                        0
                );

        TextRecognizer recognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );

        recognizer.process(image)

                .addOnSuccessListener(
                        visionText -> {

                            String text =
                                    visionText.getText();

                            if (
                                    text == null ||
                                    text.trim().isEmpty()
                            ) {

                                Toast.makeText(
                                        this,
                                        "لم أستطع قراءة الورقة",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            showRecognizedText(text);
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "حدث خطأ أثناء قراءة الصورة",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // عرض النص المقروء
    // =====================================================

    private void showRecognizedText(
            String text
    ) {

        EditText textBox =
                new EditText(this);

        textBox.setText(text);

        textBox.setGravity(
                Gravity.TOP
        );

        textBox.setMinLines(8);

        textBox.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                25,
                10,
                25,
                10
        );

        TextView info =
                new TextView(this);

        info.setText(
                "راجع النص المقروء وعدّل أي خطأ قبل الحفظ:"
        );

        info.setTextSize(17);

        info.setPadding(
                0,
                0,
                0,
                15
        );

        layout.addView(info);

        layout.addView(
                textBox,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        new AlertDialog.Builder(this)

                .setTitle(
                        "📷 نتيجة تصوير التركيبة"
                )

                .setView(layout)

                .setPositiveButton(
                        "استخراج البيانات",
                        (dialog, which) -> {

                            parseRecognizedText(
                                    textBox.getText()
                                            .toString()
                            );
                        }
                )

                .setNegativeButton(
                        "إلغاء",
                        null
                )

                .show();
    }

    // =====================================================
    // استخراج الكود والأوزان
    // =====================================================

    private void parseRecognizedText(
            String text
    ) {

        String[] lines =
                text.split("\\r?\\n");

        String detectedCode = "";

        ArrayList<String> pigmentNames =
                new ArrayList<>();

        ArrayList<Double> pigmentWeights =
                new ArrayList<>();

        Pattern codePattern =
                Pattern.compile(
                        "\\b\\d{3,5}\\b"
                );

        Pattern numberPattern =
                Pattern.compile(
                        "(\\d+(?:[\\.,]\\d+)?)"
                );

        for (String line : lines) {

            String clean =
                    line.trim();

            if (clean.isEmpty()) {
                continue;
            }

            // البحث عن كود اللون
            if (detectedCode.isEmpty()) {

                Matcher codeMatcher =
                        codePattern.matcher(clean);

                if (codeMatcher.find()) {

                    detectedCode =
                            codeMatcher.group();
                }
            }

            // البحث عن الوزن
            Matcher numberMatcher =
                    numberPattern.matcher(clean);

            ArrayList<String> numbers =
                    new ArrayList<>();

            while (
                    numberMatcher.find()
            ) {

                numbers.add(
                        numberMatcher.group(1)
                );
            }

            if (!numbers.isEmpty()) {

                String lastNumber =
                        numbers.get(
                                numbers.size() - 1
                        );

                try {

                    double weight =
                            Double.parseDouble(
                                    lastNumber.replace(
                                            ",",
                                            "."
                                    )
                            );

                    // تجاهل الكود نفسه
                    if (
                            detectedCode.equals(
                                    lastNumber
                            )
                    ) {
                        continue;
                    }

                    String name =
                            clean.replace(
                                    lastNumber,
                                    ""
                            )
                            .replace(
                                    ":",
                                    ""
                            )
                            .replace(
                                    "-",
                                    ""
                            )
                            .trim();

                    // لو فيه اسم واضح
                    if (
                            !name.isEmpty() &&
                            weight >= 0
                    ) {

                        pigmentNames.add(name);
                        pigmentWeights.add(weight);
                    }

                } catch (Exception ignored) {
                }
            }
        }

        showExtractedDataDialog(
                detectedCode,
                pigmentNames,
                pigmentWeights
        );
    }

    // =====================================================
    // مراجعة البيانات قبل الحفظ
    // =====================================================

    private void showExtractedDataDialog(
            String detectedCode,
            ArrayList<String> names,
            ArrayList<Double> weights
    ) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                25,
                10,
                25,
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

        codeInput.setText(
                detectedCode
        );

        layout.addView(
                codeInput
        );

        int count =
                Math.min(
                        names.size(),
                        4
                );

        EditText[] nameInputs =
                new EditText[4];

        EditText[] weightInputs =
                new EditText[4];

        for (
                int i = 0;
                i < 4;
                i++
        ) {

            TextView title =
                    new TextView(this);

            title.setText(
                    "الصبغة " + (i + 1)
            );

            title.setTextSize(16);

            title.setPadding(
                    0,
                    15,
                    0,
                    5
            );

            layout.addView(title);

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            nameInputs[i] =
                    new EditText(this);

            nameInputs[i].setHint(
                    "اسم الصبغة"
            );

            nameInputs[i].setSingleLine(
                    true
            );

            weightInputs[i] =
                    new EditText(this);

            weightInputs[i].setHint(
                    "الوزن / 100 كجم"
            );

            weightInputs[i].setInputType(
                    InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            );

            weightInputs[i].setSingleLine(
                    true
            );

            if (i < count) {

                nameInputs[i].setText(
                        names.get(i)
                );

                weightInputs[i].setText(
                        format(
                                weights.get(i)
                        )
                );
            }

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
                    nameInputs[i],
                    nameParams
            );

            row.addView(
                    weightInputs[i],
                    weightParams
            );

            layout.addView(row);
        }

        new AlertDialog.Builder(this)

                .setTitle(
                        "مراجعة التركيبة"
                )

                .setView(layout)

                .setPositiveButton(
                        "💾 حفظ",
                        (dialog, which) -> {

                            saveScannedRecipe(
                                    codeInput,
                                    nameInputs,
                                    weightInputs
                            );
                        }
                )

                .setNegativeButton(
                        "إلغاء",
                        null
                )

                .show();
    }

    // =====================================================
    // حفظ التركيبة المصورة
    // =====================================================

    private void saveScannedRecipe(
            EditText codeInput,
            EditText[] names,
            EditText[] weights
    ) {

        String code =
                codeInput.getText()
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
                        names[i].getText()
                                .toString()
                                .trim();

                String weightText =
                        weights[i].getText()
                                .toString()
                                .trim();

                if (
                        name.isEmpty() &&
                        weightText.isEmpty()
                ) {
                    continue;
                }

                if (
                        name.isEmpty() ||
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
                                weightText.replace(
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

                recipe.put(item);
            }

            if (recipe.length() == 0) {

                Toast.makeText(
                        this,
                        "لم يتم العثور على صبغات",
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

            colorCode.setText(code);

            Toast.makeText(
                    this,
                    "تم حفظ التركيبة " +
                            code +
                            " ✅",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "حدث خطأ أثناء حفظ التركيبة",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // الحساب التلقائي
    // =====================================================

    private void calculateAutomatically() {

        String code =
                colorCode.getText()
                        .toString()
                        .trim();

        String weightText =
                tankWeight.getText()
                        .toString()
                        .trim();

        if (
                code.isEmpty() ||
                weightText.isEmpty()
        ) {

            return;
        }

        double tank;

        try {

            tank =
                    Double.parseDouble(
                            weightText.replace(
                                    ",",
                                    "."
                            )
                    );

        } catch (Exception e) {

            return;
        }

        if (tank <= 0) {
            return;
        }

        String recipe =
                preferences.getString(
                        "recipe_" + code,
                        ""
                );

        if (
                recipe.isEmpty() &&
                code.equals("557")
        ) {

            recipe =
                    createDefault557();
        }

        if (recipe.isEmpty()) {

            showMessage(
                    "كود اللون " +
                            code +
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

            showMessage(
                    "حدث خطأ في قراءة التركيبة"
            );
        }
    }

    // =====================================================
    // الحساب بالزر
    // =====================================================

    private void calculateRecipe() {

        calculateAutomatically();

        if (
                colorCode.getText()
                        .toString()
                        .trim()
                        .isEmpty()
        ) {

            showMessage(
                    "اكتب كود اللون أولًا"
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

        resultsContainer.removeAllViews();

        TextView title =
                new TextView(this);

        title.setText(
                "النتيجة النهائية\n\n" +
                        "كود اللون: " +
                        code +
                        "\n" +
                        "وزن الحوض: " +
                        format(tank) +
                        " كجم"
        );

        title.setTextSize(21);

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                10,
                10,
                25
        );

        resultsContainer.addView(
                title
        );

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

        resultsContainer.addView(
                subtitle
        );

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

            double calculated =
                    baseWeight *
                            tank /
                            100.0;

            addColorCard(
                    resultsContainer,
                    name,
                    calculated
            );
        }
    }

    // =====================================================
    // رسالة
    // =====================================================

    private void showMessage(
            String message
    ) {

        resultsContainer.removeAllViews();

        TextView text =
                new TextView(this);

        text.setText(message);

        text.setTextSize(18);

        text.setGravity(
                Gravity.CENTER
        );

        text.setPadding(
                15,
                25,
                15,
                25
        );

        resultsContainer.addView(text);
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
                getTextColor(
                        backgroundColor
                )
        );

        TextView weightView =
                new TextView(this);

        weightView.setText(
                format(weight) +
                        " جم"
        );

        weightView.setTextSize(20);

        weightView.setTextColor(
                getTextColor(
                        backgroundColor
                )
        );

        weightView.setGravity(
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

        if (
                name.contains("احمر") ||
                name.contains("أحمر") ||
                name.contains("red")
        ) {
            return Color.rgb(
                    220,
                    50,
                    50
            );
        }

        if (
                name.contains("اصفر") ||
                name.contains("أصفر") ||
                name.contains("yellow")
        ) {
            return Color.rgb(
                    245,
                    205,
                    35
            );
        }

        if (
                name.contains("اخضر") ||
                name.contains("أخضر") ||
                name.contains("green")
        ) {
            return Color.rgb(
                    45,
                    170,
                    75
            );
        }

        if (
                name.contains("ازرق") ||
                name.contains("أزرق") ||
                name.contains("blue")
        ) {
            return Color.rgb(
                    45,
                    100,
                    210
            );
        }

        if (
                name.contains("تركواز") ||
                name.contains("turquoise")
        ) {
            return Color.rgb(
                    40,
                    190,
                    190
            );
        }

        if (
                name.contains("بني") ||
                name.contains("brown")
        ) {
            return Color.rgb(
                    125,
                    75,
                    40
            );
        }

        if (
                name.contains("بيج") ||
                name.contains("beige")
        ) {
            return Color.rgb(
                    210,
                    190,
                    145
            );
        }

        if (
                name.contains("اسود") ||
                name.contains("أسود") ||
                name.contains("black")
        ) {
            return Color.rgb(
                    35,
                    35,
                    35
            );
        }

        if (
                name.contains("ابيض") ||
                name.contains("أبيض") ||
                name.contains("white")
        ) {
            return Color.rgb(
                    235,
                    235,
                    235
            );
        }

        if (
                name.contains("موف") ||
                name.contains("mauve") ||
                name.contains("purple")
        ) {
            return Color.rgb(
                    145,
                    75,
                    180
            );
        }

        if (
                name.contains("فوشيا") ||
                name.contains("fuchsia")
        ) {
            return Color.rgb(
                    220,
                    50,
                    150
            );
        }

        if (
                name.contains("وردي") ||
                name.contains("pink")
        ) {
            return Color.rgb(
                    235,
                    120,
                    160
            );
        }

        if (
                name.contains("برتقالي") ||
                name.contains("orange")
        ) {
            return Color.rgb(
                    240,
                    125,
                    35
            );
        }

        if (
                name.contains("رمادي") ||
                name.contains("رصاصي") ||
                name.contains("grey") ||
                name.contains("gray")
        ) {
            return Color.rgb(
                    125,
                    125,
                    125
            );
        }

        if (
                name.contains("كحلي") ||
                name.contains("navy")
        ) {
            return Color.rgb(
                    35,
                    55,
                    110
            );
        }

        if (
                name.contains("زيتي") ||
                name.contains("olive")
        ) {
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
    // لون النص
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

        return brightness > 170
                ? Color.BLACK
                : Color.WHITE;
    }

    // =====================================================
    // إضافة تركيبة يدويًا
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

        for (
                int i = 0;
                i < 4;
                i++
        ) {

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

            mainLayout.addView(row);
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

                            saveManualRecipe(
                                    codeInput,
                                    names,
                                    weights
                            );
                        }
                )

                .setNegativeButton(
                        "إلغاء",
                        null
                )

                .show();
    }

    // =====================================================
    // حفظ يدوي
    // =====================================================

    private void saveManualRecipe(
            EditText codeInput,
            EditText[] names,
            EditText[] weights
    ) {

        String code =
                codeInput.getText()
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
                        names[i].getText()
                                .toString()
                                .trim();

                String weightText =
                        weights[i].getText()
                                .toString()
                                .trim();

                if (
                        name.isEmpty() &&
                        weightText.isEmpty()
                ) {
                    continue;
                }

                if (
                        name.isEmpty() ||
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
                                weightText.replace(
                                        ",",
                                        "."
                                )
                        );

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

                recipe.put(item);
            }

            if (recipe.length() == 0) {

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

            colorCode.setText(code);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "حدث خطأ أثناء الحفظ",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // حفظ التركيبة الحالية
    // =====================================================

    private void saveCurrentRecipe() {

        String code =
                colorCode.getText()
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
                recipe.isEmpty() &&
                code.equals("557")
        ) {

            preferences.edit()
                    .putString(
                            "recipe_557",
                            createDefault557()
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
                colorCode.getText()
                        .toString()
                        .trim();

        if (code.isEmpty()) {

            showMessage(
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
                recipe.isEmpty() &&
                code.equals("557")
        ) {

            recipe =
                    createDefault557();
        }

        if (recipe.isEmpty()) {

            showMessage(
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

            showMessage(
                    text.toString()
            );

        } catch (Exception e) {

            showMessage(
                    "حدث خطأ في قراءة التركيبة"
            );
        }
    }

    // =====================================================
    // تركيبة 557
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

            recipe.put(beige);

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

            recipe.put(brown);

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

            recipe.put(yellow);

            return recipe.toString();

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // تنسيق الرقم
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
