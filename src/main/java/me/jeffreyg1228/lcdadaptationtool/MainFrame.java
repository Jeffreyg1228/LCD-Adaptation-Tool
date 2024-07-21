package me.jeffreyg1228.lcdadaptationtool;

import me.jeffreyg1228.lcdadaptationtool.dialog.BlockBenchConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

    Action load;
    JMenu setLookAndFeel;
    Action exit;
    Action about;

    public MainFrame() {
        super(Main.APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        load = new AbstractAction("加载 BBMODEL 文件") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BlockBenchConfigDialog(MainFrame.this, (fileName, jsonObject, modelingMethod) -> {
                    setContentPane(new TreePanel(fileName, jsonObject, modelingMethod));
                    revalidate();
                    repaint();
                });
            }
        };
        fileMenu.add(load);

        setLookAndFeel = new JMenu("设置皮肤");
        Main.LAFS.forEach((lafName, className) -> {
            JMenuItem item = new JMenuItem(lafName);
            item.addActionListener(l -> {
                try {
                    UIManager.setLookAndFeel(className);
                    SwingUtilities.updateComponentTreeUI(this);
                } catch (Throwable e) {
                    LOGGER.error("", e);
                }
            });
            setLookAndFeel.add(item);
        });
        fileMenu.add(setLookAndFeel);

        exit = new AbstractAction("退出") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        fileMenu.add(exit);

        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        about = new AbstractAction("关于…") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.this, "By Jeffreyg1228.\n按照 MIT License 授权。\nGithub: https://github.com/Jeffreyg1228/LCD-Adaptation-Tool\n当前版本：" + Main.APP_VERSION, Main.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        };
        helpMenu.add(about);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        setSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}