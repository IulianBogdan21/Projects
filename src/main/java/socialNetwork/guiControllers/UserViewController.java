package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Page;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class UserViewController implements Observer<Event> {
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();

    @FXML
    TableView<User> friendshipTableView;
    @FXML
    TableColumn<User,Long> tableColumnId;
    @FXML
    TableColumn<User,String> tableColumnFirstName;
    @FXML
    TableColumn<User,String> tableColumnLastName;

    @FXML
    TableView<User> friendshipSearchTableView;
    @FXML
    TableColumn<User,Long> tableColumnSearchId;
    @FXML
    TableColumn<User,String> tableColumnSearchFirstName;
    @FXML
    TableColumn<User,String> tableColumnSearchLastName;

    @FXML
    Button deleteFriendshipButton;
    @FXML
    Button addFriendshipButton;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;

    NetworkController networkController;
    Page rootPage;
    Stage displayStage;

    public void setNetworkController(Stage primaryStage, NetworkController service, Page rootPage){
        this.networkController = service;
        networkController.getNetworkService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        rootPage.refresh(rootPage.getRoot().getUsername());
        initModelFriends();
    }

    private void initModelFriends(){
        List< User > friendListForUser = rootPage.getFriendList();
        modelFriends.setAll(friendListForUser);
    }

    @FXML
    public void initialize(){
        tableColumnId.setCellValueFactory(new PropertyValueFactory<User,Long>("id"));
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<User,String>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<User,String>("lastName"));
        friendshipTableView.setItems(modelFriends);

        tableColumnSearchId.setCellValueFactory(new PropertyValueFactory<User,Long>("id"));
        tableColumnSearchFirstName.setCellValueFactory(new PropertyValueFactory<User,String>("firstName"));
        tableColumnSearchLastName.setCellValueFactory(new PropertyValueFactory<User,String>("lastName"));
        friendshipSearchTableView.setItems(modelSearchFriends);

        searchFriendshipField.textProperty().addListener(o -> handleFilter());
    }

    @Override
    public void update(Event event) {
        initModelFriends();
    }

    @FXML
    public void handleFriendshipDelete(){
        User mainUser = rootPage.getRoot();
        if(friendshipTableView.getSelectionModel().getSelectedItem() != null){
            User user = friendshipTableView.getSelectionModel().getSelectedItem();
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
    public void handleFriendshipRequest(){
        User mainUser = rootPage.getRoot();
        if(friendshipSearchTableView.getSelectionModel().getSelectedItem() != null){
            User user = friendshipSearchTableView.getSelectionModel().getSelectedItem();
            Long idFirstUser = mainUser.getId();
            Long idSecondUser = user.getId();
            try {
                networkController.sendInvitationForFriendships(idFirstUser, idSecondUser);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,
                        "Sent invitation", "The invitation has just been sent");
            } catch (ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage, exceptionBaseClass.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    @FXML
    public void handleFriendRequests(ActionEvent event) throws IOException {
        User mainUser = rootPage.getRoot();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialNetwork.gui/friendshipStatusView.fxml"));
        Parent root = loader.load();
        displayStage =  (Stage)(((Node)event.getSource()).getScene().getWindow());
        displayStage.setScene(new Scene(root));
        FriendshipStatusController friendshipStatusController = loader.getController();
        friendshipStatusController.setNetworkController(displayStage,networkController,rootPage);
        displayStage.show();
    }

    private void handleFilter(){
        User mainUser = rootPage.getRoot();
        Predicate<User> nameOfUserPredicate = u -> u.getFirstName()
                .startsWith(searchFriendshipField.getText());
        List<User> userListWithoutMainUser = networkController.getAllUsers()
                                .stream()
                                .filter(x -> !x.getId().equals(mainUser.getId()))
                                .toList();
        modelSearchFriends.setAll(userListWithoutMainUser.stream()
                .filter(nameOfUserPredicate)
                .toList());
    }

}
