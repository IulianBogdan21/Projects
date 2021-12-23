package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.MessageChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.List;

public class MessageController implements Observer<Event> {
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<Chat> modelChatsName = FXCollections.observableArrayList();
    Chat chatConversation = null;

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
    @FXML
    TextField messageField;
    @FXML
    FontAwesomeIconView sendMessageIcon;
    @FXML
    ScrollPane conversationScrollPane;
    @FXML
    VBox conversationVerticalBox;
    @FXML
    Label welcomeMessageLabel;
    @FXML
    AnchorPane conversationAnchorPane;

    NetworkController networkController;
    Page rootPage;
    Stage displayStage;
    boolean firstTime = true;

    public void setNetworkController(Stage primaryStage, NetworkController service,Page rootPage){
        this.networkController = service;
        networkController.getMessageService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        rootPage.refresh(rootPage.getRoot().getUsername());
        usernameLabelChat.setText(rootPage.getRoot().getUsername());
        ListViewInitialize.createListViewWithChats(chatsNameListView,modelChatsName,rootPage.getRoot());
        initModelChatsName();
    }

    private void initModelChatsName(){
        modelChatsName.setAll(rootPage.getChatList());
    }

    private void updateChatWithTheLatestMessages(){

    }

    @FXML
    public void initialize(){
        conversationScrollPane.setVisible(false);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        ListViewInitialize.createListViewWithUser(startConversationListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
        searchUserToStartConversationField.textProperty().addListener(o -> handleFilterSearchUserForNewConversation());
        messageField.textProperty().addListener(o -> handleMessageIcon());

        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    sendMessage();
                }
            }
        });
    }

    @Override
    public void update(Event event) {
        if(event instanceof MessageChangeEvent)
            initModelChatsName();
    }

    @FXML
    public void loadConversation(){
        if(firstTime) {
            conversationScrollPane.setVisible(true);
            welcomeMessageLabel.setVisible(false);
            messageField.setVisible(true);
            firstTime = false;
        }
        conversationVerticalBox.getChildren().clear();
        chatConversation = chatsNameListView.getSelectionModel().getSelectedItem();
        conversationVerticalBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                conversationScrollPane.setVvalue((Double) newValue);
            }
        });
        User root = rootPage.getRoot();
        List<Message> chatMessages = chatConversation.getMessageList();
        List<ReplyMessage> chatReplyMessages = chatConversation.getReplyMessageList();
        int i = 0, j = 0;
        int n = chatMessages.size();
        int m = chatReplyMessages.size();
        while(i < n && j < m){
            if(chatMessages.get(i).getDate().compareTo(chatReplyMessages.get(j).getDate()) < 0){
                Message message = chatMessages.get(i);
                if(message.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",message.getText());
                }
                else{
                    putMessageInScrollPane("receive",message.getText());
                }
                i++;
            }
            else{
                ReplyMessage replyMessage = chatReplyMessages.get(j);
                if(replyMessage.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",replyMessage.getText());
                }
                else{
                    putMessageInScrollPane("receive",replyMessage.getText());
                }
                j++;
            }
        }

        while(i < n){
            Message message = chatMessages.get(i);
            if(message.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",message.getText());
            }
            else{
                putMessageInScrollPane("receive",message.getText());
            }
            i++;
        }

        while(j < m){
            ReplyMessage replyMessage = chatReplyMessages.get(j);
            if(replyMessage.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",replyMessage.getText());
            }
            else{
                putMessageInScrollPane("receive",replyMessage.getText());
            }
            j++;
        }
    }

    private void putMessageInScrollPane(String action, String messageText){
        HBox hBox = new HBox();
        if(action.equals("sent")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        hBox.setPadding(new Insets(5,5,5,10));
        Text text = new Text(messageText);
        TextFlow textFlow = new TextFlow(text);
        if(action.equals("sent")) {
            textFlow.setStyle("-fx-color: rgb(239,242,255);" +
                    "-fx-background-color: rgb(15,125,242);" +
                    "-fx-background-radius: 20px"
                    );
        }
        else{
            textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                    "-fx-background-radius: 20px"
            );
        }
        textFlow.setPadding(new Insets(5,10,5,10));
        if(action.equals("sent")){
            text.setFill(Color.color(0.934, 0.945, 0.996));
        }
        hBox.getChildren().add(textFlow);
        conversationVerticalBox.getChildren().add(hBox);
    }

    @FXML
    public void sendMessage(){
        String text = messageField.getText();
        Long idUserFrom = rootPage.getRoot().getId();
        List<Long> to = idMembersWithoutRootForChat(chatConversation, rootPage.getRoot());
        networkController.sendMessages(idUserFrom,to,text);
        //conversationVerticalBox Is the same with the selected chat
        putMessageInScrollPane("sent",text);
        messageField.clear();
    }

    private List<Long> idMembersWithoutRootForChat(Chat chat,User root){
        return chat.getMembers()
                .stream()
                .filter(user -> !user.getId().equals(root.getId()))
                .map(user -> user.getId())
                .toList();
    }

    private void handleMessageIcon(){
        if(messageField.getText().equals(""))
            sendMessageIcon.setVisible(false);
        else
            sendMessageIcon.setVisible(true);
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
        conversationAnchorPane.setEffect(null);
        conversationAnchorPane.setDisable(false);
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
        conversationAnchorPane.setEffect(lighting);
        conversationAnchorPane.setDisable(true);
    }

}
