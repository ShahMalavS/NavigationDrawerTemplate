package com.malav.cme.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.malav.cme.R;
import com.malav.cme.fragments.FragmentDrawer;
import com.malav.cme.models.User;
import com.malav.cme.utils.AppUtils;
import com.malav.cme.utils.CommonUtils;
import com.malav.cme.utils.Constants;
import com.malav.cme.utils.QueryMapper;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by shahmalav on 22/04/17.
 */

public class DashboardActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private SharedPreferences someData;
    private Uri mImageCaptureUri;
    private de.hdodenhof.circleimageview.CircleImageView banar1;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private GoogleApiClient mGoogleApiClient;
    private static String TAG = DashboardActivity.class.getSimpleName();
    private User user;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        someData = getSharedPreferences(Constants.filename, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if(CommonUtils.hasNoText(someData.getString("user_id", ""))){

        }
        user = new User();
        user.setId(someData.getString("user_id", ""));
        //myProfilePic = someData.getString("profilePic", "");
        String myName = someData.getString("name", "");
        String myEmail = someData.getString("emailId", "");
        String mypic = someData.getString("profilePic", "");

        String[] web = DashboardActivity.this.getResources().getStringArray(R.array.nav_drawer_labels);

        CustomList adapter1 = new CustomList(DashboardActivity.this, web, Constants.imageId);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter1);

        final String [] items = new String [] {"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<> (this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder	= new AlertDialog.Builder(this);

        builder.setTitle("Select Image");
        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                    try {
                        intent.putExtra("return-data", true);
                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else { //pick from file
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                }
            }
        } );

        final AlertDialog dialog = builder.create();
        banar1 = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.banar1);
        if(CommonUtils.isNotNull(someData.getString("profilePic",""))){
            Glide.with(DashboardActivity.this).load(someData.getString("profilePic","")).crossFade(500).into(banar1);
        }
        banar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        TextView drawerName = (TextView) findViewById(R.id.earn);
        drawerName.setText(myName);

        FragmentDrawer drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;

            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();
                doCrop();
                break;

            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    banar1.setImageBitmap(photo);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                        uploadMultipart();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()){
                    f.delete();
                }

                break;

        }
    }

    public void uploadMultipart() {

        //getting the actual path of the image
        String path = AppUtils.getPath(mImageCaptureUri, DashboardActivity.this);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            new MultipartUploadRequest(this, uploadId, QueryMapper.URL_UPLOAD_IMAGE)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("name", "name") //Adding text parameter to the request
                    .addParameter("user_id", someData.getString("login_id","0")) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void doCrop() {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0 );
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
        } else {
            intent.setData(mImageCaptureUri);
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            Intent i = new Intent(intent);
            ResolveInfo res	= list.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Errro Logging out.. Please try again", Toast.LENGTH_SHORT).show();
    }

    public class CustomList extends ArrayAdapter<String> {

        private final Activity context;
        private final String[] web;
        private final Integer[] imageId;
        public CustomList(Activity context, String[] web, Integer[] imageId) {
            super(context, R.layout.nav_drawer_row, web);
            this.context = context;
            this.web = web;
            this.imageId = imageId;
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View rowView = inflater.inflate(R.layout.nav_drawer_row, null, true);

            LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.ll);
            TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
            txtTitle.setText(web[position]);
            imageView.setImageResource(imageId[position]);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToNewPage(position);
                }
            });
            return rowView;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void navigateToNewPage(int position){

        Intent i = new Intent(DashboardActivity.this, DashboardActivity.class);
        switch(position){
            case 0:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 1:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 2:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 3:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 4:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 5:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 6:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Click on https://play.google.com/store/apps/details?id=com.malav.moodapp to download ME - By Holistree";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download ME - By Holistree");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case 7:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 8:
                i = new Intent(DashboardActivity.this, DashboardActivity.class);
                break;
            case 9:
                SharedPreferences.Editor editor = someData.edit();
                editor.remove("role");
                editor.remove("login");
                editor.remove("name");
                editor.remove("profilePic");
                editor.remove("emailId");
                editor.remove("profilePic");
                //editor.remove("token");
                editor.remove("registered");
                editor.remove("user_id");
                editor.remove("login_id");
                editor.remove("wordJsonArray");
                editor.apply();
                editor.commit();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                            }
                        });
                i = new Intent(DashboardActivity.this, LoginActivity.class);
                break;
        }
        if(position!=6) {
            startActivity(i);
        }
        if (position == 9) {
            finish();
        }
    }

}
