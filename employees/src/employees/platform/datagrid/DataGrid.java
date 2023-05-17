package employees.platform.datagrid;

import employees.platform.datagrid.row.FileDataRow;
import employees.platform.datagrid.row.PairDataRow;

import javax.swing.*;


public class DataGrid {
    private FileTableModel fileTableModel = new FileTableModel();
    private PairTableModel pairTableModel = new PairTableModel();
    private JTable table;
    public DataGrid() {
        table = new JTable();
    }
    public void setFileTableModel() {
        table.setModel(fileTableModel);
    }
    public void setPairTableModel() {
        table.setModel(pairTableModel);
    }

    public void clearTable() {
        fileTableModel.setRowCount(0);
        pairTableModel.setRowCount(0);
    }

    public JTable getTable() {
        return table;
    }

    public void addFileDataRow(FileDataRow fileDataRow) {
        Object[] rowData = {fileDataRow.employeeId(), fileDataRow.projectId(),
                fileDataRow.dateFrom(), fileDataRow.dateTo()};
        fileTableModel.addRow(rowData);
    }

    public void addPairDataRow(PairDataRow pairDataRow) {
        Object[] rowData = {pairDataRow.employee1Id(), pairDataRow.employee2Id(),
                pairDataRow.projectId(), pairDataRow.daysWorked()};
        pairTableModel.addRow(rowData);
    }

}
