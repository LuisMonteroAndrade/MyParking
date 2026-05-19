package com.miestacionamiento.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val LANGUAGE = stringPreferencesKey("language")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_TYPE = stringPreferencesKey("user_type")
    private val USER_ID = intPreferencesKey("user_id")
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    private val VEHICLE_BRAND = stringPreferencesKey("vehicle_brand")
    private val LICENSE_PLATE = stringPreferencesKey("license_plate")
    private val VEHICLE_PHOTO_URI = stringPreferencesKey("vehicle_photo_uri")
    private val USER_ADDRESS = stringPreferencesKey("user_address")
    private val USER_COMMUNE = stringPreferencesKey("user_commune")
    private val USER_REGION = stringPreferencesKey("user_region")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "es" }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "Usuario" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[USER_EMAIL] ?: "" }
    val userType: Flow<String> = context.dataStore.data.map { it[USER_TYPE] ?: "DRIVER" }
    val userId: Flow<Int> = context.dataStore.data.map { it[USER_ID] ?: 0 }
    val authToken: Flow<String> = context.dataStore.data.map { it[AUTH_TOKEN] ?: "" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val profileImageUri: Flow<String> = context.dataStore.data.map { it[PROFILE_IMAGE_URI] ?: "" }
    val vehicleBrand: Flow<String> = context.dataStore.data.map { it[VEHICLE_BRAND] ?: "" }
    val licensePlate: Flow<String> = context.dataStore.data.map { it[LICENSE_PLATE] ?: "" }
    val vehiclePhotoUri: Flow<String> = context.dataStore.data.map { it[VEHICLE_PHOTO_URI] ?: "" }
    val userAddress: Flow<String> = context.dataStore.data.map { it[USER_ADDRESS] ?: "" }
    val userCommune: Flow<String> = context.dataStore.data.map { it[USER_COMMUNE] ?: "" }
    val userRegion: Flow<String> = context.dataStore.data.map { it[USER_REGION] ?: "" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun saveUserSession(
        id: Int,
        name: String,
        email: String,
        type: String,
        token: String,
        vehicleBrand: String? = null,
        vehiclePlate: String? = null,
        address: String? = null,
        commune: String? = null,
        region: String? = null
    ) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USER_ID] = id
            it[USER_NAME] = name
            it[USER_EMAIL] = email
            it[USER_TYPE] = type
            it[AUTH_TOKEN] = token
            if (vehicleBrand != null) it[VEHICLE_BRAND] = vehicleBrand
            if (vehiclePlate != null) it[LICENSE_PLATE] = vehiclePlate
            if (address != null) it[USER_ADDRESS] = address
            if (commune != null) it[USER_COMMUNE] = commune
            if (region != null) it[USER_REGION] = region
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateProfile(name: String, email: String, photoUri: String) {
        context.dataStore.edit {
            it[USER_NAME] = name
            it[USER_EMAIL] = email
            it[PROFILE_IMAGE_URI] = photoUri
        }
    }

    suspend fun updateVehicleInfo(brand: String, plate: String, photoUri: String) {
        context.dataStore.edit {
            it[VEHICLE_BRAND] = brand
            it[LICENSE_PLATE] = plate
            it[VEHICLE_PHOTO_URI] = photoUri
        }
    }

    suspend fun updateOwnerInfo(address: String, commune: String, region: String) {
        context.dataStore.edit {
            it[USER_ADDRESS] = address
            it[USER_COMMUNE] = commune
            it[USER_REGION] = region
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
