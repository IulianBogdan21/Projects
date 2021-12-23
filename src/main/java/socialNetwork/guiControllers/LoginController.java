package socialNetwork.guiControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Page;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.StageBuilder;
import socialNetwork.utilitaries.UnorderedPair;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    NetworkController networkController;

    @FXML
    Button loginButton;
    @FXML
    Button signupButton;
    @FXML
    TextField usernameField;
    @FXML
    PasswordField passwordField;

    Stage stage;

    public void setNetworkController(Stage primaryStage, NetworkController service){
        networkController = service;
        stage = primaryStage;
    }

    @FXML
    public void handleLogin(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            Page rootPage =  networkController.logIn(username, password);
            UnorderedPair<Stage,FXMLLoader> unorderedPair = StageBuilder.buildStage(
                    getClass(),
                    "/socialNetwork.gui/userView.fxml",
                    Optional.empty(),"Kage");
            Stage userViewStage = unorderedPair.left;
            FXMLLoader loader = unorderedPair.right;
            UserViewController userViewController = loader.getController();
            userViewController.setNetworkController(userViewStage,networkController,rootPage);
            userViewStage.show();
            stage.close();
        } catch (ExceptionBaseClass | IOException exception) {
            MessageAlert.showErrorMessage(stage, exception.getMessage());
        }
        finally {
            usernameField.clear();
            passwordField.clear();
        }
    }

    @FXML
    public void handleSignUp() throws IOException {
        UnorderedPair<Stage, FXMLLoader> unorderedPair = StageBuilder
                .buildStage(getClass(),"/socialNetwork.gui/signUp.fxml",
                        Optional.of("/css/signUp.css") ,"Kage");
        Stage signUpStage = unorderedPair.left;
        FXMLLoader loader = unorderedPair.right;
        SignUpController signUpController = loader.getController();
        signUpController.setNetworkController(signUpStage, networkController);
        signUpStage.show();
    }

}