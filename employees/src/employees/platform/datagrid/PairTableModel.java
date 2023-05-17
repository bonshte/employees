package employees.platform.datagrid;

import javax.swing.table.DefaultTableModel;

public class PairTableModel extends DefaultTableModel {
    private static final String EMPLOYEE_ID_1_MESSAGE = "Employee ID #1";
    private static final String EMPLOYEE_ID_2_MESSAGE = "Employee ID #2";
    private static final String PROJECT_ID_MESSAGE = "Project ID";
    private static final String DAYS_WORKED_MESSAGE = "Days Worked";

    private static final String[] COLUMN_NAMES =
            {EMPLOYEE_ID_1_MESSAGE, EMPLOYEE_ID_2_MESSAGE, PROJECT_ID_MESSAGE, DAYS_WORKED_MESSAGE};

    public PairTableModel() {
        super(COLUMN_NAMES, 0);
    }




}
