package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class activity_chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Button btnTokenizationDemo, btnLanguage;
    private TextView tvStatus, tvTokenInfo;

    private List<ChatMessage> messageList;
    private chatbotadapter adapter;

    // ✅ Google Gemini API
    private static final String API_KEY = "AIzaSyAi_iu3bTFc0FhriQnnar1g7--D8KZA_ig";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ✅ Multilingual Support
    private String currentLanguage = "en"; // Default: English
    private Map<String, String> languageNames;
    private Map<String, String> languageFlags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);

        btnLanguage = findViewById(R.id.btnLanguage);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);


        // Setup toolbar
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTitle.setText("AI Chatbot (Multilingual)");

        // Initialize OkHttp
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize multilingual support
        initializeLanguages();

        // Initialize message list
        messageList = new ArrayList<>();

        // Add welcome message in current language
        addWelcomeMessage();

        // Setup RecyclerView
        adapter = new chatbotadapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set status
        updateStatus();

        // Setup button listeners
        setupButtonListeners();

        // Scroll to bottom
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void initializeLanguages() {
        // Language names
        languageNames = new HashMap<>();
        languageNames.put("en", "English");
        languageNames.put("hi", "Hindi");
        languageNames.put("es", "Spanish");
        languageNames.put("fr", "French");
        languageNames.put("de", "German");
        languageNames.put("zh", "Chinese");
        languageNames.put("ar", "Arabic");
        languageNames.put("bn", "Bengali");
        languageNames.put("ur", "Urdu");

        // Language flags/emojis
        languageFlags = new HashMap<>();
        languageFlags.put("en", "🇺🇸");
        languageFlags.put("hi", "🇮🇳");
        languageFlags.put("es", "🇪🇸");
        languageFlags.put("fr", "🇫🇷");
        languageFlags.put("de", "🇩🇪");
        languageFlags.put("zh", "🇨🇳");
        languageFlags.put("ar", "🇸🇦");
        languageFlags.put("bn", "🇧🇩");
        languageFlags.put("ur", "🇵🇰");
    }

    private void addWelcomeMessage() {
        Map<String, String> welcomeMessages = new HashMap<>();
        welcomeMessages.put("en", "Hello! I'm your AI assistant. I can help you with app features, password reset, ads, and real-time chat. What would you like to know?");
        welcomeMessages.put("hi", "नमस्ते! मैं आपकी AI सहायक हूं। मैं आपकी ऐप की सुविधाओं, पासवर्ड रीसेट, विज्ञापनों और रीयल-टाइम चैट में मदद कर सकती हूं। आप क्या जानना चाहते हैं?");
        welcomeMessages.put("es", "¡Hola! Soy tu asistente de IA. Puedo ayudarte con funciones de la aplicación, restablecimiento de contraseñas, anuncios y chat en tiempo real. ¿Qué te gustaría saber?");
        welcomeMessages.put("fr", "Bonjour ! Je suis votre assistant IA. Je peux vous aider avec les fonctionnalités de l'application, la réinitialisation du mot de passe, les publicités et le chat en temps réel. Que voudriez-vous savoir ?");
        welcomeMessages.put("de", "Hallo! Ich bin Ihr KI-Assistent. Ich kann Ihnen bei App-Funktionen, Passwort-Reset, Werbung und Echtzeit-Chat helfen. Was möchten Sie wissen?");
        welcomeMessages.put("zh", "你好！我是你的AI助手。我可以帮助你处理应用功能、密码重置、广告和实时聊天。你想知道什么？");
        welcomeMessages.put("ar", "مرحبًا! أنا مساعدك الذكي. يمكنني مساعدتك في ميزات التطبيق، إعادة تعيين كلمة المرور، الإعلانات والدردشة الفورية. ماذا تريد أن تعرف؟");
        welcomeMessages.put("bn", "হ্যালো! আমি আপনার AI সহকারী। আমি আপনাকে অ্যাপের বৈশিষ্ট্য, পাসওয়ার্ড রিসেট, বিজ্ঞাপন এবং রিয়েল-টাইম চ্যাটে সাহায্য করতে পারি। আপনি কি জানতে চান?");
        welcomeMessages.put("ur", "ہیلو! میں آپ کی AI اسسٹنٹ ہوں۔ میں آپ کی ایپ کی خصوصیات، پاس ورڈ ری سیٹ، اشتہارات اور ریئل ٹائم چیٹ میں مدد کر سکتی ہوں۔ آپ کیا جاننا چاہتے ہیں؟");

        ChatMessage welcomeMsg = new ChatMessage();
        welcomeMsg.setMessage(welcomeMessages.getOrDefault(currentLanguage, welcomeMessages.get("en")));
        welcomeMsg.setSender("bot");
        welcomeMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(welcomeMsg);
    }

    private void updateStatus() {
        String langName = languageNames.get(currentLanguage);
        String flag = languageFlags.get(currentLanguage);
        tvStatus.setText("Status: Connected • " + flag + " " + langName);

        // Update language button
        btnLanguage.setText(flag + " " + currentLanguage.toUpperCase());
    }

    private void setupButtonListeners() {
        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

//


        // Language button
        btnLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        // Send on Enter key
        inputMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == 66) { // Enter key
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"English 🇺🇸", "Hindi 🇮🇳", "Spanish 🇪🇸", "French 🇫🇷",
                "German 🇩🇪", "Chinese 🇨🇳", "Arabic 🇸🇦", "Bengali 🇧🇩", "Urdu 🇵🇰"};
        String[] codes = {"en", "hi", "es", "fr", "de", "zh", "ar", "bn", "ur"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    currentLanguage = codes[which];
                    updateStatus();

                    // Update welcome message in new language
                    updateWelcomeMessage();

                    Toast.makeText(this, "Language changed to " + languages[which],
                            Toast.LENGTH_SHORT).show();
                });
        builder.show();
    }

    private void updateWelcomeMessage() {
        Map<String, String> welcomeMessages = new HashMap<>();
        welcomeMessages.put("en", "Hello! I'm your AI assistant. I can help you with app features, password reset, ads, and real-time chat. What would you like to know?");
        welcomeMessages.put("hi", "नमस्ते! मैं आपकी AI सहायक हूं। मैं आपकी ऐप की सुविधाओं, पासवर्ड रीसेट, विज्ञापनों और रीयल-टाइम चैट में मदद कर सकती हूं। आप क्या जानना चाहते हैं?");
        welcomeMessages.put("es", "¡Hola! Soy tu asistente de IA. Puedo ayudarte con funciones de la aplicación, restablecimiento de contraseñas, anuncios y chat en tiempo real. ¿Qué te gustaría saber?");
        welcomeMessages.put("fr", "Bonjour ! Je suis votre assistant IA. Je peux vous aider avec les fonctionnalités de l'application, la réinitialisation du mot de passe, les publicités et le chat en temps réel. Que voudriez-vous savoir ?");
        welcomeMessages.put("de", "Hallo! Ich bin Ihr KI-Assistent. Ich kann Ihnen bei App-Funktionen, Passwort-Reset, Werbung und Echtzeit-Chat helfen. Was möchten Sie wissen?");
        welcomeMessages.put("zh", "你好！我是你的AI助手。我可以帮助你处理应用功能、密码重置、广告和实时聊天。你想知道什么？");
        welcomeMessages.put("ar", "مرحبًا! أنا مساعدك الذكي. يمكنني مساعدتك في ميزات التطبيق، إعادة تعيين كلمة المرور، الإعلانات والدردشة الفورية. ماذا تريد أن تعرف؟");
        welcomeMessages.put("bn", "হ্যালো! আমি আপনার AI সহকারী। আমি আপনাকে অ্যাপের বৈশিষ্ট্য, পাসওয়ার্ড রিসেট, বিজ্ঞাপন এবং রিয়েল-টাইম চ্যাটে সাহায্য করতে পারি। আপনি কি জানতে চান?");
        welcomeMessages.put("ur", "ہیلو! میں آپ کی AI اسسٹنٹ ہوں۔ میں آپ کی ایپ کی خصوصیات، پاس ورڈ ری سیٹ، اشتہارات اور ریئل ٹائم چیٹ میں مدد کر سکتی ہوں۔ آپ کیا جاننا چاہتے ہیں؟");

        if (!messageList.isEmpty()) {
            messageList.get(0).setMessage(welcomeMessages.getOrDefault(currentLanguage,
                    welcomeMessages.get("en")));
            adapter.notifyItemChanged(0);
        }
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Add user message
            ChatMessage userMsg = new ChatMessage();
            userMsg.setMessage(messageText);
            userMsg.setSender("user");
            userMsg.setTimestamp(System.currentTimeMillis());
            messageList.add(userMsg);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            inputMessage.setText("");

            // Show typing indicator
            showTypingIndicator();

            // Generate AI response
            generateAIResponse(messageText);
        }
    }

    private void showTypingIndicator() {
        ChatMessage typingMsg = new ChatMessage();
        typingMsg.setMessage("...");
        typingMsg.setSender("typing");
        typingMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(typingMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void removeTypingIndicator() {
        if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getSender().equals("typing")) {
            messageList.remove(messageList.size() - 1);
            adapter.notifyItemRemoved(messageList.size());
        }
    }

    private void generateAIResponse(String userMessage) {
        removeTypingIndicator();

        // Add language-specific prompt
        String languagePrompt = getLanguagePrompt(currentLanguage);
        String enhancedMessage = languagePrompt + userMessage;

        // Prepare Gemini API request
        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            JSONObject part = new JSONObject();
            part.put("text", enhancedMessage);

            content.put("parts", new org.json.JSONArray().put(part));
            content.put("role", "user");

            jsonBody.put("contents", new org.json.JSONArray().put(content));

            // Add generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1000);
            jsonBody.put("generationConfig", generationConfig);

        } catch (JSONException e) {
            e.printStackTrace();
            showLocalResponse(userMessage);
            return;
        }

        // Call Gemini API
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder().url(URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showLocalResponse(userMessage);
                    tvStatus.setText("Status: Using Local Responses");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String aiResponse = extractAIResponse(jsonResponse);

                        runOnUiThread(() -> {
                            addBotMessage(aiResponse);
                            tvStatus.setText("Status: Gemini Active • " +
                                    languageNames.get(currentLanguage));
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> showLocalResponse(userMessage));
                    }
                } else {
                    runOnUiThread(() -> showLocalResponse(userMessage));
                }
            }
        });
    }

    private String getLanguagePrompt(String langCode) {
        Map<String, String> prompts = new HashMap<>();
        prompts.put("en", "Respond in English about app features, password reset, ads, or chat: ");
        prompts.put("hi", "ऐप की सुविधाओं, पासवर्ड रीसेट, विज्ञापनों या चैट के बारे में हिंदी में उत्तर दें: ");
        prompts.put("es", "Responde en español sobre funciones de la aplicación, restablecimiento de contraseñas, anuncios o chat: ");
        prompts.put("fr", "Répondez en français sur les fonctionnalités de l'application, la réinitialisation du mot de passe, les publicités ou le chat: ");
        prompts.put("de", "Antworten Sie auf Deutsch zu App-Funktionen, Passwort-Reset, Werbung oder Chat: ");
        prompts.put("zh", "用中文回答有关应用程序功能、密码重置、广告或聊天的问题: ");
        prompts.put("ar", "الرد باللغة العربية حول ميزات التطبيق، إعادة تعيين كلمة المرور، الإعلانات أو الدردشة: ");
        prompts.put("bn", "অ্যাপের বৈশিষ্ট্য, পাসওয়ার্ড রিসেট, বিজ্ঞাপন বা চ্যাট সম্পর্কে বাংলায় উত্তর দিন: ");
        prompts.put("ur", "ایپ کی خصوصیات، پاس ورڈ ری سیٹ، اشتہارات یا چیٹ کے بارے میں اردو میں جواب دیں: ");

        return prompts.getOrDefault(langCode, prompts.get("en"));
    }

    private void showLocalResponse(String userMessage) {
        String response = getLocalResponse(userMessage, currentLanguage);
        addBotMessage(response);
    }

    private String getLocalResponse(String userMessage, String langCode) {
        String lowerMsg = userMessage.toLowerCase();

        // ✅ ENGLISH Responses
        if (langCode.equals("en")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **Password Reset Assistance**\n\n" +
                        "To reset your password:\n\n" +
                        "1. Go to **Settings > Account**\n" +
                        "2. Tap **'Reset Password'**\n" +
                        "3. Enter your registered email\n" +
                        "4. Check inbox for reset link\n" +
                        "5. Click link and set new password";
            }
            if (lowerMsg.contains("ad") || lowerMsg.contains("ads") || lowerMsg.contains("advertising")) {
                return "📢 **AdMob Advertising**\n\n" +
                        "This app uses Google AdMob for ads:\n\n" +
                        "• **Banner Ads**: Bottom of screen\n" +
                        "• **Interstitial Ads**: Full-screen on button click\n" +
                        "• **Test Mode**: Currently active\n" +
                        "• **Ad IDs**: Test IDs for development";
            }
            if (lowerMsg.contains("feature") || lowerMsg.contains("function")) {
                return "🌟 **App Features**\n\n" +
                        "Available features in this app:\n\n" +
                        "• **AI Chatbot**: Multilingual assistant\n" +
                        "• **Advertising**: AdMob integration\n" +
                        "• **Password Management**: Reset functionality\n" +
                        "• **Real-time Chat**: Instant messaging\n" +
                        "• **Tokenization**: Text analysis demo";
            }
            if (lowerMsg.contains("chat") || lowerMsg.contains("message")) {
                return "💬 **Chat Features**\n\n" +
                        "You're currently using the chat feature:\n\n" +
                        "• **Multilingual**: 9 languages supported\n" +
                        "• **AI-powered**: Gemini API integration\n" +
                        "• **Real-time**: Instant responses\n" +
                        "• **Local Fallback**: Works offline too";
            }
            // ✅ CHANGED: Short message only
            return "Please ask things relevant to the app only.";
        }

        // ✅ HINDI Responses
        else if (langCode.equals("hi")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **पासवर्ड रीसेट सहायता**\n\n" +
                        "पासवर्ड रीसेट करने के लिए:\n\n" +
                        "1. **सेटिंग्स > अकाउंट** पर जाएं\n" +
                        "2. **'पासवर्ड रीसेट'** टैप करें\n" +
                        "3. अपना रजिस्टर्ड ईमेल दर्ज करें\n" +
                        "4. रीसेट लिंक के लिए अपना इनबॉक्स चेक करें\n" +
                        "5. लिंक पर क्लिक करें और नया पासवर्ड सेट करें";
            }
            // ✅ CHANGED: Short message only
            return "कृपया केवल ऐप से संबंधित प्रश्न पूछें।";
        }

        // ✅ GERMAN Responses
        else if (langCode.equals("de")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **Passwort-Reset-Hilfe**\n\n" +
                        "So setzen Sie Ihr Passwort zurück:\n\n" +
                        "1. Gehen Sie zu **Einstellungen > Konto**\n" +
                        "2. Tippen Sie auf **'Passwort zurücksetzen'**\n" +
                        "3. Geben Sie Ihre registrierte E-Mail ein\n" +
                        "4. Prüfen Sie Ihren Posteingang auf den Reset-Link\n" +
                        "5. Klicken Sie auf den Link और setzen Sie ein neues Passwort";
            }
            // ✅ CHANGED: Short message only
            return "Bitte stellen Sie nur app-bezogene Fragen.";
        }

        // ✅ CHINESE Responses
        else if (langCode.equals("zh")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **密码重置帮助**\n\n" +
                        "要重置密码：\n\n" +
                        "1. 转到**设置 > 账户**\n" +
                        "2. 点击**'重置密码'**\n" +
                        "3. 输入您注册的邮箱\n" +
                        "4. 检查收件箱中的重置链接\n" +
                        "5. 点击链接并设置新密码";
            }
            // ✅ CHANGED: Short message only
            return "请仅询问与应用程序相关的问题。";
        }

        // ✅ ARABIC Responses
        else if (langCode.equals("ar")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **مساعدة إعادة تعيين كلمة المرور**\n\n" +
                        "لإعادة تعيين كلمة المرور:\n\n" +
                        "1. انتقل إلى **الإعدادات > الحساب**\n" +
                        "2. اضغط على **'إعادة تعيين كلمة المرور'**\n" +
                        "3. أدخل بريدك الإلكتروني المسجل\n" +
                        "4. تحقق من البريد الوارد للحصول على رابط إعادة التعيين\n" +
                        "5. انقر على الرابط وقم بتعيين كلمة مرور جديدة";
            }
            // ✅ CHANGED: Short message only
            return "يرجى طرح الأسئلة المتعلقة بالتطبيق فقط۔";
        }

        // ✅ BENGALI Responses
        else if (langCode.equals("bn")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **পাসওয়ার্ড রিসেট সাহায্য**\n\n" +
                        "আপনার পাসওয়ার্ড রিসেট করতে:\n\n" +
                        "1. **সেটিংস > অ্যাকাউন্ট**-এ যান\n" +
                        "2. **'পাসওয়ার্ড রিসেট'** ট্যাপ করুন\n" +
                        "3. আপনার নিবন্ধিত ইমেল লিখুন\n" +
                        "4. রিসেট লিঙ্কের জন্য আপনার ইনবক্স চেক করুন\n" +
                        "5. লিঙ্কে ক্লিক করুন এবং নতুন পাসওয়ার্ড সেট করুন";
            }
            // ✅ CHANGED: Short message only
            return "দয়া করে শুধুমাত্র অ্যাপ-সম্পর্কিত জিনিস জিজ্ঞাসা করুন।";
        }

        // ✅ URDU Responses
        else if (langCode.equals("ur")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **پاس ورڈ ری سیٹ مدد**\n\n" +
                        "اپنا پاس ورڈ ری سیٹ کرنے کے لیے:\n\n" +
                        "1. **ترتیبات > اکاؤنٹ** پر جائیں\n" +
                        "2. **'پاس ورڈ ری سیٹ'** پر ٹیپ کریں\n" +
                        "3. اپنا رجسٹرڈ ای میل درج کریں\n" +
                        "4. ری سیٹ لنک کے لیے اپنا ان باکس چیک کریں\n" +
                        "5. لنک پر کلک کریں اور نیا پاس ورڈ سیٹ کریں";
            }
            // ✅ CHANGED: Short message only
            return "براہ کرم صرف ایپ سے متعلق چیزوں کے بارے میں پوچھیں۔";
        }

        // ✅ SPANISH Responses
        else if (langCode.equals("es")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **Asistencia para restablecer contraseña**\n\n" +
                        "Para restablecer su contraseña:\n\n" +
                        "1. Vaya a **Configuración > Cuenta**\n" +
                        "2. Toque **'Restablecer contraseña'**\n" +
                        "3. Ingrese su correo electrónico registrado\n" +
                        "4. Revise su bandeja de entrada para el enlace de restablecimiento\n" +
                        "5. Haga clic en el enlace y establezca una nueva contraseña";
            }
            // ✅ CHANGED: Short message only
            return "Por favor, pregunte solo cosas relevantes para la aplicación.";
        }

        // ✅ FRENCH Responses
        else if (langCode.equals("fr")) {
            if (lowerMsg.contains("password") || lowerMsg.contains("reset")) {
                return "🔐 **Aide à la réinitialisation du mot de passe**\n\n" +
                        "Pour réinitialiser votre mot de passe:\n\n" +
                        "1. Allez dans **Paramètres > Compte**\n" +
                        "2. Appuyez sur **'Réinitialiser le mot de passe'**\n" +
                        "3. Entrez votre email enregistré\n" +
                        "4. Vérifiez votre boîte de réception para le lien de réinitialisation\n" +
                        "5. Cliquez sur le lien et définissez un nouveau mot de passe";
            }
            // ✅ CHANGED: Short message only
            return "Veuillez poser des questions pertinentes pour l'application uniquement.";
        }

        // Default English response
        return "Please ask things relevant to the app only.";
    }

    private void addBotMessage(String message) {
        ChatMessage botMsg = new ChatMessage();
        botMsg.setMessage(message);
        botMsg.setSender("bot");
        botMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(botMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // ✅ NO TOKEN COUNT UPDATE - Empty method
        // updateTokenizationInfo(message); // Commented out
    }

//    private void showTokenizationDemo() {
//        String exampleQuery = "How do I reset my app password?";
//        String[] tokens = exampleQuery.split("\\s+");
//        int tokenCount = tokens.length;
//
//        StringBuilder tokenList = new StringBuilder();
//        for (int i = 0; i < tokens.length; i++) {
//            tokenList.append(i + 1).append(". ").append(tokens[i]);
//            if (i < tokens.length - 1) tokenList.append("\n");
//        }
//
//        ChatMessage demoMsg = new ChatMessage();
//        demoMsg.setMessage("🔍 **Tokenization Demo**\n\n" +
//                "**Query:** \"" + exampleQuery + "\"\n\n" +
//                "**Tokens:** " + tokenCount + "\n" + tokenList.toString() + "\n\n" +
//                "**Language:** " + languageNames.get(currentLanguage));
//        demoMsg.setSender("system");
//        demoMsg.setTimestamp(System.currentTimeMillis());
//
//        messageList.add(demoMsg);
//        adapter.notifyItemInserted(messageList.size() - 1);
//        recyclerView.scrollToPosition(messageList.size() - 1);
//
//        // ✅ NO TOKEN COUNT - Just keep default text
//        tvTokenInfo.setText("NLP: Ready");
//
//        Toast.makeText(this, "Tokenization demo shown", Toast.LENGTH_SHORT).show();
//    }

    // ✅ REMOVED TOKEN COUNT UPDATE METHOD
    // private void updateTokenizationInfo(String message) {
    //     // Empty method - No token count update
    // }

    private String extractAIResponse(JSONObject jsonResponse) {
        try {
            if (jsonResponse.has("candidates")) {
                org.json.JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    if (candidate.has("content")) {
                        JSONObject content = candidate.getJSONObject("content");
                        if (content.has("parts")) {
                            org.json.JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                return part.getString("text");
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getLocalResponse("I need help", currentLanguage);
    }
}