package com.app.lsnhdsteth.hdsteth;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.hdstethsdk.hdsteth.ConnectToHDSteth;
import com.android.hdstethsdk.hdsteth.HDSteth;
import com.androidnetworking.widget.ANImageView;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.app.lsnhdsteth.R;
import com.app.lsnhdsteth.model.Advt;
import com.app.lsnhdsteth.model.Item;
import com.app.lsnhdsteth.model.ResponseJsonData;
import com.app.lsnhdsteth.network.ApiResponse;
import com.app.lsnhdsteth.network.NetworkConnectivity;
import com.app.lsnhdsteth.network.Status;
import com.app.lsnhdsteth.ui.calendar.CalendarGridAdapter;
import com.app.lsnhdsteth.ui.calendar.UtilCalendar;
import com.app.lsnhdsteth.utils.Constant;
import com.app.lsnhdsteth.utils.DataManager;
import com.app.lsnhdsteth.utils.MyApplication;
import com.app.lsnhdsteth.utils.MySharePrefernce;
import com.app.lsnhdsteth.utils.Utility;
import com.example.example.HdStethRecords;
import com.example.example.Rec;
import com.example.example.ResponseMessage;
import com.google.gson.Gson;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainGraphActivity extends AppCompatActivity implements View.OnClickListener, CalendarRecordAdapter.ListItemClickListener {


    private XYPlot plotECG, plotHS;
    private Redrawer redrawerECG, redrawerHS;
    private String[] permissionM = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] permissionS = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageView bck, recrd, mail, history;
    private BluetoothDevice connectedDevice = null;
    HDStethCallBack hdStethCallBack;
    ConnectToHDSteth connectToHDSteth;
    MyFadeFormatter ecgFormatter, hsFormatter, murFormatter;
    ECGModel ecgSeries, hsSeries, murSeries;
    LinearLayout llGraph, ll_calndrVew;
    int PCG_LOWER_BOUND, PCG_UPPER_BOUND, ECG_LOWER_BOUND, ECG_UPPER_BOUND;
    int graphIndex = 0, murIndex = 0, hsIndex = 0;
    BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    Context context;
    private boolean isPlotting = true;

    int mail_color_disable = Color.parseColor("#848282");
    int mail_color_enable = Color.parseColor("#ffffff");
    int next_color_enable = Color.parseColor("#000000");


    // customization
    ImageView bottom_iv;
    VideoView bottom_vid;
    TextView dr_name_tv, date_tv, time_tv, no_record_tv, calendar_date_tv, counter_tv;
    RecyclerView records_rv;
    RelativeLayout bottom_advt_rl, report_rl, root_rl;
    LinearLayout img_progress_ll, loading_ll;
    CalendarView calendarView;
    ViewPager vp_image_pager;
    SpringDotsIndicator springDotsIndicator;
    // target component
    LinearLayout target_rl;
    ImageView target_iv;
    VideoView target_vid;
    WebView target_wv;
    GridView calendar_gridview;
    CalendarGridAdapter calendarGridAdapter;

    MySharePrefernce pref;
    String selected_date = Utility.getRecordDate();
    int selected_day = Calendar.getInstance().get(Calendar.DATE);
    int selected_month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    String selected_report_id = "";
    String selected_report_path = "";
    String selected_report_time = "";
    int image_count = 0;
    String dr_name = "";

    public static final String REPORT_IMAGE_1 = "screen_1.jpg";
    public static final String REPORT_IMAGE_2 = "screen_2.jpg";


    BroadcastReceiver _broadcastReceiver;
    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("HH:mm:ss");

    // device conected flag
    boolean ble_connected = false;
    int prev_view = -1;
    public static final int prev_view_calendar = 1;
    public static final int prev_view_report = 2;

    // view model
    GraphViewModel graphViewModel;
    CalendarRecordAdapter adapter;
    ImageAdapter img_adapter;
    CountDownTimer yourCountDownTimer;
    int ideal_time = 120;
    String format = "24";

    // calendar
    Calendar c = Calendar.getInstance();
    int day = c.get(Calendar.DATE);
    int month = c.get(Calendar.MONTH);
    int sel_month = c.get(Calendar.MONTH);
    int year = c.get(Calendar.YEAR);
    boolean is_current_month = true;

    ImageView prev_month_iv;
    ImageView next_month_iv;
    ImageView no_internet_iv;
    TextView selected_date_tv;

    // live data
    NetworkConnectivity connectionLiveData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_graph);
        graphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);
        dr_name = getIntent().getStringExtra("dr");
        context = this;
        customInitialize();
        initObserver();
        if (checkPermission()) {
            init();
        } else {
            requestPermisson();
        }
        setDate();
        calendarGridAdapter = new CalendarGridAdapter(UtilCalendar.Companion.getMonth(UtilCalendar.getFirstDay(year, month), month), String.valueOf(day), context, month, sel_month);
        calendar_gridview.setAdapter(calendarGridAdapter);

        calendar_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                initCountDownTimer();
                List list = UtilCalendar.Companion.getMonth(UtilCalendar.getFirstDay(year, month), month);
                int today_date = c.get(Calendar.DATE);
                if (!list.get(i).toString().equals("")) {
                    int selected_day = Integer.parseInt(list.get(i).toString());
                    if (is_current_month && selected_day > today_date) {
                        return;
                    }
                    day = Integer.parseInt(list.get(i).toString());
                    String mon1 = String.valueOf(month + 1);
                    String day1 = String.valueOf(day);
                    if (mon1.length() == 1) mon1 = "0" + mon1;
                    if (day1.length() == 1) day1 = "0" + day1;
                    selected_date = year + "-" + mon1 + "-" + day1;
                    sel_month = month;
                    changeCalendarAdapter();
                }
            }
        });
    }

    private void setDate() {
        if (month == sel_month)
            selected_date_tv.setText(UtilCalendar.Companion.getMonthName(month) + " " + day + "," + year);
        else selected_date_tv.setText(UtilCalendar.Companion.getMonthName(month) + "," + year);
    }

    private void initObserver() {
        // upload file observer
        graphViewModel.get_upload_file_api_result().observe(this, new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {
//                ResponseMessage responseMessage = new Gson().fromJson(apiResponse.getData(),ResponseMessage.class);
//                if(responseMessage.getStatus().equals("success")){
//                    Utility.showToast(context,"Recording Saved");
//                }
            }
        });

        // content result observer
        graphViewModel.get_record_api_result().observe(this, new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {

                isPlotting = false;
//                setVisibility(false, true, false, false, false);

                if (apiResponse.getStatus() == Status.SUCCESS) {
                    String data = apiResponse.getData();
                    HdStethRecords record_data_ = new Gson().fromJson(apiResponse.getData(), HdStethRecords.class);
                    // visible calendar

                    if (record_data_.getRec() != null && record_data_.getRec().size() > 0) {
                        records_rv.setVisibility(View.VISIBLE);
                        no_record_tv.setVisibility(View.GONE);
                        // set adapter
                        if (adapter == null) {
                            adapter = new CalendarRecordAdapter(context, record_data_.getRec(), record_data_.getInfo().getStorage());
                            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                            records_rv.setLayoutManager(layoutManager);
                            records_rv.setItemAnimator(new DefaultItemAnimator());
                            records_rv.setAdapter(adapter);
                            setListener();
                        } else {
                            adapter.setData(record_data_.getRec(), record_data_.getInfo().getStorage());
                            records_rv.scrollToPosition(0);
                        }
                    } else {
                        records_rv.setVisibility(View.GONE);
                        no_record_tv.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // send email result observer
        graphViewModel.get_email_api_result().observe(this, new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {
                initCountDownTimer();
                loading_ll.setVisibility(View.GONE);
                ResponseMessage responseMessage = new Gson().fromJson(apiResponse.getData(), ResponseMessage.class);
                Utility.showToast(context, responseMessage.getDesc());
            }
        });
    }

    private void setListener() {
        adapter.setListerner(this);
    }

    private void customInitialize() {
        bottom_iv = findViewById(R.id.bottom_ad_img);
        bottom_vid = findViewById(R.id.bottom_ad_video);
        dr_name_tv = findViewById(R.id.tv_dr_name);
        date_tv = findViewById(R.id.tv_date);
        time_tv = findViewById(R.id.tv_time);
        no_record_tv = findViewById(R.id.tv_no_records);
        calendar_date_tv = findViewById(R.id.tv_calendar_date);
        counter_tv = findViewById(R.id.counter_tv);
        records_rv = findViewById(R.id.rv_calendar);
        bottom_advt_rl = findViewById(R.id.rl_bottom_advert);
        report_rl = findViewById(R.id.rl_report);
        root_rl = findViewById(R.id.rl_graph_root_layout);
        img_progress_ll = findViewById(R.id.ll_progress);
        loading_ll = findViewById(R.id.ll_loading);
        calendarView = findViewById(R.id.calendarView);
        vp_image_pager = findViewById(R.id.vp_image_pager);
        springDotsIndicator = findViewById(R.id.pager_indicator);
        target_rl = findViewById(R.id.rl_target);
        target_iv = findViewById(R.id.iv_target);
        target_vid = findViewById(R.id.vv_target);
        target_wv = findViewById(R.id.wv_target);
        calendar_gridview = findViewById(R.id.calendar_gridview);
        next_month_iv = findViewById(R.id.iv_next_month);
        no_internet_iv = findViewById(R.id.iv_graph_no_internet);
        prev_month_iv = findViewById(R.id.iv_prev_month);
        selected_date_tv = findViewById(R.id.tv_selected_date);

        next_month_iv.setColorFilter(mail_color_disable);

        // screen always on mode
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pref = new MySharePrefernce(context);
        calendar_date_tv.setText(selected_date);
        playContent(0);

        if (!checkNetwork()) no_internet_iv.setVisibility(View.VISIBLE);
        NetworkConnectivity connectionLiveData = new NetworkConnectivity(getApplication());
        connectionLiveData.observe(this, internet -> {
//            if(internet) Utility.showToast(context,"internet");
//            else Utility.showToast(context,"No internet");
            if (internet) no_internet_iv.setVisibility(View.GONE);
            else no_internet_iv.setVisibility(View.VISIBLE);
        });

        root_rl.setOnClickListener(this);
    }

    /**
     * Checking Permission Start
     **/
    boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.M && SDK_INT <= Build.VERSION_CODES.R) {
            int loc = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int fineLoc = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int camera = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.CAMERA);
            int recordAudio = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.RECORD_AUDIO);
            int readStorage = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStorage = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return loc == PackageManager.PERMISSION_GRANTED && fineLoc == PackageManager.PERMISSION_GRANTED
                    && camera == PackageManager.PERMISSION_GRANTED && recordAudio == PackageManager.PERMISSION_GRANTED
                    && readStorage == PackageManager.PERMISSION_GRANTED && writeStorage == PackageManager.PERMISSION_GRANTED;
        } else if (SDK_INT >= Build.VERSION_CODES.S) {
            int bleScn = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.BLUETOOTH_SCAN);
            int bleConnect = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.BLUETOOTH_CONNECT);
            int bleAdv = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE);
            int readStorage = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStorage = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int loc = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int fineLoc = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int camera = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.CAMERA);
            int recordAudio = ContextCompat.checkSelfPermission(MainGraphActivity.this, Manifest.permission.RECORD_AUDIO);

            return bleScn == PackageManager.PERMISSION_GRANTED && bleConnect == PackageManager.PERMISSION_GRANTED &&
                    bleAdv == PackageManager.PERMISSION_GRANTED && loc == PackageManager.PERMISSION_GRANTED &&
                    fineLoc == PackageManager.PERMISSION_GRANTED && camera == PackageManager.PERMISSION_GRANTED &&
                    recordAudio == PackageManager.PERMISSION_GRANTED && readStorage == PackageManager.PERMISSION_GRANTED &&
                    writeStorage == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermisson() {
        if (SDK_INT >= Build.VERSION_CODES.M && SDK_INT <= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(MainGraphActivity.this, permissionM, 1);
        } else {
            ActivityCompat.requestPermissions(MainGraphActivity.this, permissionS, 3);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                init();
            }
        } else if (requestCode == 3) {
            if (grantResults.length > 0) {
                init();
            }
        }
    }

    /**
     * Checking Permission End
     **/


    public void init() {
        bck = findViewById(R.id.bck);
        recrd = findViewById(R.id.recrd);
        mail = findViewById(R.id.mail);
        history = findViewById(R.id.history);
        PCG_LOWER_BOUND = 0;
        PCG_UPPER_BOUND = 64;
        ECG_LOWER_BOUND = 0;
        ECG_UPPER_BOUND = 64;

        llGraph = (LinearLayout) findViewById(R.id.llGraph);
        ll_calndrVew = findViewById(R.id.ll_calendr_view);

        mail.setColorFilter(mail_color_disable);


        setVisibility(true, false, false, false, false);

        plotECG = (XYPlot) findViewById(R.id.plot2);
        plotHS = (XYPlot) findViewById(R.id.plot1);

        ecgSeries = new ECGModel(3200, 200);
        hsSeries = new ECGModel(3200, 200);
        murSeries = new ECGModel(3200, 200);


        murFormatter = new MyFadeFormatter(3200, getResources().getColor(R.color.color4));
        murFormatter.setLegendIconEnabled(false);

        ecgFormatter = new MyFadeFormatter(3200, getResources().getColor(R.color.color12));
        ecgFormatter.setLegendIconEnabled(false);


        hsFormatter = new MyFadeFormatter(3200, getResources().getColor(R.color.color3));
        hsFormatter.setLegendIconEnabled(false);


        plotECG.addSeries(ecgSeries, ecgFormatter);
        plotHS.addSeries(hsSeries, hsFormatter);
        plotHS.addSeries(murSeries, murFormatter);

        XYGraphWidget widget = plotHS.getGraph();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            plotHS.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }
        int color = Color.TRANSPARENT;
        plotHS.getBorderPaint().setColor(color);
        plotHS.getBackgroundPaint().setColor(color);
        widget.getBackgroundPaint().setColor(color);
        widget.getGridBackgroundPaint().setColor(color);


        widget = plotECG.getGraph();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            plotECG.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        plotECG.getBorderPaint().setColor(color);
        plotECG.getBackgroundPaint().setColor(color);
        widget.getBackgroundPaint().setColor(color);
        widget.getGridBackgroundPaint().setColor(color);


        plotHS.setRangeBoundaries(PCG_LOWER_BOUND, PCG_UPPER_BOUND, BoundaryMode.FIXED);
        plotHS.setDomainBoundaries(0, 3200, BoundaryMode.FIXED);

//
        plotECG.setRangeBoundaries(ECG_LOWER_BOUND, ECG_UPPER_BOUND, BoundaryMode.FIXED);
        plotECG.setDomainBoundaries(0, 3200, BoundaryMode.FIXED);


        plotECG.setLinesPerRangeLabel(1);
        plotHS.setLinesPerRangeLabel(1);

        llGraph.setBackground(getResources().getDrawable(R.drawable.newgraph));

        plotHS.getGraph().setDomainGridLinePaint(null);
        plotHS.getGraph().setRangeGridLinePaint(null);

        plotECG.getGraph().setDomainGridLinePaint(null);
        plotECG.getGraph().setRangeGridLinePaint(null);


        ecgSeries.start(new WeakReference<>(plotECG.getRenderer(AdvancedLineAndPointRenderer.class)));
        hsSeries.start(new WeakReference<>(plotHS.getRenderer(AdvancedLineAndPointRenderer.class)));
        murSeries.start(new WeakReference<>(plotECG.getRenderer(AdvancedLineAndPointRenderer.class)));

        // set a redraw rate of 30hz and start immediately:
        redrawerECG = new Redrawer(plotECG, 30, true);
        redrawerHS = new Redrawer(plotHS, 30, true);


        hdStethCallBack = new HDStethCallBack();

        connectToHDSteth = new ConnectToHDSteth();
        connectToHDSteth.init(context, hdStethCallBack);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(MainGraphActivity.this)
                    .setMessage("Location is turned Off.")
                    .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                showBluetoothDialog();
            } else {

                if (connectedDevice == null) {
                    connectToHDSteth.fnDetectDevice(context);
                } else {
                    connectToHDSteth.fnConnectDevice(context, connectedDevice);
                }
            }

        }


        bck.setOnClickListener(this);
        recrd.setOnClickListener(this);
        mail.setOnClickListener(this);
        history.setOnClickListener(this);

        next_month_iv.setOnClickListener(this);
        prev_month_iv.setOnClickListener(this);
    }

    private void showBluetoothDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainGraphActivity.this);
        builderSingle.setTitle("Turn On Bluetooth");
        builderSingle.setMessage("Bluetooth needs to be turned on to see the auscalation");
        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1000);
            }
        });
        AlertDialog dialog = builderSingle.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
    }

    public void initCountDownTimer() {

        // upload pending files due to internet
//        File f1 = new File(getExternalFilesDir(""), "HDSteth");
//        if(f1.isDirectory()){
//            if(checkNetwork())
//                if(f1.listFiles().length>0){
//                    graphViewModel.uploadZipFile(pref,f1.listFiles()[0].getAbsolutePath(),dr_name,false);
//                }
//        }

        if (yourCountDownTimer != null) yourCountDownTimer.cancel();
        yourCountDownTimer = new CountDownTimer(ideal_time * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                counter_tv.setText(millisUntilFinished + "");
            }

            public void onFinish() {
                finish();
            }
        }.start();
    }

    public void stopCounter() {
        if (yourCountDownTimer != null) yourCountDownTimer.cancel();
    }

    @Override
    public void onClick(View view) {
        initCountDownTimer();
        stopSound();
        int v_month = c.get(Calendar.MONTH);
        int v_year = c.get(Calendar.YEAR);

        switch (view.getId()) {

            case R.id.iv_next_month:
                if (v_year > year || v_year == year && v_month >= month + 1) {
                    month = month + 1;
                    if (month == 12) {
                        month = 0;
                        year = year + 1;
                    }
                    ;
                    if (v_month == month && year == v_year) {
                        is_current_month = true;
                        next_month_iv.setColorFilter(mail_color_disable);
                    } else {
                        is_current_month = false;
                        next_month_iv.setColorFilter(next_color_enable);
                    }
                    changeCalendarAdapter();
                }
                break;

            case R.id.iv_prev_month:
                next_month_iv.setColorFilter(next_color_enable);
                month = month - 1;
                if (month == -1) {
                    month = 11;
                    year = year - 1;
                }
                if (v_month == month && year == v_year) is_current_month = true;
                else is_current_month = false;
                changeCalendarAdapter();
                break;
            case R.id.bck:
                stopSound();
                if (prev_view == prev_view_report) {
                    prev_view = prev_view_calendar;
                    setReport();
                } else if (prev_view == prev_view_calendar || report_rl.getVisibility() == View.VISIBLE) {
                    prev_view = -1;
                    openCalendar();
                } else {
                    setVisibility(true, false, false, false, false);
                    isPlotting = true;
                    if (ble_connected) return;

                    if (connectedDevice != null) {
                        connectToHDSteth.fnConnectDevice(context, connectedDevice);
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            showBluetoothDialog();
                        } else {
                            connectToHDSteth.fnDetectDevice(context);
                        }
                    }
                }
                break;
            case R.id.recrd:
                stopCounter();
                if (llGraph.getVisibility() == View.GONE) {
                    return;
                }
                if (!ble_connected) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        showBluetoothDialog();
                    } else {
                        connectToHDSteth.fnDetectDevice(context);
                    }

                    return;
                }
                File f1 = new File(getExternalFilesDir(""), "HDSteth");
                if (!f1.exists()) {
                    if (f1.mkdirs()) {
                        Log.i("Folder ", "created");
                    }
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD_HH-mm-ss");
                String sTime = simpleDateFormat.format(new Date());

                f1 = new File(f1, sTime);
                if (!f1.exists()) {
                    if (f1.mkdirs()) {
                        Log.i("Folder ", "created");
                    }
                }
                Utility.showToast(context, "Recording Start");
                stopCounter();
                int r_time = pref.getIntData(MySharePrefernce.KEY_RECORD_TIME);
                connectToHDSteth.fnRecordData(context, f1.getAbsolutePath(), r_time);
                break;

            case R.id.mail:
                if (report_rl.getVisibility() == View.VISIBLE) {
                    isPlotting = false;
                    stopCounter();
                    Dialog dialog = new Dialog(this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialog_dialog);

                    EditText email_et = dialog.findViewById(R.id.et_dia_emailaddress);
                    Button send_bt = dialog.findViewById(R.id.bt_dia_send);
                    Button cancel_bt = dialog.findViewById(R.id.bt_dia_cancel);
                    TextView file_name = dialog.findViewById(R.id.tv_dia_file);
                    if (format.equals("24"))
                        file_name.setText(Utility.changeDateFormat(selected_date) + " | " + selected_report_time);
                    else
                        file_name.setText(Utility.changeDateFormat(selected_date) + " | " + Utility.getFormatedTime(selected_report_time));
                    send_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkNetwork()) {
                                String email = email_et.getText().toString();
                                if (Utility.isValidEmail(email)) {
                                    dialog.dismiss();
                                    loading_ll.setVisibility(View.VISIBLE);
                                    graphViewModel.sendEmail(selected_report_id, email);
                                } else
                                    Utility.showToast(context, "Please enter valid email address");
                            }
                        }
                    });
                    cancel_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
                break;
            case R.id.history:
                openCalendar();
                break;
        }
    }

    private void stopSound() {
        if(img_adapter!=null)img_adapter.changeScroll();
        if(target_vid.getVisibility()==View.VISIBLE && target_vid.isPlaying()){
            target_vid.stopPlayback();
            target_vid.setVideoURI(null);
        }
    }

    private void changeCalendarAdapter() {
        if (calendarGridAdapter != null) {
            setDate();
            calendarGridAdapter.clear(UtilCalendar.Companion.getMonth(UtilCalendar.getFirstDay(year, month), month)
                    , String.valueOf(day), month, sel_month);
            String response = pref.getJsonData();
            ResponseJsonData data_obj = new Gson().fromJson(response, ResponseJsonData.class);
            if (checkNetwork()) graphViewModel.fetchRecords(String.valueOf(data_obj.getDevice().get(0).getId()), selected_date);
        }
    }

    @Override
    public void onItemClick(@NonNull Rec rec, String storage) {
        initCountDownTimer();
        selected_report_id = String.valueOf(rec.getId());
        selected_report_path = storage + rec.getDir();
        selected_report_time = rec.getTime();
        image_count = rec.getNoOfPages();
        setReport();
    }

    public void setReport() {

        setVisibility(false, false, true, false, false);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= image_count; i++) {
            String path = selected_report_path + "screen_" + i + ".jpg";
            Log.d("TAG", "setReport: imagepath-" + path);
            list.add(path);
        }

//        list.add(selected_report_path+REPORT_IMAGE_1);
//        list.add(selected_report_path+REPORT_IMAGE_2);
        img_adapter = new ImageAdapter(context, list, selected_report_path);
        vp_image_pager.setAdapter(img_adapter);
        springDotsIndicator.setViewPager(vp_image_pager);
        vp_image_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                initCountDownTimer();
                stopSound();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @SuppressLint("ResourceType")
    public void openCalendar() {
        setVisibility(false,true,false,false,false);
        String response = pref.getJsonData();
        ResponseJsonData data_obj = new Gson().fromJson(response, ResponseJsonData.class);
        graphViewModel.fetchRecords(String.valueOf(data_obj.getDevice().get(0).getId()), selected_date);
    }

    public void playAudio() {
        try {
            Uri uri = Uri.parse(selected_report_path + "wav.wav");
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
            player.setDataSource(this, uri);
            player.prepare();
            player.start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public class HDStethCallBack implements HDSteth {

        Dialog dialog = new Dialog(context);

        @Override
        public void fnReceiveData(String sPointType, int iPoint) {
            if (isPlotting) {
                if (sPointType.equalsIgnoreCase("ecg")) {
                    if (graphIndex < 3200 - 1) {
                        graphIndex++;
                        ecgSeries.data.add(iPoint);
                    } else {
                        ecgSeries.data.remove(0);
                        ecgSeries.data.add(iPoint);
                    }
                    if (redrawerECG == null) {
                        redrawerECG = new Redrawer(plotECG, 30, true);
                    }
                    if (redrawerECG != null) {
                        redrawerECG.start();
                    }
                } else if (sPointType.equalsIgnoreCase("mur")) {
                    if (murIndex < 3200 - 1) {
                        murIndex++;
                        murSeries.data.add(iPoint);
                    } else {
                        murSeries.data.remove(0);
                        murSeries.data.add(iPoint);

                    }
                    if (redrawerHS == null) {
                        redrawerHS = new Redrawer(plotHS, 30, true);
                    }
                    if (redrawerHS != null) {
                        redrawerHS.start();
                    }
                } else if (sPointType.equalsIgnoreCase("hs")) {
                    if (hsIndex < 3200 - 1) {
                        hsIndex++;
                        hsSeries.data.add(iPoint);
                    } else {
                        hsSeries.data.remove(0);
                        hsSeries.data.add(iPoint);

                    }
                    if (redrawerHS == null) {
                        redrawerHS = new Redrawer(plotHS, 30, true);
                    }
                    if (redrawerHS != null) {
                        redrawerHS.start();
                    }
                }
            }
        }

        @Override
        public void fnDetectDevice(ArrayList<BluetoothDevice> bluetoothDevices) {

            stopCounter();
            ArrayList<String> devicesNames = new ArrayList<String>();
            for (BluetoothDevice device : bluetoothDevices) {
                devicesNames.add(device.getName() + "( " + device.getAddress() + " )");
            }

            if (!dialog.isShowing()) {

                Log.d("TAG", "fnDetectDevice: 1111");

//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_no_device);

                TextView cancel_tv = dialog.findViewById(R.id.tv_scandia_cancel);
                TextView title_tv = dialog.findViewById(R.id.tv_dialog_title);
                TextView rescan_tv = dialog.findViewById(R.id.tv_scandia_rescan);
                ListView listView = dialog.findViewById(R.id.lv_scandia_list);

                if (devicesNames.size() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, devicesNames);
                    listView.setAdapter(arrayAdapter);
                } else {
                    title_tv.setText("No Device Found");
                    listView.setVisibility(View.GONE);
                }

                cancel_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        initCountDownTimer();
                    }
                });
                rescan_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectToHDSteth.fnDetectDevice(context);
                        dialog.dismiss();
                    }
                });
                try {
                    if(llGraph.getVisibility()==View.VISIBLE)dialog.show();
                }catch (Exception ex){}

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        initCountDownTimer();
                        connectToHDSteth.fnConnectDevice(context, bluetoothDevices.get(i));
                        connectedDevice = bluetoothDevices.get(i);
                        dialog.dismiss();
                    }
                });
            }
        }

        @Override
        public void fnRecordData(String[] sPaths) {
            try {
                if (!sPaths[0].equals("ECG File not Saved")) {
                    String msg = sPaths[0];
                    Log.d("fnRecordData", sPaths[0]);
                    initCountDownTimer();
                    Utility.showToast(context, "Recording Complete");
                    if (checkNetwork())
                        graphViewModel.uploadZipFile(pref, sPaths[0], dr_name, true);
                }
            } catch (Exception e) {
                Log.e("ConsentMedia", e.getMessage());
            }
        }

        @Override
        public void fnDisconnected() {
            connectedDevice = null;
            Log.d("TAG", "fnDisconnected: Device Disconnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_connected = false;
                    Utility.showToast(context, "HD Steth Disconnected");
                }
            });
        }

        @Override
        public void fnConnected(String sDeviceType) {
            ECG_LOWER_BOUND = 0;
            ECG_UPPER_BOUND = 4096;

            plotECG.setRangeBoundaries(ECG_LOWER_BOUND, ECG_UPPER_BOUND, BoundaryMode.FIXED);
            plotECG.setDomainBoundaries(0, 3200, BoundaryMode.FIXED);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_connected = true;
                    Utility.showToast(context, "HD Steth Connected");
                }
            });
        }


    }


    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;
        int color;

        MyFadeFormatter(int trailSize, int color) {
            this.trailSize = trailSize;
            this.color = color;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if (thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset = latestIndex - thisIndex;
            }
            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            getLinePaint().setColor(color);
            return getLinePaint();
        }
    }

    void playContent(int pos) {

        String data = getIntent().getStringExtra("advt");
        Item item_obj = new Gson().fromJson(data, Item.class);
        if (item_obj != null) dr_name_tv.setText("Dr. " + item_obj.getDr());
        String setting = item_obj.getSettings();
        try {
            JSONObject object = new JSONObject(setting);
            ideal_time = object.getInt("iTime");
            int rTime = object.getInt("rTime");
            String lang = object.getString("lang");
            format = object.getString("format");
            pref.putStringData(MySharePrefernce.KEY_LANGUAGE, lang);
            pref.putStringData(MySharePrefernce.KEY_TIME_FORMAT, format);
            pref.putIntData(MySharePrefernce.KEY_RECORD_TIME, rTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        date_tv.setText(Utility.getOnlydate());
        if (format.equals("24")) time_tv.setText(Utility.getOnlytime().substring(0, 5));
        else time_tv.setText(Utility.getFormatedTime(Utility.getOnlytime()));
        if (item_obj != null && item_obj.getAdvt() != null && item_obj.getAdvt().size() > 0) {
            if (pos < item_obj.getAdvt().size()) {
                if (item_obj.getAdvt().get(pos).getType().equals(Constant.CONTENT_IMAGE)) {
                    loadImage(item_obj.getAdvt().get(pos), pos);
                }
                if (item_obj.getAdvt().get(pos).getType().equals(Constant.CONTENT_VIDEO)) {
                    playVideo(item_obj.getAdvt().get(pos), pos);
                }
            } else {
                playContent(0);
            }
        } else {
            bottom_advt_rl.setVisibility(View.GONE);
        }
    }

    // load image
    void loadImage(Advt advert, int pos) {

        String file = advert.getFileName();
        String path = DataManager.getDirectory() + File.separator + file;

        if (Utility.isFileCompleteDownloaded(file, advert.getFilesize())) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            bottom_iv.setImageBitmap(BitmapFactory.decodeFile(path, options));
            bottom_vid.setVisibility(View.GONE);
            bottom_iv.setVisibility(View.VISIBLE);

            bottom_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initCountDownTimer();
                    stopSound();
                    isPlotting = false;
                    setVisibility(false, false, false, true, false);
                    setTarget(advert);
                }
            });

            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            pref.createReport(advert.getId(), advert.getDuration());
                            bottom_iv.setImageBitmap(null);
                            playContent(pos + 1);
                        }
                    }, (long) (advert.getDuration() * 1000)
            );

        } else {
            playContent(pos + 1);
        }
    }

    private void setTarget(Advt advert) {
        if (advert.getT().get(0).getTType().equals(Constant.TARGET_CONTENT_CONTENT)) {
            if (advert.getT().get(0).getType().equals(Constant.CONTENT_IMAGE)) {
                String path = DataManager.getDirectory() + File.separator + advert.getT().get(0).getFileName();

                if (Utility.isFileCompleteDownloaded(advert.getT().get(0).getFileName(), advert.getT().get(0).getFilesize())) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inJustDecodeBounds = false;

                    target_iv.setImageBitmap(BitmapFactory.decodeFile(path, options));
                    target_iv.setVisibility(View.VISIBLE);
                    target_vid.setVisibility(View.GONE);
                    target_wv.setVisibility(View.GONE);
                }
            }
            if (advert.getT().get(0).getType().equals(Constant.CONTENT_VIDEO)) {
                String path = DataManager.getDirectory() + File.separator + advert.getT().get(0).getFileName();
                if (Utility.isFileCompleteDownloaded(advert.getT().get(0).getFileName(), advert.getT().get(0).getFilesize())) {
                    target_iv.setVisibility(View.GONE);
                    target_vid.setVisibility(View.VISIBLE);
                    target_wv.setVisibility(View.GONE);

                    target_vid.setVideoPath(path);
                    target_vid.start();

                    target_vid.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            target_vid.start();
                        }
                    });
                }
            }
        }
        if (advert.getT().get(0).getTType().equals(Constant.TARGET_CONTENT_URL)) {
            target_iv.setVisibility(View.GONE);
            target_vid.setVisibility(View.GONE);
            target_wv.setVisibility(View.VISIBLE);
            target_wv.getSettings().setJavaScriptEnabled(true);
            target_wv.setWebViewClient(new WebViewClient());
            target_wv.loadUrl(advert.getT().get(0).getUrl());
        }
    }

    // play local storage video
    private void playVideo(Advt advert, int pos) {

        String file = advert.getFileName();
        String path = DataManager.getDirectory() + File.separator + file;
        Log.d("file_path- ", path);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(this, context.getPackageName() + ".provider", new File(path));
        }

        if (Utility.isFileCompleteDownloadedForPlay(file, advert.getFilesize(), this)) {
            Log.d("file_path_exist- ", path);

            bottom_vid.setVideoPath(path);
            bottom_vid.start();
            bottom_iv.setVisibility(View.GONE);
            bottom_vid.setVisibility(View.VISIBLE);

            MediaController mc = new MediaController(this);
            mc.setVisibility(View.GONE);
            bottom_vid.setMediaController(mc);

            bottom_vid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initCountDownTimer();
                    stopSound();
                    isPlotting = false;
                    setVisibility(false, false, false, true, false);
                    setTarget(advert);
                }
            });
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (bottom_vid.isPlaying() == true) {
                                bottom_vid.stopPlayback();
                                bottom_vid.setVideoURI(null);
                            }
                        }
                    }, (long) (advert.getDuration() * 1000)
            );

        } else {
            playContent(pos + 1);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
                    if (format.equals("24"))
                        time_tv.setText(_sdfWatchTime.format(new Date()).substring(0, 5));
                    else time_tv.setText(Utility.getFormatedTime(_sdfWatchTime.format(new Date())));
            }
        };
        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
        finish();
    }

    // set visibility
    public void setVisibility(boolean graph, boolean calendar, boolean report, boolean target, boolean progress) {
//        initCountDownTimer();
        if (target) {
            if (ll_calndrVew.getVisibility() == View.VISIBLE) prev_view = prev_view_calendar;
            if (report_rl.getVisibility() == View.VISIBLE) prev_view = prev_view_report;
            target_rl.setVisibility(View.VISIBLE);
        } else target_rl.setVisibility(View.GONE);
        if (graph) {
            recrd.setColorFilter(mail_color_enable);
            llGraph.setVisibility(View.VISIBLE);
        } else {
            recrd.setColorFilter(mail_color_disable);
            llGraph.setVisibility(View.GONE);
        }
        if (calendar){
//            Utility.showToast(context,"change calendar");
            day = c.get(Calendar.DATE);
            month = c.get(Calendar.MONTH);
            sel_month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
            is_current_month = true;
            setDate();
            calendarGridAdapter = new CalendarGridAdapter(UtilCalendar.Companion.getMonth(UtilCalendar.getFirstDay(year, month), month), String.valueOf(day), context, month, sel_month);
            calendar_gridview.setAdapter(calendarGridAdapter);

            String response = pref.getJsonData();
            ResponseJsonData data_obj = new Gson().fromJson(response, ResponseJsonData.class);
            selected_date = Utility.getRecordDate();
            if (checkNetwork()) graphViewModel.fetchRecords(String.valueOf(data_obj.getDevice().get(0).getId()), selected_date);

            ll_calndrVew.setVisibility(View.VISIBLE);
        }
        else ll_calndrVew.setVisibility(View.GONE);
        if (report) {
            mail.setColorFilter(mail_color_enable);
            report_rl.setVisibility(View.VISIBLE);
        } else {
            mail.setColorFilter(mail_color_disable);
            report_rl.setVisibility(View.GONE);
        }
        if (progress || report) img_progress_ll.setVisibility(View.VISIBLE);
        else img_progress_ll.setVisibility(View.GONE);
    }

    private boolean checkNetwork() {
        boolean wifiAvailable = false;
        boolean mobileAvailable = false;
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    wifiAvailable = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    mobileAvailable = true;
        }
        return wifiAvailable || mobileAvailable;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ble_connected) connectToHDSteth.fnDisconnectDevice(context);
        stopSound();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopSound();
    }
}