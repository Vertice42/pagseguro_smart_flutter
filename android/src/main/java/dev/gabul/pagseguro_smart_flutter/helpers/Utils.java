package dev.gabul.pagseguro_smart_flutter.helpers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Utils {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] convertStringToBytes(String content) {
        byte[] ret = new byte[16];
        byte[] buf = content.getBytes(StandardCharsets.UTF_8);
        int retLen = ret.length;
        int bufLen = buf.length;
        boolean b = retLen > bufLen;

        for (int i = 0; i < retLen; i++) {
            if (b && i >= bufLen) {
                ret[i] = 0;
                continue;
            }
            ret[i] = buf[i];
        }
        return ret;
    }

    public static String convertBytesToString(byte[] data, boolean mShowDataAsHexString) {
        String ret;
        if (mShowDataAsHexString) {
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                sb.append(Integer.toHexString((int) b & 0xFF).toUpperCase());
            }
            ret = sb.toString();
        } else {
            int pos = data.length;
            for (int i = data.length - 1; i >= 0; i--) {
                if (data[i] != 0) {
                    break;
                }
                pos = i;
            }
            ret = new String(data, 0, pos, StandardCharsets.UTF_8);
        }
        return ret;
    }

    public static byte[] convertIntToBytes(int value, int byteSize) {
        return ByteBuffer.allocate(byteSize).putInt(value).array();
    }

    public static int convertBytesToInt(byte[] array) {
        if (array.length < 4) {
            throw new IllegalArgumentException("O array deve ter pelo menos 4 bytes de comprimento");
        }
        return ByteBuffer.wrap(array).getInt();
    }

    public static float convertBytesToFloat(byte[] array) {
        byte[] result = new byte[4];
        System.arraycopy(array, 0, result, 0, 4);
        return ByteBuffer.wrap(result).getFloat();
    }

    public static ArrayList<byte[]> sliceBytes(byte[] array, int size) {
        ArrayList<byte[]> arrays = new ArrayList<>();
        for (int i = 0; i < array.length; i += size) {
            byte[] slice = new byte[size];
            System.arraycopy(array, i, slice, 0, Math.min(size, array.length - i));
            arrays.add(slice);
        }
        return arrays;
    }

    public static String removeAsterisk(String valor) {
        if(valor != null) {
            return valor.replace("*", "");
        }
        return "";
    }

    public static String addAsterisk(String valor) {
        if (valor.length() >= 16) {
            return valor;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 16 - valor.length()) {
            sb.append('*');
        }
        sb.append(valor);
        return sb.toString();
    }
}
