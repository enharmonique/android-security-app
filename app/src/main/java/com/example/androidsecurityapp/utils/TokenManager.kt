package com.example.androidsecurityapp.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object TokenManager {

    private const val FILE_NAME = "secure_prefs"
    private const val TOKEN_KEY = "jwt_token"

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            FILE_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveToken(context: Context, token: String) {
        val editor = getEncryptedPrefs(context).edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getEncryptedPrefs(context).getString(TOKEN_KEY, null)
    }

    fun clearToken(context: Context) {
        val editor = getEncryptedPrefs(context).edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
}