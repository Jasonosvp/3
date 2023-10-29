package invaders.engine;

import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

import invaders.ConfigReader;
import invaders.entities.EntityViewImpl;
import invaders.entities.SpaceBackground;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import invaders.entities.EntityView;
import invaders.rendering.Renderable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.json.simple.JSONObject;

public class GameWindow {
    private final int width;
    private final int height;
    private Scene scene;
    private Pane pane;
    private GameEngine model;
    private List<EntityView> entityViews =  new ArrayList<EntityView>();
    private Renderable background;

    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    // private static final double VIEWPORT_MARGIN = 280.0;

    private final ComboBox<String> difficultyComboBox;

    private Label scoreLabel;
    private Label timeLabel;
    private double startTime;


    public GameWindow(GameEngine model){
        this.model = model;
        this.width =  model.getGameWidth();
        this.height = model.getGameHeight();

        pane = new Pane();
        scene = new Scene(pane, width, height);
        this.background = new SpaceBackground(model, pane);

        this.difficultyComboBox = new ComboBox<>(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficultyComboBox.setFocusTraversable(false);
        difficultyComboBox.setValue("Easy");
        difficultyComboBox.setOnAction(event -> changeDifficulty());

        Button Savebutton = new Button("Save");
        Button Restorebutton = new Button("Restore");

        Savebutton.setOnAction(e -> model.saveFunction());
        Restorebutton.setOnAction(e -> model.restoreFunction());
        Savebutton.setFocusTraversable(false);
        Restorebutton.setFocusTraversable(false);


        this.scoreLabel = new Label("Score: "+ model.getTotalScore());
        scoreLabel.setStyle("-fx-text-fill: white;");

        this.timeLabel = new Label("Time: 0.00s");
        timeLabel.setStyle("-fx-text-fill: white;");

        HBox controlsBox = new HBox();
        controlsBox.getChildren().addAll(difficultyComboBox, Savebutton,Restorebutton,scoreLabel, timeLabel);

        VBox vBox = new VBox();
        vBox.setPickOnBounds(false);
        vBox.getChildren().addAll(controlsBox, pane);
        vBox.setStyle("-fx-background-color: black;");


        this.startTime = System.currentTimeMillis();

        scene = new Scene(vBox, width, height);

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(this.model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);
        pane.requestFocus();


    }

    public void run() {

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17), t -> this.draw()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void draw(){
        model.update();

        List<Renderable> renderables = model.getRenderables();
        for (Renderable entity : renderables) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (Renderable entity : renderables){
            if (!entity.isAlive()){
                for (EntityView entityView : entityViews){
                    if (entityView.matchesEntity(entity)){
                        entityView.markForDelete();
                    }
                }
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }


        model.getGameObjects().removeAll(model.getPendingToRemoveGameObject());
        model.getGameObjects().addAll(model.getPendingToAddGameObject());
        model.getRenderables().removeAll(model.getPendingToRemoveRenderable());
        model.getRenderables().addAll(model.getPendingToAddRenderable());

        model.getPendingToAddGameObject().clear();
        model.getPendingToRemoveGameObject().clear();
        model.getPendingToAddRenderable().clear();
        model.getPendingToRemoveRenderable().clear();

        entityViews.removeIf(EntityView::isMarkedForDelete);

        double elapsedSeconds = (System.currentTimeMillis() - this.startTime) / 1000.0;
        int minutes = (int) elapsedSeconds / 60;
        int seconds = (int) elapsedSeconds % 60;
        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        scoreLabel.setText("Score: " + model.getTotalScore());


    }

    public Scene getScene() {
        return scene;
    }

    private void changeDifficulty() {
        String selectedDifficulty = difficultyComboBox.getValue();
        String configFile = "src/main/resources/config_easy.json";

        switch (selectedDifficulty) {
            case "Easy":
                configFile = "src/main/resources/config_easy.json";
                this.startTime = System.currentTimeMillis();
                break;
            case "Medium":
                configFile = "src/main/resources/config_medium.json";
                this.startTime = System.currentTimeMillis();
                break;
            case "Hard":
                configFile = "src/main/resources/config_hard.json";
                this.startTime = System.currentTimeMillis();
                break;
        }

        model = new GameEngine(configFile);
        this.pane.getChildren().clear();
        this.entityViews.clear();
        this.background = new SpaceBackground(model, pane);
        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(this.model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);
        pane.requestFocus();
    }



}
