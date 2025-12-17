package net.ooml.jpostman.ui.components.left;

import net.ooml.jpostman.model.Collection;
import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

/**
 * Request List Panel - Left sidebar showing collections and requests
 */
public class RequestListPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(RequestListPanel.class);

    private final MainFrame mainFrame;

    // UI Components
    private ProjectSwitcher projectSwitcher;
    private JTree requestTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public RequestListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
        loadCollections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 0));

        // Project switcher at top
        projectSwitcher = new ProjectSwitcher(mainFrame);
        add(projectSwitcher, BorderLayout.NORTH);

        // Tree for collections and requests
        rootNode = new DefaultMutableTreeNode("Collections");
        treeModel = new DefaultTreeModel(rootNode);
        requestTree = new JTree(treeModel);
        requestTree.setRootVisible(false);
        requestTree.setShowsRootHandles(true);
        requestTree.setCellRenderer(new RequestTreeCellRenderer());
        requestTree.addTreeSelectionListener(e -> onTreeSelectionChanged());

        // Add context menu (right-click menu)
        requestTree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(requestTree);
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar at bottom
        add(createToolbar(), BorderLayout.SOUTH);
    }

    /**
     * Create toolbar with action buttons
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // New Request button
        JButton newRequestBtn = new JButton(I18nManager.get("menu.file.new_request"));
        newRequestBtn.setToolTipText(I18nManager.get("menu.file.new_request"));
        newRequestBtn.addActionListener(e -> createNewRequest());
        toolbar.add(newRequestBtn);

        // New Collection button
        JButton newCollectionBtn = new JButton(I18nManager.get("menu.file.new_collection"));
        newCollectionBtn.setToolTipText(I18nManager.get("menu.file.new_collection"));
        newCollectionBtn.addActionListener(e -> createNewCollection());
        toolbar.add(newCollectionBtn);

        // Delete button
        JButton deleteBtn = new JButton(I18nManager.get("menu.edit.delete"));
        deleteBtn.setToolTipText(I18nManager.get("menu.edit.delete"));
        deleteBtn.addActionListener(e -> deleteSelectedItem());
        toolbar.add(deleteBtn);

        return toolbar;
    }

    /**
     * Load collections from storage
     */
    public void loadCollections() {
        try {
            rootNode.removeAllChildren();

            String projectName = mainFrame.getCurrentProject();
            List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

            for (Collection collection : collections) {
                DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(collection);

                // Sort requests by name
                List<Request> sortedRequests = new java.util.ArrayList<>(collection.getRequests());
                sortedRequests.sort((r1, r2) -> {
                    String name1 = r1.getName() != null ? r1.getName() : "";
                    String name2 = r2.getName() != null ? r2.getName() : "";
                    return name1.compareToIgnoreCase(name2);
                });

                // Add sorted requests to collection node
                for (Request request : sortedRequests) {
                    DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(request);
                    collectionNode.add(requestNode);
                }

                rootNode.add(collectionNode);
            }

            treeModel.reload();
            expandAllNodes();

            log.debug("Loaded {} collections", collections.size());
        } catch (Exception e) {
            log.error("Failed to load collections", e);
            mainFrame.showError("Failed to load collections: " + e.getMessage());
        }
    }

    /**
     * Expand all tree nodes
     */
    private void expandAllNodes() {
        for (int i = 0; i < requestTree.getRowCount(); i++) {
            requestTree.expandRow(i);
        }
    }

    /**
     * Handle tree selection change
     */
    private void onTreeSelectionChanged() {
        TreePath path = requestTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObject = node.getUserObject();

            if (userObject instanceof Request) {
                Request request = (Request) userObject;
                log.debug("Request selected: {}", request.getName());
                mainFrame.getRightPanel().openRequest(request);
            } else if (userObject instanceof Collection) {
                Collection collection = (Collection) userObject;
                log.debug("Collection selected: {}", collection.getName());
            }
        }
    }

    /**
     * Create new request
     */
    public void createNewRequest() {
        try {
            // Load collections
            String projectName = mainFrame.getCurrentProject();
            List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

            // Check if there are any collections
            if (collections.isEmpty()) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        I18nManager.get("dialog.new_request.no_collection"),
                        I18nManager.get("dialog.new_request.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    createNewCollection();
                }
                return;
            }

            // Create custom dialog
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

            // Request name field
            JTextField nameField = new JTextField(20);
            panel.add(new JLabel(I18nManager.get("common.name") + ":"));
            panel.add(nameField);

            // Collection selector
            JComboBox<String> collectionComboBox = new JComboBox<>();
            for (Collection collection : collections) {
                collectionComboBox.addItem(collection.getName());
            }

            // Pre-select current collection if available
            Collection selectedCollection = getSelectedCollection();
            if (selectedCollection != null) {
                for (int i = 0; i < collections.size(); i++) {
                    if (collections.get(i).getId().equals(selectedCollection.getId())) {
                        collectionComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            panel.add(new JLabel(I18nManager.get("dialog.new_request.collection") + ":"));
            panel.add(collectionComboBox);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    I18nManager.get("dialog.new_request.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                if (name == null || name.trim().isEmpty()) {
                    mainFrame.showError(I18nManager.get("dialog.new_request.name_required"));
                    return;
                }

                int selectedIndex = collectionComboBox.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }

                Collection targetCollection = collections.get(selectedIndex);

                // Create new request
                Request newRequest = Request.createNew(name.trim(), net.ooml.jpostman.model.enums.HttpMethod.GET);
                newRequest.setUrl("https://httpbin.org/get");

                // Add request to collection
                targetCollection.getRequests().add(newRequest);

                // Save collection
                mainFrame.getStorageService().saveCollection(projectName, targetCollection);

                // Reload collections to show new request
                loadCollections();

                // Open request in new tab
                mainFrame.getRightPanel().openRequest(newRequest);

                log.info("Created new request: {}", name);
            }
        } catch (Exception e) {
            log.error("Failed to create request", e);
            mainFrame.showError("Failed to create request: " + e.getMessage());
        }
    }

    /**
     * Create new collection
     */
    public void createNewCollection() {
        String name = JOptionPane.showInputDialog(
                this,
                I18nManager.get("dialog.new_collection.message"),
                I18nManager.get("dialog.new_collection.title"),
                JOptionPane.PLAIN_MESSAGE
        );

        if (name != null && !name.trim().isEmpty()) {
            try {
                Collection collection = Collection.createNew(name);
                mainFrame.getStorageService().saveCollection(mainFrame.getCurrentProject(), collection);
                loadCollections();
                log.info("Created new collection: {}", name);
            } catch (Exception e) {
                log.error("Failed to create collection", e);
                mainFrame.showError("Failed to create collection: " + e.getMessage());
            }
        }
    }

    /**
     * Delete selected item
     */
    public void deleteSelectedItem() {
        TreePath path = requestTree.getSelectionPath();
        if (path == null) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        String itemName = "";
        if (userObject instanceof Collection) {
            itemName = ((Collection) userObject).getName();
        } else if (userObject instanceof Request) {
            itemName = ((Request) userObject).getName();
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                I18nManager.get("dialog.delete.message") + " \"" + itemName + "\"?",
                I18nManager.get("dialog.delete.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                if (userObject instanceof Collection) {
                    Collection collection = (Collection) userObject;
                    mainFrame.getStorageService().deleteCollection(
                            mainFrame.getCurrentProject(),
                            collection.getId()
                    );
                    log.info("Deleted collection: {}", collection.getName());
                } else if (userObject instanceof Request) {
                    Request request = (Request) userObject;

                    // Close the tab if it's open
                    mainFrame.getRightPanel().closeTab(request.getId());

                    // Find and remove request from its collection
                    String projectName = mainFrame.getCurrentProject();
                    List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

                    for (Collection collection : collections) {
                        boolean removed = collection.getRequests().removeIf(req -> req.getId().equals(request.getId()));
                        if (removed) {
                            mainFrame.getStorageService().saveCollection(projectName, collection);
                            log.info("Deleted request: {}", request.getName());
                            break;
                        }
                    }
                }
                loadCollections();
            } catch (Exception e) {
                log.error("Failed to delete item", e);
                mainFrame.showError("Failed to delete: " + e.getMessage());
            }
        }
    }

    /**
     * Get currently selected request
     */
    public Request getSelectedRequest() {
        TreePath path = requestTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof Request) {
                return (Request) userObject;
            }
        }
        return null;
    }

    /**
     * Get currently selected collection
     */
    private Collection getSelectedCollection() {
        TreePath path = requestTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObject = node.getUserObject();

            // If a collection is selected, return it
            if (userObject instanceof Collection) {
                return (Collection) userObject;
            }

            // If a request is selected, return its parent collection
            if (userObject instanceof Request) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                if (parentNode != null && parentNode.getUserObject() instanceof Collection) {
                    return (Collection) parentNode.getUserObject();
                }
            }
        }
        return null;
    }

    /**
     * Get or create default collection
     */
    private Collection getOrCreateDefaultCollection() throws Exception {
        String projectName = mainFrame.getCurrentProject();
        List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

        // Try to find a collection named "Default" or "My Requests"
        for (Collection collection : collections) {
            if ("Default".equals(collection.getName()) || "My Requests".equals(collection.getName())) {
                return collection;
            }
        }

        // If no default collection exists, use the first one
        if (!collections.isEmpty()) {
            return collections.get(0);
        }

        // If no collections exist at all, create a new one
        Collection defaultCollection = Collection.createNew("My Requests");
        mainFrame.getStorageService().saveCollection(projectName, defaultCollection);
        return defaultCollection;
    }

    /**
     * Show context menu on right-click
     */
    private void showContextMenu(java.awt.event.MouseEvent e) {
        // Select the node under mouse
        TreePath path = requestTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        requestTree.setSelectionPath(path);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        JPopupMenu contextMenu = new JPopupMenu();

        if (userObject instanceof Request) {
            // Request context menu
            JMenuItem renameItem = new JMenuItem(I18nManager.get("common.edit"));
            renameItem.addActionListener(ev -> renameRequest((Request) userObject));
            contextMenu.add(renameItem);

            contextMenu.addSeparator();

            JMenuItem deleteItem = new JMenuItem(I18nManager.get("common.delete"));
            deleteItem.addActionListener(ev -> deleteSelectedItem());
            contextMenu.add(deleteItem);

        } else if (userObject instanceof Collection) {
            // Collection context menu
            JMenuItem renameItem = new JMenuItem(I18nManager.get("common.edit"));
            renameItem.addActionListener(ev -> renameCollection((Collection) userObject));
            contextMenu.add(renameItem);

            contextMenu.addSeparator();

            JMenuItem newRequestItem = new JMenuItem(I18nManager.get("menu.file.new_request"));
            newRequestItem.addActionListener(ev -> createNewRequest());
            contextMenu.add(newRequestItem);

            contextMenu.addSeparator();

            JMenuItem deleteItem = new JMenuItem(I18nManager.get("common.delete"));
            deleteItem.addActionListener(ev -> deleteSelectedItem());
            contextMenu.add(deleteItem);
        }

        contextMenu.show(requestTree, e.getX(), e.getY());
    }

    /**
     * Rename a request
     */
    private void renameRequest(Request request) {
        String newName = JOptionPane.showInputDialog(
                this,
                I18nManager.get("common.name") + ":",
                request.getName()
        );

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(request.getName())) {
            try {
                // Find and update the request in storage
                String projectName = mainFrame.getCurrentProject();
                List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

                for (Collection collection : collections) {
                    for (Request req : collection.getRequests()) {
                        if (req.getId().equals(request.getId())) {
                            // Update the request in the collection
                            req.setName(newName.trim());
                            req.touch();

                            // Save the collection
                            mainFrame.getStorageService().saveCollection(projectName, collection);

                            // Reload the tree to show updated name
                            loadCollections();

                            // Update tab title if this request is open
                            mainFrame.getRightPanel().updateTabTitle(request.getId(), newName.trim());

                            log.info("Request renamed to: {}", newName);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to rename request", e);
                mainFrame.showError("Failed to rename request: " + e.getMessage());
            }
        }
    }

    /**
     * Rename a collection
     */
    private void renameCollection(Collection collection) {
        String newName = JOptionPane.showInputDialog(
                this,
                I18nManager.get("common.name") + ":",
                collection.getName()
        );

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(collection.getName())) {
            try {
                // Reload from storage to get the latest data
                String projectName = mainFrame.getCurrentProject();
                List<Collection> collections = mainFrame.getStorageService().loadAllCollections(projectName);

                // Find the collection by ID and update it
                for (Collection coll : collections) {
                    if (coll.getId().equals(collection.getId())) {
                        coll.setName(newName.trim());
                        coll.touch();
                        mainFrame.getStorageService().saveCollection(projectName, coll);
                        loadCollections();
                        log.info("Collection renamed to: {}", newName);
                        return;
                    }
                }

                // If we get here, collection was not found
                mainFrame.showError("Collection not found");
            } catch (Exception e) {
                log.error("Failed to rename collection", e);
                mainFrame.showError("Failed to rename collection: " + e.getMessage());
            }
        }
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Remove old toolbar
        for (Component component : getComponents()) {
            if (component instanceof JPanel && ((JPanel) component).getComponentCount() > 0) {
                Component firstChild = ((JPanel) component).getComponent(0);
                if (firstChild instanceof JButton) {
                    remove(component);
                    break;
                }
            }
        }

        // Add new toolbar with updated labels
        add(createToolbar(), BorderLayout.SOUTH);

        // Refresh project switcher
        if (projectSwitcher != null) {
            projectSwitcher.refreshUI();
        }

        revalidate();
        repaint();
    }

    /**
     * Get project switcher component
     */
    public ProjectSwitcher getProjectSwitcher() {
        return projectSwitcher;
    }
}
