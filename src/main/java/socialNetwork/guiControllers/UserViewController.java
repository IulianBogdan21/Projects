package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.User;
import socialNetwork.utilitaries.MessageAlert;

import java.util.ArrayList;
import java.util.List;


public class UserViewController {
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
    TextField searchFriendshipField;

    NetworkController networkController;
    User mainUser;
    Stage displayStage;

    public void setNetworkController(Stage primaryStage, NetworkController service,User user){
        this.networkController = service;
        this.displayStage = primaryStage;
        this.mainUser = user;
        initModelFriends();
    }

    private void initModelFriends(){
        List< User > friendListForUser = networkController.findAllFriendshipsForUser(mainUser.getId())
                .entrySet()
                .stream()
                .map(x -> x.getKey().get())
                .toList();
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

    }

    @FXML
    public void handleFriendshipDelete(){
        if(friendshipTableView.getSelectionModel().getSelectedItem() != null){
            User user = friendshipTableView.getSelectionModel().getSelectedItem();
            Long idFirstUser = mainUser.getId();
            Long idSecondUser =  user.getId();
            networkController.removeFriendship(idFirstUser,idSecondUser);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,
                    "Delete Friendship","The Friendship has been deleted successfully!");
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is not selection!");
        }
    }
}
