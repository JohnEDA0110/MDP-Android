package com.omkar.controller.ui.dashboard;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.omkar.controller.Message;
import com.omkar.controller.MessageQueue;
import com.omkar.controller.MessageQueueProvider;
import com.omkar.controller.R;
import com.omkar.controller.SpeechProvider;
import com.omkar.controller.databinding.FragmentDashboardBinding;
import com.omkar.controller.ui.dashboard.models.Arena;
import com.omkar.controller.ui.dashboard.models.Coordinate;
import com.omkar.controller.ui.dashboard.models.Direction;
import com.omkar.controller.ui.dashboard.models.Obstacle;
import com.omkar.controller.ui.dashboard.models.ObstacleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private GridLayout base_layout;
    private TextView robotStatus;
    private Arena arena;
    private Obstacle obstacle;
    private View arenaCarView;
    private View arenaObstacleView;
    private boolean afterFirstVisit = false;
    private MutableLiveData<MessageQueue> mutableMessageQueue = MessageQueueProvider.getMutableLiveMessageQueue();
    private MutableLiveData<TextToSpeech> mutableTextToSpeech = SpeechProvider.getSpeechAssistant();
    private TextToSpeech textToSpeech;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        base_layout = root.findViewById(R.id.base_layout);
        robotStatus = root.findViewById(R.id.robotStatus);
        if(arena == null){
            arena = Arena.getInstance();
        }
        arena.setRobotStatus(robotStatus);

        base_layout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent event) {
                int action = event.getAction();
                switch (action) {
                    default:
                        break;
                }
                return true;
            }
        });

        Button startButton = root.findViewById(R.id.start);

        //Start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendStartCommand();
                // Empty the message queue
                mutableMessageQueue.setValue(new MessageQueue());
                mutableMessageQueue.postValue(new MessageQueue());
                textToSpeech.speak("Starting the car", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        Button sendButton = root.findViewById(R.id.sendToCar);

        //Send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendArenaToCar();
                textToSpeech.speak("Arena sent to car, waiting to receive status update", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        Button clearButton = root.findViewById(R.id.resetArena);

        //Reset button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clear all obstacles from the arena object and redraw the grid
                arena.clearObstacles();
                arena.normalizeCellsOccupiedByRobot();
                arena.normalizeGrid();
                arena.clearRobot();
            }
        });

        drawDrawer();
        drawGrid();
        bindDirectionButtonFunctionality();

        //Listens to RPI message: image-rec (update obstacle ui) or location (update robot location ui)
        mutableMessageQueue.observeForever(messageQueue -> {
            if(messageQueue.getMessages().size() > 0){
                if(!afterFirstVisit){
                    // only perform all sequential operations if this is the first visit
                    for(Message message : messageQueue.getMessages()){
                        // Use JSON parser to identify message properties
                        // Example message type: {"cat": "image-rec", "value": {"image_id": "11", "obstacle_id":  "1"}}
                        String messageContent = message.content;
                        interpretMessage(messageContent);
                    }
                    afterFirstVisit = true;
                } else {
                    // Only look at the last message in the queue
                    Message message = messageQueue.getMessages().get(messageQueue.getMessages().size() - 1);
                    String messageContent = message.content;
                    interpretMessage(messageContent);
                }
            }
        });

        textToSpeech = mutableTextToSpeech.getValue();

        RadioButton imageRec = root.findViewById(R.id.radio_imageRec);
        imageRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arena.setMode(0);
            }
        });

        RadioButton exploration = root.findViewById(R.id.radio_fastestCar);
        exploration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arena.setMode(1);
            }
        });

        return root;
    }

    public void interpretMessage(String messageContent){
        try {
            JSONObject obj = new JSONObject(messageContent);
            String cat = obj.getString("cat");
            if(cat.equals("image-rec")){
                JSONObject value = obj.getJSONObject("value");
                String imageId = value.getString("image_id");
                String obstacleId = value.getString("obstacle_id");
                if(!imageId.equals("NA")){
                    arena.redrawIdentifiedObstacle(Integer.parseInt(obstacleId), Integer.parseInt(imageId));
                    textToSpeech.speak("Obstacle " + obstacleId + " is identified as ID " + imageId, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    textToSpeech.speak("Skipping obstacle " + obstacleId, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            } else if(cat.equals("location")){
                JSONObject value = obj.getJSONObject("value");
                String x = value.getString("x");
                String y = value.getString("y");
                String direction = value.getString("d");
                arena.renderRobot(Integer.parseInt(x), Integer.parseInt(y), getDirectionFromInteger(Integer.parseInt(direction)));
            } else {
                String value = obj.getString("value");
                if(value.endsWith("Robot is ready to move.")){
                    textToSpeech.speak("Robot is ready to move", TextToSpeech.QUEUE_FLUSH, null, null);
                } else if(value.equals("finished")){
                    textToSpeech.speak("Arena exploration finished", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }
    }

    public Direction getDirectionFromInteger(int d){
        if(d == 1) return Direction.NORTH;
        else if(d == 2) return Direction.SOUTH;
        else if(d == 3) return Direction.EAST;
        else return Direction.WEST;
    }

    public Direction getDirectionFromChar(char c){
        if(c == 'N') return Direction.NORTH;
        else if(c == 'E') return Direction.EAST;
        else if(c == 'S') return Direction.SOUTH;
        else return Direction.WEST;
    }

    public void bindDirectionButtonFunctionality(){

        ImageButton forwardButton = binding.forward;
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("f");
            }
        });

        ImageButton backwardButton = binding.backward;
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("b");
            }
        });

        ImageButton forwardLeftButton = binding.forwardLeft;
        forwardLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("sl");
            }
        });

        ImageButton forwardRightButton = binding.forwardRight;
        forwardRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("sr");
            }
        });

        ImageButton backwardLeftButton = binding.leftBackward;
        backwardLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("tl");
            }
        });

        ImageButton backwardRightButton = binding.rightBackward;
        backwardRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the arena object to the car
                arena.sendMoveCommand("tr");
            }
        });
    }

    public void drawDrawer(){
        GridLayout drawer = binding.drawerForArena;
        // the first column should have a black rectangle to indicate obstacles that can be dragged and dropped in the grid
        View obstacleView = new TextView(getContext());

        // Obstacle option

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 75;
        params.height = 75;
        params.columnSpec = GridLayout.spec(0, 1, 1);
        params.rowSpec = GridLayout.spec(0, 1, 1);

        obstacleView.setLayoutParams(params);

        obstacleView.setBackgroundResource(R.drawable.baseline_block_24);

        ((TextView) obstacleView).setText("");

        obstacleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(null, shadowBuilder, view, 0);
                return true;
            }
        });

        drawer.addView(obstacleView);
        arenaObstacleView = obstacleView;

        // Car option
        View carView = new TextView(getContext());

        GridLayout.LayoutParams carParams = new GridLayout.LayoutParams();
        carParams.width = 75;
        carParams.height = 75;
        carParams.columnSpec = GridLayout.spec(1, 1, 1);
        carParams.rowSpec = GridLayout.spec(0, 1, 1);

        carView.setLayoutParams(carParams);

        carView.setBackgroundResource(R.drawable.baseline_directions_car_24);

        ((TextView) carView).setText("");

        carView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(null, shadowBuilder, view, 0);
                return true;
            }
        });

        drawer.addView(carView);
        arenaCarView = carView;
        Arena.getInstance().setCarView(carView);
    }

    public void drawGrid(){
        arena = Arena.getInstance();
        arena.clearCellViewMap();

        ArrayList<Obstacle> existingObstacles = arena.getObstacles();
        boolean obstaclesExist = existingObstacles.size() > 0;

        for(int i = 0 ; i < 21 ; i++) {
            for (int j = 0 ; j < 21 ; j++) {
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = base_layout.getWidth() / 21;
                params.height = base_layout.getHeight() / 21;
                params.columnSpec = GridLayout.spec(j, 1, 1);
                params.rowSpec = GridLayout.spec(i, 1, 1);

                View view = new TextView(getContext());
                view.setLayoutParams(params);

                // First row and column of the grid are different and must display
                // numbers 1-20 in each direction
                if (j == 0 && i != 20) {
                    ((TextView) view).setText("" + (20 - i - 1));
                    ((TextView) view).setTextColor(Color.parseColor("#000000"));
                    ((TextView) view).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                } else if (i == 20 && j != 0) {
                    ((TextView) view).setText("" + (j - 1));
                    ((TextView) view).setTextColor(Color.parseColor("#000000"));
                    ((TextView) view).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                } else if (i == 20 && j == 0) {
                    ((TextView) view).setText("");
                } else {
                    if(obstaclesExist && arena.doesCoordinateHaveObstacle(j - 1, 19 - i)){
                        // Obstacle exists at this coordinate
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        view = new ObstacleView(getContext());
                        view.setLayoutParams(layoutParams);
                        arena.addCellView(new Coordinate(j - 1, 20 - i - 1), view);
                        ((ObstacleView) view).redraw(arena.getObstacleByCoordinate(j - 1, 19 - i).getDirection());
                        view.setId(arena.getObstacleByCoordinate(j - 1, 19 - i).getId());
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                arena.rotateObstacleDirection(v);
                            }
                        });
                        view.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                                view.startDragAndDrop(null, shadowBuilder, view, 0);

                                // Remove obstacle from the list
                                arena.removeObstacleFromView(view);

                                // Return the cell view back to normal
                                arena.convertViewFromObstacleToCell(view);

                                return true;
                            }
                        });
                    } else {
                        // Setting the coordinates of the cell as the text of the cell
                        ((TextView) view).setText( (j - 1) + "," + (20 - i - 1));
                        ((TextView) view).setTextColor(Color.parseColor("#D3D3D3"));
                        view.setBackgroundResource(R.drawable.grid_cell);
                        view.getBackground().setColorFilter(Color.parseColor("#D3D3D3"), PorterDuff.Mode.DARKEN);
                        ((TextView) view).setTextSize(10); // Should not be seen by the user
                        arena.addCellView(new Coordinate(j - 1, 20 - i - 1), view);

                        // Setting the drag listener for the cell
                        view.setOnDragListener(new View.OnDragListener() {
                            @Override
                            public boolean onDrag(View view, DragEvent event) {
                                int action = event.getAction();
                                switch (action) {
                                    case DragEvent.ACTION_DROP:
                                        // Get the view that is being dropped
                                        View droppedView = (View) event.getLocalState();
                                        if(droppedView == arenaCarView){
                                            // get coordinates of the view dropped inside of
                                            Coordinate robotCenter = arena.getCoordinateFromView(view);
                                            arena.setRobotCenter(robotCenter.getX(), robotCenter.getY());
                                            arena.setRobotDirection(Direction.NORTH);
                                            arena.renderRobot(robotCenter.getX(), robotCenter.getY(), Direction.NORTH);
                                            //robotStatus.setText("Robot Status: X = " + robotCenter.getX() + ", Y = " + robotCenter.getY() + ", Direction = NORTH");
                                        } else {
                                            arena.convertViewFromCellToObstacle(view);
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
                base_layout.addView(view);
            }
        }
        if(arena.isRobotActive()){
            arena.renderRobot(arena.getRobotCenter().getX(), arena.getRobotCenter().getY(), arena.getRobotDirection());
            robotStatus.setText("Robot Status: X = " + arena.getRobotCenter().getX() + ", Y = " + arena.getRobotCenter().getY() + ", Direction = " + arena.getRobotDirection());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}