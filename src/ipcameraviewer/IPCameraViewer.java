package ipcameraviewer;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class IPCameraViewer extends Application {

    private Button buttonAddCamera;
    private Button buttonDeleteCamera;
    private ContextMenu contextMenu;
    private TextField name;
    private TextField ip_address;
    private List<Camera> camerasList;
    private ListView<Camera> listView;
    private Image imageOn;
    private Image imageOff;
    private Camera selectedCam;

    @Override
    public void start(Stage primaryStage) {
        camerasList = new ArrayList<>();

        String appMain = System.getProperty("user.dir");
        imageOn = new Image("file:" + appMain + "/src/images/on.png");
        imageOff = new Image("file:" + appMain + "/src/images/off.png");

        buttonAddCamera = new Button();
        buttonAddCamera.setText("Dodaj kamerę");
        buttonAddCamera.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addCamera();
            }
        });
        buttonAddCamera.setLayoutX(380);
        buttonAddCamera.setLayoutY(10);

        buttonDeleteCamera = new Button();
        buttonDeleteCamera.setText("Usuń kamerę");
        buttonDeleteCamera.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteCamera();
            }
        });
        buttonDeleteCamera.setLayoutX(475);
        buttonDeleteCamera.setLayoutY(10);

        name = new TextField();
        name.setPromptText("Podaj nazwę kamery");
        name.setLayoutX(60);
        name.setLayoutY(10);

        ip_address = new TextField();
        ip_address.setPromptText("Podaj adres IP kamery");
        ip_address.setLayoutX(220);
        ip_address.setLayoutY(10);

        listView = new ListView<>();
        listView.getItems().addAll(camerasList);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setLayoutX(10);
        listView.setLayoutY(50);
        listView.setPrefWidth(620);
        listView.setPrefHeight(260);
        listView.setCellFactory(new Callback<ListView<Camera>, ListCell<Camera>>() {
            @Override
            public ListCell<Camera> call(ListView<Camera> listView) {
                return new CustomListCell();
            }
        });
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2 && click.getButton() == MouseButton.PRIMARY) {
                    selectedCam = listView.getSelectionModel().getSelectedItem();
                    if (selectedCam.isState()) {
                        try {
                            showView(selectedCam.getName(), selectedCam.getIp_address());
                        } catch (FrameGrabber.Exception ex) {
                            Logger.getLogger(IPCameraViewer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setTitle("Status");
                        alert.setHeaderText(null);
                        alert.setContentText("Ta kamera jest offline");
                        alert.showAndWait();
                    }

                }
                if (click.getButton() == MouseButton.SECONDARY) {
                    selectedCam = listView.getSelectionModel().getSelectedItem();
                    if (checkConnection(selectedCam.getIp_address())) {
                        listView.getSelectionModel().getSelectedItem().setState(true);
                    } else {
                        listView.getSelectionModel().getSelectedItem().setState(false);
                    }
                    listView.refresh();
                }
            }
        });

        
        Pane root = new Pane();
        root.getChildren().add(buttonAddCamera);
        root.getChildren().add(name);
        root.getChildren().add(ip_address);
        root.getChildren().add(buttonDeleteCamera);
        root.getChildren().add(listView);

        Scene scene = new Scene(root, 640, 320);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("IPCameraViewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public boolean showView(String name, String ip_address) throws FrameGrabber.Exception {
        String url = "http://" + ip_address + ":81/videostream.cgi?user=user&pwd=user&dummy=param.mjpg";
        OpenCVFrameGrabber frameGrabber = null;
        try {
            frameGrabber = new OpenCVFrameGrabber(url);
            frameGrabber.setFormat("mjpeg");
            frameGrabber.start();
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(name);
            alert.setHeaderText(null);
            alert.setContentText("Nie udalo sie ustanowic polaczenia");
            alert.showAndWait();
            return false;
        }

        opencv_core.IplImage iPimg = frameGrabber.grab();
        CanvasFrame canvasFrame = new CanvasFrame(name);
        canvasFrame.setCanvasSize(iPimg.width(), iPimg.height());

        while (canvasFrame.isVisible() && (iPimg = frameGrabber.grab()) != null) {
            canvasFrame.showImage(iPimg);
        }
        frameGrabber.stop();
        canvasFrame.dispose();
        return true;
    }

    public void addCamera() {
        String nameText = name.getText();
        String ip_addressText = ip_address.getText();
        if (nameText.length() > 0 && ip_addressText.length() > 0) {
            Camera camera = new Camera(nameText, ip_addressText, checkConnection(ip_addressText));
            camerasList.add(camera);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Status");
            alert.setHeaderText(null);
            alert.setContentText("Dodano nową kamerę.");
            alert.showAndWait();
            name.setText("");
            ip_address.setText("");
            listView.getItems().add(camera);
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Dodawanie kamery");
            alert.setHeaderText(null);
            alert.setContentText("Musisz uzupelnic wszystkie dane!");
            alert.showAndWait();
        }
    }

    public void deleteCamera() {
        camerasList.remove(listView.getSelectionModel().getSelectedItem());
        listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
    }

    public boolean checkConnection(String ip_address) {
        boolean reachable = false;
        try {
            InetAddress address = InetAddress.getByName(ip_address);
            reachable = address.isReachable(20);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return reachable;
    }
    
    private class CustomListCell extends ListCell<Camera> {

        private HBox content;
        private Text name;
        private Text ip_address;
        private ImageView imageState;
        private HBox hBox;
        private VBox vBox;

        public CustomListCell() {
            super();
            name = new Text();
            ip_address = new Text();
            imageState = new ImageView(imageOff);
            vBox = new VBox(name, ip_address);
            hBox = new HBox(imageState, vBox);
            hBox.setAlignment(Pos.CENTER_LEFT);
            content = hBox;
            content.setSpacing(10);
        }

        @Override
        public void updateItem(Camera item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) { // <== test for null item and empty parameter
                name.setText(item.getName());
                ip_address.setText(item.getIp_address());
                vBox = new VBox(name, ip_address);
                if (item.isState()) {
                    imageState = new ImageView(imageOn);
                    hBox = new HBox(imageState, vBox);
                } else {
                    imageState = new ImageView(imageOff);
                    hBox = new HBox(imageState, vBox);
                }
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setSpacing(10);
                setGraphic(hBox);
            } else {
                setGraphic(null);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
