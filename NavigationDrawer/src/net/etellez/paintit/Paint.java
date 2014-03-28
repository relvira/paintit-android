package net.etellez.paintit;

import java.util.Timer;
import java.util.TimerTask;

import android.app.*;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

public class Paint extends Activity implements View.OnTouchListener {

    // Navitation drawer stuff
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mColorTitles;
    private int mGrosor;
    private ImageView imageView;

    // Painting stuff
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private android.graphics.Paint paint;
    private int BrushColor;
    private float previousLocationX = 0, previousLocationY = 0, actualLocationX = 0, actualLocationY = 0; // Finger location variables

    // Create timer handler, timer and it's timertask
    final Handler handler = new Handler();
    final Timer timer = new Timer(false);
    private TimerTask timerTask;
    private SeekBar seekbar;

    /**
     * Class constructor
     */
    public Paint() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BUTTON & EDIT TEXT CASTING
        imageView = (ImageView) this.findViewById(R.id.imageView1);
        RelativeLayout r = (RelativeLayout) findViewById(R.id.relLayout);

        mTitle = mDrawerTitle = getTitle();
        /* Get the menu xml and use it! */
        mColorTitles = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        // GET DISPLAY DIMENSIONS
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int dw = metrics.widthPixels;
        int dh = metrics.heightPixels - 250; // CRAP!! no time to fix this

        // Create Bitmap & canvas. Set default propierties for painting brush
        BrushColor = Color.BLUE;
        mBitmap = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        paint = new android.graphics.Paint();
        paint.setStrokeWidth(10);
        imageView.setImageBitmap(mBitmap);
        imageView.setOnTouchListener(this);


        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mColorTitles){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.parseColor("#FFFFFF")); // text color

                // This is very dirty...
                try {
                    if (position == 0){ //blue
                        view.setBackgroundColor(Color.parseColor("#ff33b5e5"));
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 1){ //red
                        view.setBackgroundColor(Color.parseColor("#ffff4444"));
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 2){ // green
                        view.setBackgroundColor(Color.parseColor("#ff99cc00"));
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 3){ // black
                        view.setBackgroundColor(Color.parseColor("#000000"));
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 4){ // white
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        ((TextView) view).setTextColor(Color.parseColor("#000000")); // text color
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 5){ // yellow
                        view.setBackgroundColor(Color.parseColor("#FFFF00"));
                        ((TextView) view).setTextColor(Color.parseColor("#000000")); // text color
                        ((TextView) view).setTextSize(15.0f);
                    } else if(position == 6){ // pink
                        view.setBackgroundColor(Color.parseColor("#FF00FF"));
                        ((TextView) view).setTextSize(15.0f);
                    }
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return view;
            }
        });

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }catch(NullPointerException e) {
            e.printStackTrace();
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

        // This timer will paint while i'm moving my finger
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCanvas.drawLine(actualLocationX, actualLocationY, previousLocationX, previousLocationY, paint);
                        mCanvas.drawCircle(actualLocationX,actualLocationY,mGrosor/2,paint);
                        previousLocationX = actualLocationX;
                        previousLocationY = actualLocationY;
                    }
                });
            }
        };
        timer.schedule(timerTask, 100); // repeat each 100 miliseconds
        timerTask.cancel();

        // seekbar used to manage the thickness of the brush
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGrosor = progress;
                paint.setStrokeWidth(mGrosor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The click listner for ListView in the navigation drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);

        }
    }

    /**
     * Called when we select some item in the menu item list
     * @param position
     */
    private void selectItem(int position) {
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);

        setBrushColor(mColorTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Set Brush color
     * @param title
     */
    public void setBrushColor(CharSequence title) {
        String mTitle = title.toString();

        // Change color to paint with
        if(mTitle.equals("Blue")) {
            BrushColor = Color.parseColor("#ff33b5e5");
        } else if(mTitle.equals("Red")) {
            BrushColor = Color.parseColor("#ffff4444");
        } else if(mTitle.equals("Green")) {
            BrushColor = Color.parseColor("#ff99cc00");
        } else if(mTitle.equals("Black")) {
            BrushColor = Color.parseColor("#000000");
        } else if(mTitle.equals("White")) {
            BrushColor = Color.parseColor("#FFFFFF");
        } else if(mTitle.equals("Yellow")) {
            BrushColor = Color.parseColor("#FFFF00");
        } else if(mTitle.equals("Pink")) {
            BrushColor = Color.parseColor("#FF00FF");
        }
        paint.setColor(BrushColor);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Manage the onTouch actions of the app
     * @param v View recieved
     * @param event MotionEvent recieved
     * @return boolean
     */
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                actualLocationX = event.getX();
                actualLocationY = event.getY();
                previousLocationX = event.getX();
                previousLocationY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                actualLocationX = event.getX();
                actualLocationY = event.getY();
                timerTask.run();
                timerTask.cancel();
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // nothing
                break;
            case MotionEvent.ACTION_CANCEL:
                // nothing
                break;
            default:
                // nothing
                break;
        }
        return true;
    }
}