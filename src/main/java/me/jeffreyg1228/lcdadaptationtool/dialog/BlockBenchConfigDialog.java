package me.jeffreyg1228.lcdadaptationtool.dialog;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.jeffreyg1228.lcdadaptationtool.Main;
import me.jeffreyg1228.lcdadaptationtool.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;

public class BlockBenchConfigDialog extends JDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockBenchConfigDialog.class);

    private JTextField modelPathField;
    private JComboBox<String> directionComboBox;

    public static final String Z_POSITIVE = "Z+";
    public static final String Z_NEGATIVE = "Z-";

    public BlockBenchConfigDialog(JFrame parent, Callback callback) {
        super(parent, true);
        setSize(500, 250);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 标题
        JLabel titleLabel = new JLabel("配置 BlockBench 模型");
        titleLabel.setFont(Main.UI_FONT.deriveFont(18.0f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(titleLabel, gbc);

        // 模型路径
        JLabel pathLabel = new JLabel("模型路径：");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel.add(pathLabel, gbc);

        modelPathField = new JTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel.add(modelPathField, gbc);

        JButton browseButton = new JButton("浏览...");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("BlockBench 模型文件 (*.bbmodel)", "bbmodel"));
            int result = fileChooser.showOpenDialog(BlockBenchConfigDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                modelPathField.setText(selectedFile.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 5, 10);
        panel.add(browseButton, gbc);

        // 车头方向
        JLabel directionLabel = new JLabel("车头方向：");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel.add(directionLabel, gbc);

        directionComboBox = new JComboBox<>(new String[]{Z_POSITIVE, Z_NEGATIVE});
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 10);
        panel.add(directionComboBox, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        // 帮助按钮
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton helpButton = new JButton("帮助(H)");
        helpButton.setMnemonic(KeyEvent.VK_H);
        helpButton.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", BlockBenchConfigDialog.this));
        leftButtonPanel.add(helpButton);
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);

        // 确定和取消按钮
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("确定(O)");
        okButton.setMnemonic(KeyEvent.VK_O);
        okButton.addActionListener(e -> {
            String modelPath = modelPathField.getText();
            String selectedDirection = (String) directionComboBox.getSelectedItem();

            File jsonFile = new File(modelPath);
            if (!jsonFile.exists() || jsonFile.isDirectory()) {
                JOptionPane.showMessageDialog(BlockBenchConfigDialog.this, "路径 " + modelPath + " 不存在或不是文件。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (FileReader reader = new FileReader(jsonFile)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                String fileName = jsonFile.getName();
                callback.onConfigSelected(fileName, jsonObject, selectedDirection);
                dispose();
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(BlockBenchConfigDialog.this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                LOGGER.error("", ex);
            }
        });
        rightButtonPanel.add(okButton);

        JButton cancelButton = new JButton("取消(C)");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(e -> dispose());
        rightButtonPanel.add(cancelButton);

        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
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
        void onConfigSelected(String fileName, JsonObject jsonObject, String direction);
    }
}