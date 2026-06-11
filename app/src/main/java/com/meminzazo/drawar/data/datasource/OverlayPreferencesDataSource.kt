package com.meminzazo.drawar.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.meminzazo.drawar.domain.model.OverlayState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OverlayPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_IMAGE_URI = stringPreferencesKey("image_uri")
        val KEY_OPACITY = floatPreferencesKey("opacity")
        val KEY_OFFSET_X = floatPreferencesKey("offset_x")
        val KEY_OFFSET_Y = floatPreferencesKey("offset_y")
        val KEY_SCALE = floatPreferencesKey("scale")
        val KEY_ROTATION = floatPreferencesKey("rotation")
        val KEY_FLIP_H = booleanPreferencesKey("flip_horizontal")
        val KEY_FLIP_V = booleanPreferencesKey("flip_vertical")
        val KEY_IS_LOCKED = booleanPreferencesKey("is_locked")
        val KEY_IS_TORCH_ON = booleanPreferencesKey("is_torch_on")
    }

    val overlayState: Flow<OverlayState> = dataStore.data.map { prefs ->
        OverlayState(
            imageUri = prefs[KEY_IMAGE_URI],
            opacity = prefs[KEY_OPACITY] ?: 0.5f,
            offsetXPercent = prefs[KEY_OFFSET_X] ?: 0f,
            offsetYPercent = prefs[KEY_OFFSET_Y] ?: 0f,
            scale = prefs[KEY_SCALE] ?: 1f,
            rotation = prefs[KEY_ROTATION] ?: 0f,
            flipHorizontal = prefs[KEY_FLIP_H] ?: false,
            flipVertical = prefs[KEY_FLIP_V] ?: false,
            isLocked = prefs[KEY_IS_LOCKED] ?: false,
            isTorchOn = prefs[KEY_IS_TORCH_ON] ?: false
        )
    }

    suspend fun save(state: OverlayState){
        dataStore.edit { prefs ->
            state.imageUri?.let { prefs[KEY_IMAGE_URI] = it }
                ?: prefs.remove(KEY_IMAGE_URI)

            prefs[KEY_OPACITY] = state.opacity
            prefs[KEY_OFFSET_X] = state.offsetXPercent
            prefs[KEY_OFFSET_Y] = state.offsetYPercent
            prefs[KEY_SCALE] = state.scale
            prefs[KEY_ROTATION] = state.rotation
            prefs[KEY_FLIP_H] = state.flipHorizontal
            prefs[KEY_FLIP_V] = state.flipVertical
            prefs[KEY_IS_LOCKED] = state.isLocked
            prefs[KEY_IS_TORCH_ON] = state.isTorchOn
        }
    }

    suspend fun reset(){
        dataStore.edit { prefs ->
            // Conserva la imagen, resetea solo los parámetros visuales
            val savedUir = prefs[KEY_IMAGE_URI]
            prefs.clear()
            savedUir?.let { prefs[KEY_IMAGE_URI] = it }
        }
    }
}