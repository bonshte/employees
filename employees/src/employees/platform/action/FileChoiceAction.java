package employees.platform.action;

import employees.file.status.FileStatus;
import employees.platform.Platform;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileChoiceAction implements ActionListener {
    private static final String CSV_EXTENSION = "csv";
    private static final String CSV_FILES = "CSV FILES";

    private Platform platformAttached;

    public FileChoiceAction(Platform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("null passed to constructor");
        }
        this.platformAttached = platform;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // create a file chooser dialog
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(CSV_FILES, CSV_EXTENSION);
        fileChooser.setFileFilter(csvFilter);

        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // show the dialog and wait for user selection
        int result = fileChooser.showOpenDialog(platformAttached);

        // process the selected file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile().getAbsoluteFile();
            if (!selectedFile.getName().endsWith(CSV_EXTENSION)) {
                platformAttached.setStatus(FileStatus.BAD_EXTENSION);
                platformAttached.setStatusMessage(FileStatus.BAD_EXTENSION.toString());
            } else {
                platformAttached.setStatus(FileStatus.OK);
                platformAttached.setStatusMessage(FileStatus.OK.toString());
            }
            platformAttached.setFileName(selectedFile);
            platformAttached.setCSVFile(selectedFile);
        }
    }
}
