package rotacsufbo.view;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import rotacsufbo.FileExplorer;
import rotacsufbo.Main;
import rotacsufbo.Obfuscator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StartLayoutController implements Initializable{
    private File sourceFileOrDirectory;
    private File destinationFileOrDirectory;
    private File decryptor;
    private File decryptorLoc;

    @FXML
    private Label srcFileLabel;

    @FXML
    private Button sourceFilePickerBtn;

    @FXML
    private Label dstFileLabel;

    @FXML
    private Button destinationFilePickerBtn;

    @FXML
    private Button nextStepBtn;

    @FXML
    private Button decryptorFilePickerBtn;


    @FXML
    private Button decryptorLocationFileBtn;


    @FXML
    public void selectSourceFile() {
        DirectoryChooser dc = new DirectoryChooser();
        sourceFileOrDirectory = dc.showDialog(new Stage());
        if (sourceFileOrDirectory != null) {
            srcFileLabel.setText(sourceFileOrDirectory.getPath());
            if (destinationFileOrDirectory != null) {
                nextStepBtn.setDisable(false);
            }
        } else {
            nextStepBtn.setDisable(true);
        }

    }

    public void selectDestinationFile() {
        DirectoryChooser dc = new DirectoryChooser();
        destinationFileOrDirectory = dc.showDialog(new Stage());
        if (destinationFileOrDirectory != null) {
            dstFileLabel.setText(destinationFileOrDirectory.getPath());
            if (sourceFileOrDirectory != null) {
                nextStepBtn.setDisable(false);
            }
        } else {
            nextStepBtn.setDisable(true);
        }
    }

    @FXML
    void selectDecryptorFile() {
        FileChooser fc = new FileChooser();
        decryptor = fc.showOpenDialog(new Stage());
    }

    @FXML
    void selectDecryptorLocationFile() {
        DirectoryChooser dc = new DirectoryChooser();
        decryptorLoc = dc.showDialog(new Stage());
    }




    @FXML
    public void confirmFiles() {
        if (sourceFileOrDirectory != null && destinationFileOrDirectory != null && decryptor != null && decryptorLoc != null) {
                try {

                    // Copy decryptor into where it belongs in original source
                    FileUtils.copyFileToDirectory(decryptor, decryptorLoc);

                    // Copy the whole source directory to new place where we overwrite
                    FileUtils.copyDirectory(sourceFileOrDirectory, destinationFileOrDirectory);



                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("view/FilesLayout.fxml"));
                    BorderPane filesPane = (BorderPane)loader.load();
                    FilesLayoutController controller = loader.<FilesLayoutController>getController();
                    controller.initialize(destinationFileOrDirectory, decryptor, decryptorLoc); //where we want to write to
                    BorderPane root = Main.getRoot();
                    root.setCenter(filesPane);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nextStepBtn.setDisable(true);
    }

}
