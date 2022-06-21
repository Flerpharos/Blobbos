package me.flerpharos.games.maps.util;

import com.badlogic.gdx.Input;

public class KeybindSystem {
    private static int shootingBind = Input.Keys.SPACE;
    private static int forwardBind = Input.Keys.W;
    private static int backwardBind = Input.Keys.S;
    private static int leftBind = Input.Keys.A;
    private static int rightBind = Input.Keys.D;

    public static int getForward(){
        return forwardBind;
    }

    public static int getBackward(){
        return backwardBind;
    }

    public static int getLeft(){
        return leftBind;
    }

    public static int getRight(){
        return rightBind;
    }

    public static int getShoot(){
        return shootingBind;
    }

    public static void setForward(int bind){
        forwardBind = bind;
    }

    public static void setBackward(int bind){
        backwardBind = bind;
    }

    public static void setLeft(int bind){
        leftBind = bind;
    }

    public static void setRight(int bind){
        rightBind = bind;
    }

    public static void setShooting(int bind){
        shootingBind = bind;
    }


}
