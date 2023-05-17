package employees.platform.action;

import employees.platform.datagrid.row.FileDataRow;
import employees.file.status.FileStatus;
import employees.exception.CorruptedFileContentException;
import employees.exception.InvalidDataRowException;
import employees.platform.Platform;
import employees.platform.datagrid.row.PairDataRow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class FindPairEmployeesAction implements ActionListener {
    private Platform platformAttached;
    public FindPairEmployeesAction(Platform platform) {
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
            platformAttached.getDataGrid().setPairTableModel();
            var groups = fileReader.lines()
                    .map(stringRow -> {
                        try {
                            return FileDataRow.of(stringRow);
                        } catch (InvalidDataRowException ide) {
                            throw new CorruptedFileContentException("file format is corrupted", ide);
                        }
                    })
                    .collect(Collectors.groupingBy(FileDataRow::employeeId));

            if (groups.isEmpty()) {
                platformAttached.setStatus(FileStatus.EMPTY);
                platformAttached.setStatusMessage(FileStatus.EMPTY.toString());

            } else if (groups.size() == 1) {
                platformAttached.setStatus(FileStatus.NOT_ENOUGH_DATA);
                platformAttached.setStatusMessage(FileStatus.NOT_ENOUGH_DATA.toString());

            } else {
                var bestPairTimeByProject = calculateProjectTimeForPairEmployeesWorkedTogetherTheMost(groups);
                bestPairTimeByProject.stream()
                        .forEach(pairDataRow -> platformAttached.getDataGrid().addPairDataRow(pairDataRow));
                platformAttached.setStatusMessage(FileStatus.OK.toString());
                platformAttached.setStatus(FileStatus.OK);

            }
        } catch (CorruptedFileContentException c) {
            platformAttached.setStatus(FileStatus.CORRUPTED);
            platformAttached.setStatusMessage(FileStatus.CORRUPTED.toString());
        } catch (IOException ioe) {
            platformAttached.setStatus(FileStatus.READ_ERROR);
            platformAttached.setStatusMessage(FileStatus.READ_ERROR.toString());
        }
    }

    private List<PairDataRow>
        calculateProjectTimeForPairEmployeesWorkedTogetherTheMost(Map<Integer, List<FileDataRow>> employeeToWork)  {
        Integer[] empIds = employeeToWork.keySet().toArray(new Integer[0]);
        int bestEmp1Id = empIds[0];
        int bestEmp2Id = empIds[1];
        int mostDaysWorkedTogether = 0;
        Map<Integer, Integer> topPairProjectToTimeTogether = new HashMap<>();
        for (int i = 0; i < empIds.length - 1; ++i) {
            for (int j = i + 1; j < empIds.length; ++j) {

                var projectToTimeTogether =
                        calculateCommonProjectsTimeTogether(employeeToWork.get(empIds[i]),
                                employeeToWork.get(empIds[j]));
                int daysWorkedTogether = 0;
                for (var projectId : projectToTimeTogether.keySet()) {
                    daysWorkedTogether += projectToTimeTogether.get(projectId);
                }
                if (daysWorkedTogether > mostDaysWorkedTogether) {
                    topPairProjectToTimeTogether = projectToTimeTogether;
                    mostDaysWorkedTogether = daysWorkedTogether;
                    bestEmp1Id = empIds[i];
                    bestEmp2Id = empIds[j];
                }
            }
        }
        List<PairDataRow>  bestPairTimeByProject = new LinkedList<>();
        for (var projectID : topPairProjectToTimeTogether.keySet()) {
            bestPairTimeByProject.add(new PairDataRow(bestEmp1Id, bestEmp2Id,
                    projectID, topPairProjectToTimeTogether.get(projectID)));
        }
        return bestPairTimeByProject;
    }

    private Map<Integer, Integer> calculateCommonProjectsTimeTogether(List<FileDataRow> emp1Projects,
                                                                      List<FileDataRow> emp2Projects) {
        class WorkEndPoint implements Comparable {
            int employeeId;
            int projectId;
            LocalDate time;
            boolean start;

            public WorkEndPoint(int employeeId, int projectId, LocalDate time, boolean start) {
                this.employeeId = employeeId;
                this.projectId = projectId;
                this.time = time;
                this.start = start;
            }

            @Override
            public int compareTo(Object o) {
                if ( !(o instanceof WorkEndPoint)) {
                    throw new IllegalArgumentException("illegal comparison");
                }
                WorkEndPoint other = (WorkEndPoint) o;
                if (this.time.compareTo(other.time) == 0) {
                    return Boolean.compare(other.start, this.start);
                } else {
                    return this.time.compareTo(other.time);
                }
            }
        }

        List<WorkEndPoint> workEndPoints = new LinkedList<>();
        for (var emp1Work : emp1Projects) {
            workEndPoints.add(new WorkEndPoint(emp1Work.employeeId(), emp1Work.projectId(),
                    emp1Work.dateFrom(), true));
            workEndPoints.add(new WorkEndPoint(emp1Work.employeeId(), emp1Work.projectId(),
                    emp1Work.dateTo(), false));
        }


        for (var emp2Work : emp2Projects) {
            workEndPoints.add(new WorkEndPoint(emp2Work.employeeId(), emp2Work.projectId(),
                    emp2Work.dateFrom(), true));
            workEndPoints.add(new WorkEndPoint(emp2Work.employeeId(), emp2Work.projectId(),
                    emp2Work.dateTo(), false));
        }

        Collections.sort(workEndPoints);

        Map<Integer, Integer> projectDaysTogether = new HashMap<>();
        int emp1Id = emp1Projects.get(0).employeeId();
        int emp2Id = emp2Projects.get(0).employeeId();
        //we assume an employee could have worked on multiple projects at the same time
        Map<Integer, LocalDate> emp1CurrentProjects = new HashMap<>();
        Map<Integer, LocalDate> emp2CurrentProjects = new HashMap<>();


        for (var workTimeUnit : workEndPoints) {
            if (workTimeUnit.employeeId == emp1Id) {
                if (workTimeUnit.start) {
                    emp1CurrentProjects.put(workTimeUnit.projectId, workTimeUnit.time);
                } else {
                    if (!emp1CurrentProjects.containsKey(workTimeUnit.projectId)) {
                        throw new CorruptedFileContentException("employee " + emp1Id +
                                " finished project " + workTimeUnit.projectId + " without starting on it");
                    }

                    if (emp2CurrentProjects.containsKey(workTimeUnit.projectId)) {
                        LocalDate startTimeEmp1 = emp1CurrentProjects.get(workTimeUnit.projectId);
                        LocalDate startTimeEmp2 = emp2CurrentProjects.get(workTimeUnit.projectId);
                        LocalDate firstDayTogether =
                                startTimeEmp2.isBefore(startTimeEmp1) ? startTimeEmp1 : startTimeEmp2;
                        int daysOnProjectTogether = (int) ChronoUnit.DAYS.between(firstDayTogether, workTimeUnit.time);
                        if (projectDaysTogether.containsKey(workTimeUnit.projectId)) {
                            projectDaysTogether.replace(workTimeUnit.projectId,
                                    projectDaysTogether.get(workTimeUnit.projectId) + daysOnProjectTogether);
                        } else {
                            projectDaysTogether.put(workTimeUnit.projectId, daysOnProjectTogether);
                        }
                    }
                    emp1CurrentProjects.remove(workTimeUnit.projectId);
                }
            } else {
                if (workTimeUnit.start) {
                    emp2CurrentProjects.put(workTimeUnit.projectId, workTimeUnit.time);
                } else {
                    if (!emp2CurrentProjects.containsKey(workTimeUnit.projectId)) {
                        throw new CorruptedFileContentException("employee " + emp2Id +
                               " finished project " + workTimeUnit.projectId + " without starting on it");
                    }
                    if (emp1CurrentProjects.containsKey(workTimeUnit.projectId)) {
                        LocalDate startTimeEmp1 = emp1CurrentProjects.get(workTimeUnit.projectId);
                        LocalDate startTimeEmp2 = emp2CurrentProjects.get(workTimeUnit.projectId);
                        LocalDate firstDayTogether =
                                startTimeEmp2.isBefore(startTimeEmp1) ? startTimeEmp1 : startTimeEmp2;
                        int daysOnProjectTogether = (int) ChronoUnit.DAYS.between(firstDayTogether, workTimeUnit.time);
                        if (projectDaysTogether.containsKey(workTimeUnit.projectId)) {
                            projectDaysTogether.replace(workTimeUnit.projectId,
                                    projectDaysTogether.get(workTimeUnit.projectId) + daysOnProjectTogether);
                        } else {
                            projectDaysTogether.put(workTimeUnit.projectId, daysOnProjectTogether);
                        }
                    }
                    emp2CurrentProjects.remove(workTimeUnit.projectId);
                }
            }
        }
        return projectDaysTogether;
    }
}
