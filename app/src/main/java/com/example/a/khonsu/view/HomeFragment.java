package com.example.a.khonsu.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.example.a.khonsu.util.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Fetching data from server starts here, using DatabaseOpenHelper and Retrofit2
 */

public class HomeFragment extends Fragment  {

    // VIEW REFERENCES
    private Button cameraBtn;
    private EditText locationEditText;

    // HELPER OBJECTS
    private ServerAPI mAPI;
    private DatabaseOpenHelper dbHelper;
    private SharedPreferences sharedPref;

    private List<Location> locList = new ArrayList<>();
    private List<Route> routeList = new ArrayList<>();
    private List<Floor> floorList = new ArrayList<>();

    public static String START_LOCATION = "S_LOCATION";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up databaseOpenHelper and Retrofit objects.
        mAPI = ApiUtils.getServerAPI();
        dbHelper = new DatabaseOpenHelper(getContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //fetch data from online JSON if it is not updated
        if(!sharedPref.getBoolean(getString(R.string.updated_database), false)) {
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

        // request permission from users, if granted then direct to ARFinderActivity.
        cameraBtn = v.findViewById(R.id.camera_button);
        setUpCameraBtn();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraBtn.setEnabled(false); //prevent users accidentally hit the button twice
                ((MainActivity)getActivity()).requestCameraPermission();
            }
        });

        // if no sticker is provided, this editText is to input location manually
        locationEditText = v.findViewById(R.id.location_editText);
        locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    locationEditText.setHint(getString(R.string.enter_location_hint));
                } else {
                    locationEditText.setHint("");
                    InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        Button locationBtn = v.findViewById(R.id.location_submit_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEditText.getText() != null) {
                    String searchTerm = locationEditText.getText().toString().trim().toLowerCase();
                    LoadLocation task = new LoadLocation(getContext());
                    task.execute(searchTerm); //find location in another Thread
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CustomDialog dialog = new CustomDialog(getContext(), getString(R.string.dialog_title), getString(R.string.home_dialog_message));
        dialog.show();
        return true;
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

    /*
    Data is saved on metropolia server in JSON. This method performs GET call from Retrofit object to fetch data
     */
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

                    // After fetching successfully, write data into SQLite database
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

    /*
     Write data into SQLite db run on another thread. JSON file contents 4 types of objects: Location, Floor, Route and Path. Each type of objects
     link to others using foreign keys, except each object of Route contains and array of Paths.
      */

    public class SaveIntoDatabase extends Thread {

        @Override
        public void run() {

            for (Location loc: locList) dbHelper.insertLocation(loc);
            for (Floor floor: floorList) dbHelper.insertFloor(floor);
            for (Route route: routeList) {
                // Loop through the list of Paths in a Route and write them to their own table
                List<Path> pathList = route.getPaths();

                dbHelper.insertRoute(route);

                for (Path path: pathList) {
                    dbHelper.insertPath(path);
                    dbHelper.insertRoutePath(route.getRouteId(), path.getPathId());
                }
            }
            Log.i("Home", "Save Complete");
            // set SharedPreferences value to indicate db has been already updated, skip it next time.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.updated_database), true);
            editor.apply();
        }
    }

    // This method search for matching location in database based on location names or sticker uuid.
    private class LoadLocation extends AsyncTask<String, Void, Location> {

        private Context context;

        LoadLocation(Context context) {
            this.context = context;
        }

        @Override
        protected Location doInBackground(String... strings) {
            // the name or sticker uuid of location are passed in this params
            return dbHelper.getStartLocation(strings[0]);
        }

        @Override
        protected void onPostExecute(Location location) {
            if (location == null) {
                Toast.makeText(getContext(), "Location is not found", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, MapNavActivity.class);
                intent.putExtra(START_LOCATION, location);
                startActivity(intent);
            }
        }
    }
}
