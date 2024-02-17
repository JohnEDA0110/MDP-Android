package com.omkar.controller.ui.dashboard.models;

import java.util.ArrayList;

public class Obstacle {

    private static int count = 1;
    private static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private int realId = -1;
    private int id;
    private Coordinate coordinate;
    private Direction direction;

    public Obstacle(Coordinate coordinate, Direction direction){
        this.id = count++;
        this.coordinate = coordinate;
        this.direction = direction;
        obstacles.add(this);
    }

    public int getRealId(){
        return realId;
    }

    public void setRealId(int realId){
        this.realId = realId;
    }

    public static ArrayList<Obstacle> getObstacles(){
        return obstacles;
    }

    public static void clearObstacles(){
        obstacles = new ArrayList<Obstacle>();
        count = 1;
    }

    public int getId(){
        return id;
    }

    public Coordinate getCoordinate(){
        return coordinate;
    }

    public Direction getDirection(){
        return direction;
    }

    public void setCoordinate(Coordinate coordinate){
        this.coordinate = coordinate;
    }

    public void setDirection(Direction direction){
        this.direction = direction;
    }

    public static Obstacle getObstacleById(int id){
        for(Obstacle obstacle : obstacles){
            if(obstacle.getId() == id){
                return obstacle;
            }
        }
        return null;
    }

    public static void removeObstacleById(int id){
        // Remove the obstacle with the given id from the list of obstacles
        // Then change the id of all the obstacles with id greater than the given id
        // by decrementing their id by 1

        Obstacle obstacle = getObstacleById(id);
        obstacles.remove(obstacle);
        for(Obstacle o : obstacles){
            if(o.getId() > id){
                o.id--;
            }
        }
    }

    public void rotateDirection(){
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


}
