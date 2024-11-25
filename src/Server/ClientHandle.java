package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DTO.RoomDTO;
import Database.DaoUser;

import java.util.Date;
import java.io.Serializable;
import Model.ActionBroadcast;
import Model.Card;
import Model.ClientData;
import Model.Deck;
import Model.Room;
import Model.User;
import javafx.application.Platform;

public class ClientHandle extends Thread implements Serializable {
    private Socket socket;
    public User user;
    private ObjectOutputStream writeObject;
    private ObjectInputStream readObject;
    private Integer postionRoom = null;
    private boolean isSkip = false;
    private boolean isStart = false;
    private Date time;
    private DaoUser daoUser = new DaoUser();

    public ClientHandle(Socket socket) throws IOException {
        this.socket = socket;

        readObject = new ObjectInputStream(socket.getInputStream());
        writeObject = new ObjectOutputStream(socket.getOutputStream());
        this.time = new Date();

    }

    @Override
    public void run() {
        while (true) {
            try {
                ActionBroadcast actionBroadcast = (ActionBroadcast) readObject.readObject();
                System.out.println("action server get : " + actionBroadcast);
                // + 2 -> tạo phòng

                if (actionBroadcast.getCode() == 2) {
                    String name = (String) actionBroadcast.getData();
                    Room room = Server.createRoom(this, name);
                    RoomDTO RoomDTO = room.toRoomDTO();
                    ActionBroadcast<RoomDTO> actionNewRoom = new ActionBroadcast<RoomDTO>(3, RoomDTO);
                    Server.broadCastAllClients(actionNewRoom);

                    ClientData clientData = getClientData();
                    Map<String, Object> mapData = Map.of(
                            "roomDTO", RoomDTO,
                            "clientData", clientData);
                    ActionBroadcast<Map<String, Object>> actionEnterRoom = new ActionBroadcast<Map<String, Object>>(4,
                            mapData);

                    broadcastThisClient(actionEnterRoom);
                    Server.refreshDataServer();
                } else if (actionBroadcast.getCode() == 0) {
                    Server.removeClient(this);
                }
                // 5 -> yêu cầu tham gia vào phòng
                else if (actionBroadcast.getCode() == 5) {

                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);
                    room.addClient(this);

                    List<ClientData> clientDataOther = room.getListClient().stream().map(handle -> {
                        return handle.getClientData();
                    }).toList();

                    Map<String, Object> mapData = Map.of(
                            "roomDTO", room.toRoomDTO(),
                            "clientDatas", clientDataOther);

                    ActionBroadcast<Map<String, Object>> actionOAllInClient = new ActionBroadcast<Map<String, Object>>(
                            6,
                            mapData);
                    broadcastClientAllinRoom(room.getListClient(), actionOAllInClient);
                    Server.refreshDataServer();

                    List<RoomDTO> roomsDto = Server.rooms.stream().map(room1 -> {
                        return room.toRoomDTO();
                    }).toList();

                    sendBroadcastDataNewRoomToAllClient(roomsDto);

                }
                // + set isStart cho client do va check xem tat ca cac client da sang ht ch
                else if (actionBroadcast.getCode() == 7) {
                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);
                    room.getListClient().forEach(client -> {
                        if (client.equals(this)) {
                            client.setStart(true);
                        }
                    });
                    boolean isAllStart = room.getListClient().stream().allMatch(client -> client.isStart);
                    if (isAllStart) {
                        System.out.println("tat ca da san san");
                        broadcastSendCartClients(room);
                        ClientHandle clientHandFirst = findClientHandFist(room);
                        ActionBroadcast<List<Card>> actonNewTurn = new ActionBroadcast<List<Card>>(9, null);
                        clientHandFirst.getWriteObject().writeObject(actonNewTurn);
                    } else {
                        System.out.println("not tat ca da san san");
                    }
                }
                // + nhận cart từ client
                else if (actionBroadcast.getCode() == 10) {
                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);
                    List<Card> cards = (List<Card>) actionBroadcast.getData();
                    System.out.println("card server from client : " + cards);

                    List<ClientHandle> clientHandles = room.getListClient();

                    ActionBroadcast<List<Card>> actionCartToServer = new ActionBroadcast<List<Card>>(12, cards);
                    broadcastClientInRoom(clientHandles, actionCartToServer);

                    ActionBroadcast actionWaitTurn = new ActionBroadcast<>(11);
                    broadcastThisClient(actionWaitTurn);

                    ActionBroadcast actionNextClient = new ActionBroadcast<>(13);

                    nextTurnHand(clientHandles, actionNextClient);
                }
                // + client bo luot
                else if (actionBroadcast.getCode() == 14) {
                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);

                    isSkip = true;

                    ActionBroadcast actionWaitTurn = new ActionBroadcast<>(11);
                    broadcastThisClient(actionWaitTurn);

                    ActionBroadcast actionNextClient = new ActionBroadcast<>(13);

                    nextTurnHand(room.getListClient(), actionNextClient);

                }
                // + end game
                else if (actionBroadcast.getCode() == 16) {
                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);

                    ActionBroadcast actionNewGame = new ActionBroadcast<>(17);
                    broadcastClientAllinRoom(room.getListClient(), actionNewGame);

                    // set nguoi do danh truoc

                    room.setPostionStartHand(postionRoom);
                    resetDataClient(room.getListClient());

                }
                // handle login
                else if (actionBroadcast.getCode() == 20) {
                    User user = (User) actionBroadcast.getData();
                    boolean check = daoUser.checkUserCredentials(user);
                    if (check) {
                        this.user = user;
                        Server.addClients(this);
                    } else {
                        user = null;
                    }
                    ActionBroadcast actionResultLogin = new ActionBroadcast<User>(21, user);
                    broadcastThisClient(actionResultLogin);
                    // gui danh sach room sau khi login thanh cong
                    broadcastListRoom();
                }

                else if (actionBroadcast.getCode() == 22) {
                    User user = (User) actionBroadcast.getData();
                    boolean check = daoUser.insert(user);
                    ActionBroadcast actionResultLogin = new ActionBroadcast<Boolean>(23, check);
                    broadcastThisClient(actionResultLogin);
                }

                else if (actionBroadcast.getCode() == 30) {
                    int idRoom = actionBroadcast.getIdRoom();
                    Room room = Server.getRoomById(idRoom);

                    if (room.getCountMember() > 1) {
                        room.removeClient(this);
                        room.addPostionAvaiable(postionRoom);
                        List<ClientHandle> clientHandles = room.getListClient();
                        ActionBroadcast actionClientExitRoom = new ActionBroadcast<>(31, postionRoom);
                        broadcastClientInRoom(clientHandles, actionClientExitRoom);
                    } else {
                        Server.removeRoom(room);
                    }
                    postionRoom = null;
                    Server.refreshDataServer();

                    List<RoomDTO> roomsDto = Server.rooms.stream().map(room1 -> {
                        return room.toRoomDTO();
                    }).toList();

                    sendBroadcastDataNewRoomToAllClient(roomsDto);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetDataClient(List<ClientHandle> clientHandles) {
        clientHandles.forEach(t -> {
            t.isSkip = false;
            t.isStart = false;
        });
    }

    private void nextTurnHand(List<ClientHandle> clientHandles,
            @SuppressWarnings("rawtypes") ActionBroadcast actionNextClient) throws IOException {
        int postionCurrentHandTurn = postionRoom;
        while (true) {
            if (postionCurrentHandTurn + 1 > 4) {
                postionCurrentHandTurn = 1;
            } else {
                postionCurrentHandTurn++;
            }
            final int clientPostionNext = postionCurrentHandTurn;

            ClientHandle clientNext = clientHandles.stream().filter(t -> {
                if (t.getPostionRoom() == clientPostionNext && !t.isSkip) {
                    return true;
                }
                return false;
            })
                    .findFirst().orElse(null);
            if (clientNext != null) {
                clientNext.getWriteObject().writeObject(actionNextClient);
                boolean checkNewTurn = checkNewTurn(clientHandles, clientNext);
                if (checkNewTurn) {
                    resetTurnHand(clientHandles, clientNext);
                } else {
                    System.out.println("not reset turn hand");
                }
                System.out.println(checkNewTurn);
                break;
            }
        }

    }

    private void broadcastSendCartClients(Room room) {
        Deck deck = new Deck();
        room.getListClient().forEach(client -> {
            ActionBroadcast<List<Card>> actionGetCarts = new ActionBroadcast<List<Card>>(8,
                    deck.getCarts());
            try {
                client.getWriteObject().writeObject(actionGetCarts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private ClientHandle findClientHandFist(Room room) {
        Integer postionStartHand = room.getPostionStartHand();
        if (postionStartHand == null) {
            // tim client có postion nhỏ nhât
            return room.getListClient().stream()
                    .filter(client -> client.getPostionRoom() != null)
                    .min((c1, c2) -> Integer.compare(c1.getPostionRoom(), c2.getPostionRoom()))
                    .orElse(null);
        } else {
            return room.getListClient().stream().filter(t -> t.getPostionRoom() == postionStartHand).findFirst()
                    .orElse(null);
        }
    }

    private void broadcastListRoom() throws IOException {
        List<RoomDTO> roomsDto = Server.rooms.stream().map(room -> {
            return room.toRoomDTO();
        }).toList();
        ActionBroadcast<List<RoomDTO>> actionSendRooms = new ActionBroadcast<List<RoomDTO>>(1, roomsDto);
        writeObject.writeObject(actionSendRooms);
    }

    private void broadcastThisClient(@SuppressWarnings("rawtypes") ActionBroadcast actionBroadcast) throws IOException {
        writeObject.writeObject(actionBroadcast);
    }

    private void broadcastClientInRoom(List<ClientHandle> clientHandles,
            @SuppressWarnings("rawtypes") ActionBroadcast actionBroadcast) {
        clientHandles.forEach(client -> {
            if (!client.equals(this)) {
                try {
                    client.getWriteObject().writeObject(actionBroadcast);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void broadcastClientAllinRoom(List<ClientHandle> clientHandles,
            @SuppressWarnings("rawtypes") ActionBroadcast actionBroadcast) {
        clientHandles.forEach(client -> {
            try {
                client.getWriteObject().writeObject(actionBroadcast);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkNewTurn(List<ClientHandle> clientHandles, ClientHandle clientHandleNext) {
        System.out.println(clientHandles);
        return clientHandles.stream()
                .filter(t -> !t.equals(clientHandleNext))
                .allMatch(t -> t.isSkip);
    }

    private void resetTurnHand(List<ClientHandle> clientHandles, ClientHandle clientHandleNext) throws IOException {
        clientHandles.forEach(t -> {
            t.isSkip = false;
            ActionBroadcast actionResetCart = new ActionBroadcast<>(15);
            try {
                t.getWriteObject().writeObject(actionResetCart);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ActionBroadcast<List<Card>> actionNewTurn = new ActionBroadcast<List<Card>>(9, null);
        System.out.println("reset tủrn + action : " + actionNewTurn);
        clientHandleNext.getWriteObject().writeObject(actionNewTurn);

    }

    public void sendBroadcastDataNewRoomToAllClient(List<RoomDTO> roomDTOs) {
        ActionBroadcast<List<RoomDTO>> actionRefreshListRoomToAllClient = new ActionBroadcast<List<RoomDTO>>(
                41, roomDTOs);
        Server.broadCastAllClients(actionRefreshListRoomToAllClient);
    }

    public ClientData getClientData() {
        return new ClientData(socket.getLocalAddress().toString(), socket.getPort(), postionRoom, this.user);
    }

    public Integer getPostionRoom() {
        return postionRoom;
    }

    public void setPostionRoom(Integer postionRoom) {
        this.postionRoom = postionRoom;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean isSkip) {
        this.isSkip = isSkip;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getWriteObject() {
        return writeObject;
    }

    public ObjectInputStream getReadObject() {
        return readObject;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((socket == null) ? 0 : socket.hashCode());
        result = prime * result + ((writeObject == null) ? 0 : writeObject.hashCode());
        result = prime * result + ((readObject == null) ? 0 : readObject.hashCode());
        result = prime * result + ((postionRoom == null) ? 0 : postionRoom.hashCode());
        result = prime * result + (isSkip ? 1231 : 1237);
        result = prime * result + (isStart ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientHandle other = (ClientHandle) obj;
        if (socket == null) {
            if (other.socket != null)
                return false;
        } else if (!socket.equals(other.socket))
            return false;
        if (writeObject == null) {
            if (other.writeObject != null)
                return false;
        } else if (!writeObject.equals(other.writeObject))
            return false;
        if (readObject == null) {
            if (other.readObject != null)
                return false;
        } else if (!readObject.equals(other.readObject))
            return false;
        if (postionRoom == null) {
            if (other.postionRoom != null)
                return false;
        } else if (!postionRoom.equals(other.postionRoom))
            return false;
        if (isSkip != other.isSkip)
            return false;
        if (isStart != other.isStart)
            return false;
        return true;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "ClientHandle [socket=" + socket + ", postionRoom=" + postionRoom + ", isSkip=" + isSkip + ", isStart="
                + isStart + "]";
    }

}
