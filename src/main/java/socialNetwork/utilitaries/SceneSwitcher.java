package socialNetwork.utilitaries;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.PageUser;
import socialNetwork.guiControllers.FriendshipStatusController;
import socialNetwork.guiControllers.MessageController;
import socialNetwork.guiControllers.UserViewController;

import java.io.IOException;

public class SceneSwitcher {

    private static void defaultSceneOperationForUserViewSwitch(FXMLLoader loader, ActionEvent event, Class sourceClass,
                                                               NetworkController networkController,
                                                               PageUser rootPageUser, Stage displayStage) throws IOException {
        Parent root = loader.load();
        displayStage =  (Stage)(((Node)event.getSource()).getScene().getWindow());
        displayStage.setScene(new Scene(root));
        UserViewController userViewController = loader.getController();
        userViewController.setNetworkController(displayStage,networkController, rootPageUser);
        displayStage.show();
    }

    private static void defaultSceneOperationForFriendshipRequestSwitch(FXMLLoader loader, ActionEvent event, Class sourceClass,
                                                                        NetworkController networkController,
                                                                        PageUser rootPageUser, Stage displayStage) throws IOException {
        Parent root = loader.load();
        displayStage =  (Stage)(((Node)event.getSource()).getScene().getWindow());
        displayStage.setScene(new Scene(root));
        FriendshipStatusController friendshipStatusController = loader.getController();
        friendshipStatusController.setNetworkController(displayStage,networkController, rootPageUser);
        displayStage.show();
    }

    private static void defaultSceneOperationForMessageSwitch(FXMLLoader loader, ActionEvent event, Class sourceClass,
                                                              NetworkController networkController,
                                                              PageUser rootPageUser, Stage displayStage) throws IOException {
        Parent root = loader.load();
        displayStage =  (Stage)(((Node)event.getSource()).getScene().getWindow());
        Scene newScene = new Scene(root);
        newScene.getStylesheets().add(sourceClass.getResource("/css/chatNameListView.css").toExternalForm());
        displayStage.setScene(newScene);
        MessageController messageController = loader.getController();
        messageController.setNetworkController(displayStage,networkController, rootPageUser);
        displayStage.show();
    }

    public static void switchToUserViewScene(ActionEvent event, Class sourceClass, NetworkController networkController,
                                             PageUser rootPageUser, Stage displayStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(sourceClass.getResource("/socialNetwork.gui/userView.fxml"));
        defaultSceneOperationForUserViewSwitch(loader, event, sourceClass, networkController, rootPageUser, displayStage);
    }

    public static void switchToFriendshipRequestScene(ActionEvent event, Class sourceClass,
                                                      NetworkController networkController, PageUser rootPageUser,
                                                      Stage displayStage) throws IOException{
        FXMLLoader loader = new FXMLLoader(sourceClass.getResource("/socialNetwork.gui/friendshipStatusView.fxml"));
        defaultSceneOperationForFriendshipRequestSwitch(loader, event, sourceClass, networkController, rootPageUser, displayStage);
    }

    public static void switchToMessageScene(ActionEvent event, Class sourceClass,
                                                      NetworkController networkController, PageUser rootPageUser,
                                                      Stage displayStage) throws IOException{
        FXMLLoader loader = new FXMLLoader(sourceClass.getResource("/socialNetwork.gui/messagesView.fxml"));
        defaultSceneOperationForMessageSwitch(loader, event, sourceClass, networkController, rootPageUser, displayStage);
    }

}
