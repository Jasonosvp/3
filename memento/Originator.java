package invaders.memento;

import invaders.engine.GameEngine;

public class Originator {
    private GameEngine gameEngine;

    public Memento saveStateToMemento(){
        return new Memento(gameEngine.getGameObjects(), gameEngine.getRenderables(), gameEngine.getPlayer());
    }

    public void getStateFromMemento(Memento memento){
        gameEngine.setGameObjects(memento.getGameObjectsState());
        gameEngine.setRenderables(memento.getRenderablesState());
        gameEngine.setPlayer(memento.getPlayer());
    }

    public GameEngine getGameEngine(){
        return gameEngine;
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
}
