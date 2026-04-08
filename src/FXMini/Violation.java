package FXMini;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Violation implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient StringProperty plate;
    private transient StringProperty timestamp;
    private transient StringProperty location;
    private transient StringProperty violationType;
    private transient StringProperty fineStatus;
    private transient StringProperty vehicleType;
    private transient StringProperty imagePath;

    public Violation(String plate, String timestamp, String location, String violationType, String fineStatus, String vehicleType, String imagePath) {
        this.plate = new SimpleStringProperty(plate);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.location = new SimpleStringProperty(location);
        this.violationType = new SimpleStringProperty(violationType);
        this.fineStatus = new SimpleStringProperty(fineStatus);
        this.vehicleType = new SimpleStringProperty(vehicleType);
        this.imagePath = new SimpleStringProperty(imagePath);
    }
    
    // Compatibility constructor
    public Violation(String plate, String timestamp, String location, String violationType, String fineStatus, String vehicleType) {
        this(plate, timestamp, location, violationType, fineStatus, vehicleType, "");
    }

    public StringProperty plateProperty() { return plate; }
    public StringProperty timestampProperty() { return timestamp; }
    public StringProperty locationProperty() { return location; }
    public StringProperty violationTypeProperty() { return violationType; }
    public StringProperty fineStatusProperty() { return fineStatus; }
    public StringProperty vehicleTypeProperty() { return vehicleType; }

    public String getPlate() { return plate.get(); }
    public String getTimestamp() { return timestamp.get(); }
    public String getLocation() { return location.get(); }
    public String getViolationType() { return violationType.get(); }
    public String getFineStatus() { return fineStatus.get(); }
    public String getVehicleType() { return vehicleType.get(); }
    public String getImagePath() { return imagePath == null ? "" : imagePath.get(); }
    
    public int getFineAmount() {
        String type = getViolationType().toLowerCase();
        if (type.contains("speeding")) return 1000;
        if (type.contains("red light")) return 2500;
        if (type.contains("parking")) return 1250;
        return 500; // Default
    }

    public void setFineStatus(String status) {
        this.fineStatus.set(status);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeUTF(getPlate());
        s.writeUTF(getTimestamp());
        s.writeUTF(getLocation());
        s.writeUTF(getViolationType());
        s.writeUTF(getFineStatus());
        s.writeUTF(getVehicleType());
        s.writeUTF(getImagePath());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.plate = new SimpleStringProperty(s.readUTF());
        this.timestamp = new SimpleStringProperty(s.readUTF());
        this.location = new SimpleStringProperty(s.readUTF());
        this.violationType = new SimpleStringProperty(s.readUTF());
        this.fineStatus = new SimpleStringProperty(s.readUTF());
        this.vehicleType = new SimpleStringProperty(s.readUTF());
        String imgPath = "";
        try { imgPath = s.readUTF(); } catch(Exception e) {}
        this.imagePath = new SimpleStringProperty(imgPath);
    }
}
