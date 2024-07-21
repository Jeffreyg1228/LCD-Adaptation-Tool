package me.jeffreyg1228.lcdadaptationtool.util;

import com.google.gson.JsonArray;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

    private static final float EPSILON = 0.00001f;

    public static void openURL(String url, Component parent) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Throwable e) {
            LOGGER.error("", e);
            copyText(url);
            JOptionPane.showMessageDialog(parent, "无法打开帮助页面，链接已复制到剪贴板。\n请手动访问：" + url, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void copyText(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static boolean areEqual(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean isNullOrZero(float[] floatArray) {
        return floatArray == null || floatArray.length == 0 || (floatArray[0] == 0.0f && floatArray[1] == 0.0f && floatArray[2] == 0.0f);
    }

    public static boolean isNullOrEmpty(float[][] floatArray) {
        return floatArray == null || floatArray.length == 0;
    }

    public static Vector3f negateX(Vector3f v) {
        return new Vector3f(-v.x, v.y, v.z);
    }

    public static float[] negateFirstElement(float[] array) {
        float[] result = array.clone();
        result[0] = -result[0];
        return result;
    }

    public static float[] negateAllElements(float[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = -array[i];
        }
        return result;
    }

    public static void swapZ(Vector3f v1, Vector3f v2) {
        float tempZ = v1.z;
        v1.z = v2.z;
        v2.z = tempZ;
    }

    public static void swapAll(Vector3f v1, Vector3f v2) {
        float tempX = v1.x;
        float tempY = v1.y;
        float tempZ = v1.z;

        v1.x = v2.x;
        v1.y = v2.y;
        v1.z = v2.z;

        v2.x = tempX;
        v2.y = tempY;
        v2.z = tempZ;
    }

    public static float[] jsonArrayToFloatArray(JsonArray originArray) {
        float[] floatArray = new float[originArray.size()];
        for (int i = 0; i < originArray.size(); i++) {
            floatArray[i] = originArray.get(i).getAsFloat();
        }
        return floatArray;
    }

    public static String floatArrayToString(float[] array) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String vectorToString(Vector3f v) {
        return "(" + v.x() + ", " + v.y() + ", " + v.z() + ")";
    }
}