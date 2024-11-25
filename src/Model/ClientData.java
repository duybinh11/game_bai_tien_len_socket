package Model;

import java.io.Serializable;

public class ClientData implements Serializable {
    private String host;
    private int port;
    private int postion;
    private User user;

    

    

    public ClientData(int postion, User user) {
        this.postion = postion;
        this.user = user;
    }
    @Override
    public String toString() {
        return "ClientData [host=" + host + ", port=" + port + ", postion=" + postion + ", user=" + user + "]";
    }
    public ClientData(String host, int port, int postion, User user) {
        this.host = host;
        this.port = port;
        this.postion = postion;
        this.user = user;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPostion() {
        return postion;
    }
    public void setPostion(int postion) {
        this.postion = postion;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    

}