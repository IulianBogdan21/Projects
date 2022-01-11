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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
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
import socialNetwork.utilitaries.events.MessageChangeEventType;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
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
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
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
    PageUser rootPageUser;
    Stage displayStage;
    boolean firstTime = true;
    Long idUserLastMessage = -1L;

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPageUser){
        System.out.println("Hai cu bubuseala " + service);
        this.networkController = service;
        networkController.getMessageService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPageUser = rootPageUser;
        rootPageUser.refresh(rootPageUser.getRoot().getUsername());
        usernameLabelChat.setText(rootPageUser.getRoot().getUsername());
        ListViewInitialize.createListViewWithChats(chatsNameListView,modelChatsName, rootPageUser.getRoot());
        initModelChatsName();
    }

    private void initModelChatsName(){
        modelChatsName.setAll(rootPageUser.getChatList());
    }

    @FXML
    public void initialize(){
        conversationScrollPane.setVisible(false);
        startConversationListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

    private void updateActualChatWithMessages(Event event){
//        MessageChangeEvent messageChangeEvent = (MessageChangeEvent) event;
//        Message message = messageChangeEvent.getData().getMainMessage();
//        MessageChangeEventType typeOfMessage = messageChangeEvent.getType();
//
//        if( typeOfMessage.equals(MessageChangeEventType.SEND) ) {
//            User user = message.getFrom();
//            if(  )
//            putMessageInScrollPane("sent", message);
//        }
        System.out.println(rootPageUser.getRoot());
        loadConversationForSpecificSchat();
    }

    @Override
    public void update(Event event) {
        if(event instanceof MessageChangeEvent)
             updateActualChatWithMessages(event);
    }

    private void loadConversationForSpecificSchat(){
        //conversationVerticalBox.getChildren().clear();
        System.out.println("Cine vine pe aixi sa mi spuna: " + rootPageUser.getRoot() );
        conversationVerticalBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                conversationScrollPane.setVvalue((Double) newValue);
            }
        });
        User root = rootPageUser.getRoot();
        List<Message> chatMessages = chatConversation.getMessageList();
        List<ReplyMessage> chatReplyMessages = chatConversation.getReplyMessageList();
        //chatMessages.forEach(c -> System.out.println(c.getId() + " " + c.getText()));
        //chatReplyMessages.forEach(c -> System.out.println(c.getId() + " " + c.getText()));
        int i = 0, j = 0;
        int n = chatMessages.size();
        int m = chatReplyMessages.size();
        while(i < n && j < m){
            if(chatMessages.get(i).getDate().compareTo(chatReplyMessages.get(j).getDate()) < 0){
                Message message = chatMessages.get(i);
                if(message.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",message);
                }
                else{
                    putMessageInScrollPane("receive",message);
                }
                i++;
            }
            else{
                ReplyMessage replyMessage = chatReplyMessages.get(j);
                if(replyMessage.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",replyMessage);
                }
                else{
                    putMessageInScrollPane("receive",replyMessage);
                }
                j++;
            }
        }

        while(i < n){
            Message message = chatMessages.get(i);
            if(message.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",message);
            }
            else{
                putMessageInScrollPane("receive",message);
            }
            i++;
        }

        while(j < m){
            ReplyMessage replyMessage = chatReplyMessages.get(j);
            if(replyMessage.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",replyMessage);
            }
            else{
                putMessageInScrollPane("receive",replyMessage);
            }
            j++;
        }
    }

    @FXML
    public void loadConversation(){
        if(chatsNameListView.getSelectionModel().getSelectedItem() == null)
            return;
        if(firstTime) {
            conversationScrollPane.setVisible(true);
            welcomeMessageLabel.setVisible(false);
            messageField.setVisible(true);
            firstTime = false;
        }
        idUserLastMessage = -1L;
        conversationVerticalBox.getChildren().clear();
        chatConversation = chatsNameListView.getSelectionModel().getSelectedItem();
        loadConversationForSpecificSchat();
    }

    private void putMessageInScrollPane(String action, Message message){
        String messageText = message.getText();
        HBox hBox = new HBox();
      //  hBox.setId(message.getId().toString());
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

        User userThatSendTheLastMessage = message.getFrom();
        if(!userThatSendTheLastMessage.getId().equals(idUserLastMessage)){
            idUserLastMessage = userThatSendTheLastMessage.getId();
            HBox hBoxLabelNameUser = createLabelForUserThatWriteMessages(action,userThatSendTheLastMessage);
            conversationVerticalBox.getChildren().add(hBoxLabelNameUser);
        }

        if(message instanceof ReplyMessage){
            ReplyMessage replyMessage = (ReplyMessage) message;
            HBox hBoxReplyMessage = createReplyMessageForShowGUI(action,
                    replyMessage.getMessage().getText());
            conversationVerticalBox.getChildren().add(hBoxReplyMessage);
        }

        hBox.getChildren().add(textFlow);
        conversationVerticalBox.getChildren().add(hBox);
    }

    private HBox createReplyMessageForShowGUI(String action,String messageText){
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
        textFlow.setPadding(new Insets(5,10,5,10));
        textFlow.setStyle("-fx-background-color: #948c8c;" +
                "-fx-background-radius: 20px"
        );
        hBox.getChildren().add(textFlow);
        return hBox;
    }

    private HBox createLabelForUserThatWriteMessages(String action,User userThatSendTheLastMessage){
        HBox hBox = new HBox();
        Label label = new Label();
        label.setText(userThatSendTheLastMessage.getFirstName() + " "
                + userThatSendTheLastMessage.getLastName());
        label.setFont(new Font("Arial",10));
        label.setTextFill(Color.web("#9c9999"));
        hBox.getChildren().add(label);
        if(action.equals("sent")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        hBox.setPadding(new Insets(5,5,5,10));
        return hBox;
    }

    @FXML
    public void sendMessage(){
        String text = messageField.getText();
        Long idUserFrom = rootPageUser.getRoot().getId();
        List<User> to = idMembersWithoutRootForChat(chatConversation, rootPageUser.getRoot());
        List<Long> idTo = to.stream().map(user -> user.getId()).toList();


        networkController.sendMessages(idUserFrom,idTo,text);
        // ??? se pierde id-ul la mesaj
        Message message = new Message(rootPageUser.getRoot(), to , text);
        //conversationVerticalBox is the same with the selected chat
        putMessageInScrollPane("sent",message);
        messageField.clear();
    }

    private List<User> idMembersWithoutRootForChat(Chat chat,User root){
        return chat.getMembers()
                .stream()
                .filter(user -> !user.getId().equals(root.getId()))
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
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPageUser, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromMessageScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromMessageScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToMessagesSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToReportsSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToReportsScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        usersListView.getSelectionModel().clearSelection();

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
        ListViewInitialize.handleFilter(networkController, rootPageUser, searchFriendshipField, modelSearchFriends);
    }

    private void handleFilterSearchUserForNewConversation(){
        ListViewInitialize.handleFilter(networkController, rootPageUser, searchUserToStartConversationField, modelSearchFriends);
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
        if(startConversationListView.getSelectionModel().getSelectedItems()
                .equals( FXCollections.observableArrayList() )){
            MessageAlert.showErrorMessage(displayStage, "You have not selected anyone to start a conversation with!");
            closeStartConversationWindow();
            return;
        }
        List<User> members = new ArrayList<>( startConversationListView.getSelectionModel()
                        .getSelectedItems()
                        .stream()
                        .toList() );
        System.out.println(members);
        closeStartConversationWindow();
        members.add(rootPageUser.getRoot());
        if(checkIfChatExists(members, rootPageUser.getChatList()))
            return;
        Chat temporaryChat = new Chat(members,new ArrayList<Message>(), new ArrayList<ReplyMessage>());
        modelChatsName.add(temporaryChat);
    }

    private boolean checkIfChatExists(List<User> members, List<Chat> allChats){
        for(Chat chat: allChats){
            boolean checkIfCurrentChatExists = true;
            if(chat.getMembers().size() != members.size())
                continue;
            for (User member : members) {
                if (!chat.getMembers().contains(member)) {
                    checkIfCurrentChatExists = false;
                    break;
                }
            }
            if(checkIfCurrentChatExists)
                return true;
        }
        return false;
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
