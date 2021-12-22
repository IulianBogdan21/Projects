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
import socialNetwork.domain.models.*;
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

    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTO = FXCollections.observableArrayList();
    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTOReact = FXCollections.observableArrayList();

    NetworkController networkController;
    Page rootPage;
    Stage displayStage;

    @FXML
    TableView<RequestInvitationGUIDTO> requestFriendshipTableView;
    @FXML
    TableColumn<RequestInvitationGUIDTO,Long> tableColumnRequestId;
    @FXML
    TableColumn<RequestInvitationGUIDTO,String> tableColumnRequestFirstName;
    @FXML
    TableColumn<RequestInvitationGUIDTO,String> tableColumnRequestLastName;
    @FXML
    TableColumn<RequestInvitationGUIDTO,LocalDateTime> tableColumnRequestDate;
    @FXML
    TableColumn<RequestInvitationGUIDTO,InvitationStage> tableColumnRequestStatus;

    @FXML
    TableView<RequestInvitationGUIDTO> requestFriendshipTableViewReact;
    @FXML
    TableColumn<RequestInvitationGUIDTO,Long> tableColumnRequestIdReact;
    @FXML
    TableColumn<RequestInvitationGUIDTO,String> tableColumnRequestFirstNameReact;
    @FXML
    TableColumn<RequestInvitationGUIDTO,String> tableColumnRequestLastNameReact;
    @FXML
    TableColumn<RequestInvitationGUIDTO,LocalDateTime> tableColumnRequestDateReact;
    @FXML
    TableColumn<RequestInvitationGUIDTO,InvitationStage> tableColumnRequestStatusReact;

    @FXML
    Button approveRequestButton;
    @FXML
    Button rejectRequestButton;
    @FXML
    Button closeButton;
    @FXML
    Button resubmissionRequestButton;

    public void setNetworkController(Stage primaryStage, NetworkController service,Page rootPage){
        this.networkController = service;
        networkController.getFriendRequestService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        initModelFriendRequest();
    }

    private void initModelFriendRequest(){
        User mainUser = rootPage.getRoot();
        List<FriendshipRequestDTO> friendshipRequestDTOList = networkController
                .findAllRequestFriendsForUser(mainUser.getId());

        List<RequestInvitationGUIDTO> mainUserIsOneWhoSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatSendsRequest().getId()))
                .map(friendshipRequestDTO -> {
                    return new RequestInvitationGUIDTO(friendshipRequestDTO.getUserThatReceivesRequest().getId(),
                            friendshipRequestDTO.getUserThatReceivesRequest().getFirstName(),
                            friendshipRequestDTO.getUserThatReceivesRequest().getLastName(),
                            friendshipRequestDTO.getLocalDateTime(),
                            friendshipRequestDTO.getInvitationStage());
                })
                .toList();

        List<RequestInvitationGUIDTO> notMainUserSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatReceivesRequest().getId()))
                .map(friendshipRequestDTO -> {
                    return new RequestInvitationGUIDTO(friendshipRequestDTO.getUserThatSendsRequest().getId(),
                            friendshipRequestDTO.getUserThatSendsRequest().getFirstName(),
                            friendshipRequestDTO.getUserThatSendsRequest().getLastName(),
                            friendshipRequestDTO.getLocalDateTime(),
                            friendshipRequestDTO.getInvitationStage());
                })
                .toList();

        modelFriendshipRequestDTO.setAll(mainUserIsOneWhoSendsRequests);
        modelFriendshipRequestDTOReact.setAll(notMainUserSendsRequests);
    }

    @FXML
    public void initialize(){
        nonDuplicate(tableColumnRequestId, tableColumnRequestFirstName, tableColumnRequestLastName, tableColumnRequestDate, tableColumnRequestStatus, requestFriendshipTableView, modelFriendshipRequestDTO);

        nonDuplicate(tableColumnRequestIdReact, tableColumnRequestFirstNameReact, tableColumnRequestLastNameReact, tableColumnRequestDateReact, tableColumnRequestStatusReact, requestFriendshipTableViewReact, modelFriendshipRequestDTOReact);
    }

    private void nonDuplicate(TableColumn<RequestInvitationGUIDTO, Long> tableColumnRequestIdReact, TableColumn<RequestInvitationGUIDTO, String> tableColumnRequestFirstNameReact, TableColumn<RequestInvitationGUIDTO, String> tableColumnRequestLastNameReact, TableColumn<RequestInvitationGUIDTO, LocalDateTime> tableColumnRequestDateReact, TableColumn<RequestInvitationGUIDTO, InvitationStage> tableColumnRequestStatusReact, TableView<RequestInvitationGUIDTO> requestFriendshipTableViewReact, ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTOReact) {
        tableColumnRequestIdReact.setCellValueFactory(new PropertyValueFactory<RequestInvitationGUIDTO,Long>("id"));
        tableColumnRequestFirstNameReact.setCellValueFactory(new PropertyValueFactory<RequestInvitationGUIDTO,String>("firstName"));
        tableColumnRequestLastNameReact.setCellValueFactory(new PropertyValueFactory<RequestInvitationGUIDTO,String >("lastName"));
        tableColumnRequestDateReact.setCellValueFactory(new PropertyValueFactory<RequestInvitationGUIDTO,LocalDateTime>("localDateTime"));
        tableColumnRequestStatusReact.setCellValueFactory(new PropertyValueFactory<RequestInvitationGUIDTO,InvitationStage>("invitationStage"));
        requestFriendshipTableViewReact.setItems(modelFriendshipRequestDTOReact);
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
        userViewController.setNetworkController(displayStage,networkController,rootPage);
        displayStage.show();
    }

    @FXML
    public void handleApprovedFriend(){
        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestFriendshipTableViewReact
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){
            Long idSecondUser = mainUser.getId();
            Long idFirstUser = requestInvitationGUIDTO.getId();
            try {
                networkController.updateApprovedFriendship(idFirstUser, idSecondUser);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Approved Request",
                        "The friendship request has been approved!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
            finally {
                requestFriendshipTableViewReact.getSelectionModel().clearSelection();
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
   }

    @FXML
    public void handleRejectFriend(){
        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestFriendshipTableViewReact
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){
            Long idSecondUser = mainUser.getId();
            Long idFirstUser = requestInvitationGUIDTO.getId();
            try {
                networkController.updateRejectedFriendship(idFirstUser, idSecondUser);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Rejected Request",
                        "The friendship request has been rejected!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
            finally {
                requestFriendshipTableViewReact.getSelectionModel().clearSelection();
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    @FXML
    public void handleResubmissionRequest(){
        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestFriendshipTableViewReact
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO == null){
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
            return;
        }

        if(!requestInvitationGUIDTO.getInvitationStage().equals(InvitationStage.REJECTED)){
            MessageAlert.showErrorMessage(displayStage,"You cannot resubmit request that is already pending/approved");
            return;
        }

        try{
            Long idUserThatSend = mainUser.getId();
            Long idUserThatReceive = requestInvitationGUIDTO.getId();
            networkController.updateRejectedToPendingFriendship(idUserThatSend,idUserThatReceive);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Resubmitted Request",
                    "The friendship request has been resubmitted!");
        }
        catch(ExceptionBaseClass exceptionBaseClass){
            MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
        }
        finally {
            requestFriendshipTableViewReact.getSelectionModel().clearSelection();
        }
    }

}
