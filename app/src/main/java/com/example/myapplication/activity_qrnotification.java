//package com.example.myapplication;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.net.Uri;
//import android.os.Build;
//import android.util.Log;
//
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//public class activity_qrnotification {
//
//    private static final String TAG = "QRNotification";
//    private static final String CHANNEL_ID = "qr_scanner_channel";
//    private static final String CHANNEL_NAME = "QR Scanner Notifications";
//    private static int notificationId = 1000; // Starting ID
//
//    // Create notification channel (call this once in your main activity)
//    public static void createNotificationChannel(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    CHANNEL_NAME,
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            channel.setDescription("Notifications for QR code scanning");
//            channel.enableLights(true);
//            channel.setLightColor(Color.GREEN);
//            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
//
//            NotificationManager manager = context.getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//                Log.d(TAG, "QR notification channel created");
//            }
//        }
//    }
//
//    // Show QR scan success
//    public static void showScanSuccess(Context context, String qrContent) {
//        try {
//            // Create intent to open app
//            Intent intent = new Intent(context, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    context,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//            );
//
//            // Trim content for display
//            String displayText = (qrContent.length() > 40)
//                    ? qrContent.substring(0, 40) + "..."
//                    : qrContent;
//
//            // Build notification
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_qr_code)
//                    .setContentTitle("✅ QR Code Scanned!")
//                    .setContentText(displayText)
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText("Scanned Content:\n" + qrContent))
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true)
//                    .setColor(Color.GREEN)
//                    .setLights(Color.GREEN, 1000, 1000);
//
//            // Add actions if it's a URL
//            if (qrContent.startsWith("http://") || qrContent.startsWith("https://")) {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrContent));
//                PendingIntent browserPendingIntent = PendingIntent.getActivity(
//                        context,
//                        1,
//                        browserIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//                );
//
//                builder.addAction(R.drawable.ic_open, "Open Link", browserPendingIntent);
//            }
//
//            // Show notification
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            int currentId = notificationId++;
//            notificationManager.notify(currentId, builder.build());
//
//            Log.d(TAG, "QR success notification shown with ID: " + currentId);
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error showing QR notification: " + e.getMessage());
//        }
//    }
//
//    // Show QR scan error
//    public static void showScanError(Context context, String errorMessage) {
//        try {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_error)
//                    .setContentTitle("❌ QR Scan Failed")
//                    .setContentText(errorMessage)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setAutoCancel(true)
//                    .setColor(Color.RED);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            int currentId = notificationId++;
//            notificationManager.notify(currentId, builder.build());
//
//            Log.d(TAG, "QR error notification shown with ID: " + currentId);
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error showing error notification: " + e.getMessage());
//        }
//    }
//
//    // Show QR saved to history
//    public static void showSavedToHistory(Context context, String qrContent) {
//        try {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_save)
//                    .setContentTitle("💾 QR Saved")
//                    .setContentText("Saved to scan history")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .setAutoCancel(true)
//                    .setColor(Color.BLUE);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            int currentId = notificationId++;
//            notificationManager.notify(currentId, builder.build());
//
//            Log.d(TAG, "QR saved notification shown with ID: " + currentId);
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error showing saved notification: " + e.getMessage());
//        }
//    }
//
//    // Show multiple QR codes scanned
//    public static void showBatchScanResult(Context context, int scanCount) {
//        try {
//            Intent intent = new Intent(context, HistoryActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    context,
//                    2,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//            );
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_batch)
//                    .setContentTitle("📊 Batch Scan Complete")
//                    .setContentText("Successfully scanned " + scanCount + " QR codes")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true)
//                    .setColor(Color.MAGENTA);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            int currentId = notificationId++;
//            notificationManager.notify(currentId, builder.build());
//
//            Log.d(TAG, "Batch scan notification shown for " + scanCount + " items");
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error showing batch notification: " + e.getMessage());
//        }
//    }
//
//    // Clear all QR notifications
//    public static void clearAllNotifications(Context context) {
//        try {
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            notificationManager.cancelAll();
//            Log.d(TAG, "All QR notifications cleared");
//        } catch (Exception e) {
//            Log.e(TAG, "Error clearing notifications: " + e.getMessage());
//        }
//    }
//
//    // Check if notifications are enabled
//    public static boolean areNotificationsEnabled(Context context) {
//        try {
//            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//            return manager.areNotificationsEnabled();
//        } catch (Exception e) {
//            Log.e(TAG, "Error checking notification status: " + e.getMessage());
//            return false;
//        }
//    }
//
//    // Get next notification ID
//    public static int getNextNotificationId() {
//        return notificationId++;
//    }
//}