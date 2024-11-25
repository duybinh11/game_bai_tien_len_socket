package Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DTO.RoomDTO;
import Interface.ClickClientEvent;
import Model.Card;
import Model.ClientData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class ClientRoomController {
    @FXML
    private AnchorPane containerHandInRoom;

    @FXML
    private Label txtNotification;

    @FXML
    private Button btnThoat;

    @FXML
    private Button btnDanh;

    @FXML
    private Label txtPort1;

    @FXML
    private Label txtPort2;

    @FXML
    private Label txtPort3;

    @FXML
    private Label txtPort4;

    @FXML
    private Label txtPosition1;

    @FXML
    private Label txtPosition2;

    @FXML
    private Label txtPosition3;

    @FXML
    private Label txtPosition4;
    @FXML
    private Button btnStart;

    @FXML
    private HBox containerCards;

    private List<Card> cardsSelect = new ArrayList<>();

    private boolean isStart = false;

    private ClickClientEvent clickClientEvent;

    public void setClickClientEvent(ClickClientEvent clickClientEvent) {
        this.clickClientEvent = clickClientEvent;
    }

    @FXML
    void btnStart(ActionEvent event) {
        clickClientEvent.clickStart();
        isStart = !isStart;
        if (isStart) {
            btnStart.setText("Đã sẵn sàng");
            inVisibleBtnExit();
        } else {
            visibleBtnExit();
            btnStart.setText("sẵn sàng");
        }

    }

    @FXML
    private Button btnSkip;

    @FXML
    private void initialize() {
        inVisibleBtnDanh();
        inVisibleBtnSkip();
    }

    @FXML
    void btnSkip(ActionEvent event) {
        clickClientEvent.clickSkip();
    }

    public void updateClientInRoom(ClientData c, List<ClientData> clientDatas) {
        setPostion1(c);

        int index = c.getPostion();

        for (int i = 0; i < 3; i++) {
            if (index + 1 > 4) {
                index = 1;
            } else {
                index++;
            }
            int currentIndex = index;
            ClientData clientData = clientDatas.stream().filter(t -> t.getPostion() == currentIndex).findFirst()
                    .orElse(null);
            if (clientData != null) {
                if (i == 0) {
                    setPostion2(clientData);
                } else if (i == 1) {
                    setPostion3(clientData);
                } else if (i == 2) {
                    setPostion4(clientData);
                }
            }

        }
    }

    public void updateAfterCreateRoom(ClientData clientData) {
        setPostion1(clientData);
    }

    public void setPostion1(ClientData c) {
        txtPosition1.setText("" + c.getPostion());
        txtPort1.setText("" + c.getUser().getUsername());
    }

    public void setPostion2Empty() {
        txtPosition2.setText("");
        txtPort2.setText("");
    }

    public void setPostion3Empty() {
        txtPosition3.setText("");
        txtPort3.setText("");
    }

    public void setPostion4Empty() {
        txtPosition4.setText("");
        txtPort4.setText("");
    }

    public void setPostion1Empty() {
        txtPosition1.setText("");
        txtPort1.setText("");
    }

    public void setPostion2(ClientData c) {
        txtPosition2.setText("" + c.getPostion());
        txtPort2.setText("" + c.getUser().getUsername());
    }

    public void setPostion3(ClientData c) {
        txtPosition3.setText("" + c.getPostion());
        txtPort3.setText("" + c.getUser().getUsername());
    }

    public void setPostion4(ClientData c) {
        txtPosition4.setText("" + c.getPostion());
        txtPort4.setText("" + c.getUser().getUsername());
    }

    void visibleBtnStart() {
        btnStart.setVisible(true);
        btnStart.setManaged(true);
    }

    void inVisibleBtnStart() {
        btnStart.setVisible(false);
        btnStart.setManaged(false);
    }

    void visibleBtnDanh() {
        btnDanh.setVisible(true);
        btnDanh.setManaged(true);
    }

    void inVisibleBtnDanh() {
        btnDanh.setVisible(false);
        btnDanh.setManaged(false);
    }

    void visibleBtnSkip() {
        btnSkip.setVisible(true);
        btnSkip.setManaged(true);
    }

    void inVisibleBtnSkip() {
        btnSkip.setVisible(false);
        btnSkip.setManaged(false);
    }

    void visibleBtnExit() {
        btnThoat.setVisible(true);
        btnThoat.setManaged(true);
    }

    void inVisibleBtnExit() {
        btnThoat.setVisible(false);
        btnThoat.setManaged(false);
    }

    @FXML
    void btnDanh(ActionEvent event) {
        System.out.println("click danh");
        clickClientEvent.clickDanh(cardsSelect);

    }

    void resetCartSlect() {
        cardsSelect.clear();
    }

    public void displayCardAll(List<Card> cards) {
        System.out.println("card danh" + cards);
        // Xóa tất cả các phần tử hiện có trong containerHandInRoom trước khi thêm mới
        containerHandInRoom.getChildren().clear();

        // Tạo danh sách đường dẫn đến các hình ảnh của lá bài
        List<String> imagePaths = cards.stream()
                .map(t -> "img/" + t.toString() + ".jpeg")
                .toList();

        // Tạo StackPane để chứa các ImageView (hình ảnh của lá bài)
        StackPane stackPane = new StackPane();
        double offset = 80; // Khoảng cách giữa các hình ảnh

        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            try {
                // Tạo hình ảnh từ đường dẫn
                Image image = new Image(imagePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.fitHeightProperty().bind(containerHandInRoom.heightProperty());
                imageView.setPreserveRatio(true);

                // Tạo hình chữ nhật để cắt hình ảnh theo kích thước đã đặt
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(imageView.fitWidthProperty());
                clip.heightProperty().bind(imageView.fitHeightProperty());
                imageView.setClip(clip);

                // Đặt vị trí của hình ảnh dựa trên chỉ số để tạo hiệu ứng chồng lẫn nhau
                imageView.setTranslateX(i * offset);

                // Thêm hình ảnh vào StackPane
                stackPane.getChildren().add(imageView);
            } catch (IllegalArgumentException e) {
                System.out.println("Error URL: " + imagePath);
            }
        }

        // Căn chỉnh StackPane vào vị trí phía trên bên trái của AnchorPane
        stackPane.setAlignment(Pos.TOP_LEFT);

        // Thêm StackPane vào containerHandInRoom
        containerHandInRoom.getChildren().add(stackPane);
    }

    public void setMessageNotifilcation(String data) {
        txtNotification.setText(data);
    }

    public void displayCardImages(List<Card> cards) {
        List<String> imagePaths = cards.stream()
                .map(t -> "img/" + t.toString() + ".jpeg")
                .toList();
        // Sử dụng ArrayList thay vì danh sách bất biến

        containerCards.getChildren().clear();

        List<Boolean> selectsBool = new ArrayList<>(Collections.nCopies(imagePaths.size(), true));
        StackPane stackPane = new StackPane();
        double offset = 80;
        double offsetY = -40;

        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            try {
                Image image = new Image(imagePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.fitHeightProperty().bind(containerCards.heightProperty());
                imageView.setPreserveRatio(true);

                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(imageView.fitWidthProperty());
                clip.heightProperty().bind(imageView.fitHeightProperty());
                imageView.setClip(clip);

                imageView.setTranslateX(i * offset);

                final int index = i;
                imageView.setOnMouseClicked(event -> {
                    if (selectsBool.get(index)) {
                        imageView.setTranslateY(offsetY);
                        cardsSelect.add(cards.get(index));
                        selectsBool.set(index, false);
                    } else {
                        imageView.setTranslateY(0);
                        cardsSelect.remove(cards.get(index));
                        selectsBool.set(index, true);
                    }
                });

                stackPane.getChildren().add(imageView);
            } catch (IllegalArgumentException e) {
                System.out.println("Error URL: " + imagePath);
            }
        }

        stackPane.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        containerCards.setAlignment(Pos.CENTER);
        containerCards.getChildren().add(stackPane);
    }

    @FXML
    void btnThoat(ActionEvent event) {
        clickClientEvent.clickExit();
    }

    public void setFalseIsStart() {
        isStart = false;
        visibleBtnExit();
        btnStart.setText("sẵn sàng");
    }
}
