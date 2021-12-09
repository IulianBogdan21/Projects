module socialNetwork.ui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens socialNetwork.ui to javafx.fxml;
    exports socialNetwork.ui;
    exports socialNetwork.gui;
    opens socialNetwork.gui to javafx.fxml;
    exports socialNetwork.guiControllers;
    opens socialNetwork.guiControllers to javafx.fxml;

}