package employees.platform.action;

import employees.platform.datagrid.row.FileDataRow;
import employees.file.status.FileStatus;
import employees.exception.CorruptedFileContentException;
import employees.exception.InvalidDataRowException;
import employees.platform.Platform;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class VisualizeFileAction implements ActionListener {
    private Platform platformAttached;

    public VisualizeFileAction(Platform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("null passed to constructor");
        }
        this.platformAttached = platform;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File fileChosen = platformAttached.getCSVFile();
        if (fileChosen == null || platformAttached.getStatus() == FileStatus.NO_FILE ||
                platformAttached.getStatus() == FileStatus.BAD_EXTENSION) {
            return;
        }

        if (!platformAttached.getCSVFile().exists()) {
            platformAttached.setStatus(FileStatus.DOES_NOT_EXIST);
            platformAttached.setStatusMessage(FileStatus.DOES_NOT_EXIST.toString());
            return;
        } else if (!platformAttached.getCSVFile().canRead()) {
            // no reading permission for the file
            platformAttached.setStatus(FileStatus.NO_PERMISSION);
            platformAttached.setStatusMessage(FileStatus.NO_PERMISSION.toString());
            return;
        }

        try (BufferedReader fileReader = Files.newBufferedReader(platformAttached.getCSVFile().toPath())) {
            platformAttached.getDataGrid().clearTable();
            platformAttached.getDataGrid().setFileTableModel();
            fileReader.lines()
                    .map(stringRow -> {
                        try {
                            return FileDataRow.of(stringRow);
                        } catch (InvalidDataRowException ide) {
                            throw new CorruptedFileContentException("file format is corrupted", ide);
                        }
                    })
                    .forEach(x -> platformAttached.getDataGrid().addFileDataRow(x));
            platformAttached.setStatusMessage(FileStatus.OK.toString());
            platformAttached.setStatus(FileStatus.OK);
        } catch (CorruptedFileContentException c) {
            platformAttached.setStatus(FileStatus.CORRUPTED);
            platformAttached.setStatusMessage(FileStatus.CORRUPTED.toString());
        }  catch (IOException ioe) {
            platformAttached.setStatus(FileStatus.READ_ERROR);
            platformAttached.setStatusMessage(FileStatus.READ_ERROR.toString());
        }
    }
}
