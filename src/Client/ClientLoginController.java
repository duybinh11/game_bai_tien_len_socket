package Client;

import Interface.ClickClientEvent;
import Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ClientLoginController {
    private ClickClientEvent clickClientRoom;

    public void setInterfaceClick(ClickClientEvent clickClientRoom) {
        this.clickClientRoom = clickClientRoom;
    }

    @FXML
    private Button btnLogin;

    @FXML
    private TextField tfPassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private Text txt_notification;

    @FXML
    private Button btnCreate;

    @FXML
    void btnLogin(ActionEvent event) {
        User user = new User(tfUsername.getText(), tfPassword.getText());
        clickClientRoom.clickLogin(user);
    }

    @FXML
    void btnCreate(ActionEvent event) {
        clickClientRoom.nextViewCreate();
    }

    public void setNotification(String message) {
        txt_notification.setText(message);
    }
}
