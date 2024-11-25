package Client;

import java.lang.classfile.Label;

import Interface.ClickClientEvent;
import Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ClientCreateController {
    private ClickClientEvent clickClientRoom;

    public void setInterfaceClick(ClickClientEvent clickClientRoom) {
        this.clickClientRoom = clickClientRoom;
    }

    @FXML
    private Button btnBack;

    @FXML
    private Button btnCreate;

    @FXML
    private TextField tfPassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private Text txtThongBao;

    @FXML
    void btnCreate(ActionEvent event) {
        String username = tfUsername.getText();
        String password = tfPassword.getText();
        User user = new User(username, password);
        clickClientRoom.clickCreateAccount(user);
    }

    public void setNotification(String message) {
        txtThongBao.setText(message);
    }

    @FXML
    void btnBack(ActionEvent event) {
        clickClientRoom.clickBackToLogin();
    }
}
