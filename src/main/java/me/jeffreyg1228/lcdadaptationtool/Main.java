package me.jeffreyg1228.lcdadaptationtool;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jthemedetecor.OsThemeDetector;
import me.jeffreyg1228.lcdadaptationtool.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static final String APP_NAME = "LCD Adaptation Tool";
    public static final String APP_VERSION = Main.class.getPackage().getImplementationVersion() == null ? "(开发环境)" : Main.class.getPackage().getImplementationVersion();
    private static final List<String> UPDATE_URLS = Arrays.asList(
            "https://cdn.jsdelivr.net/gh/Jeffreyg1228/MTR-Toolbox-Releases@main/lcd-adaptation-tool/update.json",
            "https://fastly.jsdelivr.net/gh/Jeffreyg1228/MTR-Toolbox-Releases@main/lcd-adaptation-tool/update.json",
            "https://gcore.jsdelivr.net/gh/Jeffreyg1228/MTR-Toolbox-Releases@main/lcd-adaptation-tool/update.json"
    );

    public static final Font UI_FONT = new Font("Microsoft Yahei UI", Font.PLAIN, 12); // TODO 针对 macOS 支持

    public static final String FLATLAF_LIGHT = "com.formdev.flatlaf.FlatLightLaf";
    public static final String FLATLAF_DARK = "com.formdev.flatlaf.FlatDarkLaf";
    public static final String FLATLAF_INTELLIJ = "com.formdev.flatlaf.FlatIntelliJLaf";
    public static final String FLATLAF_DARCULA = "com.formdev.flatlaf.FlatDarculaLaf";
    public static final String FLATLAF_MACOS_LIGHT = "com.formdev.flatlaf.themes.FlatMacLightLaf";
    public static final String FLATLAF_MACOS_DARK = "com.formdev.flatlaf.themes.FlatMacDarkLaf";
    public static final Map<String, String> LAFS = new HashMap<>() {
        {
            put(FlatLightLaf.NAME, Main.FLATLAF_LIGHT);
            put(FlatDarkLaf.NAME, Main.FLATLAF_DARK);
            put(FlatIntelliJLaf.NAME, Main.FLATLAF_INTELLIJ);
            put(FlatDarculaLaf.NAME, Main.FLATLAF_DARCULA);
            put(FlatMacLightLaf.NAME, Main.FLATLAF_MACOS_LIGHT);
            put(FlatMacDarkLaf.NAME, Main.FLATLAF_MACOS_DARK);
        }
    };

    public static void main(String[] args) {
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", APP_NAME);
            System.setProperty("apple.awt.application.appearance", "system");
        }

        SwingUtilities.invokeLater(() -> {
            final OsThemeDetector detector = OsThemeDetector.getDetector();
            try {
                if (detector.isDark()) {
                    UIManager.setLookAndFeel(SystemInfo.isMacOS ? FLATLAF_MACOS_DARK : FLATLAF_DARK);
                } else {
                    UIManager.setLookAndFeel(SystemInfo.isMacOS ? FLATLAF_MACOS_LIGHT : FLATLAF_LIGHT);
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }

            checkForUpdates(new MainFrame());
        });
    }

    private static void checkForUpdates(JFrame frame) {
        new Thread(() -> {
            for (String updateUrl : UPDATE_URLS) {
                try {
                    URL url = new URL(updateUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000); // 5 seconds timeout
                    connection.setReadTimeout(5000); // 5 seconds timeout

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                    boolean hasUpdate = json.get("has_update").getAsBoolean();
                    String moreInfoUrl = json.get("more_info_url").getAsString();

                    LOGGER.debug("Updates from: {}:\n{}", updateUrl, json);

                    if (hasUpdate) {
                        SwingUtilities.invokeLater(() -> showUpdateDialog(frame, moreInfoUrl));
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to check update from: {}", updateUrl, e);
                }
            }
            LOGGER.info("No updates found.");
        }).start();
    }

    private static void showUpdateDialog(JFrame frame, String moreInfoUrl) {
        JDialog dialog = new JDialog(frame, true);
        dialog.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel(APP_NAME + " 已过期，请安装最新版本。");
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        dialog.add(messageLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton downloadButton = new JButton("了解详情");
        downloadButton.addActionListener(e -> {
            Utility.openURL(moreInfoUrl, dialog);
        });
        dialog.getRootPane().setDefaultButton(downloadButton);
        buttonPanel.add(downloadButton);

        JButton closeButton = new JButton("关闭程序");
        closeButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(closeButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }
}