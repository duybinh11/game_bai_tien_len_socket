package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import DTO.RoomDTO;
import Server.ClientHandle;
import java.io.Serializable;

public class Room implements Serializable {
    private int id;
    private String name;
    List<ClientHandle> listClient = new ArrayList<>();
    private Integer postionStartHand;
    private List<Integer> listPostionAvaiable = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Room(int id) {
        this.id = id;
    }

    public void addClient(ClientHandle clientHandle) {
        if (listClient.size() < 4) {
            listClient.add(clientHandle);
            clientHandle.setPostionRoom(getPostion());
        } else {
            System.out.println("fullllll");
        }

    }

    public int getCountMember() {
        return listClient.size();
    }

    public void removeClient(ClientHandle clientHandle) {
        listClient.removeIf(item -> {
            return item.equals(clientHandle);
        });

    }

    private int getPostion() {
        int index = listPostionAvaiable.get(0);
        listPostionAvaiable.remove(0);
        return index;
    }

    public void addPostionAvaiable(int position) {
        listPostionAvaiable.add(position);
        Collections.sort(listPostionAvaiable);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClientHandle> getListClient() {
        return listClient;
    }

    public void setListClient(List<ClientHandle> listClient) {
        this.listClient = listClient;
    }

    public Integer getPostionStartHand() {
        return postionStartHand;
    }

    public void setPostionStartHand(int postionStartHand) {
        this.postionStartHand = postionStartHand;
    }

    public List<Integer> getListPostionAvaiable() {
        return listPostionAvaiable;
    }

    public void setListPostionAvaiable(List<Integer> listPostionAvaiable) {
        this.listPostionAvaiable = listPostionAvaiable;
    }

    public int getNumberOfClients() {
        return listClient.size();
    }

    public RoomDTO toRoomDTO() {
        return new RoomDTO(id, name, listClient.size());
    }

    @Override
    public String toString() {
        return "Room [id=" + id + ", name=" + name + "]";
    }

}
