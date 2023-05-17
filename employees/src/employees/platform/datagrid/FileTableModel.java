package employees.platform.datagrid;

import javax.swing.table.DefaultTableModel;

public class FileTableModel extends DefaultTableModel {

    private static final String EMPLOYEE_ID_MESSAGE = "Employee ID";
    private static final String PROJECT_ID_MESSAGE = "Project ID";
    private static final String FROM_DATE_MESSAGE = "From";
    private static final String TO_DATE_MESSAGE = "To";

    private static final String[] COLUMN_NAMES = {EMPLOYEE_ID_MESSAGE, PROJECT_ID_MESSAGE,
            FROM_DATE_MESSAGE, TO_DATE_MESSAGE};

    public FileTableModel() {
        super(COLUMN_NAMES, 0);
    }
}
