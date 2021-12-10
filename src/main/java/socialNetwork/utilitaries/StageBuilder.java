package socialNetwork.utilitaries;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;

import java.io.IOException;

public class StageBuilder {
    public static UnorderedPair<Stage, FXMLLoader> buildStage(Class sourceClass , String path, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(sourceClass.getResource(path));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        Scene newScene = new Scene(root);
        stage.setScene(newScene);
        return new UnorderedPair<>(stage,loader);
    }
}
