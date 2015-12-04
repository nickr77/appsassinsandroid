package teamrocket.appsassins;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class NotificationAlert extends DialogFragment {
    private int notifID;
    private int type;
    private String jsonData;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (type == 3)
            builder.setMessage(R.string.dialog_notifications);
        else
            builder.setMessage(R.string.dialog_confirm_kill);

        //add buttons
        builder.setNegativeButton(R.string.notif_deny, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE Denied MISSILES!
                        Integer whatID = notifID;

                        OkHttpClient client = new OkHttpClient();

                        RequestBody formBody = new FormEncodingBuilder()
                                .add("notifID", whatID.toString())
                                .add("accepted", "0")
                                .build();
                        String url = "http://54.149.40.71/appsassins/api/index.php/getNotifications";
                        url = "http://private-f462a-appsassins.apiary-mock.com/confirm";
                        Request request = new Request.Builder().url(url).post(formBody).build();


                        Call call = client.newCall(request);
                        //dialog.show();
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                Log.e("Error", "confirm request failed");
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                try {
                                    jsonData = response.body().string();
                                    parseResponse();
                                } catch (JSONException e) {
                                    Log.e("parse", e.toString());
                                }
                            }
                        });
                    }
                })
                .setPositiveButton(R.string.notif_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE Confirm MISSILES!
                        Integer whatID = notifID;
                        OkHttpClient client = new OkHttpClient();

                        RequestBody formBody = new FormEncodingBuilder()
                                .add("notifID", whatID.toString())
                                .add("accepted", "1")
                                .build();
                        String url = "http://54.149.40.71/appsassins/api/index.php/getNotifications";
                        url = "http://private-f462a-appsassins.apiary-mock.com/confirm";
                        Request request = new Request.Builder().url(url).post(formBody).build();


                        Call call = client.newCall(request);
                        //dialog.show();
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                               Log.e("Error", "confirm request failed");
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                try {
                                    jsonData = response.body().string();
                                    parseResponse();
                                } catch (JSONException e) {
                                    Log.e("parse", e.toString());
                                }
                            }
                        });
                    }

                })
                .setNeutralButton(R.string.notif_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });


        // Create the AlertDialog object and return it
        final AlertDialog dialog = builder.create();
        return dialog;
    }

    public void setNotifID(int id) {
        this.notifID = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    private void parseResponse() throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        String status = json.getString("status");
        Log.i("sendConfirmStatus", status);
    }

}
