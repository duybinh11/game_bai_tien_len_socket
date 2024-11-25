package Interface;

import java.util.List;

import DTO.RoomDTO;
import Model.Card;
import Model.User;

public interface ClickClientEvent {
    public void clickCreateRoom(String name);

    public void clickExit();

    public void doubleClickJoinRoom(RoomDTO roomDTO);

    public void clickStart();

    public void clickDanh(List<Card> cards);

    public void clickSkip();

    public void clickLogin(User user);

    public void nextViewCreate();

    public void clickCreateAccount(User user);

    public void clickBackToLogin();
}
