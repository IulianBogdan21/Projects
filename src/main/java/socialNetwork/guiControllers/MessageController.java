package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Chat;
import socialNetwork.domain.models.Page;
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
    ObservableList<Chat> modelChatsName = FXCollections.observableArrayList();

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    HBox mainHorizontalBox;
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
    @FXML
    FontAwesomeIconView newPersonConversationIcon;
    @FXML
    FontAwesomeIconView closeConversationStartIcon;
    @FXML
    FontAwesomeIconView confirmDestinationIcon;
    @FXML
    TextField searchUserToStartConversationField;
    @FXML
    ListView<User> startConversationListView;
    @FXML
    VBox newConversationBox;
    @FXML
    Label usernameLabelChat;
    @FXML
    ListView<Chat> chatsNameListView;

    NetworkController networkController;
    Page rootPage;
    Stage displayStage;

    public void setNetworkController(Stage primaryStage, NetworkController service,Page rootPage){
        this.networkController = service;
        networkController.getMessageService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        usernameLabelChat.setText(rootPage.getRoot().getUsername());
        ListViewInitialize.createListViewWithChats(chatsNameListView,modelChatsName,rootPage.getRoot());
        initModelChatsName();
    }

    private void initModelChatsName(){
        modelChatsName.setAll(rootPage.getChatList());
    }

    @FXML
    public void initialize(){
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        ListViewInitialize.createListViewWithUser(startConversationListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
        searchUserToStartConversationField.textProperty().addListener(o -> handleFilterSearchUserForNewConversation());
    }

    @Override
    public void update(Event event) {
        initModelChatsName();
    }

    @FXML
    public void handleFriendshipRequestFromMessageController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPage, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromMessageScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromMessageScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToMessagesSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPage, displayStage);
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
        ListViewInitialize.handleFilter(networkController, rootPage, searchFriendshipField, modelSearchFriends);
    }

    private void handleFilterSearchUserForNewConversation(){
        ListViewInitialize.handleFilter(networkController, rootPage, searchUserToStartConversationField, modelSearchFriends);
    }

    @FXML
    public void closeStartConversationWindow(){
        mainAnchorPane.setEffect(null);
        mainAnchorPane.setDisable(false);
        mainHorizontalBox.setEffect(null);
        mainHorizontalBox.setDisable(false);
        newConversationBox.setVisible(false);
    }

    @FXML
    public void proceedWithNewConversation(){
        closeStartConversationWindow();
    }

    @FXML
    public void openWindowNewConversation(){
        Lighting lighting = new Lighting();
        mainAnchorPane.setEffect(lighting);
        mainAnchorPane.setDisable(true);
        mainHorizontalBox.setEffect(lighting);
        mainHorizontalBox.setDisable(true);
        newConversationBox.setVisible(true);
    }

}
