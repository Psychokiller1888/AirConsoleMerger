package com.psycho.airconsolemerger.view;

import com.psycho.airconsolemerger.Main;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @FXML
    private Text _checkNewVersionLink;

    private Stage _stage;

    public MainController() {}

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
    private void onClickLink() {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/Psychokiller1888/AirConsoleMerger/releases").toURI());
        }
        catch (Exception e) {}
    }

    @FXML
    private void onMerge() {
        _mergeButton.setText("Working, please wait...");
        _mergeButton.setDisable(true);

        _selectScreenSourceButton.setDisable(true);
        _selectControllerSourceButton.setDisable(true);
        _selectDestinationButton.setDisable(true);

        File index;
        File screenSources;
        File controllerSources;

        if (_screenSources.getText() != null) {
            screenSources = new File(_screenSources.getText());
            if (!screenSources.exists()) {
                displayError("Screen sources error","The screen sources directory does not exist","Please make sure you've selected a screen sources directory");
                return;
            }
            else {
                index = new File(screenSources.getAbsolutePath() + File.separator + "index.html");
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
            controllerSources = new File(_controllerSources.getText());
            if (!controllerSources.exists()) {
                displayError("Controller sources error","The controller directory sources does not exist","Please make sure you've selected a controller sources directory");
                return;
            }
            else {
                index = new File(controllerSources.getAbsolutePath() + File.separator + "index.html");
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
            File destination = new File(_destinationFolder.getText());
            if (destination.exists()) {
                try {
                    FileUtils.cleanDirectory(destination);

                    try {
                        File screenDest = new File(destination.getAbsolutePath() + File.separator + "screen");
                        File controllerDest = new File(destination.getAbsolutePath() + File.separator + "controller");
                        FileUtils.forceMkdir(screenDest);
                        FileUtils.forceMkdir(controllerDest);

                        FileUtils.copyDirectory(screenSources, screenDest);
                        FileUtils.copyDirectory(controllerSources, controllerDest);

                        File screenIndex = new File(screenDest.getAbsolutePath() + File.separator + "index.html");
                        File controllerIndex = new File(controllerDest.getAbsolutePath() + File.separator + "index.html");

                        FileUtils.copyFile(screenIndex, new File(destination.getAbsolutePath() + File.separator + "/screen.html"));
                        FileUtils.copyFile(controllerIndex, new File(destination.getAbsolutePath() + File.separator + "/controller.html"));

                        List<String> screenLines = Files.readAllLines(Paths.get(destination.getAbsolutePath() + File.separator + "/screen.html"));
                        if (screenLines.size() < 4) {
                            displayError("Merge error","Screen index.html corrupted","The screen.html file seem corrupted, not enough lines");
                            return;
                        }
                        else {
                            screenLines.add(3, "\t<base href=\"screen/\">");
                            Files.write(Paths.get(destination.getAbsolutePath() + File.separator + "/screen.html"), screenLines, StandardCharsets.UTF_8);
                        }

                        List<String> controllerLines = Files.readAllLines(Paths.get(destination.getAbsolutePath() + File.separator + "/controller.html"));
                        if (controllerLines.size() < 4) {
                            displayError("Merge error","Controller index.html corrupted","The controller.html file seem corrupted, not enough lines");
                            return;
                        }
                        else {
                            controllerLines.add(3, "\t<base href=\"controller/\">");
                            Files.write(Paths.get(destination.getAbsolutePath() + File.separator + "/controller.html"), controllerLines, StandardCharsets.UTF_8);
                        }

                        if (_deleteIndexFileCheckBox.isSelected()) {
                            FileUtils.forceDelete(screenIndex);
                            FileUtils.forceDelete(controllerIndex);
                        }

                        if (_zipResultCheckBox.isSelected()) {
                            try {
                                pack(destination.getAbsolutePath(), destination.getAbsolutePath() + File.separator + "UploadMe.zip");
                            }
                            catch(IOException e) {
                                displayError("Merge error","Zipping error","Couldn't zip the project.\r\n\r\n" + e.getMessage());
                                return;
                            }
                        }

                        Alert popup = new Alert(Alert.AlertType.INFORMATION);
                        popup.setTitle("Done!");
                        popup.setHeaderText("Merging success");
                        popup.setContentText("Both projects have been merged in the destination folder");
                        popup.showAndWait();

                        _mergeButton.setText("Merge!");
                        _mergeButton.setDisable(false);

                        _selectScreenSourceButton.setDisable(false);
                        _selectControllerSourceButton.setDisable(false);
                        _selectDestinationButton.setDisable(false);
                    }
                    catch (IOException e) {
                        displayError("Merge error","Merging failed","Check you have permissions to write in the destination directory.\r\n\r\n" + e.getMessage());
                    }
                }
                catch (IOException e) {
                    displayError("Destination error","The destination directory does not exist","Please make sure you've selected a destination directory");
                }
            }
            else {
                displayError("Destination error","The destination directory does not exist","Please make sure you've selected a destination directory");
            }
        }
        else {
            displayError("Destination error","No selected destination directory","Please make sure you've selected a destination directory");
        }
    }

    private void displayError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();

        _mergeButton.setText("Merge!");
        _mergeButton.setDisable(false);

        _selectScreenSourceButton.setDisable(false);
        _selectControllerSourceButton.setDisable(false);
        _selectDestinationButton.setDisable(false);
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
            ByteBuffer buf;
            UserDefinedFileAttributeView view = getAttrView();
            if (view != null) {
                buf = ByteBuffer.allocate(view.size(name));
                view.read(name, buf);
                buf.flip();
                return Charset.defaultCharset().decode(buf).toString();
            }
            else {
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Boolean attributeExists(String var)
    {
        String search = "user." + var;
        List<String> attributes;

        try {
            attributes = getAttrView().list();
            return attributes.contains(search);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void deleteAttribute(String var)
    {
        if (!attributeExists(var)) {
            return;
        }

        try {
            UserDefinedFileAttributeView view = getAttrView();
            if (view != null) {
                view.delete("user." + var);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UserDefinedFileAttributeView getAttrView()
    {
        String path;
        try {
            path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            Path file = new File(path).toPath();
            return Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        if (!path.toString().endsWith("UploadMe.zip")) {
                            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                            try {
                                zs.putNextEntry(zipEntry);
                                Files.copy(path, zs);
                                zs.closeEntry();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }
}
