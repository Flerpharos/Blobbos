package me.flerpharos.games.maps.util;

public class Score implements Comparable<Score> {

    String name;
    int score;

    public Score() {
        this.name = "NUL";
        this.score = 0;
    }

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int compareTo(Score s) {
        return score - s.score;
    }

    public String toString() {
        return String.format("%-10s %05d", name, score);
    }
}