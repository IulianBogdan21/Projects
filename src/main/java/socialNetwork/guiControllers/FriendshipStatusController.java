package socialNetwork.guiControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.List;

public class FriendshipStatusController implements Observer<Event> {

    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTO = FXCollections.observableArrayList();
    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTOReact = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();

    NetworkController networkController;
    Page rootPage;
    Stage displayStage;

    @FXML
    Button showRequestsSentButton;
    @FXML
    Button showRequestsReceivedButton;
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
    ListView<User> usersListView;
    @FXML
    ListView<RequestInvitationGUIDTO> requestsSentListView;
    @FXML
    ListView<RequestInvitationGUIDTO> requestsReceivedListView;
    @FXML
    Button approveRequestButton;
    @FXML
    Button rejectRequestButton;
    @FXML
    Button resubmissionRequestButton;

    public void setNetworkController(Stage primaryStage, NetworkController service,Page rootPage){
        this.networkController = service;
        networkController.getFriendRequestService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        rootPage.refresh(rootPage.getRoot().getUsername());
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
        showRequestsSentButton.setStyle("-fx-font-weight: bold");
        initializeListView(requestsSentListView, modelFriendshipRequestDTO);
        initializeListView(requestsReceivedListView, modelFriendshipRequestDTOReact);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInFriendshipStatusController());
    }

    private void initializeListView(ListView<RequestInvitationGUIDTO> listView,
                                    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequest) {
        listView.setItems(modelFriendshipRequest);
        listView.setCellFactory(r -> new ListCell<RequestInvitationGUIDTO>(){
            @Override
            protected void updateItem(RequestInvitationGUIDTO item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText("Friendship with " + item.getFirstName() + " " + item.getLastName() + " is currently on " +
                            item.getInvitationStage() + " since " + item.getLocalDateTime().toLocalDate());
                }
            }
        });
    }

    @Override
    public void update(Event event) {
        initModelFriendRequest();
    }

    @FXML
    public void switchToUserViewScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPage, displayStage);

    }

    @FXML
    public void handleApprovedFriend(){

        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){
            Long userThatReceivesInvitationAndAcceptedId = mainUser.getId();
            Long userThatSendInvitationAndWaitVerdictId = requestInvitationGUIDTO.getId();
            try {
                networkController.updateApprovedFriendship(userThatReceivesInvitationAndAcceptedId,userThatSendInvitationAndWaitVerdictId);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Approved Request",
                        "The friendship request has been approved!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
            finally {
                requestsReceivedListView.getSelectionModel().clearSelection();
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
   }

    @FXML
    public void handleRejectFriend(){

        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){

            Long userThatReceivesInvitationAndRejectedId = mainUser.getId();
            Long userThatSendInvitationAndWaitVerdictId = requestInvitationGUIDTO.getId();
            try {
                networkController.updateRejectedFriendship(userThatReceivesInvitationAndRejectedId , userThatSendInvitationAndWaitVerdictId);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Rejected Request",
                        "The friendship request has been rejected!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    @FXML
    public void handleResubmissionRequest(){

        User mainUser = rootPage.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
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
            Long idUserThatRejectButChangeHisMind = mainUser.getId();
            Long idUserThatSendInitiallyInvitation = requestInvitationGUIDTO.getId();
            networkController.updateRejectedToPendingFriendship(idUserThatRejectButChangeHisMind,idUserThatSendInitiallyInvitation);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Resubmitted Request",
                    "The friendship request has been resubmitted!");
        }
        catch(ExceptionBaseClass exceptionBaseClass){
            MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
        }
        finally {
            requestsReceivedListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void showRequestsReceivedListView(){
        showRequestsSentButton.setStyle("");
        showRequestsReceivedButton.setStyle("-fx-font-weight: bold");
        requestsSentListView.setVisible(false);
        requestsReceivedListView.setVisible(true);
        approveRequestButton.setDisable(false);
        rejectRequestButton.setDisable(false);
        resubmissionRequestButton.setDisable(false);
    }

    @FXML
    public void showRequestsSentListView(){
        showRequestsSentButton.setStyle("-fx-font-weight: bold");
        showRequestsReceivedButton.setStyle("");
        requestsSentListView.setVisible(true);
        requestsReceivedListView.setVisible(false);
        approveRequestButton.setDisable(true);
        rejectRequestButton.setDisable(true);
        resubmissionRequestButton.setDisable(true);
    }

    private void handleFilterInFriendshipStatusController(){
        ListViewInitialize.handleFilter(networkController,rootPage, searchFriendshipField, modelSearchFriends);
    }

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
        usersListView.setVisible(true);
        triangleAuxiliaryLabel.setVisible(true);
    }

    @FXML
    public void setUsersListViewOnInvisible(){
        usersListView.setVisible(false);
        triangleAuxiliaryLabel.setVisible(false);
        searchFriendshipField.clear();
    }

    @FXML
    public void handleFriendshipRequestFromRequestsController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPage, networkController, displayStage);
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        addFriendshipButton.setDisable(true);
        rejectRequestButton.setDisable(false);
        approveRequestButton.setDisable(false);
        resubmissionRequestButton.setDisable(false);
        requestsSentListView.getSelectionModel().clearSelection();
    }

    @FXML
    public void disableWhenSearchForUser(){
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(false);
            rejectRequestButton.setDisable(true);
            approveRequestButton.setDisable(true);
            resubmissionRequestButton.setDisable(true);
        }
    }

    @FXML
    public void disableButtonsOnRequestSentListView(){
        if(requestsSentListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            rejectRequestButton.setDisable(true);
            approveRequestButton.setDisable(true);
            resubmissionRequestButton.setDisable(true);
        }
    }

    @FXML
    public void disableButtonsOnRequestReceivedListView(){
        if(requestsReceivedListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            rejectRequestButton.setDisable(false);
            approveRequestButton.setDisable(false);
            resubmissionRequestButton.setDisable(false);
        }
    }

}
