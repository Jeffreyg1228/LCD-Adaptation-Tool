package me.jeffreyg1228.lcdadaptationtool.util;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ReferenceCube {

    @Nullable
    public final JsonObject elementObj;
    public final boolean isLeftDoor, isLeftCube;
    public final String slotName;
    public final float[] originalP1, originalP3;
    public Vector3f p1, p2, p3, p4, pCenter;
    public float rx = 0.0f, ry = 0.0f, rz = 0.0f;
    public Vector3f[] rotatedVertices;
    public float pixelsPerBlock = 50;

    public VerificationResult verificationResult;
    public String additionalInfo = "";

    private static final int MIN_HEIGHT = 280;

    public ReferenceCube(JsonObject elementObj, boolean isLeftDoor, boolean isLeftCube) {
        this.elementObj = elementObj;
        this.isLeftDoor = isLeftDoor;
        this.isLeftCube = isLeftCube;
        this.slotName = "lcd_door_" + (isLeftCube ? "left" : "right");

        originalP1 = transformCoordinates(Utility.jsonArrayToFloatArray(elementObj.get("from").getAsJsonArray()), isLeftCube);
        originalP3 = transformCoordinates(Utility.jsonArrayToFloatArray(elementObj.get("to").getAsJsonArray()), isLeftCube);
        pCenter = new Vector3f(transformCoordinates(Utility.jsonArrayToFloatArray(elementObj.get("origin").getAsJsonArray()), isLeftCube));

        if (elementObj.has("rotation")) {
            var array = elementObj.get("rotation").getAsJsonArray();
            rx = array.get(0).getAsFloat();
            ry = array.get(1).getAsFloat();
            rz = array.get(2).getAsFloat();
        }

        calculate();
        resetPPB();
    }

    public ReferenceCube(boolean isLeftDoor, boolean isLeftCube, float[] originalP1, float[] originalP3, Vector3f p1, Vector3f p3, Vector3f pCenter, float rx, float ry, float rz) {
        this.elementObj = null;
        this.isLeftDoor = isLeftDoor;
        this.isLeftCube = isLeftCube;
        this.slotName = "lcd_door_" + (isLeftCube ? "left" : "right");
        this.originalP1 = originalP1;
        this.originalP3 = originalP3;
        this.p1 = p1;
        this.p3 = p3;
        this.pCenter = pCenter;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;

        calculate();
        resetPPB();
    }

    public static ReferenceCube getOppositeCube(ReferenceCube originalCube) {
        return new ReferenceCube(!originalCube.isLeftDoor, !originalCube.isLeftCube,
                Utility.negateFirstElement(originalCube.originalP1), Utility.negateFirstElement(originalCube.originalP3),
                Utility.negateX(originalCube.p1), Utility.negateX(originalCube.p3), Utility.negateX(originalCube.pCenter),
                -originalCube.rx, -originalCube.ry, -originalCube.rz);
    }

    private void calculateVertices() {
        if (p1.x == p3.x) {
            p2 = new Vector3f(p1.x, p3.y, p1.z);
            p4 = new Vector3f(p1.x, p1.y, p3.z);
        } else if (p1.y == p3.y) {
            p2 = new Vector3f(p3.x, p1.y, p1.z);
            p4 = new Vector3f(p1.x, p1.y, p3.z);
        } else if (p1.z == p3.z) {
            p2 = new Vector3f(p3.x, p1.y, p1.z);
            p4 = new Vector3f(p1.x, p3.y, p1.z);
        }
    }

    private void rotate() {
        Vector3f v1 = new Vector3f(p1.x, p1.y, p1.z);
        Vector3f v2 = new Vector3f(p2.x, p2.y, p2.z);
        Vector3f v3 = new Vector3f(p3.x, p3.y, p3.z);
        Vector3f v4 = new Vector3f(p4.x, p4.y, p4.z);
        Vector3f c = new Vector3f(pCenter.x, pCenter.y, pCenter.z);

        rotatedVertices = new Vector3f[]{v1, v2, v3, v4};
        for (Vector3f v : rotatedVertices) {
            v.sub(c);
            v.rotateX((float) Math.toRadians(rx));
            v.rotateY((float) Math.toRadians(ry));
            v.rotateZ((float) Math.toRadians(rz));
            v.add(c);
        }
    }

    private static float[] transformCoordinates(float[] coords, boolean isLeft) {
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = isLeft ? floorToDecimals(coords[i] / 16f) : ceilToDecimals(coords[i] / 16f);
        }
        return result;
    }

    private static float floorToDecimals(float value) {
        return (float) (Math.floor(value * 1000) / 1000);
    }

    private static float ceilToDecimals(float value) {
        return (float) (Math.ceil(value * 1000) / 1000);
    }

    private boolean verify() {
        // TODO 扩展到所有数组
        var verificationResult = new VerificationResult();
        if (verificationResult.step1(Utility.areEqual(rotatedVertices[0].y, rotatedVertices[3].y) && Utility.areEqual(rotatedVertices[1].y, rotatedVertices[2].y)
                && rotatedVertices[0].y > rotatedVertices[1].y && rotatedVertices[3].y > rotatedVertices[2].y)) {
            if (slotName.contains("left")) {
                if (verificationResult.step2(Utility.areEqual(rotatedVertices[0].z, rotatedVertices[1].z) && Utility.areEqual(rotatedVertices[3].z, rotatedVertices[2].z)
                        && rotatedVertices[3].z > rotatedVertices[0].z && rotatedVertices[2].z > rotatedVertices[1].z)) {
                    verificationResult.step3(Utility.areEqual(rotatedVertices[0].x, rotatedVertices[3].x) && Utility.areEqual(rotatedVertices[1].x, rotatedVertices[2].x)
                            && rotatedVertices[1].x > rotatedVertices[0].x && rotatedVertices[2].x > rotatedVertices[3].x);
                }
            } else if (slotName.contains("right")) {
                if (verificationResult.step2(Utility.areEqual(rotatedVertices[0].z, rotatedVertices[1].z) && Utility.areEqual(rotatedVertices[3].z, rotatedVertices[2].z)
                        && rotatedVertices[3].z < rotatedVertices[0].z && rotatedVertices[2].z < rotatedVertices[1].z)) {
                    verificationResult.step3(Utility.areEqual(rotatedVertices[0].x, rotatedVertices[3].x) && Utility.areEqual(rotatedVertices[1].x, rotatedVertices[2].x)
                            && rotatedVertices[1].x < rotatedVertices[0].x && rotatedVertices[2].x < rotatedVertices[3].x);
                }
            }
        }
        this.verificationResult = verificationResult;
        return verificationResult.isAllPassed();
    }

    public class VerificationResult {
        private boolean step1, step2, step3;

        boolean step1(boolean passed) {
            step1 = passed;
            return passed;
        }

        boolean step2(boolean passed) {
            step2 = passed;
            return passed;
        }

        boolean step3(boolean passed) {
            step3 = passed;
            return passed;
        }

        public boolean isAllPassed() {
            return step1 && step2 && step3;
        }

        @Override
        public String toString() {
            if (!step1) {
                return "\n第一步验证未通过。这是由于，旋转后的顶点坐标不符合下列条件的一种或两种：\n1) A、D 两点的 Y 坐标相等，且 B、C 两点的 Y 坐标相等；2) A 的 Y 坐标大于 B 的，且 D 的 Y 坐标大于 C 的。" + additionalInfo;
            }
            if (slotName.contains("left")) {
                if (!step2) {
                    return "\n第二步验证未通过。这是由于，旋转后的顶点坐标不符合下列条件的一种或两种：\n1) A、B 两点的 Z 坐标相等，且 D、C 两点的 Z 坐标相等；2) D 的 Z 坐标大于 A 的，且 C 的 Z 坐标大于 B 的。" + additionalInfo;
                }
                if (!step3) {
                    return "\n第三步验证未通过。这是由于，旋转后的顶点坐标不符合下列条件的一种或两种：\n1) A、D 两点的 X 坐标相等，且 B、C 两点的 X 坐标相等；2) B 的 X 坐标大于 A 的，且 C 的 X 坐标大于 D 的。\n请注意，为方便在 GeoGebra 中观察，下列 X 坐标均已取相反数。" + additionalInfo;
                }
            } else if (slotName.contains("right")) {
                if (!step2) {
                    return "\n第二步验证未通过。这是由于，旋转后的顶点坐标不符合下列条件的一种或两种：\n1) A、B 两点的 Z 坐标相等，且 D、C 两点的 Z 坐标相等；2) D 的 Z 坐标小于 A 的，且 C 的 Z 坐标小于 B 的。" + additionalInfo;
                }
                if (!step3) {
                    return "\n第三步验证未通过。这是由于，旋转后的顶点坐标不符合下列条件的一种或两种：\n1) A、D 两点的 X 坐标相等，且 B、C 两点的 X 坐标相等；2) B 的 X 坐标小于 A 的，且 C 的 X 坐标小于 D 的。\n请注意，为方便在 GeoGebra 中观察，下列 X 坐标均已取相反数。" + additionalInfo;
                }
            }
            return "验证全部通过！" + additionalInfo;
        }
    }

    public void reset() {
        p1 = new Vector3f(originalP1);
        p3 = new Vector3f(originalP3);
        calculateVertices();
        rotate();
    }

    public void calculate() {
        additionalInfo = "";
        reset();
        if (verify()) {
            additionalInfo += "\n尝试 1：原坐标（未经交换），成功通过验证。";
        } else {
            reset();
            additionalInfo += "\n尝试 1：原坐标（未经交换），但未通过验证。已重置坐标。";
            if (tryToSwapAll()) {
                additionalInfo += "\n尝试 2：进行了 A、C 点和 B、D 点全部坐标交换，成功通过验证。";
            } else {
                reset();
                additionalInfo += "\n尝试 2：进行了 A、C 点和 B、D 点全部坐标交换，但未通过验证。已重置坐标。";
                if (tryToSwapZ()) {
                    additionalInfo += "\n尝试 3：进行了 A、C 点 Z 坐标交换，B、D 点由此计算，成功通过验证。";
                } else {
                    reset();
                    additionalInfo += "\n尝试 3：进行了 A、C 点 Z 坐标交换，B、D 点由此计算，但未通过验证。已重置坐标。";
                }
            }
        }
    }

    public boolean tryToSwapAll() {
        // TODO 扩展到所有数组
        Utility.swapAll(p1, p3);
        calculateVertices();
        rotate();
        return verify();
    }

    public boolean tryToSwapZ() {
        // TODO 扩展到所有数组
        Utility.swapZ(p1, p3);
        calculateVertices();
        rotate();
        return verify();
    }

    public void resetPPB() {
        pixelsPerBlock = 50;
        while (Math.abs((p3.x - p1.x) * pixelsPerBlock * 16) < MIN_HEIGHT) {
            pixelsPerBlock++;
        }
    }

    public float getTexAreaWidth() {
        // TODO 扩展到所有数组
        if (p1.y == p3.y) {
            return Math.abs((p3.z - p1.z) * pixelsPerBlock * 16);
        } else {
            return 0;
        }
    }

    public float getTexAreaHeight() {
        // TODO 扩展到所有数组
        if (p1.y == p3.y) {
            return Math.abs((p3.x - p1.x) * pixelsPerBlock * 16);
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "在下方配置中对应的位置：" + slotName + "\n" +
                "PPB 数值：" + pixelsPerBlock + "\n\n" +
                (verificationResult == null ? "" : "验证状态：" + verificationResult + "\n\n") +
                "旋转前的各顶点坐标：\n" +
                "左上顶点 = " + Utility.vectorToString(p1) + "\n" +
                "左下顶点 = " + Utility.vectorToString(p2) + "\n" +
                "右下顶点 = " + Utility.vectorToString(p3) + "\n" +
                "右上顶点 = " + Utility.vectorToString(p4) + "\n" +
                "旋转中心 = " + Utility.vectorToString(pCenter) + "\n" +
                "X 轴旋转角度 = " + rx + "，Y 轴旋转角度 = " + ry + "，Z 轴旋转角度 = " + rz + "\n\n" +
                "旋转后的各顶点坐标：\n" +
                "左上顶点 = " + Utility.vectorToString(rotatedVertices[0]) + "\n" +
                "左下顶点 = " + Utility.vectorToString(rotatedVertices[1]) + "\n" +
                "右下顶点 = " + Utility.vectorToString(rotatedVertices[2]) + "\n" +
                "右上顶点 = " + Utility.vectorToString(rotatedVertices[3]);
    }

    public String toDebugString() {
        return "在配置中对应的位置：" + slotName + "\n\n" +
                (verificationResult == null ? "" : "验证状态：" + verificationResult + "\n\n") +
                "旋转前的各顶点坐标：（其中 A 为左上顶点，B 为左下顶点，C 为右下顶点，D 为右上顶点，O 为旋转中心）\n" +
                "A = " + Utility.vectorToString(Utility.negateX(p1)) + "\n" +
                "B = " + Utility.vectorToString(Utility.negateX(p2)) + "\n" +
                "C = " + Utility.vectorToString(Utility.negateX(p3)) + "\n" +
                "D = " + Utility.vectorToString(Utility.negateX(p4)) + "\n" +
                "O = " + Utility.vectorToString(Utility.negateX(pCenter)) + "\n" +
                "X 轴旋转角度 = " + rx + "，Y 轴旋转角度 = " + ry + "，Z 轴旋转角度 = " + rz + "\n\n" +
                "旋转后的各顶点坐标：（其中 A 为左上顶点，B 为左下顶点，C 为右下顶点，D 为右上顶点）\n" +
                "A = " + Utility.vectorToString(Utility.negateX(rotatedVertices[0])) + "\n" +
                "B = " + Utility.vectorToString(Utility.negateX(rotatedVertices[1])) + "\n" +
                "C = " + Utility.vectorToString(Utility.negateX(rotatedVertices[2])) + "\n" +
                "D = " + Utility.vectorToString(Utility.negateX(rotatedVertices[3]));
    }
}