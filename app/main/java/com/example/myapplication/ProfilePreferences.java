package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class ProfilePreferences {

    private static final String PREF_NAME = "user_profile_prefs";

    public static void saveProfile(Context context, String uid, String name, int age,
                                   String position, String experience,
                                   String preferredFoot, String trainingGoal, String photoUrl) {
        if (context == null || uid == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(uid + "_name", name);
        editor.putInt(uid + "_age", age);
        editor.putString(uid + "_position", position);
        editor.putString(uid + "_experience", experience);
        editor.putString(uid + "_preferredFoot", preferredFoot);
        editor.putString(uid + "_trainingGoal", trainingGoal);
        if (photoUrl != null) {
            editor.putString(uid + "_photoUrl", photoUrl);
        }
        editor.putBoolean(uid + "_has_profile", true);
        editor.apply();
    }

    public static boolean hasProfile(Context context, String uid) {
        if (context == null || uid == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(uid + "_has_profile", false);
    }

    public static Map<String, Object> getProfile(Context context, String uid) {
        if (context == null || uid == null) return null;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains(uid + "_name") && !prefs.contains(uid + "_age") && !prefs.getBoolean(uid + "_has_profile", false)) {
            return null;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", prefs.getString(uid + "_name", ""));
        profile.put("age", prefs.getInt(uid + "_age", 0));
        profile.put("position", prefs.getString(uid + "_position", ""));
        profile.put("experience", prefs.getString(uid + "_experience", ""));
        profile.put("preferredFoot", prefs.getString(uid + "_preferredFoot", ""));
        profile.put("trainingGoal", prefs.getString(uid + "_trainingGoal", ""));
        profile.put("photoUrl", prefs.getString(uid + "_photoUrl", ""));
        return profile;
    }
}
