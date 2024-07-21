package me.jeffreyg1228.lcdadaptationtool.dialog;

import me.jeffreyg1228.lcdadaptationtool.Main;
import me.jeffreyg1228.lcdadaptationtool.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

public class LCDConfigDialog extends JDialog {
    private JComboBox<String> positionComboBox;
    private JCheckBox oppotsiteCheckBox;

    public static final String LEFT_DOOR = "以建模的车头朝向为前进方向，左侧";
    public static final String RIGHT_DOOR = "以建模的车头朝向为前进方向，右侧";

    public LCDConfigDialog(JFrame parent, String itemName, String headDirection, boolean hasLeft, boolean hasRight, Callback callback) {
        super(parent, true);
        setSize(460, 230);
        setResizable(false);
        setLocationRelativeTo(parent);

        final boolean canAddOpposite = !hasLeft && !hasRight;

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 标题
        JLabel titleLabel = new JLabel("选定 LCD 参考方块");
        titleLabel.setFont(Main.UI_FONT.deriveFont(18.0f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(titleLabel, gbc);

        // 建模方式
        JLabel methodLabel = new JLabel("即将选择“" + itemName + "”作为参考方块。" + (canAddOpposite ? "" : "由于已添加了一侧 LCD，部分选项不可用。"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel.add(methodLabel, gbc);

        // LCD 位置
        JLabel positionLabel = new JLabel("LCD 位置：");
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel.add(positionLabel, gbc);

        Vector<String> options = new Vector<>();
        if (headDirection.equals(BlockBenchConfigDialog.Z_POSITIVE) ? !hasLeft : !hasRight)
            options.add(LEFT_DOOR);
        if (headDirection.equals(BlockBenchConfigDialog.Z_POSITIVE) ? !hasRight : !hasLeft)
            options.add(RIGHT_DOOR);
        positionComboBox = new JComboBox<>(options);
        positionComboBox.setEnabled(canAddOpposite);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 10);
        panel.add(positionComboBox, gbc);

        // 是否生成对侧 LCD
        oppotsiteCheckBox = new JCheckBox("生成对侧 LCD");
        oppotsiteCheckBox.setEnabled(canAddOpposite);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel.add(oppotsiteCheckBox, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        // 帮助按钮
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton helpButton = new JButton("帮助(H)");
        helpButton.setMnemonic(KeyEvent.VK_H);
        helpButton.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", LCDConfigDialog.this));
        leftButtonPanel.add(helpButton);
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);

        // 确定和取消按钮
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("确定(O)");
        okButton.setMnemonic(KeyEvent.VK_O);
        okButton.addActionListener(e -> {
            String selectedPosition = (String) positionComboBox.getSelectedItem();
            callback.onConfigSelected(selectedPosition, oppotsiteCheckBox.isSelected());
            dispose();
        });
        rightButtonPanel.add(okButton);

        JButton cancelButton = new JButton("取消(C)");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(e -> dispose());
        rightButtonPanel.add(cancelButton);

        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(buttonPanel, gbc);

        getRootPane().setDefaultButton(okButton);
        setContentPane(panel);
        setVisible(true);
    }

    @FunctionalInterface
    public interface Callback {
        void onConfigSelected(String lcdPosition, boolean addOpposite);
    }
}