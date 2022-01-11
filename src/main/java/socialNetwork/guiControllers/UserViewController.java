package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.PageUser;
import socialNetwork.domain.models.User;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.List;


public class UserViewController implements Observer<Event> {
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    ListView<User> listViewOfFriends;
    @FXML
    ListView<User> usersListView;
    @FXML
    Button deleteFriendshipButton;
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
    ScrollBar scrollBarListViewOfFriends;
    @FXML
    Label reportsLabel;


    NetworkController networkController;
    PageUser rootPage;
    Stage displayStage;

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPage){
        this.networkController = service;
        networkController.getNetworkService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        rootPage.refresh(rootPage.getRoot().getUsername());
        initModelFriends();
    }

    private ScrollBar getListViewScrollBar(ListView<?> listView) {
        ScrollBar scrollbar = null;
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    scrollbar = bar;
                }
            }
        }
        return scrollbar;
    }

    private void initModelFriends(){
        List< User > friendListForUser = rootPage.getFriendList();
        modelFriends.setAll(friendListForUser);
    }

    @FXML
    public void initialize(){
        ListViewInitialize.createListViewWithUser(listViewOfFriends, modelFriends);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
        //scrollBarListViewOfFriends = getListViewScrollBar(listViewOfFriends);
    }

    @Override
    public void update(Event event) {
        if(event instanceof FriendshipChangeEvent)
            initModelFriends();
    }

    @FXML
    public void handleScrollListViewFriends(ScrollEvent event){
        System.out.println(event.getX() + " " +  event.getY());
    }

    @FXML
    public void handleFriendshipDelete(){

        User mainUser = rootPage.getRoot();
        if(listViewOfFriends.getSelectionModel().getSelectedItem() != null){
            User user = listViewOfFriends.getSelectionModel().getSelectedItem();
            Long idFirstUser = mainUser.getId();
            Long idSecondUser =  user.getId();
            networkController.removeFriendship(idFirstUser,idSecondUser);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,
                    "Delete Friendship","The Friendship has been deleted successfully!");
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    @FXML
    public void handleFriendshipRequestFromUserViewController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPage, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromUserScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToMessagesViewSceneFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToReportsViewSceneFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToReportsScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void disableAddFriendship(){
        if(listViewOfFriends.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            deleteFriendshipButton.setDisable(false);
        }
    }

    @FXML
    public void disableDeleteFriendship(){
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(false);
            deleteFriendshipButton.setDisable(true);
        }
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        addFriendshipButton.setDisable(false);
        deleteFriendshipButton.setDisable(false);
        listViewOfFriends.getSelectionModel().clearSelection();
    }

    @FXML
    public void setUsersListViewOnVisible(){
        UsersSearchProcess.setUsersListViewOnVisible(usersListView, triangleAuxiliaryLabel);
    }

    @FXML
    public void setUsersListViewOnInvisible(){
        UsersSearchProcess.setUsersListViewOnInvisible(usersListView, triangleAuxiliaryLabel, searchFriendshipField);
    }

    @FXML
    private void handleFilterInUserController(){
        ListViewInitialize.handleFilter(networkController, rootPage, searchFriendshipField, modelSearchFriends);
    }

}
