package com.mad.pogoenhancer.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

//https://stackoverflow.com/questions/45552353/export-and-import-all-sharedpreferences-to-a-file
public class PreferenceSerializer {

    private final SharedPreferences _settings;

    public PreferenceSerializer(SharedPreferences preferences) {
        this._settings = preferences;
    }

    public boolean exportSettings() {
        boolean success = false;
        try {
            File direct = new File("/storage/emulated/0/");

            if (!direct.exists()) {
                direct.mkdir();
            }
            String fileName = "pogosettings_pogoenhancer.pgdr";

            File file = new File("/storage/emulated/0/" + fileName);

            file.createNewFile();

            if (file.exists()) {

                OutputStream fileOutputStream = new FileOutputStream(file, false);
                success = serialize(fileOutputStream);
                fileOutputStream.close();
            }

            //if (context != null)
            //MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

        } catch (Exception e) {
            Logger.error("PogoEnhancerJ", "Error while logging into file : " + e);
        }
        return success;
    }

    /**
     * Serialize all preferences into an output stream
     *
     * @param os OutputStream to write to
     * @return True iff successful
     */
    private boolean serialize(final @NonNull OutputStream os) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(_settings.getAll());
            oos.close();
        } catch (IOException e) {
            Logger.error(Constants.LOGTAG, "Error serializing preferences: " + e.toString());
            return false;
        } finally {
//            Utils.closeQuietly(oos, os);
        }
        return true;
    }

    public boolean importSettings(String path) {
        boolean success = false;
        try {
            File settingsFile = new File(path);
            if (!settingsFile.exists() || !settingsFile.isFile() || !settingsFile.canRead()) {
                Logger.warning("PogoEnhancerJ", "Cannot read file " + path);
                return false;
            }

            InputStream fileInputStream = new FileInputStream(settingsFile);

            success = deserialize(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.error("PogoEnhancerJ", "File not found: " + path);
        } catch (IOException e) {
            Logger.warning("PogoEnhancerJ", "importSettings: could not close input stream");
        }

        return success;
    }

    /**
     * Read all preferences from an input stream.
     * Schedules a full preference clean, then deserializes the options present in the given stream.
     * If the given object contains an unknown class, the deserialization is aborted and the underlying
     * preferences are not changed by this method
     *
     * @param is Input stream to load the preferences from
     * @return True iff the new values were successfully written to persistent storage
     * @throws IllegalArgumentException
     */
    private boolean deserialize(final @NonNull InputStream is) {
        ObjectInputStream ois = null;
        Map<String, Object> map = null;
        try {
            ois = new ObjectInputStream(is);
            map = (Map) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.error(Constants.LOGTAG, "Error deserializing preferences: " + e.toString());
            return false;
        } finally {
            //Utils.closeQuietly(ois, is);
        }

        SharedPreferences.Editor editor = _settings.edit();
        editor.clear();

        for (Map.Entry<String, Object> e : map.entrySet()) {
            // Unfortunately, the editor only provides typed setters
            if (e.getKey().equals(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID)) {
                //we do not want to move the deviceID...
                continue;
            } else if (e.getKey().equals(Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED)) {
                // neither do we want to store IV overlay enabled since that permission needs to be present
                continue;
            }

            if (e.getValue() instanceof Boolean) {
                editor.putBoolean(e.getKey(), (Boolean) e.getValue());
            } else if (e.getValue() instanceof String) {
                editor.putString(e.getKey(), (String) e.getValue());
            } else if (e.getValue() instanceof Integer) {
                editor.putInt(e.getKey(), (int) e.getValue());
            } else if (e.getValue() instanceof Float) {
                editor.putFloat(e.getKey(), (float) e.getValue());
            } else if (e.getValue() instanceof Long) {
                editor.putLong(e.getKey(), (Long) e.getValue());
            } else if (e.getValue() instanceof Set) {
                editor.putStringSet(e.getKey(), (Set<String>) e.getValue());
            } else {
                throw new IllegalArgumentException("Type " + e.getValue().getClass().getName() + " is unknown");
            }
        }
        return editor.commit();
    }
}
