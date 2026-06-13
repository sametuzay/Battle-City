public interface GameStateListener { // bunu neden interface ypatık mesela bunu da anlatalım bence
    void onLivesChanged(int currentLives);

    void onScoreChanged(int score);

    void onLevelChanged(int currentLevel);

    void onGameOver();
}
