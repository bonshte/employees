package employees.platform.datagrid.row;

import employees.exception.InvalidDataRowException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record FileDataRow(int employeeId, int projectId, LocalDate dateFrom, LocalDate dateTo) {
    private static final String DATA_SEPARATOR = ",";
    private static final DateTimeFormatter[] TIME_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd MM yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    };
    private static final String NO_DATE = "NULL";
    private static final int ROW_WORDS_COUNT = 4;
    private static final int EMPLOYEE_ID_INDEX = 0;
    private static final int PROJECT_ID_INDEX = 1;
    private static final int DATE_FROM_INDEX = 2;
    private static final int DATE_TO_INDEX = 3;


    public FileDataRow {
        if (employeeId < 1 || projectId < 1 || dateFrom == null || dateTo == null
            || dateFrom.isAfter(dateTo) || dateTo.isAfter(LocalDate.now()) || dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("invalid arguments passed to FileDataRow constructor");
        }
    }

    public static FileDataRow of(String line) throws InvalidDataRowException {
        String[] words = line.split(DATA_SEPARATOR);
        if (words.length != ROW_WORDS_COUNT) {
            throw new InvalidDataRowException("employee data does not match the protocol");
        }
        try {
            int employeeId = Integer.parseInt(words[EMPLOYEE_ID_INDEX].trim());
            int projectId = Integer.parseInt(words[PROJECT_ID_INDEX].trim());
            LocalDate startTime = parseLocalDate(words[DATE_FROM_INDEX].trim());
            LocalDate endTime;

            if (NO_DATE.equals(words[DATE_TO_INDEX].trim())) {
                endTime = LocalDate.now();
            } else {
                endTime = parseLocalDate(words[DATE_TO_INDEX].trim());
            }

            return new FileDataRow(employeeId, projectId, startTime, endTime);
        } catch (Exception e) {
            throw new InvalidDataRowException("employee data does not match the protocol", e);
        }
    }
    private static LocalDate parseLocalDate(String dateString) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                //keep trying next
            }
        }
        throw new IllegalArgumentException("Unknown format " + dateString);
    }


}
