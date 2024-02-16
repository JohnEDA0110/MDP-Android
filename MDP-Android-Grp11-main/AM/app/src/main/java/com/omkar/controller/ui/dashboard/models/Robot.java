package com.omkar.controller.ui.dashboard.models;

public class Robot {

    private static Robot robot;
    private Coordinate center;
    private Direction direction;

    public Robot(){
    }

    public static Robot getInstance(){
        if(robot == null){
            robot = new Robot();
        }
        return robot;
    }

    public void rotate(){
        switch(direction){
            case NORTH:
                direction = Direction.EAST;
                break;
            case EAST:
                direction = Direction.SOUTH;
                break;
            case SOUTH:
                direction = Direction.WEST;
                break;
            case WEST:
                direction = Direction.NORTH;
                break;
        }
    }

    public Coordinate getCenter(){
        return center;
    }

    public Direction getDirection(){
        return direction;
    }

    public void setCenter(Coordinate center){
        this.center = center;
    }

    public void setDirection(Direction direction){
        this.direction = direction;
    }

}
