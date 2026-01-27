package com.mad.pogoenhancer.services;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.mad.pogoenhancer.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class UnixSender {
    private static final String derp = "";
    private static byte getNthByteOfInt(int number, int n) {
        byte ret = -1;
        if(n < 0 || n > 3) {
            ret = -1;
        } else {
            ret = (byte) ((number >> (8*n)) & 0xff);
        }
        return ret;
    }

    public static void sendMessage(String message) {
        synchronized (derp) {
            Logger.debug("PogoEnhancerJ", "Sending data");
            LocalSocketAddress socketAddress = null;
            socketAddress = new LocalSocketAddress("proto");
            LocalSocket localSocket = new LocalSocket();
            try {
                localSocket.connect(socketAddress);
            } catch (IOException e) {
                Logger.error("PogoEnhancerJ", "Failed connecting to socket: " + e.getMessage() + " -> " + e);
                return;
            }
            OutputStream outputStream = null;
            try {
                outputStream = localSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    localSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }

            // Now format the massage...
            StringBuilder toSend = new StringBuilder();
            // TODO: Encrypt...
            toSend.append(message);
            try {
                outputStream.write(toSend.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                localSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.debug("PogoEnhancerJ", "Done sending data");
        }

    }
}
