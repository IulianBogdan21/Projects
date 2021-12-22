package socialNetwork.utilitaries;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.User;

import java.util.List;
import java.util.function.Predicate;

public class ListViewInitialize {

    public static void createListViewWithUser(ListView<User> listView, ObservableList<User> modelFriends){
        Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
        listView.setItems(modelFriends);
        listView.setCellFactory(u -> new ListCell<User>(){
            private ImageView imageView = new ImageView();
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }
                else{
                    imageView.setImage(genericUserImage);
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setPreserveRatio(true);
                    setText(item.getUsername());
                    setGraphic(imageView);
                }
            }
        });
    }

    public static void handleFilter(NetworkController networkController, User mainUser, TextField searchFriendshipField,
                                    ObservableList<User> modelSearchFriends){
        Predicate<User> nameOfUserPredicate = u -> u.getUsername()
                .startsWith(searchFriendshipField.getText());
        List<User> userListWithoutMainUser = networkController.getAllUsers()
                .stream()
                .filter(x -> !x.getId().equals(mainUser.getId()))
                .toList();
        modelSearchFriends.setAll(userListWithoutMainUser.stream()
                .filter(nameOfUserPredicate)
                .toList());
    }
}
