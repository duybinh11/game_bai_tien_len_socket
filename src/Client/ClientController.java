package Client;

import Interface.ClickClientEvent;
import Model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.classfile.Label;
import java.util.*;

import DTO.RoomDTO;

public class ClientController {

    @FXML
    private Button btnCreate;

    @FXML
    private TableView<RoomDTO> tableRooms;

    @FXML
    private TextField tfNameRoom;

    @FXML
    private Text txtName;

    @FXML
    private TableColumn<RoomDTO, Integer> columnId;

    @FXML
    private TableColumn<RoomDTO, Integer> columnMenber;

    @FXML
    private TableColumn<RoomDTO, String> columnName;

    private ClickClientEvent clickClientRoom;

    public void setInterfaceClick(ClickClientEvent clickClientRoom) {
        this.clickClientRoom = clickClientRoom;
    }

    @FXML
    public void initialize() {
        tableRooms.setRowFactory(tv -> {
            TableRow<RoomDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    RoomDTO rowData = row.getItem();
                    clickClientRoom.doubleClickJoinRoom(rowData);
                }
            });
            return row;
        });
    }

    @FXML
    void btnCreate(ActionEvent event) {
        System.out.println(1);
        String nameRoom = tfNameRoom.getText();
        clickClientRoom.clickCreateRoom(nameRoom);
    }

    void updateRooms(List<RoomDTO> rooms) {
        ObservableList<RoomDTO> observableList = FXCollections.observableArrayList(rooms);

        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnMenber.setCellValueFactory(new PropertyValueFactory<>("numberOfClients"));

        tableRooms.setItems(observableList);
    }

    void doubleClickRowData() {
        tableRooms.setRowFactory(tv -> {
            TableRow<RoomDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    RoomDTO rowData = row.getItem();
                    System.out.println("Clicked on: " + rowData);
                    System.out.println("clickROom");
                    // clickClientRoom.doubleClickJoinRoom(rowData);
                }
            });
            return row;
        });
    }

    public void setUsername(String username) {
        txtName.setText(username);
    }

}
