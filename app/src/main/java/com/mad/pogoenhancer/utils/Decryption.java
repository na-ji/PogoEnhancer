package com.mad.pogoenhancer.utils;

import com.mad.pogoenhancer.BackendStorage;
import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decryption {
//    private static final String prototype = "YWJjZGVmZ2hpamtsbW9wcQ==;J7d2VgAxCwLxljzSx14dYg==";
//    private static final String key = "abcdefghijklmopq";

    public static String decrypt(String messageBase64Encoded) {
        String[] split = messageBase64Encoded.split(";");
        if (split.length != 2) {
            return null;
        }
        byte[] decodedIv = Base64.decode(split[0]);
        byte[] decodedPayload = Base64.decode(split[1]);

        try {
            IvParameterSpec iv = new IvParameterSpec(decodedIv);
//            String key = BackendStorage.getInstance().getSessionId().substring(0, 16);
            String key = BackendStorage.getInstance().getDeviceId();
            try {
                key = Constants.hexadecimal(key);
            } catch (UnsupportedEncodingException e) {
                Logger.fatal("PogoEnhancerJ", "Failed converting to hex in dec");
                return null;
            }
            int toBePadded = 16 - key.length() + 1;
            for (int i = 1; i < toBePadded; i++) {
                key = key.concat(String.valueOf(i));
            }
            key = key.substring(0, 16); //in case we got above 9

            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(decodedPayload);
            //Log.v("Decryption successful", new String(original, "UTF-8"));
            return new String(original);
        } catch (Exception ex) {
            Logger.debug("PogoEnhancerJ", "Coult not decrypt: " + ex.toString());
        }
        return null;
    }
}
