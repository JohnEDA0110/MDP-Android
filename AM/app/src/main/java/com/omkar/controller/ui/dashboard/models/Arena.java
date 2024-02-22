package com.omkar.controller.ui.dashboard.models;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.omkar.controller.BluetoothServiceProvider;
import com.omkar.controller.R;
import com.omkar.controller.ui.bluetooth.ConnectionException;
import com.omkar.controller.ui.bluetooth.services.BluetoothService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Arena {

    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private HashMap<Coordinate, View> cellViewMap = new HashMap<>();

    private Robot robot = Robot.getInstance();
    private View carView;

    private static Arena arena;

    private int mode = 0;

    private TextView robotStatus;

    public Arena() {
    }

    public void setMode(int mode){
        this.mode = mode;
    }

    public void setCarView(View carView) {
        this.carView = carView;
        this.carView.setBackgroundResource(R.drawable.baseline_directions_car_24);
    }

    public static Arena getInstance(){
        if(arena == null){
            arena = new Arena();
        }
        return arena;
    }

    public boolean doesCoordinateHaveObstacle(Coordinate coordinate){
        if(getObstacle(coordinate.getX(), coordinate.getY()) != null){
            return true;
        }
        return false;
    }

    public boolean doesCoordinateHaveObstacle(int x, int y){
        if(getObstacle(x, y) != null){
            return true;
        }
        return false;
    }

    public void clearCellViewMap(){
        cellViewMap.clear();
    }

    public Obstacle getObstacle(int x, int y){
        for(Obstacle obstacle : obstacles){
            if(obstacle.getCoordinate().getX() == x && obstacle.getCoordinate().getY() == y){
                return obstacle;
            }
        }
        return null;
    }

    public void addCellView(Coordinate coordinate, View view){
        cellViewMap.put(coordinate, view);
    }

    public View getCellView(Coordinate coordinate){
        return cellViewMap.get(coordinate);
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public void addObstacle(Coordinate coordinate) {
        obstacles.add(new Obstacle(coordinate, Direction.NORTH));
    }

    public Obstacle getObstacleByCoordinate(int x, int y){
        for(Obstacle obstacle : obstacles){
            if(obstacle.getCoordinate().getX() == x && obstacle.getCoordinate().getY() == y){
                return obstacle;
            }
        }
        return null;
    }

    public void removeObstacle(int x, int y) {
        for(Obstacle obstacle : obstacles){
            if(obstacle.getCoordinate().getX() == x && obstacle.getCoordinate().getY() == y){
                obstacles.remove(obstacle);
                obstacle.setCount(obstacle.getCount()-1);
                break;
            }
        }
    }

    public void rotateObstacleDirection(View view) {
        Coordinate coordinateOfObstacle = getCoordinateFromView(view);

        Obstacle obstacle = getObstacle(coordinateOfObstacle.getX(), coordinateOfObstacle.getY());
        obstacle.rotateDirection();

        ((ObstacleView) view).redraw(obstacle.getDirection());
    }

    public void normalizeGrid(){
        // Find all the obstacle views and convert them to cell views
        cellViewMap.forEach((coordinate, cellView) -> {
            if(cellView instanceof ObstacleView){
                convertViewFromObstacleToCell(cellView);
            }
        });
    }

    public Coordinate getCoordinateFromView(View view){
        final Coordinate[] coordinateOfObstacle = {null};

        // Get coordinate of the view
        cellViewMap.forEach((coordinate, cellView) -> {
            if(cellView.equals(view)){
                coordinateOfObstacle[0] = coordinate;
            }
        });

        return coordinateOfObstacle[0];
    }

    public void clearObstacles(){
        obstacles.clear();
        Obstacle.resetCount();
    }

    public void addObstacleFromView(View view){
        // Get the coordinate of the view
        cellViewMap.forEach((coordinate, cellView) -> {
            if(cellView.equals(view)){
                addObstacle(coordinate);
            }
        });
    }

    public void removeObstacleFromView(View view){
        // Get the coordinate of the view
        cellViewMap.forEach((coordinate, cellView) -> {
            if(cellView.equals(view)){
                removeObstacle(coordinate.getX(), coordinate.getY());
            }
        });
    }

    public void convertViewFromCellToObstacle(View view){
        if(view == null) return;
        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null){
            int index = parent.indexOfChild(view);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            view.setLayoutParams(layoutParams);
            parent.removeView(view);
            ObstacleView north = new ObstacleView(view.getContext());
            north.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rotateObstacleDirection(v);
                }
            });

            // Allow obstacle to be dragged
            north.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDragAndDrop(null, shadowBuilder, view, 0);

                    // Remove obstacle from the list
                    removeObstacleFromView(view);

                    // Return the cell view back to normal
                    convertViewFromObstacleToCell(view);

                    return true;
                }
            });

            parent.addView(north, index, layoutParams);
            // replace the view in the cellViewMap
            cellViewMap.forEach((coordinate, cellView) -> {
                if(cellView.equals(view)){
                    cellViewMap.put(coordinate, north);
                    addObstacle(coordinate);
                    // Get the added obstacle
                    Obstacle obstacle = getObstacle(coordinate.getX(), coordinate.getY());
                    north.setId(obstacle.getId());
                }
            });
        }
    }

    public void convertViewFromObstacleToCell(View view){
        if(view == null) return;
        // Find coordinate of the view from the cellViewMap
        Coordinate coordinate = getCoordinateFromView(view);
        // Remove view from the grid
        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null){

            int index = parent.indexOfChild(view);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            view.setLayoutParams(layoutParams);
            parent.removeView(view);

            // Add the view back to the grid
            View cellView = new TextView(view.getContext());
            cellView.setBackgroundResource(R.drawable.grid_cell);
            cellView.getBackground().setColorFilter(Color.parseColor("#D3D3D3"), PorterDuff.Mode.DARKEN);

            parent.addView(cellView, index, layoutParams);
            // replace the view in the cellViewMap
            cellViewMap.put(coordinate, cellView);

            // Setting the drag listener for the cell

            cellView.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case DragEvent.ACTION_DROP:
                            // Get the view that is being dropped
                            View droppedView = (View) event.getLocalState();
                            if(droppedView == carView){
                                // get coordinates of the view dropped inside of
                                Coordinate robotCenter = getCoordinateFromView(view);
                                setRobotCenter(robotCenter.getX(), robotCenter.getY());
                                setRobotDirection(Direction.NORTH);
                                renderRobot(robotCenter.getX(), robotCenter.getY(), Direction.NORTH);
                            } else {
                                convertViewFromCellToObstacle(view);
                            }
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    public View getViewAtCoordinate(int x, int y){
        if(x > 19 || x < 0 || y > 19 || y < 0) return null;
        final View[] view = new View[1];
        cellViewMap.forEach((coordinate, cellView) -> {
            if(coordinate.getX() == x && coordinate.getY() == y){
                view[0] = cellView;
            }
        });
        return view[0];
    }

    public void normalizeCellsOccupiedByRobot(){
        if(robot == null || robot.getCenter() == null) return;
        Coordinate center = robot.getCenter();
        int x = center.getX();
        int y = center.getY();
        for(int i = x - 1; i <= x + 1; i++){
            for(int j = y - 1; j <= y + 1; j++){
                convertViewFromObstacleToCell(getViewAtCoordinate(i, j));
            }
        }
    }

    public void setRobotStatus(TextView statusView){
        this.robotStatus = statusView;
    }



    public void renderRobot(int x, int y, Direction direction){
        // the robot is 3x3. If the x coordinate is 0 or 19 or the y coordinate is 0 or 19, return
        if(x <= 0 || x >= 19 || y <= 0 || y >= 19){
            return;
        }
        if(robot.getCenter() != null){
            // Robot occupied a 3x3 matrix before, normalize these cells first
            normalizeCellsOccupiedByRobot();
        }
        robot.setCenter(new Coordinate(x, y));
        robot.setDirection(direction);
        robotStatus.setText("Robot Status: X = " + robot.getCenter().getX() + ", Y = " + robot.getCenter().getY() + ", Direction = " + robot.getDirection().toString());
        for(int i = x - 1; i <= x + 1; i++){
            for(int j = y - 1; j <= y + 1; j++){
                // set the color of the views in 3x3 block to red
                View view = getViewAtCoordinate(i, j);
                ViewGroup parent = (ViewGroup) view.getParent();
                if(parent != null){
                    int index = parent.indexOfChild(view);
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    view.setLayoutParams(layoutParams);
                    parent.removeView(view);

                    View redView = new TextView(view.getContext());
                    redView.setBackgroundResource(R.drawable.robot_body);

                    redView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robot.rotate();
                            renderRobot(robot.getCenter().getX(), robot.getCenter().getY(), robot.getDirection());
                        }
                    });

                    redView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                            view.startDragAndDrop(null, shadowBuilder, carView, 0);

                            // Return the cell view back to normal
                            normalizeCellsOccupiedByRobot();

                            // Remove the robot from the arena
                            ///clearRobot();

                            return true;
                        }
                    });

                    parent.addView(redView, index, layoutParams);
                    // replace the view in the cellViewMap
                    cellViewMap.forEach((coordinate, cellView) -> {
                        if(cellView.equals(view)){
                            cellViewMap.put(coordinate, redView);
                        }
                    });
                }
            }

        }
        // rendering the robot camera
        switch (direction){
            case NORTH:
                renderRobotCamera(x, y + 1, direction);
                break;
            case SOUTH:
                renderRobotCamera(x, y - 1, direction);
                break;
            case EAST:
                renderRobotCamera(x + 1, y, direction);
                break;
            case WEST:
                renderRobotCamera(x - 1, y, direction);
                break;
        }
    }

    public void renderRobotCamera(int x, int y, Direction direction){
        View view = getViewAtCoordinate(x, y);
        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null){
            int index = parent.indexOfChild(view);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            view.setLayoutParams(layoutParams);
            parent.removeView(view);

            View redView = new TextView(view.getContext());
            redView.setBackgroundResource(R.drawable.robot_cam);

            redView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    robot.rotate();
                    renderRobot(robot.getCenter().getX(), robot.getCenter().getY(), robot.getDirection());
                }
            });

            parent.addView(redView, index, layoutParams);
            // replace the view in the cellViewMap
            cellViewMap.forEach((coordinate, cellView) -> {
                if(cellView.equals(view)){
                    cellViewMap.put(coordinate, redView);
                }
            });
        }
    }

    public Coordinate getRobotCenter(){
        return robot.getCenter();
    }

    public Direction getRobotDirection(){
        return robot.getDirection();
    }

    public boolean isRobotActive(){
        if(robot.getCenter() != null){
            return true;
        }
        return false;
    }

    public void setRobotCenter(int x, int y){
        robot.setCenter(new Coordinate(x, y));
    }

    public void setRobotDirection(Direction direction){
        robot.setDirection(direction);
    }

    public void clearRobot(){
        robot.setCenter(null);
        robot.setDirection(null);
        if (robot.getCenter() == null && robot.getDirection() == null) {
            this.robotStatus.setText("Robot Status: X = nil Y = nil Facing = nil");
        }

    }

    public int convertDirectionToInteger(Direction direction){
        switch (direction){
            case NORTH:
                return 1;
            case SOUTH:
                return 2;
            case EAST:
                return 3;
            case WEST:
                return 4;
        }
        return -1;
    }

    public void sendStartCommand(){
        BluetoothService bluetoothService = BluetoothServiceProvider.getBluetoothService();
        String message = "{\"cat\":\"control\",\"value\":\"start\"}";
        try{
            bluetoothService.writeMessageToDevice(message.getBytes());
        } catch (ConnectionException e){
            Toast.makeText(carView.getContext(), "Connection to car lost", Toast.LENGTH_SHORT).show();
        }
    }

    public String generateObstacleListString(){
        if(mode == 1){
            return "[]";
        }
        String message = "[";
        if(obstacles.size() != 0) {
            for (Obstacle obstacle : obstacles) {
                message += "{\"id\":" + obstacle.getId() + ",\"x\":" + obstacle.getCoordinate().getX() + ",\"y\":" + obstacle.getCoordinate().getY() + ",\"d\":" + convertDirectionToInteger(obstacle.getDirection()) + "},";
            }
        }
        message = message.substring(0, message.length() - 1);
        message += "]";
        return message;
    }

    public void sendArenaToCar() {
        BluetoothService bluetoothService = BluetoothServiceProvider.getBluetoothService();
        String message = "{\"cat\":\"obstacles\", \"value\": { \"obstacles\": " + generateObstacleListString() + ", \"mode\": " + mode + "}}";
        try{
            bluetoothService.writeMessageToDevice(message.getBytes());
        } catch (ConnectionException e){
            Toast.makeText(carView.getContext(), "Connection to car lost", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMoveCommand(String command){
        BluetoothService bluetoothService = BluetoothServiceProvider.getBluetoothService();
        try{
            bluetoothService.writeMessageToDevice(command.getBytes());
        } catch (ConnectionException e){
            Toast.makeText(carView.getContext(), "Connection to car lost", Toast.LENGTH_SHORT).show();
        }

    }

    public void redrawIdentifiedObstacle(int idOfObstacle, int identifiedId){
        for(Obstacle obstacle : obstacles){
            if(obstacle.getId() == idOfObstacle){
                obstacle.setRealId(identifiedId);
                Coordinate coordinateOfObstacle = obstacle.getCoordinate();
                View view = getViewAtCoordinate(coordinateOfObstacle.getX(), coordinateOfObstacle.getY());
                ((ObstacleView) view).obstacleHasBeenIdentified(identifiedId);
            }
        }

    }

}
