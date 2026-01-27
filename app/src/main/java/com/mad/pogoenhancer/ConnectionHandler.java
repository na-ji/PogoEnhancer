package com.mad.pogoenhancer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mad.pogoenhancer.ProtoParsers.RawParser;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.services.UnixSender;
import com.mad.pogoenhancer.utils.Decryption;
import com.mad.pogoenhancer.utils.IvToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ConnectionHandler {
    final String TAG = "ConnectionHandler";

    int htonl(int value) {
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
            return value;
        }
        return Integer.reverseBytes(value);
    }

    public void handle(InputStream inputStream, OutputStream outputStream,
                       SharedPreferences sharedPreferences,
                       HookReceiverService _hookService) throws IOException {
        String destination = sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.POST_DESTINATION,
                Constants.DEFAULT_VALUES.DEFAULT_POST_DESTINATION);
        //first send the token on connection for validation
        //outputStream.write(<token or other validation>);

        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        boolean end = false;
//        String dataString = "";
        StringBuilder dataString = new StringBuilder();

        //C code sends htonl (BIG Endian), java works with big endian, so we are good to go
        inputStream.read(buffer, 0, 4);

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
        int bytesToRead = byteBuffer.getInt();

        //The following code shows in detail how to read from a TCP socket
        try {
            while (!end) {
                bytesRead = inputStream.read(buffer);
                dataString.append(new String(buffer, 0, bytesRead));
                if (dataString.length() == bytesToRead) {
                    end = true;
//                    break;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            Logger.info("PogoEnhancerJ", "Failed reading buffer properly");
            return;
        }
        String received = dataString.toString();
        //let's decrypt it...
        received = Decryption.decrypt(received);
        if (received == null) {
            Logger.info("PogoEnhancerJ", "Received message not of interest, skipping.");
            return;
        }

        // we received the decrypted data, now split it to read the message typ
        String[] decryptedSplit = received.split(";", 2);
        int messageType = Integer.valueOf(decryptedSplit[0]);
        String payload = decryptedSplit[1];

//        boolean externalCommunicationDisabled = sharedPreferences.getBoolean(
//                Constants.SHAREDPERFERENCES_KEYS.DISABLE_EXTERNAL_COMMUNICATION,
//                Constants.DEFAULT_VALUES.DISABLE_EXTERNAL_COMMUNICATION
//        );
        boolean externalCommunicationDisabled = true;

        String origin = sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.POST_ORIGIN,
                Constants.DEFAULT_VALUES.POST_ORIGIN);

        boolean authEnabled = sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.AUTH_ENABLED,
                Constants.DEFAULT_VALUES.AUTH_ENABLED);
        String authUsername = sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.AUTH_USERNAME,
                Constants.DEFAULT_VALUES.AUTH_USERNAME);
        String authPassword = sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.AUTH_PASSWORD,
                Constants.DEFAULT_VALUES.AUTH_PASSWORD);
        Logger.debug("PogoEnhancerJ", "Handling type " + messageType);
        switch (messageType) {
            case 1:
                JSONObject json = null;
                try {
                    RawParser.parse(payload, _hookService);
                } catch (InvalidProtocolBufferException | IndexOutOfBoundsException | JSONException e) {
                    Logger.error("PogoEnhancerJ", "Failed reading data: " + received);
                }
                break;
            case 2:

                String heightXSXL = "";
                String weightXSXL = "";
                int ditto = 0;
                // we received raw IV values to be displayed via an overlay

                // let's demangle the raw data that's sent asan ugly string
                String[] statsToBeDisplayed = payload.split(",");
                // Logger.debug("PogoEnhancerJ", Integer.toString(statsToBeDisplayed.length));
                if (statsToBeDisplayed.length < 11) {
                    Logger.info("PogoEnhancerJ", "Received invalid amount of data for IV Overlay");
                    return;
                }
                // it's usually "attack,defense,stamina"
                int attack = Integer.valueOf(statsToBeDisplayed[0]);
                int defense = Integer.valueOf(statsToBeDisplayed[1]);
                int stamina = Integer.valueOf(statsToBeDisplayed[2]);
                float cpMultiplier = Float.valueOf(statsToBeDisplayed[3]);
                float additionalCpMultiplied = Float.valueOf(statsToBeDisplayed[4]);
                int monLvl = Integer.valueOf(statsToBeDisplayed[5]);
                int shiny = Integer.valueOf(statsToBeDisplayed[6]);
                int typ = Integer.valueOf(statsToBeDisplayed[7]);
                int gender = Integer.valueOf(statsToBeDisplayed[8]);
                if (statsToBeDisplayed.length > 10) {
                    heightXSXL = String.valueOf(statsToBeDisplayed[10]);
                    weightXSXL = String.valueOf(statsToBeDisplayed[11]);
                    ditto = Integer.valueOf(statsToBeDisplayed[12]);
                }
                //int weather = Integer.valueOf(statsToBeDisplayed[9]) - 1;
                int weather = 0;

//                int level = Constants.getLevelByMultiplier(cpMultiplier + additionalCpMultiplied);
//                if(level > 0) {
//                    Logger.fatal("A", "A");
//                }
                _hookService.showIvOverlay(attack, defense, stamina, cpMultiplier, additionalCpMultiplied, monLvl, shiny, typ, gender, weather, weightXSXL, heightXSXL, ditto);
                break;
            case 3:
                JSONObject derp = new JSONObject();
                try {
                    derp.put("raw", payload);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!externalCommunicationDisabled) {
                    PostData.sendPost(derp, destination, origin, authEnabled, authUsername,
                            authPassword, _hookService);
                }
                break;
            case 4:
                // Injection requested auth data, send it over...
                try {
                    _hookService.getPogoPatcher().sendCredentials();
                } catch (JSONException e) {
                    Logger.debug("ProtoHookJ", "Failed sending data");
                }
                break;
            case 5:
                _hookService.sendToast(payload);
                break;
            case 6:
                Logger.debug("PogoEnhancerJ", "Cooldown: " + payload);
                String[] cooldown = payload.split(",");
                if (cooldown.length < 2) {
                    Logger.info("PogoEnhancerJ", "Received invalid amount of data for cooldown calculation");
                    return;
                }
                _hookService.saveCooldown(cooldown[0], cooldown[1], Long.parseLong(cooldown[2]));

        }
    }
}
