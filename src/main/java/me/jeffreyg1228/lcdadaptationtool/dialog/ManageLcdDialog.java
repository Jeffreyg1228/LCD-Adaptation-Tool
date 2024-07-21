package me.jeffreyg1228.lcdadaptationtool.dialog;

import me.jeffreyg1228.lcdadaptationtool.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManageLcdDialog extends JDialog {
    private JList<float[]> lcdList;
    private DefaultListModel<float[]> listModel;
    private JButton addButton, editButton, deleteButton;
    private JButton helpButton, okButton, cancelButton;
    private List<float[]> lcdOffsets;

    public ManageLcdDialog(JFrame parent, Callback callback) {
        super(parent, true);
        //setSize(440, 300);
        setLocationRelativeTo(parent);

        lcdOffsets = new ArrayList<>();
        listModel = new DefaultListModel<>();

        // 添加初始值
        float[] initialOffset = {0.0f, 0.0f, 0.0f};
        lcdOffsets.add(initialOffset);
        listModel.addElement(initialOffset);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 标题
        JLabel titleLabel = new JLabel("配置同侧 LCD");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(titleLabel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("添加 LCD");
        editButton = new JButton("修改 LCD");
        deleteButton = new JButton("删除 LCD");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // LCD 列表
        lcdList = new JList<>(listModel);
        lcdList.setCellRenderer(new LCDListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(lcdList);
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bottomGbc = new GridBagConstraints();

        helpButton = new JButton("帮助(H)");
        helpButton.setMnemonic(KeyEvent.VK_H);
        bottomGbc.gridx = 0;
        bottomGbc.gridy = 0;
        bottomGbc.anchor = GridBagConstraints.WEST;
        bottomGbc.weightx = 1.0;
        bottomPanel.add(helpButton, bottomGbc);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("确定(O)");
        okButton.setMnemonic(KeyEvent.VK_O);
        cancelButton = new JButton("取消(C)");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        rightButtonPanel.add(okButton);
        rightButtonPanel.add(cancelButton);

        bottomGbc.gridx = 1;
        bottomGbc.weightx = 0;
        bottomGbc.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(rightButtonPanel, bottomGbc);

        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(bottomPanel, gbc);

        setContentPane(panel);

        // 添加事件监听器
        addButton.addActionListener(e -> addLCD());
        editButton.addActionListener(e -> editLCD());
        deleteButton.addActionListener(e -> deleteLCD());
        helpButton.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", this));
        okButton.addActionListener(e -> {
            callback.onConfigSelected(lcdOffsets.toArray(new float[0][]));
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        lcdList.addListSelectionListener(e -> updateButtonStates());
        updateButtonStates();

        pack();
        setResizable(false);
    }

    private void updateButtonStates() {
        boolean isSelected = lcdList.getSelectedIndex() != -1;
        editButton.setEnabled(isSelected);
        deleteButton.setEnabled(isSelected);
    }

    private void addLCD() {
        float[] offset = showLCDDialog(this, "添加 LCD", new float[]{0.0f, 0.0f, 0.0f});
        if (offset != null) {
            lcdOffsets.add(offset);
            listModel.addElement(offset);
        }
    }

    private void editLCD() {
        int selectedIndex = lcdList.getSelectedIndex();
        if (selectedIndex != -1) {
            float[] currentOffset = lcdOffsets.get(selectedIndex);
            float[] newOffset = showLCDDialog(this, "修改 LCD", currentOffset);
            if (newOffset != null) {
                lcdOffsets.set(selectedIndex, newOffset);
                listModel.set(selectedIndex, newOffset);
            }
        }
    }

    private void deleteLCD() {
        int selectedIndex = lcdList.getSelectedIndex();
        if (selectedIndex != -1) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "确定要删除选中的 LCD 吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                lcdOffsets.remove(selectedIndex);
                listModel.remove(selectedIndex);
            }
        }
    }

    public static float[] showLCDDialog(Component parent, String title, float[] initialValues) {
        JTextField xField = new JTextField(10);
        JTextField yField = new JTextField(10);
        JTextField zField = new JTextField(10);

        if (initialValues != null) {
            xField.setText(String.valueOf(initialValues[0]));
            yField.setText(String.valueOf(initialValues[1]));
            zField.setText(String.valueOf(initialValues[2]));
        }

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("X 偏移量:"));
        panel.add(xField);
        panel.add(new JLabel("Y 偏移量:"));
        panel.add(yField);
        panel.add(new JLabel("Z 偏移量:"));
        panel.add(zField);

        int result = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                float x = Float.parseFloat(xField.getText());
                float y = Float.parseFloat(yField.getText());
                float z = Float.parseFloat(zField.getText());
                return new float[]{x, y, z};
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "请输入有效的数值", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface Callback {
        void onConfigSelected(float[][] lcdOffsets);
    }

    private static class LCDListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof float[]) {
                float[] offset = (float[]) value;
                setText(String.format("LCD %d: (%.2f, %.2f, %.2f)", index + 1, offset[0], offset[1], offset[2]));
            }
            return this;
        }
    }
}