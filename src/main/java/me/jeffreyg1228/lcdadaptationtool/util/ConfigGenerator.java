package me.jeffreyg1228.lcdadaptationtool.util;

import com.google.gson.*;
import org.joml.Vector3f;

public class ConfigGenerator {

    public ReferenceCube leftCube, rightCube;
    public float[][] lcdOffsets;
    public float[] adjustOffsets;

    public String generateConfig() {
        leftCube.calculate();
        rightCube.calculate();

        float[][] offsets = lcdOffsets.clone();
        if (!Utility.isNullOrZero(adjustOffsets)) {
            adjustLCDOffsets(offsets, adjustOffsets);
        }

        final float leftScreenWidth = leftCube.getTexAreaWidth();
        final float leftScreenHeight = leftCube.getTexAreaHeight();
        final float rightScreenWidth = rightCube.getTexAreaWidth();
        final float rightScreenHeight = rightCube.getTexAreaHeight();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject slotCfg = new JsonObject();
        slotCfg.addProperty("version", 1);

        JsonArray texSizeArray = new JsonArray();
        texSizeArray.add(leftScreenWidth + rightScreenWidth);
        texSizeArray.add(Math.max(leftScreenHeight, rightScreenHeight));
        slotCfg.add("texSize", texSizeArray);

        JsonArray slotsArray = new JsonArray();
        slotsArray.add(getSlot(gson, leftCube, offsets, 0));
        slotsArray.add(getSlot(gson, rightCube, offsets, leftScreenWidth));
        slotCfg.add("slots", slotsArray);

        return gson.toJson(slotCfg);
    }

    private JsonObject getSlot(Gson gson, ReferenceCube cube, float[][] offsets, float x) {
        JsonObject slot = new JsonObject();
        slot.addProperty("name", cube.slotName);
        slot.add("texArea", gson.toJsonTree(new Object[]{x, 0, cube.getTexAreaWidth(), cube.getTexAreaHeight()}).getAsJsonArray());
        slot.add("pos", getPos(cube));
        slot.add("offsets", gson.toJsonTree(offsets));
        return slot;
    }

    private JsonArray getPos(ReferenceCube cube) {
        JsonArray outerArray = new JsonArray();
        JsonArray innerArray = new JsonArray();
        for (Vector3f v : cube.rotatedVertices) {
            JsonArray pointArray = new Gson().toJsonTree(new float[]{v.x(), v.y(), v.z()}).getAsJsonArray();
            innerArray.add(pointArray);
        }
        outerArray.add(innerArray);
        return outerArray;
    }

    public static void adjustLCDOffsets(float[][] lcdOffsets, float[] adjustOffsets) {
        for (float[] lcdOffset : lcdOffsets) {
            for (int i = 0; i < 3; i++) {
                lcdOffset[i] += adjustOffsets[i];
            }
        }
    }
}