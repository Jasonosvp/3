package invaders.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import invaders.ConfigReader;
import invaders.builder.BunkerBuilder;
import invaders.builder.Director;
import invaders.builder.EnemyBuilder;
import invaders.factory.Projectile;
import invaders.gameobject.Bunker;
import invaders.gameobject.Enemy;
import invaders.gameobject.GameObject;
import invaders.entities.Player;
import invaders.memento.CareTaker;
import invaders.memento.Originator;
import invaders.observer.ScoreObserver;
import invaders.observer.Subject;
import invaders.rendering.Renderable;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;

/**
 * This class manages the main loop and logic of the game
 */
public class GameEngine extends Subject {
	private List<GameObject> gameObjects = new ArrayList<>(); // A list of game objects that gets updated each frame
	private List<GameObject> pendingToAddGameObject = new ArrayList<>();
	private List<GameObject> pendingToRemoveGameObject = new ArrayList<>();

	private List<Renderable> pendingToAddRenderable = new ArrayList<>();
	private List<Renderable> pendingToRemoveRenderable = new ArrayList<>();

	private List<Renderable> renderables =  new ArrayList<>();

	private Player player;

	private boolean left;
	private boolean right;
	private int gameWidth;
	private int gameHeight;
	private int timer = 45;

	private int totalScore = 0;

	private Originator originalDoc;

	private CareTaker stateKeep;


	public GameEngine(String config){
		// Read the config here
		ConfigReader onlyConfigReader = ConfigReader.getInstance();
		onlyConfigReader.parse(config);

		// Get game width and height
		gameWidth = ((Long)((JSONObject) onlyConfigReader.getGameInfo().get("size")).get("x")).intValue();
		gameHeight = ((Long)((JSONObject) onlyConfigReader.getGameInfo().get("size")).get("y")).intValue();

		//Get player info
		this.player = new Player(onlyConfigReader.getPlayerInfo());
		renderables.add(player);


		Director director = new Director();
		BunkerBuilder bunkerBuilder = new BunkerBuilder();
		//Get Bunkers info
		for(Object eachBunkerInfo:onlyConfigReader.getBunkersInfo()){
			Bunker bunker = director.constructBunker(bunkerBuilder, (JSONObject) eachBunkerInfo);
			gameObjects.add(bunker);
			renderables.add(bunker);
		}


		EnemyBuilder enemyBuilder = new EnemyBuilder();
		//Get Enemy info
		for(Object eachEnemyInfo:onlyConfigReader.getEnemiesInfo()){
			Enemy enemy = director.constructEnemy(this,enemyBuilder,(JSONObject)eachEnemyInfo);
			gameObjects.add(enemy);
			renderables.add(enemy);
		}
		attach(new ScoreObserver(this));


	}

	/**
	 * Updates the game/simulation
	 */
	public void update(){
		timer+=1;

		movePlayer();

		for(GameObject go: gameObjects){
			go.update(this);
		}

		for (int i = 0; i < renderables.size(); i++) {
			Renderable renderableA = renderables.get(i);
			for (int j = i+1; j < renderables.size(); j++) {
				Renderable renderableB = renderables.get(j);

				if((renderableA.getRenderableObjectName().equals("Enemy") && renderableB.getRenderableObjectName().equals("EnemyProjectile"))
						||(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("Enemy"))||
						(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile"))){
				}else{
					if(renderableA.isColliding(renderableB) && (renderableA.getHealth()>0 && renderableB.getHealth()>0)) {
						renderableA.takeDamage(1);
						renderableB.takeDamage(1);

						if(renderableA.getRenderableObjectName().equals("PlayerProjectile") && renderableB.getRenderableObjectName().equals("Enemy")) {
							Image slowAlien = new Image(new File("src/main/resources/slow_alien.png").toURI().toString(), 20, 20, true, true);
							Image fastAlien = new Image(new File("src/main/resources/fast_alien.png").toURI().toString(), 20, 20, true, true);
							if (imagesAreEqual(renderableB.getImage(), slowAlien)) {
								this.updateScore(3);
								Notify();
							} else if (imagesAreEqual(renderableB.getImage(), fastAlien)) {
								this.updateScore(4);
								Notify();
							}
						} else if (renderableA.getRenderableObjectName().equals("PlayerProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile")) {
							Image slowAlienProjectile = new Image(new File("src/main/resources/alien_shot_slow.png").toURI().toString(), 10, 10, true, true);
							Image fastAlienProjectile = new Image(new File("src/main/resources/alien_shot_fast.png").toURI().toString(), 10, 10, true, true);
							if (imagesAreEqual(renderableB.getImage(), slowAlienProjectile)) {
								this.updateScore(1);
								Notify();
							} else if (imagesAreEqual(renderableB.getImage(), fastAlienProjectile)) {
								this.updateScore(2);
								Notify();
							}
						}

						if(renderableB.getRenderableObjectName().equals("PlayerProjectile") && renderableA.getRenderableObjectName().equals("Enemy")) {
							Image slowAlien = new Image(new File("src/main/resources/slow_alien.png").toURI().toString(), 20, 20, true, true);
							Image fastAlien = new Image(new File("src/main/resources/fast_alien.png").toURI().toString(), 20, 20, true, true);
							if (imagesAreEqual(renderableA.getImage(), slowAlien)) {
								this.updateScore(3);
								Notify();
							} else if (imagesAreEqual(renderableA.getImage(), fastAlien)) {
								this.updateScore(4);
								Notify();
							}
						} else if (renderableB.getRenderableObjectName().equals("PlayerProjectile") && renderableA.getRenderableObjectName().equals("EnemyProjectile")) {
							Image slowAlienProjectile = new Image(new File("src/main/resources/alien_shot_slow.png").toURI().toString(), 10, 10, true, true);
							Image fastAlienProjectile = new Image(new File("src/main/resources/alien_shot_fast.png").toURI().toString(), 10, 10, true, true);
							if (imagesAreEqual(renderableA.getImage(), slowAlienProjectile)) {
								this.updateScore(1);
								Notify();
							} else if (imagesAreEqual(renderableA.getImage(), fastAlienProjectile)) {
								this.updateScore(2);
								Notify();
							}
						}
					}
				}
			}
		}


		// ensure that renderable foreground objects don't go off-screen
		int offset = 1;
		for(Renderable ro: renderables){
		if(!ro.getLayer().equals(Renderable.Layer.FOREGROUND)){
			continue;
		}
		if(ro.getPosition().getX() + ro.getWidth() >= gameWidth) {
			ro.getPosition().setX((gameWidth - offset) -ro.getWidth());
		}

		if(ro.getPosition().getX() <= 0) {
			ro.getPosition().setX(offset);
		}

		if(ro.getPosition().getY() + ro.getHeight() >= gameHeight) {
			ro.getPosition().setY((gameHeight - offset) -ro.getHeight());
		}

		if(ro.getPosition().getY() <= 0) {
			ro.getPosition().setY(offset);
		}
	}

}

	public List<Renderable> getRenderables(){
		return renderables;
	}

	public List<GameObject> getGameObjects() {
		return gameObjects;
	}
	public List<GameObject> getPendingToAddGameObject() {
		return pendingToAddGameObject;
	}

	public List<GameObject> getPendingToRemoveGameObject() {
		return pendingToRemoveGameObject;
	}

	public List<Renderable> getPendingToAddRenderable() {
		return pendingToAddRenderable;
	}

	public List<Renderable> getPendingToRemoveRenderable() {
		return pendingToRemoveRenderable;
	}


	public void leftReleased() {
		this.left = false;
	}

	public void rightReleased(){
		this.right = false;
	}

	public void leftPressed() {
		this.left = true;
	}
	public void rightPressed(){
		this.right = true;
	}

	public boolean shootPressed(){
		if(timer>45 && player.isAlive()){
			Projectile projectile = player.shoot();
			gameObjects.add(projectile);
			renderables.add(projectile);
			timer=0;
			return true;
		}
		return false;
	}

	private void movePlayer(){
		if(left){
			player.left();
		}

		if(right){
			player.right();
		}
	}

	public int getGameWidth() {
		return gameWidth;
	}

	public int getGameHeight() {
		return gameHeight;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean imagesAreEqual(Image img1, Image img2) {
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			return false;
		}

		PixelReader reader1 = img1.getPixelReader();
		PixelReader reader2 = img2.getPixelReader();

		for (int y = 0; y < img1.getHeight(); y++) {
			for (int x = 0; x < img1.getWidth(); x++) {
				Color color1 = reader1.getColor(x, y);
				Color color2 = reader2.getColor(x, y);
				if (!color1.equals(color2)) {
					return false;
				}
			}
		}
		return true;
	}

	public void updateScore(int score){
		totalScore += score;
	}

	public int getTotalScore(){
		return totalScore;
	}

	public void setGameObjects(List<GameObject> gameObjects) {
		this.gameObjects = gameObjects;
	}


	public void setRenderables(List<Renderable> renderables){
		this.renderables = renderables;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void saveFunction(){
		Originator originalDoc = new Originator();
		originalDoc.setGameEngine(this);

		CareTaker stateKeep = new CareTaker();
		stateKeep.setMemento(originalDoc.saveStateToMemento());
		this.originalDoc = originalDoc;
		this.stateKeep = stateKeep;
	}

	public void restoreFunction() {
		originalDoc.getStateFromMemento(stateKeep.getMemento());
		this.gameObjects.clear();
		this.gameObjects.addAll(originalDoc.getGameEngine().getGameObjects());
		this.renderables.clear();
		this.renderables.addAll(originalDoc.getGameEngine().getRenderables());
	}


}
