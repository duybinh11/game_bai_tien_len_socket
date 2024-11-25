package Server;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import Model.Room;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerController {

    // Sử dụng ObservableList để theo dõi sự thay đổi của dữ liệu
    private ObservableList<ClientHandle> listClients = FXCollections.observableArrayList();
    private ObservableList<Room> listRoom = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Room, ?> clCountMember;

    @FXML
    private TableColumn<Room, Integer> clId;

    @FXML
    private TableColumn<Room, String> clListPorts;

    @FXML
    private TableColumn<ClientHandle, String> clPort;

    @FXML
    private TableColumn<ClientHandle, Date> clTime;

    @FXML
    private TableColumn<ClientHandle, String> clmStatus;

    @FXML
    private TableView<ClientHandle> tblClient;

    @FXML
    private TableView<Room> tblRoom;
    @FXML
    private TableColumn<Room, String> clName;

    @FXML
    public void initialize() {
        tblClient.setItems(listClients);
        tblRoom.setItems(listRoom);

        clPort.setCellValueFactory(
                cellData -> new SimpleObjectProperty<>(cellData.getValue().user.getUsername()));
        clmStatus.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getPostionRoom() == null ? "chưa" : "rồi"));

        clTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        clId.setCellValueFactory(new PropertyValueFactory<>("id"));
        clName.setCellValueFactory(new PropertyValueFactory<>("name"));

        clCountMember.setCellValueFactory(
                cellData -> new SimpleObjectProperty(cellData.getValue().getListClient().size()));

        clListPorts.setCellValueFactory(cellData -> {
            Room room = cellData.getValue();
            String ports = room.getListClient().stream()
                    .map(clientHandle -> String.valueOf(clientHandle.user.getUsername()))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(ports);
        });
    }

    public void updateListClient(ClientHandle clientHandle) {
        listClients.add(clientHandle);
        System.out.println("controller " + listClients);
    }

    public void updateListRoom(Room room) {
        listRoom.add(room);
        System.out.println("controller " + listRoom);
    }
    

    public void refreshTable(List<ClientHandle> listClientHandleTemp, List<Room> listRoomTemp) {
        listClients.clear();
        listClients.addAll(listClientHandleTemp);
        listRoom.clear();
        listRoom.addAll(listRoomTemp);
    }

}