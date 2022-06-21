package me.flerpharos.games.maps.map;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Map {

    public String mapGroundFilepath;

    public Vector2[] graphPoints;
    public ConnectionData[] connections;

    public Vector2[] enemyLocations;
    public Vector2 playerLocation;

    public Polygon[] entityColliders;
    public Polygon[] bulletColliders;

    public Vector2[] bulletReflectors;

    public String cutsceneString;
    public String cutsceneVoicoverFilepath;

    public ObstacleData[] obstacles;

    public int scoreMax;

    private float[] toFloatArray(ArrayList<Float> arr) {
        int len = arr.size();
        float[] val = new float[len];

        for (int i=0; i<len; i++) val[i] = arr.get(i);

        return val;
    }

    public Map(String s) {

        cutsceneString = "";

        String[] lines = s.split("\n");

        int state = 0;

        ArrayList<Vector2> vectorPoints = new ArrayList<>();
        ArrayList<Polygon> polygons = new ArrayList<>();
        ArrayList<ObstacleData> obsList = new ArrayList<>();
        ArrayList<ConnectionData> connList = new ArrayList<>();

        for (String str : lines) {
            String line = str.strip();

            if (line.startsWith("#")) continue;

            if (line.equals("")) {

                switch (state) {
                    case 1 -> graphPoints = vectorPoints.toArray(new Vector2[0]);
                    case 2 -> connections = connList.toArray(new ConnectionData[0]);
                    case 3 -> enemyLocations = vectorPoints.toArray(new Vector2[0]);
                    case 4 -> playerLocation = vectorPoints.get(0);
                    case 5 -> entityColliders = polygons.toArray(new Polygon[0]);
                    case 6 -> bulletColliders = polygons.toArray(new Polygon[0]);
                    case 7 -> bulletReflectors = vectorPoints.toArray(new Vector2[0]);
                    case 8 -> obstacles = obsList.toArray(new ObstacleData[0]);
                }

                state ++;
                vectorPoints.clear();
                polygons.clear();
                continue;
            }

            switch (state) {
                case 0 -> mapGroundFilepath = line;
                case 9 -> cutsceneString += line;
                case 10 -> cutsceneVoicoverFilepath = line;
                case 11 -> scoreMax = Integer.parseInt(line);
                case 1, 3, 4, 7 -> {
                    String[] points = line.split(", ");
                    vectorPoints.add(new Vector2(Float.parseFloat(points[0]), Float.parseFloat(points[1])));
                }
                case 2 -> {
                    String[] points = line.split(" -> ");
                    connList.add(new ConnectionData(Integer.parseInt(points[0]), Integer.parseInt(points[1])));
                }
                case 5, 6 -> {
                    ArrayList<Float> floatList = new ArrayList<>();

                    String[] points = line.split(" -> ");
                    for (String point : points) {
                        String[] args = point.split(", ");
                        floatList.add(Float.parseFloat(args[0]));
                        floatList.add(Float.parseFloat(args[1]));
                    }
                    polygons.add(new Polygon(toFloatArray(floatList)));
                }
                case 8 -> {
                    String[] a = line.split(": ");
                    String[] b = a[1].split(", ");
                    obsList.add(new ObstacleData(
                        new Vector2(Float.parseFloat(b[0]), Float.parseFloat(b[1])),
                        switch (a[0]) {
                            case "BUSH" -> ObstacleTypes.BUSH;
                            case "HIGHWALL" -> ObstacleTypes.HIGHWALL;
                            case "WALL" -> ObstacleTypes.WALL;
                            case "ROCK" -> ObstacleTypes.ROCK;
                            default -> throw new IllegalStateException("Unexpected value: " + a[0]);
                        }));
                }
            }
        }
    }
}
