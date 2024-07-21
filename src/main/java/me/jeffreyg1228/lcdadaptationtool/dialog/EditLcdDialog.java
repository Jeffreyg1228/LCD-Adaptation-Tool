package me.jeffreyg1228.lcdadaptationtool.dialog;

import me.jeffreyg1228.lcdadaptationtool.util.ReferenceCube;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Vector;

public class EditLcdDialog {

    JComboBox<String> sideSelector;
    JPanel leftPanel, rightPanel;
    JTextField leftXField, leftYField, leftZField, leftPpbField;
    JTextField rightXField, rightYField, rightZField, rightPpbField;

    public EditLcdDialog(Component parent, String headDirection, ReferenceCube leftCube, ReferenceCube rightCube) {
        boolean hasLeftCube = leftCube != null;
        boolean hasRightCube = rightCube != null;

        Vector<String> options = new Vector<>();
        if (headDirection.equals(BlockBenchConfigDialog.Z_POSITIVE) ? hasLeftCube : hasRightCube)
            options.add(LCDConfigDialog.LEFT_DOOR);
        if (headDirection.equals(BlockBenchConfigDialog.Z_POSITIVE) ? hasRightCube : hasLeftCube)
            options.add(LCDConfigDialog.RIGHT_DOOR);
        sideSelector = new JComboBox<>(options);

        leftPanel = createCubePanel(leftCube, true);
        rightPanel = createCubePanel(rightCube, false);

        sideSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updatePanelVisibility();
            }
        });

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(sideSelector, gbc);

        gbc.gridy = 1;
        mainPanel.add(leftPanel, gbc);
        mainPanel.add(rightPanel, gbc);

        updatePanelVisibility();

        int result = JOptionPane.showConfirmDialog(parent, mainPanel, "编辑 LCD 属性", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                updateCube(leftCube, leftXField, leftYField, leftZField, leftPpbField);
                updateCube(rightCube, rightXField, rightYField, rightZField, rightPpbField);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "请输入有效的数字。", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createCubePanel(ReferenceCube cube, boolean isLeft) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        JTextField xField = new JTextField(10);
        JTextField yField = new JTextField(10);
        JTextField zField = new JTextField(10);
        JTextField ppbField = new JTextField(10);

        if (isLeft) {
            leftXField = xField;
            leftYField = yField;
            leftZField = zField;
            leftPpbField = ppbField;
        } else {
            rightXField = xField;
            rightYField = yField;
            rightZField = zField;
            rightPpbField = ppbField;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X 轴旋转角度："), gbc);
        gbc.gridx = 1;
        panel.add(xField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Y 轴旋转角度："), gbc);
        gbc.gridx = 1;
        panel.add(yField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Z 轴旋转角度："), gbc);
        gbc.gridx = 1;
        panel.add(zField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("PPB 数值："), gbc);
        gbc.gridx = 1;
        panel.add(ppbField, gbc);

        if (cube != null) {
            xField.setText(String.valueOf(cube.rx));
            yField.setText(String.valueOf(cube.ry));
            zField.setText(String.valueOf(cube.rz));
            ppbField.setText(String.valueOf(cube.pixelsPerBlock));
        }

        return panel;
    }

    private void updatePanelVisibility() {
        boolean isLeftSelected = sideSelector.getSelectedItem() == LCDConfigDialog.LEFT_DOOR;
        leftPanel.setVisible(isLeftSelected);
        rightPanel.setVisible(!isLeftSelected);
    }

    private void updateCube(ReferenceCube cube, JTextField xField, JTextField yField, JTextField zField, JTextField ppbField) {
        if (cube != null) {
            cube.rx = Float.parseFloat(xField.getText());
            cube.ry = Float.parseFloat(yField.getText());
            cube.rz = Float.parseFloat(zField.getText());
            float ppb = Float.parseFloat(ppbField.getText());
            if (ppb <= 0) {
                cube.resetPPB();
            } else {
                cube.pixelsPerBlock = ppb;
            }
        }
    }
}