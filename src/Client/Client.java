package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Interface.ClickClientEvent;
import Model.ActionBroadcast;
import Model.Card;
import Model.ClientData;
import Model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.*;

import DTO.RoomDTO;
import Database.DaoUser;

public class Client extends Application implements ClickClientEvent {
    private final int PORT = 12345;
    private final String HOST = "localhost";
    private ObjectOutputStream writeObject;
    private ObjectInputStream readObject;
    private List<RoomDTO> roomDTOs = new ArrayList<>();
    private ClientController controllerHome;
    private ClientRoomController controllerRoom;
    private ClientLoginController controllerLogin;
    private ClientCreateController controllerCreate;
    private Stage primaryStage;
    private Scene homeScene;
    private Socket socket;
    private RoomDTO roomJoining;
    private List<Card> cardOfClientOther = new ArrayList<>();
    private List<Card> cardOfClientThis = new ArrayList<>();
    private User user;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initViewLogin();
        // Bắt sự kiện khi người dùng nhấn nút X
        primaryStage.setOnCloseRequest(event -> {
            ActionBroadcast actionEndgame = new ActionBroadcast<>(0);
            try {
                writeObject.writeObject(actionEndgame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                writeObject = new ObjectOutputStream(socket.getOutputStream());
                readObject = new ObjectInputStream(socket.getInputStream());
                System.out.println("Connected to the game server.");

                while (true) {
                    try {
                        // send actionn khi bat dau
                        @SuppressWarnings("rawtypes")
                        ActionBroadcast actionBroadcast = (ActionBroadcast) readObject.readObject();
                        System.out.println("action client get : " + actionBroadcast);

                        // 1-> lấy danh sách tất cả các phòng
                        if (actionBroadcast.getCode() == 1) {
                            @SuppressWarnings("unchecked")
                            List<RoomDTO> rooms = (List<RoomDTO>) actionBroadcast.getData();
                            setRoomsUi(rooms);
                        }
                        // 3 -> có 1 phòng mới được tạo
                        else if (actionBroadcast.getCode() == 3) {
                            RoomDTO roomDTO = (RoomDTO) actionBroadcast.getData();
                            List<RoomDTO> dtos = Collections.singletonList(roomDTO);
                            setRoomsUi(dtos);
                        }
                        // 4 -> mời vào phòng
                        else if (actionBroadcast.getCode() == 4) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapData = (Map<String, Object>) actionBroadcast.getData();
                            roomJoining = (RoomDTO) mapData.get("roomDTO");
                            System.out.println(roomJoining);
                            ClientData clientData = (ClientData) mapData.get("clientData");
                            Platform.runLater(() -> {
                                nextViewRoom(() -> {
                                    updateAfterCreateRoom(clientData);
                                    controllerRoom.inVisibleBtnStart();
                                });
                            });
                        }
                        // +6 -> server gửi danh sách socket của các client khác ở trong phòng ddeer cap
                        // nhat ui
                        else if (actionBroadcast.getCode() == 6) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapData = (Map<String, Object>) actionBroadcast.getData();
                            roomJoining = (RoomDTO) mapData.get("roomDTO");
                            @SuppressWarnings("unchecked")
                            List<ClientData> clientDatas = (List<ClientData>) mapData.get("clientDatas");

                            ClientData clientCurrent = findClientDataCurrent(clientDatas);
                            System.out.println(roomJoining);
                            Platform.runLater(() -> {
                                nextViewRoom(() -> {
                                    updateClientInRoom(clientCurrent, clientDatas);
                                });
                            });

                        }
                        // +8 -> nhận bài để chơi
                        else if (actionBroadcast.getCode() == 8) {
                            List<Card> cards = (List<Card>) actionBroadcast.getData();
                            Collections.sort(cards, Comparator
                                    .comparing(Card::getRank)
                                    .thenComparing(Card::getSuit));
                            cardOfClientThis.addAll(cards);
                            System.out.println(cards);
                            Platform.runLater(() -> {
                                controllerRoom.displayCardImages(cards);
                                controllerRoom.inVisibleBtnStart();
                            });

                        }
                        // 9 den luot danh
                        else if (actionBroadcast.getCode() == 9) {
                            cardOfClientOther.clear();
                            Platform.runLater(() -> {
                                controllerRoom.visibleBtnDanh();
                                controllerRoom.visibleBtnSkip();
                            });
                        }
                        // 11 cho den luot
                        else if (actionBroadcast.getCode() == 11) {
                            Platform.runLater(() -> {
                                controllerRoom.inVisibleBtnDanh();
                                controllerRoom.inVisibleBtnSkip();
                            });
                        }
                        // +12 -> aciton server gui list card tu clien khac danh
                        else if (actionBroadcast.getCode() == 12) {
                            cardOfClientOther.clear();
                            List<Card> cards = (List<Card>) actionBroadcast.getData();
                            cardOfClientOther.addAll(cards);
                            System.out.println("card from server : " + cards);
                            Platform.runLater(() -> {
                                controllerRoom.displayCardAll(cards);
                            });
                        }
                        // +13 -> action gui aciton cho client khac tiep theo danh
                        else if (actionBroadcast.getCode() == 13) {
                            Platform.runLater(() -> {
                                controllerRoom.visibleBtnDanh();
                                controllerRoom.visibleBtnSkip();
                            });
                        }
                        // +15 -> reset card in room
                        else if (actionBroadcast.getCode() == 15) {
                            List<Card> cardTemp = new ArrayList<>();
                            Platform.runLater(() -> {
                                controllerRoom.displayCardAll(cardTemp);
                            });
                        }
                        // +17 -> reset game va chuan bi game moi
                        else if (actionBroadcast.getCode() == 17) {
                            List<Card> cardTemp = new ArrayList<>();
                            Platform.runLater(() -> {
                                controllerRoom.inVisibleBtnDanh();
                                controllerRoom.inVisibleBtnSkip();
                                controllerRoom.displayCardAll(cardTemp);
                                controllerRoom.displayCardImages(cardTemp);
                                controllerRoom.visibleBtnStart();
                                controllerRoom.setFalseIsStart();
                            });
                        }
                        // nhan ket qua cua login
                        else if (actionBroadcast.getCode() == 21) {
                            User userResult = (User) actionBroadcast.getData();
                            if (userResult != null) {
                                user = userResult;
                            }
                            Platform.runLater(() -> {
                                if (userResult != null) {
                                    System.out.println("login thanh cong");
                                    nextViewHome();
                                } else {
                                    controllerLogin.setNotification("tai khoan dang nhap khong dung");
                                    System.out.println("login that bai");
                                }
                            });
                        }

                        // nhan ket qua cua dang ky
                        else if (actionBroadcast.getCode() == 23) {
                            boolean check = (boolean) actionBroadcast.getData();
                            Platform.runLater(() -> {
                                if (check) {
                                    System.out.println("dang ky thanh cong");
                                    controllerCreate.setNotification("dang ky thanh cong");
                                } else {
                                    System.out.println("dang ky  that bai");
                                    controllerCreate.setNotification("dang ky  that bai");
                                }
                            });
                        }
                        // nhan vi tri cua client da thoat phong
                        else if (actionBroadcast.getCode() == 31) {
                            int position = (int) actionBroadcast.getData();
                            Platform.runLater(() -> {
                                if (position == 1) {
                                    controllerRoom.setPostion1Empty();
                                } else if (position == 2) {
                                    controllerRoom.setPostion2Empty();
                                } else if (position == 3) {
                                    controllerRoom.setPostion3Empty();
                                } else {
                                    controllerRoom.setPostion4Empty();
                                }

                            });
                        }

                        // cap nhat thong tin moi nhat cua thong tin list room
                        else if (actionBroadcast.getCode() == 41) {
                            List<RoomDTO> roomDTOs = (List<RoomDTO>) actionBroadcast.getData();
                            setRoomsUiAll(roomDTOs);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void clickCreateRoom(String name) {
        ActionBroadcast<String> actionCreateRoom = new ActionBroadcast<String>(2, name);
        try {
            writeObject.writeObject(actionCreateRoom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRoomsUi(List<RoomDTO> newRooms) {
        this.roomDTOs.addAll(newRooms);
        Platform.runLater(() -> {
            controllerHome.updateRooms(roomDTOs);
        });
    }

    public void setRoomsUiAll(List<RoomDTO> newRooms) {
        this.roomDTOs = newRooms;
        Platform.runLater(() -> {
            controllerHome.updateRooms(roomDTOs);
        });
    }

    // ui room

    public void updateAfterCreateRoom(ClientData clientData) {
        if (controllerRoom != null) {
            controllerRoom.updateAfterCreateRoom(clientData);
        } else {
            System.out.println("controller null");
        }

    }

    public void updateClientInRoom(ClientData c, List<ClientData> clientDatas) {
        if (controllerRoom != null) {
            controllerRoom.updateClientInRoom(c, clientDatas);
        } else {
            System.out.println("controller null");
        }
    }

    public void nextViewRoom(Runnable onComplete) {
        try {
            FXMLLoader loaderRoom = new FXMLLoader(getClass().getResource("ViewRoom.fxml"));
            Parent rootRoom = loaderRoom.load();
            controllerRoom = loaderRoom.getController();
            controllerRoom.setClickClientEvent(this);

            Scene roomScene = new Scene(rootRoom);
            primaryStage.setScene(roomScene);
            // primaryStage.setFullScreen(true);
            primaryStage.show();

            if (onComplete != null) {
                onComplete.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickExit() {
        primaryStage.setScene(homeScene);
        primaryStage.setFullScreen(false);

        ActionBroadcast actionExitRoom = new ActionBroadcast<>(30, roomJoining.getId());
        try {
            writeObject.writeObject(actionExitRoom);
        } catch (IOException e) {
            e.printStackTrace();
        }

        roomJoining = null;
    }

    @Override
    public void doubleClickJoinRoom(RoomDTO roomDTO) {
        if (roomDTO.getNumberOfClients() < 4) {
            ActionBroadcast actionJoinRoom = new ActionBroadcast<>(5, roomDTO.getId());
            System.out.println("gui yeu cau tham gia");
            try {
                writeObject.writeObject(actionJoinRoom);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("phong da đầy");
        }
    }

    private ClientData findClientDataCurrent(List<ClientData> clientDatas) {
        return clientDatas.stream().filter(client -> client.getPort() == socket.getLocalPort()).findFirst()
                .orElse(null);
    }

    @Override
    public void clickStart() {
        ActionBroadcast actionStart = new ActionBroadcast<>(7, roomJoining.getId());
        try {
            writeObject.writeObject(actionStart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickDanh(List<Card> cards) {
        System.out.println("click danh");
        System.out.println(cardOfClientThis);
        System.out.println("card danh : " + cards);

        // Tạo một bản sao của danh sách cards
        List<Card> cardsCopy = new ArrayList<>(cards);
        System.out.println("card of clientOther :" + cardOfClientOther);

        // checkValidator(cardsCopy);
        System.out.println("bai danh co hop le : " + checkValid(cardsCopy));

        if (cardOfClientOther.isEmpty() && checkValid(cardsCopy)) {
            broadcastDanhBai(cardsCopy);
            cardOfClientThis.removeAll(cards);
            controllerRoom.resetCartSlect();
        } else if (checkValid(cardsCopy) && checkCompareCard(cardsCopy)) {
            broadcastDanhBai(cardsCopy);
            cardOfClientThis.removeAll(cards);
            controllerRoom.resetCartSlect();
        } else {
            Platform.runLater(() -> {
                controllerRoom.setMessageNotifilcation("bai danh kh hop le");
            });
        }
        System.out.println("bai hien tai : " + cardOfClientThis);

        if (cardOfClientThis.size() == 0) {
            ActionBroadcast actionEndGame = new ActionBroadcast<>(16, roomJoining.getId());
            try {
                writeObject.writeObject(actionEndGame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean checkValid(List<Card> cards) {
        if (cards.size() == 1 || isPair(cards) || isTriplet(cards) || isFourOfAKind(cards) || isFourOfAKind(cards)
                || isStraight(cards)) {
            return true;
        }
        return false;
    }

    public void broadcastDanhBai(List<Card> cardsCopy) {
        ActionBroadcast<List<Card>> cardsHand = new ActionBroadcast<List<Card>>(10, cardsCopy, roomJoining.getId());
        try {
            writeObject.writeObject(cardsHand);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            controllerRoom.displayCardImages(cardOfClientThis);
            controllerRoom.displayCardAll(cardsCopy);
            controllerRoom.setMessageNotifilcation("");
        });
    }

    public boolean checkCompareCard(List<Card> cards) {
        if (cardOfClientOther.size() != cards.size()) {
            return false;
        }

        if (cardOfClientOther.size() == 1) {
            return compareOneCardMoreThan(cards.get(0), cardOfClientOther.get(0));
        } else if (isPair(cardOfClientOther) && isPair(cards)) {
            return compareOneCardMoreThan(getCardMax(cards), getCardMax(cardOfClientOther));
        } else if (isTriplet(cardOfClientOther) && isPair(cards)) {
            return compareOneCardMoreThan(getCardMax(cards), getCardMax(cardOfClientOther));
        } else if (isStraight(cardOfClientOther) && isStraight(cards)) {
            return compareOneCardMoreThan(getCardMax(cards), getCardMax(cardOfClientOther));
        } else if (isFourOfAKind(cardOfClientOther) && isFourOfAKind(cards)) {
            return compareOneCardMoreThan(getCardMax(cards), getCardMax(cardOfClientOther));
        }

        return false;
    }

    public Card getCardMax(List<Card> cards) {
        return cards.stream().max((card1, card2) -> {
            int rankCompare = card1.getRank().ordinal() - card2.getRank().ordinal();
            if (rankCompare == 0) {
                return card1.getSuit().ordinal() - card2.getSuit().ordinal();
            } else {
                return rankCompare;
            }
        }).orElse(null);
    }

    private boolean compareOneCardMoreThan(Card card1, Card card2) {
        if (card1.getRank().ordinal() > card2.getRank().ordinal()) {
            return true;
        } else if (card1.getRank().ordinal() == card2.getRank().ordinal()) {
            if (card1.getSuit().ordinal() > card2.getSuit().ordinal()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPair(List<Card> cards) {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public boolean isTriplet(List<Card> cards) {
        return cards.size() == 3 &&
                cards.get(0).getRank() == cards.get(1).getRank() &&
                cards.get(1).getRank() == cards.get(2).getRank();
    }

    public boolean isFourOfAKind(List<Card> cards) {
        return cards.size() == 4 &&
                cards.get(0).getRank() == cards.get(1).getRank() &&
                cards.get(1).getRank() == cards.get(2).getRank() &&
                cards.get(2).getRank() == cards.get(3).getRank();
    }

    public boolean isStraight(List<Card> cards) {
        if (cards.size() < 3) {
            return false;
        }

        cards.sort(Comparator.comparing(card -> card.getRank().ordinal()));

        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i + 1).getRank().ordinal() != cards.get(i).getRank().ordinal() + 1) {
                return false;
            }
        }
        return true;
    }

    public void nextViewHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewHome.fxml"));
            Parent root = loader.load(); // Gọi load() trước khi lấy controller

            controllerHome = loader.getController();

            if (controllerHome != null) {
                controllerHome.setInterfaceClick(this);
                System.out.println(user);
                controllerHome.setUsername(user.getUsername());
            } else {
                System.out.println("Controller is null");
            }

            homeScene = new Scene(root);
            primaryStage.setScene(homeScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickSkip() {
        ActionBroadcast actionSkip = new ActionBroadcast<>(14, roomJoining.getId());
        try {
            writeObject.writeObject(actionSkip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickLogin(User user) {
        ActionBroadcast<User> actionLogin = new ActionBroadcast<User>(20, user);
        try {
            writeObject.writeObject(actionLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void nextViewCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewCreate.fxml"));
            Parent root = loader.load();

            controllerCreate = loader.getController();

            if (controllerCreate != null) {
                controllerCreate.setInterfaceClick(this);
            } else {
                System.out.println("controllerCreate is null");
            }

            homeScene = new Scene(root);
            primaryStage.setScene(homeScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initViewLogin() throws IOException {
        FXMLLoader loaderLogin = new FXMLLoader(getClass().getResource("ViewLogin.fxml"));
        Parent rootLogin = loaderLogin.load();
        controllerLogin = loaderLogin.getController();
        homeScene = new Scene(rootLogin);
        controllerLogin.setInterfaceClick(this);
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(homeScene);
        primaryStage.show();
    }

    @Override
    public void clickCreateAccount(User user) {
        System.out.println(1);
        ActionBroadcast<User> actionLogin = new ActionBroadcast<User>(22, user);
        try {
            writeObject.writeObject(actionLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickBackToLogin() {
        try {
            initViewLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}