package com.psycho.airconsolemerger.view;

import com.psycho.airconsolemerger.Main;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class MainController {
    @FXML
    private TextField _screenSources;

    @FXML
    private TextField _controllerSources;

    @FXML
    private TextField _destinationFolder;

    @FXML
    private CheckBox _deleteIndexFileCheckBox;

    @FXML
    private CheckBox _zipResultCheckBox;

    @FXML
    private Button _selectScreenSourceButton;

    @FXML
    private Button _selectControllerSourceButton;

    @FXML
    private Button _selectDestinationButton;

    @FXML
    private Button _mergeButton;

    private Main _main;
    private Stage _stage;

    public MainController() {};

    @FXML
    private void onSelectScreenSources() {
        String path = showBrowseDialog("Please select the directory containing the screen", _screenSources);
        if (path != null) {
            writeAttribute("screen", path);
        }
    }

    @FXML
    private void onSelectControllerSources() {
        String path = showBrowseDialog("Please select the directory containing the controller", _controllerSources);
        if (path != null) {
            writeAttribute("controller", path);
        }
    }

    @FXML
    private void onSelectDestination() {
        String path = showBrowseDialog("Please select the destination directory", _destinationFolder);
        if (path != null) {
            writeAttribute("destination", path);
        }
    }

    @FXML
    private void onToggleDelete() {
        writeAttribute("deleteCheckBox", (_deleteIndexFileCheckBox.isSelected()) ? "1" : "0");
    }

    @FXML
    private void onToggleZip() {
        writeAttribute("zipCheckBox", (_zipResultCheckBox.isSelected()) ? "1" : "0");
    }

    @FXML
    private void onMerge() {
        Alert alert;
        File directory;
        File index;

        if (_screenSources.getText() != null) {
            directory = new File(_screenSources.getText());
            if (!directory.exists()) {
                displayError("Screen sources error","The screen sources directory does not exist","Please make sure you've selected a screen sources directory");
                return;
            }
            else {
                index = new File(_screenSources + File.pathSeparator + "index.html");
                if (!index.exists()) {
                    displayError("Screen sources error","The screen sources directory seem invalid, no index.html found","Please make sure you've selected a screen sources directory");
                    return;
                }
            }
        }
        else {
            displayError("Screen sources error","No selected screen sources directory","Please make sure you've selected a screen sources directory");
            return;
        }

        if (_controllerSources.getText() != null) {
            directory = new File(_controllerSources.getText());
            if (!directory.exists()) {
                displayError("Controller sources error","The controller directory sources does not exist","Please make sure you've selected a controller sources directory");
                return;
            }
            else {
                index = new File(_controllerSources + File.pathSeparator + "index.html");
                if (!index.exists()) {
                    displayError("Controller sources error","The controller sources directory seem invalid, no index.html found","Please make sure you've selected a controller sources directory");
                    return;
                }
            }
        }
        else {
            displayError("Controller sources error","No selected controller sources directory","Please make sure you've selected a controller sources directory");
            return;
        }

        if (_destinationFolder.getText() != null) {
            directory = new File(_destinationFolder.getText());
            if (directory.exists()) {
                try {
                    FileUtils.cleanDirectory(directory);
                }
                catch (IOException e) {
                    displayError("Destination error","The destination directory does not exist","Please make sure you've selected a destination directory");
                    return;
                }
            }
            else {
                displayError("Destination error","The destination directory does not exist","Please make sure you've selected a destination directory");
                return;
            }
        }
        else {
            displayError("Destination error","No selected destination directory","Please make sure you've selected a destination directory");
            return;
        }
    }

    private void displayError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setMain(Main main) {
        _main = main;
    }

    public void setStage(Stage stage) {
        _stage = stage;
    }

    public void init() {
        File directory;
        String attr = getAttribute("screen");

        if (attr != null) {
            directory = new File(attr);
            if (directory.exists()) {
                _screenSources.setText(attr);
            }
            else {
                deleteAttribute("screen");
            }
        }

        attr = getAttribute("controller");
        if (attr != null) {
            directory = new File(attr);
            if (directory.exists()) {
                _controllerSources.setText(attr);
            }
            else {
                deleteAttribute("controller");
            }
        }

        attr = getAttribute("destination");
        if (attr != null) {
            directory = new File(attr);
            if (directory.exists()) {
                _destinationFolder.setText(attr);
            }
            else {
                deleteAttribute("destination");
            }
        }

        attr = getAttribute("deleteCheckBox");
        if (attr != null) {
            _deleteIndexFileCheckBox.setSelected(attr.equalsIgnoreCase("1"));
        }

        attr = getAttribute("zipCheckBox");
        if (attr != null) {
            _zipResultCheckBox.setSelected(attr.equalsIgnoreCase("1"));
        }
    }

    private String showBrowseDialog(String title, TextField field) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(_stage);

        if (selectedDirectory != null) {
            field.setText(selectedDirectory.getAbsolutePath());
            return selectedDirectory.getAbsolutePath();
        }
        return null;
    }

    private void writeAttribute(String key, String value) {
        UserDefinedFileAttributeView view = getAttrView();
        try {
            view.write("user." + key, Charset.defaultCharset().encode(value));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAttribute(String var)
    {
        if (!attributeExists(var)) {
            return null;
        }

        try {
            String name = "user." + var;
            UserDefinedFileAttributeView view = getAttrView();

            ByteBuffer buf = null;
            buf = ByteBuffer.allocate(view.size(name));
            view.read(name, buf);
            buf.flip();

            return Charset.defaultCharset().decode(buf).toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Boolean attributeExists(String var)
    {
        String search = "user." + var;
        List<String> attributes = null;

        try {
            attributes = getAttrView().list();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return attributes.contains(search);
    }

    private void deleteAttribute(String var)
    {
        if (!attributeExists(var)) {
            return;
        }

        try {
            UserDefinedFileAttributeView view = getAttrView();
            view.delete("user." + var);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UserDefinedFileAttributeView getAttrView()
    {
        String path = null;
        try {
            path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path file = new File(path).toPath();

        return Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
    }
}
