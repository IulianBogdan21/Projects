package socialNetwork.utilitaries;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Chat;
import socialNetwork.domain.models.Message;
import socialNetwork.domain.models.Page;
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

    public static void createListViewWithMessages(ListView<Message> listView, ObservableList<Message> modelMessages){
        listView.setItems(modelMessages);
        listView.setCellFactory(u -> new ListCell<Message>(){
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(item.getText());
                }
            }
        });
    }

    public static void createComboBoxWithFriends(ComboBox<User> comboBox, ObservableList<User> modelFriends){
        comboBox.setItems(modelFriends);
        comboBox.setCellFactory(c -> new ListCell<User>(){
            @Override
            protected void updateItem(User item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(item.getUsername());
                }
            }
        });
    }

    public static void handleFilter(NetworkController networkController, Page rootPage, TextField searchFriendshipField,
                                    ObservableList<User> modelSearchFriends){
        User mainUser = rootPage.getRoot();
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

    public static void createListViewWithChats(ListView<Chat> listView, ObservableList<Chat> modelChats,User root){
        Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
        listView.setItems(modelChats);
        listView.setCellFactory(u -> new ListCell<Chat>(){
            private ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Chat item, boolean empty) {
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

                    if(item.getMembers().size() == 2){
                        User otherGuy = item.getMembers().get(0);
                        if(otherGuy.getId().equals(root.getId()))
                            otherGuy = item.getMembers().get(1);
                        setText(otherGuy.getUsername());
                    }else{
                        String nameAllMembers = item.getMembers()
                                .stream()
                                .filter(user -> !user.getId().equals(root.getId()))
                                .map(user -> user.getUsername())
                                .reduce("",(x,y) -> x + ", " + y);
                        String editNameAllMembers = nameAllMembers.substring(2);
                        setText(editNameAllMembers);
                    }
                    setGraphic(imageView);
                }
            }
        });
    }
}
