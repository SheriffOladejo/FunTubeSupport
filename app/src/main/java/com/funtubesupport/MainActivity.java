package com.funtubesupport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.funtubesupport.api.apiClient;
import com.funtubesupport.api.apiRest;
import com.funtubesupport.model.ApiResponse;
import com.funtubesupport.model.Payout;
import com.funtubesupport.model.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private NavigationView nav_view;
    private BottomNavigationView bottom_nav;
    public static PrefManager prf;
    FrameLayout frameLayout;

    public static double rate = 0;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;

    public static ArrayList<Transaction> transactionList = new ArrayList<>();
    public static ArrayList<Payout> withdrawalList = new ArrayList<>();
    public static double totalPoint = 0;
    public static double totalAmount = 0;
    SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottom_nav = findViewById(R.id.bottom_nav);
        frameLayout = findViewById(R.id.framelayout);
        refresh = findViewById(R.id.refresh);
        setSupportActionBar(toolbar);
        prf = new PrefManager(getApplicationContext());

//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Please wait...");
//        dialog.setCancelable(false);
//        dialog.show();
        refresh.setRefreshing(true);


        if(!prf.getString("LOGGED").equals("TRUE")){
            //Toast.makeText(this, "Login", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        else{
            refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getTransactions();
                }
            });
            getTransactions();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        nav_view=findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        String login = prf.getString("LOGGED");
        if(login.equals("TRUE")){
            nav_view.getMenu().findItem(R.id.login).setVisible(false);
            nav_view.getMenu().findItem(R.id.logout).setVisible(true);
        }
        else{
            nav_view.getMenu().findItem(R.id.login).setVisible(true);
            nav_view.getMenu().findItem(R.id.logout).setVisible(false);
        }

        nav_view.setNavigationItemSelectedListener(this);

        bottom_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.home:
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.framelayout, new HomeFragment()).commit();
                        return true;
                    case R.id.earnings:
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.framelayout, new TransactionFragment()).commit();
                        return true;
                    case R.id.withdrawal:
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.framelayout, new WithdrawalFragment()).commit();
                        return true;
                    case R.id.bugs:
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.framelayout, new BugReportFragment()).commit();
                        return true;
                        default:
                            break;
                }
                return false;
            }
        });

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.framelayout, new HomeFragment()).commit();
    }

    public static String truncate(double value){
        return String.format("%.2f", value);
    }

    private void getRateValue(){
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("user_id", prf.getString("ID_USER"));
        params.put("withdrawal", "withdrawal");
        client.get("http://coin2cash.club/new/select_rate.php", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                refresh.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Request Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    String rateString = jsonArray.getJSONObject(0).getString("oneusdtopoints");
                    double rate = Double.valueOf(rateString);
                    setRate(rate);
                    refresh.setRefreshing(false);
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.framelayout, new HomeFragment()).commit();
                    bottom_nav.getMenu().findItem(R.id.home).setChecked(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toasty.error(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getWithdrawals(){
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("user_id", prf.getString("ID_USER"));
        params.put("withdrawal", "withdrawal");
        client.get("http://coin2cash.club/new/select_transactions.php", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Request Failed", Toast.LENGTH_LONG).show();
                refresh.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                withdrawalList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray array =jsonObject.getJSONArray("feed");
                    for(int i=0; i<array.length(); i++){
                        String points = array.getJSONObject(i).getString("points");
                        String amount = array.getJSONObject(i).getString("amount");
                        String created = array.getJSONObject(i).getString("created");
                        String method = array.getJSONObject(i).getString("methode");
                        String account = array.getJSONObject(i).getString("account");
                        String type = array.getJSONObject(i).getString("type");
                        Payout payout = new Payout();
                        payout.setAccount(account);
                        payout.setAmount(amount);
                        payout.setState(type);
                        Log.d(TAG, "amount: " +amount);
                        payout.setPoints(Integer.valueOf(points));
                        payout.setMethod(method);
                        payout.setDate(created);
                        withdrawalList.add(payout);
                    }
                    Log.d(TAG, "withdrawal list size " +withdrawalList.size());
                } catch (JSONException e) {
                    Toasty.error(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                getRateValue();
            }
        });
    }

    private static final String TAG = "MainActivity";
    private void getTransactions(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("user_id", prf.getString("ID_USER"));
        params.put("transaction", "transaction");
        client.get("http://coin2cash.club/new/select_transactions.php", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                refresh.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Request Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                transactionList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray array = jsonObject.getJSONArray("feed");
                    for(int i=0; i<array.length(); i++){
                        String label = array.getJSONObject(i).getString("label");
                        String points = array.getJSONObject(i).getString("points");
                        totalAmount += Double.valueOf(points);
                        String time =array.getJSONObject(i).getString("created");

                        Transaction transaction = new Transaction();
                        transaction.setLabel(label);
                        transaction.setCreated(time);
                        transaction.setPoints(points);
                        transactionList.add(transaction);
                    }
                    getWithdrawals();
                } catch (JSONException e) {
                    Toasty.error(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public void logout(){
        PrefManager prf= new PrefManager(getApplicationContext());
        prf.remove("ID_USER");
        prf.remove("SALT_USER");
        prf.remove("TOKEN_USER");
        prf.remove("NAME_USER");
        prf.remove("TYPE_USER");
        prf.remove("USERN_USER");
        prf.remove("IMAGE_USER");
        prf.remove("LOGGED");
        LoginManager.getInstance().logOut();
        Menu nav_Menu = nav_view.getMenu();
        nav_Menu.findItem(R.id.login).setVisible(false);
        nav_Menu.findItem(R.id.logout).setVisible(true);

        Toast.makeText(getApplicationContext(),"Logged out successfully",Toast.LENGTH_LONG).show();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.login:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                //overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.share:
                startActivity(new Intent(MainActivity.this, InviteFriendsActivity.class));
                return true;
            case R.id.download:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(browserIntent);
                return true;
                //break;
                default:
                    return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String login = prf.getString("LOGGED");
        if(login.equals("TRUE")){
            nav_view.getMenu().findItem(R.id.login).setVisible(false);
            nav_view.getMenu().findItem(R.id.logout).setVisible(true);
        }
        else{
            nav_view.getMenu().findItem(R.id.login).setVisible(true);
        }
        //refresh.setRefreshing(true);
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRate(){
        return this.rate;
    }
}
