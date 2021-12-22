package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.User;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;

public class MessageController implements Observer<Event> {
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();

    @FXML
    ListView<User> usersListView;
    @FXML
    Button addFriendshipButton;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Label friendsLabel;
    @FXML
    Label friendRequestsLabel;
    @FXML
    Label messagesLabel;
    @FXML
    Polygon triangleAuxiliaryLabel;

    NetworkController networkController;
    User mainUser;
    Stage displayStage;

    public void setNetworkController(Stage primaryStage, NetworkController service,User user){
        this.networkController = service;
        networkController.addObserver(this);
        this.displayStage = primaryStage;
        this.mainUser = user;
        initModelFriends();
    }

    private void initModelFriends(){}

    @FXML
    public void initialize(){
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
    }

    @Override
    public void update(Event event) {
        initModelFriends();
    }

    @FXML
    public void handleFriendshipRequestFromMessageController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, mainUser, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromMessageScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, mainUser, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromMessageScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, mainUser, displayStage);
    }

    @FXML
    public void switchToMessagesSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, mainUser, displayStage);
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){}

    @FXML
    public void enableFriendsLabel(){
        friendsLabel.setVisible(true);
    }

    @FXML
    public void enableFriendRequestsLabel(){
        friendRequestsLabel.setVisible(true);
    }

    @FXML
    public void enableMessagesLabel(){
        messagesLabel.setVisible(true);
    }

    @FXML
    public void disableFriendsLabel(){
        friendsLabel.setVisible(false);
    }

    @FXML
    public void disableFriendRequestsLabel(){
        friendRequestsLabel.setVisible(false);
    }

    @FXML
    public void disableMessagesLabel(){
        messagesLabel.setVisible(false);
    }

    @FXML
    public void setUsersListViewOnVisible(){
        UsersSearchProcess.setUsersListViewOnVisible(usersListView, triangleAuxiliaryLabel);
    }

    @FXML
    public void setUsersListViewOnInvisible(){
        UsersSearchProcess.setUsersListViewOnInvisible(usersListView, triangleAuxiliaryLabel, searchFriendshipField);
    }

    private void handleFilterInUserController(){
        ListViewInitialize.handleFilter(networkController, mainUser, searchFriendshipField, modelSearchFriends);
    }

}
