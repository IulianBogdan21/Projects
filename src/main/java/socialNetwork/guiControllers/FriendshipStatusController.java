package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.FriendshipRequestDTO;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.stream.Collectors;

public class FriendshipStatusController implements Observer<Event> {

    ObservableList<FriendshipRequestDTO> modelFriendshipRequestDTO = FXCollections.observableArrayList();

    NetworkController networkController;
    User mainUser;
    Stage displayStage;

    @FXML
    TableView<FriendshipRequestDTO> requestFriendshipTableView;
    @FXML
    TableColumn<FriendshipRequestDTO,Long> tableColumnRequestId;
    @FXML
    TableColumn<FriendshipRequestDTO,String> tableColumnRequestFirstName;
    @FXML
    TableColumn<FriendshipRequestDTO,String> tableColumnRequestLastName;
    @FXML
    TableColumn<FriendshipRequestDTO,LocalDateTime> tableColumnRequestDate;
    @FXML
    TableColumn<FriendshipRequestDTO,InvitationStage> tableColumnRequestStatus;
    @FXML
    Button approveRequestButton;
    @FXML
    Button rejectRequestButton;
    @FXML
    Button closeButton;

    public void setNetworkController(Stage primaryStage, NetworkController service,User user){
        this.networkController = service;
        networkController.addObserver(this);
        this.displayStage = primaryStage;
        this.mainUser = user;
        initModelFriendRequest();
    }

    private void initModelFriendRequest(){
        List<FriendshipRequestDTO> friendshipRequestDTOList = networkController
                .findAllRequestFriendsForUser(mainUser.getId());
        /*
        mainuser
        mainuser   otheruser  (Avem o pritenie intre mainUSer si otherUser in care mainUser a trimis si OtherUser primest
        Din perspectiva a lui mainUSer ca aplicatie ,in tabela va fi afisat frien requestul cu user ul other user,dar nu v a putea acepta
        Din perspectiva lui otherUSer ca aplicatie,in tabela vi di afisa mainuser  ,si vamputea raspunde
         */

        List<FriendshipRequestDTO> mainUserIsOneWhoSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatSendsRequest().getId()))
                .toList();
        List<FriendshipRequestDTO> notMainUSerSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatReceivesRequest().getId()))
                .collect(Collectors.toList());;

        modelFriendshipRequestDTO.setAll(friendshipRequestDTOList);
    }

    @FXML
    public void initialize(){
        tableColumnRequestId.setCellValueFactory(new PropertyValueFactory<FriendshipRequestDTO,Long>("id"));
        tableColumnRequestFirstName.setCellValueFactory(new PropertyValueFactory<FriendshipRequestDTO,String>("firstName"));
        tableColumnRequestLastName.setCellValueFactory(new PropertyValueFactory<FriendshipRequestDTO,String >("lastName"));
        tableColumnRequestDate.setCellValueFactory(new PropertyValueFactory<FriendshipRequestDTO,LocalDateTime>("localDateTime"));
        tableColumnRequestStatus.setCellValueFactory(new PropertyValueFactory<FriendshipRequestDTO,InvitationStage>("invitationStage"));

        requestFriendshipTableView.setItems(modelFriendshipRequestDTO);
    }

    @Override
    public void update(Event event) {
        initModelFriendRequest();
    }

    @FXML
    public void handleCloseButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialNetwork.gui/userView.fxml"));
        Parent root = loader.load();
        displayStage =  (Stage)(((Node)event.getSource()).getScene().getWindow());
        displayStage.setScene(new Scene(root));
        UserViewController userViewController = loader.getController();
        userViewController.setNetworkController(displayStage,networkController,mainUser);
        displayStage.show();
    }

    @FXML
    public void handleApprovedFriend(){
        FriendshipRequestDTO friendshipRequestDTO = requestFriendshipTableView
                .getSelectionModel().getSelectedItem();
        if(friendshipRequestDTO != null){
            Long idFirstUser = mainUser.getId();
            Long idSecondUser = friendshipRequestDTO.getFriendUser().getId();
            if(idFirstUser.equals(friendshipRequestDTO.getIdUserThatSendsRequest())){
                MessageAlert.showErrorMessage(displayStage,
                        "You are the one who sent the request!You cannot accept!You have to wait for the other user to accept your request!");
                return;
            }
            try {
                networkController.updateApprovedFriendship(idFirstUser, idSecondUser);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Approved Request",
                        "The friendship request has been approved!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is not selection!");
        }
    }

}
