package employees.platform.datagrid.row;

public record PairDataRow(int employee1Id, int employee2Id, int projectId, int daysWorked) {

    public PairDataRow {
        if (employee1Id < 1 || employee2Id < 1 || projectId < 1 || daysWorked < 1 ) {
            throw new IllegalArgumentException("fields must be positive");
        }
    }
}
