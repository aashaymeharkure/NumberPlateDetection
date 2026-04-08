package FXMini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ViolationDataService {

    private static final String DATA_DIRECTORY_PATH =
            System.getProperty("user.dir") + File.separator + "fastDatabase";

    private static final String DATA_FILE_PATH =
            DATA_DIRECTORY_PATH + File.separator + "violations.dat";
            
    private static final String DETECTION_CSV_PATH =
            DATA_DIRECTORY_PATH + File.separator + "detections.csv";

    private ObservableList<Violation> violations;

    public ViolationDataService() {
        this.violations = FXCollections.observableArrayList();

        try {
            File dataDir = new File(DATA_DIRECTORY_PATH);
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                if (created)
                    System.out.println("Data directory created at: " + DATA_DIRECTORY_PATH);
                else
                    System.err.println("Failed to create data directory at: " + DATA_DIRECTORY_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }

        loadDataFromFile();
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromFile() {
        File file = new File(DATA_FILE_PATH);

        if (!file.exists()) {
            System.out.println("Data file not found at: " + DATA_FILE_PATH + ". Creating new one...");
            createDefaultDataFile();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            List<Violation> loadedList = (List<Violation>) ois.readObject();
            violations.clear();
            violations.addAll(loadedList);
            System.out.println("Successfully loaded " + violations.size() + " violations from " + DATA_FILE_PATH);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data from file, creating defaults: " + e.getMessage());
            createDefaultDataFile();
        }
    }

    private void saveDataToFile() {
        List<Violation> listToSave = new ArrayList<>(violations);

        try (FileOutputStream fos = new FileOutputStream(DATA_FILE_PATH);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(listToSave);
            System.out.println("Successfully saved " + listToSave.size() + " violations to " + DATA_FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
        }
    }

    private void createDefaultDataFile() {
        violations.clear();
        violations.addAll(
            new Violation("RJ14CW0002", "Sep 27, 2025 9:44 AM", "Ganesh Nagar Bopkhel", "Speeding", "Unpaid", "Car"),
            new Violation("MH02AK0047", "Oct 14, 2025 6:53 AM", "Main St, Pimpri", "Speeding", "Paid", "Car"),
            new Violation("AP19EQ0001", "Oct 15, 2025 6:53 AM", "City Avenue", "Red Light", "Paid", "Truck")
        );
        saveDataToFile();
    }

    public ObservableList<Violation> getViolations() {
        return violations;
    }

    public void addViolation(Violation violation) {
        violations.add(violation);
        saveDataToFile();
    }

    public void deleteViolation(Violation violation) {
        violations.remove(violation);
        saveDataToFile();
    }

    public void deleteAllViolations() {
        violations.clear();
        saveDataToFile();
    }

    public void updateViolationStatus(String plate, String newStatus) {
        for (Violation v : violations) {
            if (v.getPlate().equals(plate)) {
                v.setFineStatus(newStatus);
                break;
            }
        }
        saveDataToFile();
    }

    public void processPayment(Violation violation) {
        // 1. Remove from in-memory list and violations.dat
        violations.remove(violation);
        saveDataToFile();
        
        // 2. Remove from detections.csv
        removePlateFromCSV(violation.getPlate());
    }

    private void removePlateFromCSV(String plate) {
        File csvFile = new File(DETECTION_CSV_PATH);
        if (!csvFile.exists()) return;

        try {
            List<String> lines = java.nio.file.Files.readAllLines(csvFile.toPath());
            List<String> filteredLines = lines.stream()
                .filter(line -> !line.split(",")[0].trim().equalsIgnoreCase(plate.trim()))
                .collect(Collectors.toList());
            
            java.nio.file.Files.write(csvFile.toPath(), filteredLines);
            System.out.println("Removed plate " + plate + " from detections.csv");
        } catch (IOException e) {
            System.err.println("Error updating detections.csv: " + e.getMessage());
        }
    }

    public ObservableList<Violation> searchByPlate(String plateNumber) {
        return violations.stream()
                .filter(v -> v.getPlate().equalsIgnoreCase(plateNumber))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
