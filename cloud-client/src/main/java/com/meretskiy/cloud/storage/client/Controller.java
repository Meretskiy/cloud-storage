package com.meretskiy.cloud.storage.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FileUtils;
import com.meretskiy.cloud.storage.common.Command;
import com.meretskiy.cloud.storage.common.FileInfo;
import com.meretskiy.cloud.storage.common.Message;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class Controller implements Initializable {
    @FXML
    BorderPane storagePanel;

    @FXML
    TableView<FileInfo> clientFilesTable;
    @FXML
    TableView<FileInfo> serverFilesTable;

    @FXML
    TextField serverPathField;
    @FXML
    TextField clientPathField;

    private Label clientsFileLabel;
    private Label serversFileLabel;
    private Path filePath;

    private List<Callback> callbackList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            GUIHelper.serverFilesList = new ArrayList<>();
            callbackList = new ArrayList<>();
            Callback authOkCallback = this::showStoragePanel;
            Callback serverFilesRefreshCallback = this::refreshServerFilesTable;
            Callback clientsFilesRefreshCallback = this::btnRefreshClientFilesTable;
            callbackList.add(authOkCallback);
            callbackList.add(serverFilesRefreshCallback);
            callbackList.add(clientsFilesRefreshCallback);
            CountDownLatch networkStarter = new CountDownLatch(1);
            new Thread(() -> Network.getInstance().startNetwork(networkStarter, callbackList)).start();
            networkStarter.await();
        } catch (InterruptedException e) {
            GUIHelper.showError(e);
        }
    }

    public @FXML HBox authPanel;
    public @FXML TextField loginField;
    public @FXML PasswordField passField;
    public @FXML void sendAuth() {
        String login = loginField.getText();
        String password = passField.getText();
        GUIHelper.currentServerPath = Paths.get(login);
        GUIHelper.currentClientPath = Paths.get("client", loginField.getText());
        Message.authInfoMessage(Network.getInstance().getCurrentChannel(), login, password);
    }

    public @FXML void btnSendFile() {
       //TODO
    }
    public @FXML void btnClientFileDelete() {
        filePath = Paths.get(clientPathField.getText(), clientsFileLabel.getText());

        if ( deleteFileIfExist(filePath)) {
            btnRefreshClientFilesTable();
        } else {
            GUIHelper.showError(new RuntimeException ("Не удалось удалить файл"));
        }
    }

    public @FXML
    void btnDownloadFile() {
       //TODO
    }

    public @FXML void btnServerFileDelete() {
        //TODO
            }



    public void shutdown() {
        Network.getInstance().stop();
    }

    public void showStoragePanel() {
        //TODO
    }


    public void btnRefreshClientFilesTable() {
        GUIHelper.updateUI(() -> {
            try {
                clientPathField.setText(GUIHelper.currentClientPath.normalize().toAbsolutePath().toString());
                clientFilesTable.getItems().clear();
                clientFilesTable.getItems().addAll(Files.list(GUIHelper.currentClientPath).map(FileInfo::new).collect(Collectors.toList()));
                clientFilesTable.sort();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesTable() {
        GUIHelper.updateUI(() -> {
            serverPathField.setText(GUIHelper.currentServerPath.normalize().toString());
            serverFilesTable.getItems().clear();
            serverFilesTable.getItems().addAll(GUIHelper.serverFilesList);
        });
    }

    public void btnClientPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientPathField.getText()).getParent();
        if (upperPath != null) {
            GUIHelper.currentClientPath = upperPath;
            btnRefreshClientFilesTable();
        }
    }

    public void btnServerPathUpAction(ActionEvent actionEvent) {
       //TODO
    }


    private boolean deleteFileIfExist(Path delPath) {
        boolean result = true;
        try {
            if (Files.exists(delPath)) {
                if (Files.isDirectory(delPath)) {
                    FileUtils.deleteDirectory(new File(String.valueOf(delPath)));
                } else {
                    Files.delete(delPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }


}

