package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class activity_objectdetection extends AppCompatActivity {

    private ImageView imagePreview;
    private TextView tvImageInfo, tvResults, tvStatus;
    private Button btnGallery, btnCamera, btnDetect, btnShare, btnRetry;
    private LinearLayout resultsLayout;
    private ImageButton btnBack;
    private Bitmap selectedBitmap;
    private File imageFile;

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST = 101;
    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final int STORAGE_PERMISSION_CODE = 201;

    // CORRECTED: Using the new Hugging Face Inference API URL format
    private static final String HF_API_URL =  "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224";

    // Your Hugging Face Token - Keep this
    private static final String HF_TOKEN = "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";

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Increased timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectdetection);

        initializeViews();
        setupClickListeners();
        checkPermissions();

        Log.d("API_SETUP", "Using URL: " + HF_API_URL);
        Log.d("API_SETUP", "Token: " + HF_TOKEN.substring(0, Math.min(10, HF_TOKEN.length())) + "...");
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.imagePreview);
        tvImageInfo = findViewById(R.id.tvImageInfo);
        tvResults = findViewById(R.id.tvResults);
        tvStatus = findViewById(R.id.tvStatus);

        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnDetect = findViewById(R.id.btnDetect);
        btnShare = findViewById(R.id.btnShare);
        btnRetry = findViewById(R.id.btnRetry);

        resultsLayout = findViewById(R.id.resultsLayout);
        btnBack = findViewById(R.id.btnBack);

        btnDetect.setEnabled(false);
        btnRetry.setVisibility(View.GONE);
        tvStatus.setText("Google Vision Transformer (ViT) Ready");
        tvResults.setText("📱 AI Image Analysis\n\n" +
                "1. Select image from Gallery or Camera\n" +
                "2. Click ANALYZE IMAGE\n" +
                "3. Wait for AI analysis\n\n" +
                "Using URL: " + HF_API_URL + "\n" +
                "Token: " + HF_TOKEN.substring(0, 8) + "...");
    }

    private void checkPermissions() {
        // Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        // Storage permission based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGallery.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
            }
        });

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        });

        btnDetect.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                startDetection();
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            String results = tvResults.getText().toString();
            if (!results.isEmpty() && !results.contains("Select image")) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "AI Image Analysis Results:\n\n" + results);
                startActivity(Intent.createChooser(shareIntent, "Share Results"));
            } else {
                Toast.makeText(this, "No results to share", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetry.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                startDetection();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    selectedBitmap = resizeBitmap(selectedBitmap, 800);
                    handleSelectedImage();
                } catch (IOException e) {
                    showError("Error loading image: " + e.getMessage());
                }
            } else if (requestCode == CAMERA_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedBitmap = (Bitmap) extras.get("data");
                    if (selectedBitmap != null) {
                        selectedBitmap = rotateBitmapIfNeeded(selectedBitmap);
                        selectedBitmap = resizeBitmap(selectedBitmap, 800);
                        handleSelectedImage();
                    } else {
                        showError("Camera returned null image");
                    }
                }
            }
        }
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
    }

    private Bitmap resizeBitmap(Bitmap image, int maxWidth) {
        if (image.getWidth() > maxWidth) {
            int width = maxWidth;
            int height = (int) (image.getHeight() * ((float) maxWidth / image.getWidth()));
            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        return image;
    }

    private void handleSelectedImage() {
        if (selectedBitmap != null) {
            imagePreview.setImageBitmap(selectedBitmap);
            imagePreview.setVisibility(View.VISIBLE);
            tvImageInfo.setText("Image: " + selectedBitmap.getWidth() + "x" + selectedBitmap.getHeight());
            btnDetect.setEnabled(true);
            btnDetect.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            try {
                imageFile = saveBitmapToFile(selectedBitmap);
                tvStatus.setText("✅ Image Ready!");
                tvResults.setText("✅ Image loaded successfully!\n" +
                        "Size: " + selectedBitmap.getWidth() + "x" + selectedBitmap.getHeight() + "\n\n" +
                        "Click ANALYZE IMAGE for AI analysis");
                Toast.makeText(this, "✅ Image ready! Click ANALYZE IMAGE", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                showError("Error saving image");
            }
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File cacheDir = getCacheDir();
        File imageFile = new File(cacheDir, "detect_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        fos.close();
        return imageFile;
    }

    // -------------------- UPDATED startDetection() with correct API format --------------------
    private void startDetection() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        setDetectionInProgress(true);
        tvStatus.setText("🚀 Sending to Vision Transformer...");
        tvResults.setText("🔍 Analyzing image with Google ViT model...\n" +
                "URL: " + HF_API_URL + "\n" +
                "Please wait 20-40 seconds...");

        try {
            // Convert image to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Log.d("API_REQUEST", "Image size (base64): " + base64Image.length() + " chars");

            // Create the CORRECT JSON format for Hugging Face Inference API
            JSONObject json = new JSONObject();

            // For the new inference API, we need to send base64 image directly
            json.put("inputs", base64Image);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    json.toString()
            );

            Log.d("API_REQUEST", "Sending to URL: " + HF_API_URL);
            Log.d("API_REQUEST", "Token (first 10): " + HF_TOKEN.substring(0, Math.min(10, HF_TOKEN.length())));

            Request request = new Request.Builder()
                    .url(HF_API_URL)
                    .header("Authorization", "Bearer " + HF_TOKEN)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Android-App/1.0")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("NETWORK_ERROR", "Network failed: " + e.getMessage());
                    handler.post(() -> {
                        showError("Network Error\n\n" + e.getMessage() +
                                "\n\nPlease check your internet connection");
                        setDetectionInProgress(false);
                        btnRetry.setVisibility(View.VISIBLE);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    int responseCode = response.code();

                    Log.d("API_RESPONSE", "HTTP Code: " + responseCode);
                    Log.d("API_RESPONSE", "Response length: " + responseBody.length());

                    // Log first 200 characters for debugging
                    if (responseBody.length() > 0) {
                        Log.d("API_RESPONSE_BODY", responseBody.substring(0, Math.min(200, responseBody.length())));
                    }

                    handler.post(() -> {
                        try {
                            if (responseCode == 200) {
                                processSuccessfulResponse(responseBody);
                            } else {
                                handleApiError(responseCode, responseBody);
                            }
                        } catch (Exception e) {
                            Log.e("PROCESS_ERROR", "Error: " + e.getMessage());
                            showError("Processing Error: " + e.getMessage());
                        } finally {
                            setDetectionInProgress(false);
                            response.close();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showError("Error preparing image: " + e.getMessage());
            setDetectionInProgress(false);
        }
    }

    private void processSuccessfulResponse(String responseBody) {
        try {
            Log.d("API_SUCCESS", "Response: " + responseBody);

            // Try to parse as JSON array
            JSONArray predictions = new JSONArray(responseBody);
            String results = parseViTResults(predictions);
            showResults(results);

        } catch (Exception e) {
            // Try alternative parsing - maybe it's a single object
            try {
                JSONObject result = new JSONObject(responseBody);
                String results = "AI Analysis Results:\n\n" + result.toString(2);
                tvResults.setText(results);
                tvStatus.setText("✅ Analysis Complete");
                btnShare.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Analysis successful!", Toast.LENGTH_SHORT).show();
            } catch (Exception e2) {
                Log.e("PARSE_ERROR", "Parse failed: " + e2.getMessage());
                showError("Could not parse AI response\n\nRaw response:\n" +
                        responseBody.substring(0, Math.min(200, responseBody.length())));
            }
        }
    }

    private void handleApiError(int code, String responseBody) {
        Log.e("API_ERROR", "Error " + code + ": " + responseBody);

        String errorMessage;
        boolean showRetry = true;

        switch (code) {
            case 401:
                errorMessage = "🔐 Invalid API Token (401)\n\n" +
                        "Your Hugging Face token may be invalid.\n" +
                        "Token used: " + HF_TOKEN.substring(0, 12) + "...\n\n" +
                        "Please check:\n" +
                        "1. Token is active at huggingface.co/settings/tokens\n" +
                        "2. Token has READ access\n" +
                        "3. Token is correctly copied";
                break;

            case 403:
                errorMessage = "🚫 Access Denied (403)\n\n" +
                        "This model may require payment or special access.\n" +
                        "Please try a different model.";
                break;

            case 404:
            case 410:
                errorMessage = "🔍 Model Not Found (" + code + ")\n\n" +
                        "URL: " + HF_API_URL + "\n\n" +
                        "This URL may be incorrect or model removed.\n" +
                        "Try alternative URL format:\n" +
                        "https://api-inference.huggingface.co/models/google/vit-base-patch16-224";
                break;

            case 429:
                errorMessage = "⏳ Rate Limited (429)\n\n" +
                        "Too many requests to Hugging Face.\n" +
                        "Please wait 1-2 minutes.";
                showRetry = false;
                break;

            case 503:
                errorMessage = "🔄 Model Loading (503)\n\n" +
                        "Model is starting up...\n" +
                        "First use takes 20-40 seconds.\n\n" +
                        "Click RETRY in 30 seconds.";

                // Auto-retry after 30 seconds
                handler.postDelayed(() -> {
                    if (btnRetry.getVisibility() == View.VISIBLE) {
                        btnRetry.performClick();
                    }
                }, 30000);
                break;

            default:
                errorMessage = "❌ API Error " + code + "\n\n" +
                        "Response: " +
                        (responseBody.length() > 100 ?
                                responseBody.substring(0, 100) + "..." :
                                responseBody);
                break;
        }

        showError(errorMessage);
        btnRetry.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }

    private String parseViTResults(JSONArray predictions) {
        StringBuilder result = new StringBuilder();
        result.append("🤖 **AI Image Analysis Results**\n\n");
        result.append("Model: Google ViT-base-patch16-224\n");
        result.append("URL: ").append(HF_API_URL).append("\n\n");

        try {
            int topResults = Math.min(predictions.length(), 5);
            if (topResults == 0) {
                result.append("No predictions returned from the model.\n");
                result.append("The model might still be loading.");
                return result.toString();
            }

            for (int i = 0; i < topResults; i++) {
                JSONObject prediction = predictions.getJSONObject(i);
                String label = prediction.getString("label");
                double score = prediction.getDouble("score");
                double confidence = score * 100;

                if (confidence > 1) { // Only show meaningful predictions
                    String formattedLabel = formatViTLabel(label);
                    String emoji = getEmojiForLabel(formattedLabel);
                    result.append(String.format("%d. %s %-25s %.1f%%\n",
                            i + 1, emoji, formattedLabel, confidence));
                }
            }

            result.append("\n✅ **Analysis complete**");
            result.append("\n📊 **Top ").append(topResults).append(" predictions shown**");
            result.append("\n💡 ViT identifies WHAT is in the image (classification)");

        } catch (Exception e) {
            result.append("Error parsing ViT results: ").append(e.getMessage());
            Log.e("ViT_Parse", "Error: " + e.getMessage());
        }

        return result.toString();
    }

    private String formatViTLabel(String label) {
        // Clean up labels from ViT model
        String formatted = label.replace("_", " ")
                .replace("-", " ")
                .toLowerCase();

        // Capitalize first letter of each word
        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private String getEmojiForLabel(String label) {
        label = label.toLowerCase();

        if (label.contains("person") || label.contains("human")) return "👤";
        if (label.contains("cat")) return "🐱";
        if (label.contains("dog")) return "🐶";
        if (label.contains("bird")) return "🐦";
        if (label.contains("car")) return "🚗";
        if (label.contains("truck")) return "🚚";
        if (label.contains("bus")) return "🚌";
        if (label.contains("motorcycle") || label.contains("bike")) return "🏍️";
        if (label.contains("bicycle")) return "🚲";
        if (label.contains("airplane") || label.contains("plane")) return "✈️";
        if (label.contains("boat") || label.contains("ship")) return "🚢";
        if (label.contains("train")) return "🚂";
        if (label.contains("horse")) return "🐴";
        if (label.contains("cow")) return "🐄";
        if (label.contains("sheep")) return "🐑";
        if (label.contains("chair")) return "🪑";
        if (label.contains("table") || label.contains("desk")) return "🪟";
        if (label.contains("bed")) return "🛏️";
        if (label.contains("sofa") || label.contains("couch")) return "🛋️";
        if (label.contains("bottle")) return "🍾";
        if (label.contains("cup") || label.contains("glass")) return "🥛";
        if (label.contains("bowl")) return "🥣";
        if (label.contains("apple")) return "🍎";
        if (label.contains("banana")) return "🍌";
        if (label.contains("orange")) return "🍊";
        if (label.contains("pizza")) return "🍕";
        if (label.contains("burger")) return "🍔";
        if (label.contains("food")) return "🍽️";
        if (label.contains("tv") || label.contains("television")) return "📺";
        if (label.contains("laptop") || label.contains("computer")) return "💻";
        if (label.contains("phone") || label.contains("mobile")) return "📱";
        if (label.contains("book")) return "📚";
        if (label.contains("tree")) return "🌳";
        if (label.contains("flower")) return "🌸";
        if (label.contains("plant")) return "🪴";
        if (label.contains("building") || label.contains("house")) return "🏠";
        if (label.contains("clock") || label.contains("watch")) return "🕰️";
        if (label.contains("shoe") || label.contains("sneaker")) return "👟";
        if (label.contains("bag") || label.contains("handbag")) return "👜";
        if (label.contains("hat") || label.contains("cap")) return "🧢";
        if (label.contains("shirt") || label.contains("t-shirt")) return "👕";
        if (label.contains("pant") || label.contains("jeans")) return "👖";
        if (label.contains("dress")) return "👗";

        return "•";
    }

    private void showResults(String results) {
        tvResults.setText(results);
        tvStatus.setText("✅ Analysis Complete!");
        btnShare.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
        Toast.makeText(this, "✅ AI analysis successful!", Toast.LENGTH_SHORT).show();
    }

    private void showError(String error) {
        tvResults.setText(error);
        tvStatus.setText("❌ Analysis Failed");
        btnShare.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Analysis failed", Toast.LENGTH_LONG).show();
    }

    private void setDetectionInProgress(boolean inProgress) {
        btnDetect.setEnabled(!inProgress);
        btnGallery.setEnabled(!inProgress);
        btnCamera.setEnabled(!inProgress);
        btnRetry.setEnabled(!inProgress);

        if (inProgress) {
            btnDetect.setText("⏳ ANALYZING...");
            btnDetect.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            resultsLayout.setVisibility(View.VISIBLE);
        } else {
            btnDetect.setText("🔍 ANALYZE IMAGE");
            btnDetect.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            if (requestCode == CAMERA_PERMISSION_CODE) {
                openCamera();
            } else if (requestCode == STORAGE_PERMISSION_CODE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}