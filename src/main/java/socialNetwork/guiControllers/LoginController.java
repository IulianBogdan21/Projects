package socialNetwork.guiControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.exceptions.LogInException;
import socialNetwork.repository.database.MessageDTODatabaseRepository;

import java.io.IOException;

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
            networkController.logIn(username, password);
            MessageAlert.showMessage(stage, Alert.AlertType.INFORMATION, "Successful login",
                    "You have been successfully logged in");
        } catch (ExceptionBaseClass exception) {
            MessageAlert.showErrorMessage(stage, exception.getMessage());
        }
        finally {
            usernameField.clear();
            passwordField.clear();
        }
    }

    @FXML
    public void handleSignUp() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/socialNetwork.gui/signUp.fxml"));
        AnchorPane root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle("Sign up");
        newStage.initModality(Modality.WINDOW_MODAL);
        Scene newScene = new Scene(root);
        newStage.setScene(newScene);
        SignUpController signUpController = loader.getController();
        signUpController.setNetworkController(newStage, networkController);
        newStage.show();
    }
}