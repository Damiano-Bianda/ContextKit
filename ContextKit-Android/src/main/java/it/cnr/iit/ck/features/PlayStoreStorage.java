package it.cnr.iit.ck.features;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PlayStoreStorage {

    public static final String UNKNOWN_APP_CATEGORY = "UNKNOWN";

    private static final String PLAY_STORE_CATEGORIES_SHARED_PREFERENCES = PlayStoreStorage.class.getSimpleName() + "play store categories shared preferences";

    /**
     * Store given category associated with packageName, substitute category if packageName already exists in storage
     * @param context
     * @param appCategory
     */
    public static void storeAppCategory(Context context, AppCategory appCategory) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(appCategory.getPackageName(), appCategory.getCategory());
        editor.apply();
    }

    /**
     * Retrieve the category associated with packageName
     * @param context
     * @param packageName
     * @return the category or null if packageName is not stored
     */
    public static String readAppCategory(Context context, String packageName) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(packageName, null);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PLAY_STORE_CATEGORIES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

}
