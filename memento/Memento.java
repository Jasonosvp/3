package invaders.memento;

import invaders.entities.Player;
import invaders.gameobject.GameObject;
import invaders.rendering.Renderable;
import java.util.ArrayList;
import java.util.List;

public class Memento {
    private List<GameObject> gameObjectsState;
    private List<Renderable> renderablesState;

    private Player player;

    public Memento(List<GameObject> gameObjects, List<Renderable> renderables, Player player){
        this.gameObjectsState = new ArrayList<>(gameObjects);
        this.renderablesState = new ArrayList<>(renderables);
        this.player = player;
    }

    public List<GameObject> getGameObjectsState(){
        return this.gameObjectsState;
    }
    public void setGameObjectsState(List<GameObject> gameObjectsState){
        this.gameObjectsState = gameObjectsState;
    }

    public List<Renderable> getRenderablesState(){
        return this.renderablesState;
    }

    public void setRenderablesState(List<Renderable> renderablesState) {
        this.renderablesState = renderablesState;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
