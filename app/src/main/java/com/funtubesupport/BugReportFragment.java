package com.funtubesupport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.funtubesupport.api.apiClient;
import com.funtubesupport.api.apiRest;
import com.funtubesupport.model.ApiResponse;

import java.net.URLEncoder;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class BugReportFragment extends Fragment {

    public BugReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bug_report, container, false);
        final EditText email = view.findViewById(R.id.email);
        final EditText name = view.findViewById(R.id.full_name);
        final EditText message = view.findViewById(R.id.message);
        ImageView whatsapp = view.findViewById(R.id.whatsapp);
        ImageView messenger = view.findViewById(R.id.messenger);
        ImageView mail = view.findViewById(R.id.emailimage);

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager packageManager = getActivity().getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);
                try{
                    String url = "https://api.whatsapp.com/send?phone=17863759505&text="+ URLEncoder.encode("Hi, I need help", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if(i.resolveActivity(packageManager) != null){
                        getActivity().startActivity(i);
                    }
                }
                catch (Exception e){
                    Toasty.error(getActivity(), e.getMessage(), Toasty.LENGTH_SHORT).show();
                }
//                Intent sendintent = new Intent("android.intent.action.MAIN");
//                String formattedPhoneNumber = "17863759505";
//                sendintent.setAction(Intent.ACTION_SEND);
//                sendintent.putExtra(Intent.EXTRA_TEXT, "Hello, I need help");
//                sendintent.setType("text/plain");
//                sendintent.setPackage("com.whatsapp");
//                sendintent.putExtra("jid", formattedPhoneNumber+"@s.whatsapp.net");
//                startActivity(sendintent);
            }
        });

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"sherifffoladejo@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

                /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });



        Button send = view.findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString().trim();
                String nameString= name.getText().toString().trim();
                String messageString = message.getText().toString();
                if(!emailString.equals("") && isValid(emailString)){
                    if(!nameString.isEmpty() && !messageString.isEmpty()){
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Retrofit retrofit = apiClient.getClient();
                        apiRest service = retrofit.create(apiRest.class);
                        Call<ApiResponse> call = service.addSupport(emailString,nameString,messageString);
                        call.enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if(response.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toasty.success(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                                    email.setText("");
                                    message.setText("");
                                    name.setText("");
                                }else{
                                    progressDialog.dismiss();
                                    Toasty.error(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                progressDialog.dismiss();
                                Toasty.error(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toasty.error(getActivity(), "Invalid Email", Toasty.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toasty.error(getActivity(), "Invalid Email", Toasty.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}
