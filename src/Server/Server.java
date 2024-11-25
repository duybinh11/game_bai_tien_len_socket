package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Model.ActionBroadcast;
import Model.Room;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server extends Application {
    private static final int PORT = 12345;
    private static List<ClientHandle> clients = new ArrayList<>();
    public static List<Room> rooms = new ArrayList<>();
    private static ServerController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewServer.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                while (true) {
                    System.out.println("listening >>>>");
                    Socket client = serverSocket.accept();
                    ClientHandle clientHandle = new ClientHandle(client);
                    System.out.println("co 1 client moi da ket noi toi info : " + client);
                    clientHandle.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Room createRoom(ClientHandle clientHandle, String name) {
        Random random = new Random();
        int id = 10000 + random.nextInt(90000);
        Room room = new Room(id, name);

        rooms.add(room);

        room.addClient(clientHandle);
        Platform.runLater(() -> {
            controller.updateListRoom(room);
        });
        return room;

    }

    public static void removeRoom(Room room) {
        rooms.removeIf(r -> r.equals(room));
    }

    public static Room getRoomById(int id) {
        return rooms.stream().filter(room -> room.getId() == id).findFirst().orElse(null);
    }

    public static void broadCastAllClients(ActionBroadcast actionBroadcast) {
        clients.forEach(client -> {
            try {
                client.getWriteObject().writeObject(actionBroadcast);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void addClients(ClientHandle clientHandle) {
        Platform.runLater(() -> {
            clients.add(clientHandle);
            controller.updateListClient(clientHandle);
        });
    }

    public static void refreshDataServer() {
        Platform.runLater(() -> {
            controller.refreshTable(clients, rooms);
        });
    }

    public static void removeClient(ClientHandle clientHandle) {
        clients.removeIf(item -> item.equals(clientHandle));
        Platform.runLater(() -> {
            controller.refreshTable(clients, rooms);
        });
    }

}
