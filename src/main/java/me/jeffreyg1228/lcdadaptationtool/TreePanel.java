package me.jeffreyg1228.lcdadaptationtool;

import com.google.gson.*;
import me.jeffreyg1228.lcdadaptationtool.dialog.BlockBenchConfigDialog;
import me.jeffreyg1228.lcdadaptationtool.dialog.LCDConfigDialog;
import me.jeffreyg1228.lcdadaptationtool.dialog.EditLcdDialog;
import me.jeffreyg1228.lcdadaptationtool.dialog.ManageLcdDialog;
import me.jeffreyg1228.lcdadaptationtool.util.ConfigGenerator;
import me.jeffreyg1228.lcdadaptationtool.util.ReferenceCube;
import me.jeffreyg1228.lcdadaptationtool.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TreePanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreePanel.class);

    private JTree tree;
    private DefaultTreeModel unfilteredTreeModel;
    private DefaultTreeModel filteredTreeModel;
    private DefaultMutableTreeNode unfilteredRoot;
    private DefaultMutableTreeNode filteredRoot;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;
    private JLabel selectedItemsLabel;
    private JTextArea configTextArea;
    private JTextArea resultTextArea;

    private ManageLcdDialog manageLcdDialog;
    private ConfigGenerator configGenerator;

    private Map<String, JsonObject> elementMap;
    private Map<String, ReferenceCube> referenceCubeMap;

    private final String headDirection;

    private static final String SEARCH_FOR_NAME = "搜索 name";
    private static final String SEARCH_FOR_UUID = "搜索 uuid";

    public TreePanel(String fileName, JsonObject jsonObject, String headDirection) {
        setLayout(new BorderLayout());
        configGenerator = new ConfigGenerator();
        elementMap = new HashMap<>();
        referenceCubeMap = new LinkedHashMap<>(2);
        this.headDirection = headDirection;

        // Left panel
        JPanel leftPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("步骤 1：选定 LCD 参考方块"), BorderLayout.WEST);
        JButton helpButton1 = new JButton("帮助");
        helpButton1.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", this));
        topPanel.add(helpButton1, BorderLayout.EAST);
        leftPanel.add(topPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchTypeComboBox = new JComboBox<>(new String[]{SEARCH_FOR_NAME, SEARCH_FOR_UUID});
        searchField = new JTextField();
        searchPanel.add(searchTypeComboBox, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel topLeftPanel = new JPanel(new BorderLayout());
        topLeftPanel.add(topPanel, BorderLayout.NORTH);
        topLeftPanel.add(searchPanel, BorderLayout.CENTER);
        leftPanel.add(topLeftPanel, BorderLayout.NORTH);

        // Tree
        unfilteredRoot = new DefaultMutableTreeNode(new TreeNodeData(fileName));
        unfilteredTreeModel = new DefaultTreeModel(unfilteredRoot);
        filteredRoot = new DefaultMutableTreeNode(new TreeNodeData(fileName));
        filteredTreeModel = new DefaultTreeModel(filteredRoot);
        tree = new JTree(filteredTreeModel);
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.addMouseListener(new TreeMouseListener());
        ToolTipManager.sharedInstance().registerComponent(tree);
        JScrollPane treeScrollPane = new JScrollPane(tree);
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectedItemsLabel = new JLabel("未选定参考方块！");
        statusPanel.add(selectedItemsLabel);
        leftPanel.add(statusPanel, BorderLayout.SOUTH);

        // 右侧面板
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));

        // 上半部分
        JPanel upperPanel = new JPanel(new BorderLayout());
        JPanel upperTopPanel = new JPanel(new BorderLayout());
        upperTopPanel.add(new JLabel("步骤 2：进行配置"), BorderLayout.WEST);
        JPanel upperButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton helpButton2 = new JButton("帮助");
        helpButton2.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", this));
        upperButtonPanel.add(helpButton2);

        JButton editLcdButton = new JButton("编辑 LCD 属性…");
        editLcdButton.addActionListener(e -> {
            if (configGenerator.leftCube == null && configGenerator.rightCube == null) {
                JOptionPane.showMessageDialog(this, "请至少配置一侧的 LCD 参考方块。", "无法编辑 LCD 属性", JOptionPane.ERROR_MESSAGE);
            } else {
                new EditLcdDialog(this, headDirection, configGenerator.leftCube, configGenerator.rightCube);
                updateConfigTextArea();
            }
        });
        upperButtonPanel.add(editLcdButton);

        JButton manageOffsetsButton = new JButton("管理同侧 LCD…");
        manageOffsetsButton.addActionListener(e -> {
            if (manageLcdDialog == null)
                manageLcdDialog = new ManageLcdDialog((JFrame) getRootPane().getParent(), lcdOffsets -> {
                    configGenerator.lcdOffsets = lcdOffsets;
                    updateConfigTextArea();
                });
            manageLcdDialog.setVisible(true);
        });
        upperButtonPanel.add(manageOffsetsButton);

        JButton adjustOffsetButton = new JButton("微调…");
        adjustOffsetButton.addActionListener(e -> {
            configGenerator.adjustOffsets = ManageLcdDialog.showLCDDialog(getRootPane().getParent(), "微调 LCD", Utility.isNullOrZero(configGenerator.adjustOffsets) ? new float[]{0.0f, 0.0f, 0.0f} : configGenerator.adjustOffsets);
            updateConfigTextArea();
        });
        upperButtonPanel.add(adjustOffsetButton);

        upperTopPanel.add(upperButtonPanel, BorderLayout.EAST);
        upperPanel.add(upperTopPanel, BorderLayout.NORTH);

        configTextArea = new JTextArea();
        configTextArea.setEditable(false);
        JScrollPane upperScrollPane = new JScrollPane(configTextArea);
        upperPanel.add(upperScrollPane, BorderLayout.CENTER);

        // 下半部分
        JPanel lowerPanel = new JPanel(new BorderLayout());
        JPanel lowerTopPanel = new JPanel(new BorderLayout());
        lowerTopPanel.add(new JLabel("步骤 3：获取结果"), BorderLayout.WEST);
        JPanel lowerButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton helpButton3 = new JButton("帮助");
        helpButton3.addActionListener(e -> Utility.openURL("https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#use-lcd-adaptation-tool", this));
        lowerButtonPanel.add(helpButton3);

        JButton troubleshootButton = new JButton("排错助手…");
        troubleshootButton.addActionListener(e -> showDebugDialog());
        lowerButtonPanel.add(troubleshootButton);

        JButton copyButton = new JButton("复制配置(C)");
        copyButton.setMnemonic(KeyEvent.VK_C);
        copyButton.addActionListener(e -> Utility.copyText(resultTextArea.getText()));
        lowerButtonPanel.add(copyButton);

        JButton generateConfigButton = new JButton("生成配置(G)");
        generateConfigButton.setMnemonic(KeyEvent.VK_G);
        generateConfigButton.addActionListener(e -> resultTextArea.setText(generateConfig()));
        lowerButtonPanel.add(generateConfigButton);

        lowerTopPanel.add(lowerButtonPanel, BorderLayout.EAST);
        lowerPanel.add(lowerTopPanel, BorderLayout.NORTH);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane lowerScrollPane = new JScrollPane(resultTextArea);
        lowerPanel.add(lowerScrollPane, BorderLayout.CENTER);

        rightPanel.add(upperPanel);
        rightPanel.add(lowerPanel);

        // Add panels to main panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Load JSON data
        loadJsonData(jsonObject);

        // Add search listener
        SearchListener searchListener = new SearchListener();
        searchTypeComboBox.addItemListener(searchListener);
        searchField.getDocument().addDocumentListener(searchListener);

        // Add tree model listener
        filteredTreeModel.addTreeModelListener(new FilteredTreeModelListener());
    }

    private void loadJsonData(JsonObject jsonObject) {
        try {
            // Process elements
            JsonArray elements = jsonObject.getAsJsonArray("elements");
            for (JsonElement element : elements) {
                JsonObject elementObj = element.getAsJsonObject();
                String uuid = elementObj.get("uuid").getAsString();
                elementMap.put(uuid, elementObj);
            }

            // Process outliner
            JsonArray outliner = jsonObject.getAsJsonArray("outliner");
            processOutliner(outliner, unfilteredRoot);

            // Initial population of filtered tree
            populateFilteredNode(unfilteredRoot, filteredRoot);
            filteredTreeModel.reload();
        } catch (Throwable e) {

            LOGGER.error("Failed to load json data", e);
        }
    }

    private void processOutliner(JsonArray outliner, DefaultMutableTreeNode parent) {
        for (JsonElement item : outliner) {
            if (item.isJsonObject()) {
                JsonObject itemObj = item.getAsJsonObject();
                String name = itemObj.get("name").getAsString();
                String uuid = itemObj.get("uuid").getAsString();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNodeData(name, uuid, itemObj, true));
                parent.add(node);

                if (itemObj.has("children")) {
                    JsonElement childrenElement = itemObj.get("children");
                    if (childrenElement.isJsonArray()) {
                        processOutliner(childrenElement.getAsJsonArray(), node);
                    }
                }
            } else if (item.isJsonPrimitive()) {
                String uuid = item.getAsString();
                JsonObject elementObj = elementMap.get(uuid);
                if (elementObj != null) {
                    String name = elementObj.get("name").getAsString();
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNodeData(name, uuid, elementObj, false));
                    parent.add(node);
                }
            }
        }
    }

    private boolean populateFilteredNode(DefaultMutableTreeNode unfilteredNode, DefaultMutableTreeNode filteredNode) {
        boolean anyChildVisible = false;
        TreeNodeData unfilteredData = (TreeNodeData) unfilteredNode.getUserObject();

        for (int i = 0; i < unfilteredNode.getChildCount(); i++) {
            DefaultMutableTreeNode unfilteredChildNode = (DefaultMutableTreeNode) unfilteredNode.getChildAt(i);
            TreeNodeData childData = (TreeNodeData) unfilteredChildNode.getUserObject();

            DefaultMutableTreeNode filteredChildNode = new DefaultMutableTreeNode(childData.clone());
            boolean childVisible = populateFilteredNode(unfilteredChildNode, filteredChildNode);

            if (childVisible || isNodeVisible(childData)) {
                filteredNode.add(filteredChildNode);
                anyChildVisible = true;
            }
        }

        boolean currentNodeVisible = isNodeVisible(unfilteredData);
        return currentNodeVisible || anyChildVisible;
    }

    private boolean isNodeVisible(TreeNodeData data) {
        String searchText = searchField.getText().toLowerCase();
        String searchType = (String) searchTypeComboBox.getSelectedItem();

        return searchText.isEmpty() ||
                (searchType.equals(SEARCH_FOR_NAME) && data.name.toLowerCase().contains(searchText)) ||
                (searchType.equals(SEARCH_FOR_UUID) && data.uuid.toLowerCase().contains(searchText));
    }

    private void updateSelectedItemsLabel() {
        StringBuilder selectedItemsLabelText = new StringBuilder("<html>已选定的参考方块：");
        for (ReferenceCube referenceCube : referenceCubeMap.values()) {
            if (referenceCube.elementObj != null) {
                String itemName = referenceCube.elementObj.get("name").getAsString();
                selectedItemsLabelText.append("<br>").append(itemName).append(" (").append(referenceCube.elementObj.get("uuid").getAsString()).append(")");
            }
        }
        selectedItemsLabel.setText(selectedItemsLabelText.append("</html>").toString());
    }

    private void updateConfigTextArea() {
        StringBuilder configText = new StringBuilder();
        for (ReferenceCube referenceCube : referenceCubeMap.values()) {
            if (referenceCube.elementObj != null) {
                configText.append(referenceCube.isLeftDoor ? "左侧" : "右侧").append("参考方块“").append(referenceCube.elementObj.get("name").getAsString())
                        .append("” (").append(referenceCube.elementObj.get("uuid").getAsString()).append(")：").append("\n")
                        .append("========================================\n")
                        .append(referenceCube).append("\n\n");
            } else {
                configText.append("上述参考方块的对侧方块：\n")
                        .append("========================================\n")
                        .append(referenceCube).append("\n\n");
            }
        }
        if (!Utility.isNullOrEmpty(configGenerator.lcdOffsets)) {
            configText.append("offsets:\n").append("========================================\n");
            for (float[] offset : configGenerator.lcdOffsets) {
                configText.append(Utility.floatArrayToString(offset)).append("\n");
            }
            configText.deleteCharAt(configText.length() - 1).append("\n\n");
        }
        if (!Utility.isNullOrZero(configGenerator.adjustOffsets)) {
            configText.append("adjustOffsets:\n").append("========================================\n");
            configText.append(Utility.floatArrayToString(configGenerator.adjustOffsets)).append("\n\n");
        }
        configTextArea.setText(configText.toString());

        if (referenceCubeMap.size() == 2) {
            referenceCubeMap.values().forEach(v -> {
                if (v.isLeftCube)
                    configGenerator.leftCube = v;
                else
                    configGenerator.rightCube = v;
            });
        } else if (referenceCubeMap.size() == 1) {
            referenceCubeMap.values().forEach(v -> {
                if (v.isLeftCube) {
                    configGenerator.leftCube = v;
                    configGenerator.rightCube = null;
                } else {
                    configGenerator.rightCube = v;
                    configGenerator.leftCube = null;
                }
            });
        } else {
            configGenerator.leftCube = null;
            configGenerator.rightCube = null;
        }
    }

    private String generateConfig() {
        if (configGenerator.leftCube == null || configGenerator.rightCube == null) {
            JOptionPane.showMessageDialog(this, "未完整配置两侧的参考方块。", "无法生成配置", JOptionPane.ERROR_MESSAGE);
            return "";
        }
        if (Utility.isNullOrEmpty(configGenerator.lcdOffsets)) {
            JOptionPane.showMessageDialog(this, "offsets 不能为空。请通过“管理同侧 LCD”按钮添加 offset。", "无法生成配置", JOptionPane.ERROR_MESSAGE);
            return "";
        }
        String result = configGenerator.generateConfig();
        if (!configGenerator.leftCube.verificationResult.isAllPassed() || !configGenerator.rightCube.verificationResult.isAllPassed()) {
            JOptionPane.showMessageDialog(this, "部分 LCD 的坐标合法性验证未通过！\n" +
                    "这表明如果按照生成的配置绘制 LCD，未通过验证的一侧 LCD 可能不会显示或显示不正常。您需要适当修改这一侧 LCD 配置。\n" +
                    "有关更多信息，请查看“排错助手”，和参见文档：https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#lat-error-5。", "警告", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }

    private static final Color[] MARKER_COLORS = {
            Color.decode("#58C0FF"), // light_blue
            Color.decode("#F4D714"), // yellow
            Color.decode("#EC9218"), // orange
            Color.decode("#FA565D"), // red
            Color.decode("#B55AF8"), // purple
            Color.decode("#4D89FF"), // blue
            Color.decode("#00CE71"), // green
            Color.decode("#AFFF62"), // lime
            Color.decode("#F96BC5"), // pink
            Color.decode("#C7D5F6")  // silver
    };

    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon fileIcon;

        public CustomTreeCellRenderer() {
            fileIcon = UIManager.getIcon("FileView.fileIcon");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();
                if (userObject instanceof TreeNodeData data) {
                    setText(data.name);
                    if (node.isRoot()) {
                        setIcon(fileIcon);
                    } else if (!data.isParent) {
                        setIcon(createColoredIcon(data));
                    } else {
                        setIcon(UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon"));
                    }
                    setToolTipText(data.uuid);
                }
            }

            return this;
        }

        private Icon createColoredIcon(TreeNodeData data) {
            JsonObject jsonObject = elementMap.get(data.uuid);
            if (jsonObject != null && jsonObject.has("color")) {
                int colorIndex = jsonObject.get("color").getAsInt();
                if (colorIndex >= 0 && colorIndex < MARKER_COLORS.length) {
                    Color color = MARKER_COLORS[colorIndex];
                    return new ColorIcon(color);
                }
            }
            return new ColorIcon(Color.GRAY); // 默认颜色
        }
    }

    private static class ColorIcon implements Icon {
        private final Color color;
        private static final int ICON_SIZE = 14; // 您可以根据需要调整这个值

        public ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillRect(x, y, getIconWidth(), getIconHeight());
            g2d.setColor(color.darker());
            g2d.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof TreeNodeData data) {

                    if (e.getClickCount() == 2 && !data.isParent && !node.isRoot()) {
                        // Handle double-click for selection of child nodes
                        toggleSelection(data);
                        return;
                    }

                    if (SwingUtilities.isRightMouseButton(e)) {
                        tree.setSelectionPath(path);
                        JPopupMenu popupMenu = new JPopupMenu();

                        if (!node.isRoot()) {
                            // Add "Details" menu item for child nodes
                            if (data.jsonObject != null) {
                                JMenuItem detailsItem = new JMenuItem("详细信息");
                                detailsItem.addActionListener(e1 -> showDetailsDialog(data.name, data.jsonObject));
                                popupMenu.add(detailsItem);
                                popupMenu.add(new JSeparator());
                            }
                            if (data.isParent) {
                                // Add "Expand/Collapse" menu item for parent nodes
                                JMenuItem expandItem = new JMenuItem(tree.isExpanded(path) ? "折叠" : "展开");
                                expandItem.addActionListener(e1 -> {
                                    if (tree.isExpanded(path)) {
                                        tree.collapsePath(path);
                                    } else {
                                        tree.expandPath(path);
                                    }
                                });
                                popupMenu.add(expandItem);
                            } else {
                                // Add "Select/Deselect" menu item for child nodes
                                JMenuItem selectItem = new JMenuItem(referenceCubeMap.containsKey(data.uuid) ? "取消选择" : "选择");
                                selectItem.addActionListener(e1 -> toggleSelection(data));
                                popupMenu.add(selectItem);
                            }
                        }

                        if (popupMenu.getComponentCount() > 0) {
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }
        }
    }

    private void showDetailsDialog(String name, JsonObject jsonObject) {
        String formattedJson = formatJson(jsonObject.toString());
        JTextArea textArea = new JTextArea(20, 40);
        textArea.setText(formattedJson);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "项目 " + name + " 的详细信息", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDebugDialog() {
        StringBuilder dialogText = new StringBuilder();
        dialogText.append("您可前往 https://www.geogebra.org/3d 并键入显示不正常的 LCD 坐标，看位置关系是否正确。别忘了在 GeoGebra“设置”中勾选“y 轴竖直向上”。\n")
                .append("有关排错的更多信息，见 https://mtr.jeffreyg1228.me/lcd/creator-guide/adapt-to-blockbench-train#lat-error-5。")
                .append("注：可在选中文字后按 Ctrl+C 快捷键复制文字。下列坐标中 X 值均已取原来坐标的相反数。\n\n");
        for (ReferenceCube referenceCube : referenceCubeMap.values()) {
            if (referenceCube.elementObj != null) {
                dialogText.append(referenceCube.isLeftDoor ? "左侧" : "右侧").append("参考方块“").append(referenceCube.elementObj.get("name").getAsString())
                        .append("” (").append(referenceCube.elementObj.get("uuid").getAsString()).append(")：").append("\n")
                        .append("========================================\n")
                        .append(referenceCube.toDebugString()).append("\n\n");
            } else {
                dialogText.append("上述参考方块的对侧方块：\n")
                        .append("========================================\n")
                        .append(referenceCube.toDebugString()).append("\n\n");
            }
        }
        JTextArea textArea = new JTextArea();
        textArea.setText(dialogText.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "排错助手", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(json);
        return gson.toJson(je);
    }

    private void toggleSelection(TreeNodeData data) {
        if (referenceCubeMap.containsKey(data.uuid)) {
            referenceCubeMap.remove(data.uuid);
            referenceCubeMap.remove(data.uuid + " (Opposite)");
        } else if (configGenerator.leftCube != null && configGenerator.rightCube != null) {
            JOptionPane.showMessageDialog(this, "已存在两侧车门的参考方块。", "无法选择 " + data.name + " 作为参考方块", JOptionPane.ERROR_MESSAGE);
        } else {
            new LCDConfigDialog((JFrame) getRootPane().getParent(), data.name, headDirection, configGenerator.leftCube != null, configGenerator.rightCube != null, (lcdPosition, addOpposite) -> {
                final boolean isLeftDoor = lcdPosition.equals(LCDConfigDialog.LEFT_DOOR);
                final boolean isLeftCube = headDirection.equals(BlockBenchConfigDialog.Z_POSITIVE) == isLeftDoor;
                JsonObject elementObj = elementMap.get(data.uuid);
                if (elementObj != null) { // TODO 针对其它情况的适配，可以通过构建 X、Z 值为 0 的参考方块进行。别忘了无旋转情况。
                    float[] from = Utility.jsonArrayToFloatArray(elementObj.get("from").getAsJsonArray());
                    float[] to = Utility.jsonArrayToFloatArray(elementObj.get("to").getAsJsonArray());
                    if (from[0] != to[0] && from[1] != to[1] && from[2] != to[2]) {
                        JOptionPane.showMessageDialog(this, "参考方块的 X 轴、Y 轴、Z 轴尺寸值中必须有一值为 0。", "无法选择 " + data.name + " 作为参考方块", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (from[1] != to[1]) {
                        JOptionPane.showMessageDialog(this, "参考方块对角顶点的 Y 坐标不相等。很抱歉，" + Main.APP_NAME + " 还没有适配此情况。", "无法选择 " + data.name + " 作为参考方块", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ReferenceCube referenceCube = new ReferenceCube(elementObj, isLeftDoor, isLeftCube);
                    referenceCubeMap.put(data.uuid, referenceCube);
                    if (addOpposite) {
                        referenceCubeMap.put(data.uuid + " (Opposite)", ReferenceCube.getOppositeCube(referenceCube));
                    }
                }
            });
        }
        updateSelectedItemsLabel();
        updateConfigTextArea();
    }

    private class SearchListener implements DocumentListener, ItemListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterTree();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterTree();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterTree();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            filterTree();
        }

        private void filterTree() {
            filteredRoot.removeAllChildren();
            populateFilteredNode(unfilteredRoot, filteredRoot);
            filteredTreeModel.reload();
            expandAllNodes(tree, new TreePath(filteredRoot));
        }

        private void expandAllNodes(JTree tree, TreePath parent) {
            TreeNode node = (TreeNode) parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    expandAllNodes(tree, path);
                }
            }
            tree.expandPath(parent);
        }
    }

    private class FilteredTreeModelListener implements TreeModelListener {
        private Set<TreePath> expandedPaths;

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            saveExpandedPaths();
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            saveExpandedPaths();
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            saveExpandedPaths();
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            saveExpandedPaths();
        }

        private void saveExpandedPaths() {
            expandedPaths = new HashSet<>();
            for (int i = 0; i < tree.getRowCount(); i++) {
                if (tree.isExpanded(i)) {
                    expandedPaths.add(tree.getPathForRow(i));
                }
            }
            SwingUtilities.invokeLater(this::restoreExpandedPaths);
        }

        private void restoreExpandedPaths() {
            for (TreePath path : expandedPaths) {
                tree.expandPath(path);
            }
        }
    }

    private static class TreeNodeData implements Cloneable {
        String name;
        String uuid;
        JsonObject jsonObject;
        boolean isParent;

        TreeNodeData(String name, String uuid, JsonObject jsonObject, boolean isParent) {
            this.name = name;
            this.uuid = uuid;
            this.jsonObject = jsonObject;
            this.isParent = isParent;
        }

        TreeNodeData(String rootName) {
            this.name = rootName;
            this.uuid = "";
            this.isParent = true;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public TreeNodeData clone() {
            try {
                return (TreeNodeData) super.clone();
            } catch (CloneNotSupportedException e) {
                return new TreeNodeData(this.name, this.uuid, this.jsonObject, this.isParent);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TreeNodeData that = (TreeNodeData) obj;
            return isParent == that.isParent &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, uuid, isParent);
        }
    }
}