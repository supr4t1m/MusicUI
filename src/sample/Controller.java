package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import sample.model.Album;
import sample.model.Artist;
import sample.model.Datasource;

import java.io.IOException;
import java.util.Optional;

public class Controller {
    @FXML
    private TableView artistTable;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private BorderPane mainPane;

    @FXML
    public void listArtists() {
        Task<ObservableList<Artist>> task = new getAllArtistTask();
        new Thread(task).start();
        /*Through bind tableview columns are populated by artists fields and
        * multiple columns are populated the by the list the tasks value will
        * return*/
        progressBar.setVisible(true);
        artistTable.itemsProperty().bind(task.valueProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> progressBar.setVisible(false));
        task.setOnFailed(e -> progressBar.setVisible(false));
    }

    @FXML
    public void listAlbumsForArtist() {
        final Artist artist = (Artist) artistTable.getSelectionModel().getSelectedItem();
        if (artist == null) {
            System.out.println("No artist selected");
            return;
        }
        Task<ObservableList<Album>> task = new Task<>() {
            @Override
            protected ObservableList<Album> call() throws Exception {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().queryAlbumsForArtistId(artist.getId())
                );
            }
        };
        new Thread(task).start();
        artistTable.itemsProperty().bind(task.valueProperty());
    }

    @FXML
    public void updateArtist() {
        // implement a dialog pane to show for update artist name
//        final Artist artist = (Artist) artistTable.getItems().get(2);
        final Artist artist = (Artist) artistTable.getSelectionModel().getSelectedItem();
        if (artist == null) {
            System.out.println("No artist selected");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Artist Name");
        dialog.initOwner(mainPane.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("updateDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load dialog: " + e.getMessage());
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogController dialogController = loader.getController();
        dialogController.fillName(artist.getName());

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return Datasource.getInstance().updateArtistName(artist.getId(), dialogController.retreiveName());
            }
        };

        task.setOnSucceeded(e -> {
            /*if the tasks successfully updated the artist name, then we proceed to update the UI*/
            if (task.valueProperty().get()) {
                artist.setName(dialogController.retreiveName());
                artistTable.refresh();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(task).start();
        } else {
            return;
        }
    }
}

class getAllArtistTask extends Task<ObservableList<Artist>> {
    @Override
    public ObservableList<Artist> call() throws Exception {
        /*A delay to visualize the progressBar */
        for (int i=0; i<5; i++) {
            Thread.sleep(500);
            updateProgress(i+1, 5);
        }
        return FXCollections.observableArrayList(
                Datasource.getInstance().queryArtists(Datasource.ORDER_BY_ASC)
        );
    }
}
