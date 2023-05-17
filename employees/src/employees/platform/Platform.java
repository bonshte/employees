package employees.platform;

import employees.file.status.FileStatus;
import employees.platform.action.FileChoiceAction;
import employees.platform.action.FindPairEmployeesAction;
import employees.platform.action.VisualizeFileAction;
import employees.platform.datagrid.DataGrid;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Platform extends JFrame {
    private static final String FILE_BUTTON_TEXT = "Choose File";
    private static final String TABLE_BUTTON_TEXT = "Visualize Table";
    private static final String BEST_PAIR_BUTTON_TEXT = "Find Pair";

    private DataGrid dataGrid;
    private JTextField fileName;
    private JTextField statusMessage;
    private File csvFile;
    private FileStatus status;

    private static final int APP_WIDTH = 1024;
    private static final int APP_HEIGHT = 800;
    private static final int INSET_VALUE = 10;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;

    public Platform() {
        super("Employees CSV Explorer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(APP_WIDTH, APP_HEIGHT);
        setLocationRelativeTo(null);

        JPanel containerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(INSET_VALUE, INSET_VALUE, INSET_VALUE, INSET_VALUE);

        this.status = FileStatus.NO_FILE;
        statusMessage = new JTextField();
        statusMessage.setEditable(false);
        containerPanel.add(statusMessage, gbc);
        setStatusMessage(FileStatus.NO_FILE.toString());
        gbc.gridy++;

        fileName = new JTextField();
        fileName.setEditable(false);
        containerPanel.add(fileName, gbc);
        gbc.gridy++;

        dataGrid = new DataGrid();
        JScrollPane scrollPane = new JScrollPane(dataGrid.getTable());

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        containerPanel.add(scrollPane, gbc);

        //visualize it
        setContentPane(containerPanel);

        gbc.gridy++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        containerPanel.add(buttonPanel, gbc);

        JButton fileChoiceButton = new JButton(FILE_BUTTON_TEXT);
        fileChoiceButton.addActionListener(new FileChoiceAction(this));
        buttonPanel.add(fileChoiceButton);

        JButton tableButton = new JButton(TABLE_BUTTON_TEXT);
        tableButton.addActionListener(new VisualizeFileAction(this));
        buttonPanel.add(tableButton);

        JButton bestPairButton = new JButton(BEST_PAIR_BUTTON_TEXT);
        bestPairButton.addActionListener(new FindPairEmployeesAction(this));
        buttonPanel.add(bestPairButton);

        Dimension buttonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        tableButton.setPreferredSize(buttonSize);
        fileChoiceButton.setPreferredSize(buttonSize);
        bestPairButton.setPreferredSize(buttonSize);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Platform::new);
    }

    public FileStatus getStatus() {
        return status;
    }
    public File getCSVFile() {
        return csvFile;
    }
    public void setFileName(File fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("null passed as fileName");
        }
        this.fileName.setText(fileName.getAbsolutePath());
    }
    public DataGrid getDataGrid() {
        return dataGrid;
    }
    public void setCSVFile(File csvFile) {
        this.csvFile = csvFile;
    }
    public void setStatusMessage(String msg) {
        if (msg == null) {
            throw new IllegalArgumentException("null String passed to setStatusMessage");
        }
        statusMessage.setText(msg);
    }
    public void setStatus(FileStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("null passed to setStatus");
        }
        this.status = status;
    }
}
