public interface GameStateListener {
    void onLivesChanged(int currentLives);

    void onScoreChanged(int score);

    void onLevelChanged(int currentLevel);

    void onGameOver();
}
