package com.example.a.khonsu.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a.khonsu.DatabaseOpenHelper;
import com.example.a.khonsu.R;
import com.example.a.khonsu.ServerAPI;
import com.example.a.khonsu.model.Floor;
import com.example.a.khonsu.model.Location;
import com.example.a.khonsu.model.Path;
import com.example.a.khonsu.model.Route;
import com.example.a.khonsu.model.ServerObject;
import com.example.a.khonsu.util.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private  Button cameraBtn;
    private Button locationBtn;
    private EditText locationEditText;

    private ServerAPI mAPI;
    private DatabaseOpenHelper dbHelper;
    private SharedPreferences sharedPref;

    private  List<Location> locList = new ArrayList<>();
    private List<Route> routeList = new ArrayList<>();
    private List<Floor> floorList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAPI = ApiUtils.getServerAPI();
        dbHelper = new DatabaseOpenHelper(getContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //fetch data from online JSON if it is not updated
        if(sharedPref.getBoolean(getString(R.string.updated_database), false)) {
            getDataFromJSON();
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setTitle(R.string.home);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        setHasOptionsMenu(true);

        cameraBtn = v.findViewById(R.id.camera_button);
        setUpCameraBtn();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraBtn.setEnabled(false); //prevent users accidentally hit the button twice
                ((MainActivity)getActivity()).requestCameraPermission();
            }
        });

        locationEditText = v.findViewById(R.id.location_editText);
        locationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        locationBtn = v.findViewById(R.id.location_submit_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEditText.getText() != null) {
                    String searchTerm = locationEditText.getText().toString().trim().toLowerCase();
                    Intent intent = new Intent(getActivity(), MapNavActivity.class);
                    intent.putExtra("UUID", searchTerm);
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraBtn.setEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void setUpCameraBtn() {
        //scale the button
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int button_size = (int)(dm.widthPixels * 0.3);
        cameraBtn.getLayoutParams().width = button_size;
        cameraBtn.getLayoutParams().height = button_size;
        cameraBtn.requestLayout();
    }

    public void getDataFromJSON() {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.updated_database), false);
        editor.apply();

        mAPI.getAnswer().enqueue(new Callback<ServerObject>() {
            @Override
            public void onResponse(Call<ServerObject> call, Response<ServerObject> response) {
                if(response.isSuccessful()) {
                    locList = response.body().getLocation();
                    routeList = response.body().getRoutes();
                    floorList = response.body().getFloors();

                    SaveIntoDatabase task = new SaveIntoDatabase();
                    task.start();

                    Log.d("Main Activity", "posts loaded from API");
                } else {
                    int statusCode = response.code();
                    Log.d("response not successful", "statuscode: "+statusCode);
                }
            }
            @Override
            public void onFailure(Call<ServerObject> call, Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class SaveIntoDatabase extends Thread {

        @Override
        public void run() {

            for (Location loc: locList) {
                dbHelper.insertLocation(loc);
                Log.i("Home", "Saving Loc");
            }
            for (Floor floor: floorList) dbHelper.insertFloor(floor);
            for (Route route: routeList) {
                List<Path> pathList = route.getPaths();

                dbHelper.insertRoute(route);

                for (Path path: pathList) {
                    dbHelper.insertPath(path);
                    dbHelper.insertRoutePath(route.getRouteId(), path.getPathId());
                }
            }



            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.updated_database), true);
            editor.apply();
        }
    }
}
