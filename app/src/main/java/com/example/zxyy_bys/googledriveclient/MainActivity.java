package com.example.zxyy_bys.googledriveclient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener{
    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave;

    private DriveId mSelectedFileDriveId;

    boolean saved = false;

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        File root = Environment.getExternalStorageDirectory();
        FileInputStream in;
        BufferedInputStream buf;
        try {
            in = new FileInputStream("/storage/emulated/legacy/IMG_20160406_171405.jpg");
            buf = new BufferedInputStream(in);

        final Bitmap image = BitmapFactory.decodeStream(buf);
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                        try {
                            System.out.println("start Writing");
                            outputStream.write(bitmapStream.toByteArray());
                            System.out.println("Finish Writing");
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }

                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFile(mGoogleApiClient,metadataChangeSet,result.getDriveContents());
                        // Create an intent for the file chooser, and start it.
                        /*
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);


                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                        */

                    }
                });
        }catch(Exception e){
            System.out.println("open file error");
        }
    }

    public void readFileFromGoogleDrive(){

        Query query = new Query.Builder()
       //         .addFilter(Filters.and(
                       // Filters.eq(SearchableField.TITLE, "file_10K_18"),
                       // Filters.eq(SearchableField.MIME_TYPE,"text/plain")))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>()  {


                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            System.out.println("Problem while retrieving files");
                            return;
                        }

                        System.out.println("query");
                        MetadataBuffer mb = result.getMetadataBuffer();
                        int total = mb.getCount();
                        System.out.println("total:" + total);
                        for(int i = 0; i < total; ++i){
                            Metadata m = mb.get(i);
                            System.out.println(m.getDriveId());
                            System.out.println(m.getTitle());
                            System.out.println(m.getModifiedByMeDate());
                            System.out.println("id");
                            DriveId driveid = m.getDriveId();
                            DriveFile file  = Drive.DriveApi.getFile(mGoogleApiClient,driveid);
                            ResultCallback<DriveContentsResult> contentsOpenedCallback =
                                    new ResultCallback<DriveContentsResult>() {
                                        @Override
                                        public void onResult(DriveContentsResult result) {
                                            if (!result.getStatus().isSuccess()) {
                                                // display an error saying file can't be opened
                                                System.out.println("get content error");
                                                return;
                                            }
                                            // DriveContents object contains pointers
                                            // to the actual byte stream
                                            DriveContents contents = result.getDriveContents();
                                            try {
                                                BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                                                StringBuilder builder = new StringBuilder();
                                                String line;
                                                while ((line = reader.readLine()) != null) {
                                                    builder.append(line);
                                                }
                                                String contentsAsString = builder.toString();
                                                System.out.println("read succuss" + "\n" + contentsAsString);
                                            }catch(Exception e){
                                                System.out.println("Read error");
                                            }
                                        }
                                    };

                            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                    .setResultCallback(contentsOpenedCallback);
                        }
                    }
                });


        /*new DriveFile() {
            @Override
            public PendingResult<DriveContentsResult> open(GoogleApiClient googleApiClient, int i, DownloadProgressListener downloadProgressListener) {
                return null;
            }

            @Override
            public PendingResult<MetadataResult> getMetadata(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<MetadataResult> updateMetadata(GoogleApiClient googleApiClient, MetadataChangeSet metadataChangeSet) {
                return null;
            }

            @Override
            public DriveId getDriveId() {
                return null;
            }

            @Override
            public PendingResult<DriveApi.MetadataBufferResult> listParents(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> delete(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> setParents(GoogleApiClient googleApiClient, Set<DriveId> set) {
                return null;
            }

            @Override
            public PendingResult<Status> addChangeListener(GoogleApiClient googleApiClient, ChangeListener changeListener) {
                return null;
            }

            @Override
            public PendingResult<Status> removeChangeListener(GoogleApiClient googleApiClient, ChangeListener changeListener) {
                return null;
            }

            @Override
            public PendingResult<Status> addChangeSubscription(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> removeChangeSubscription(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> trash(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> untrash(GoogleApiClient googleApiClient) {
                return null;
            }
        };
        */


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.zxyy_bys.googledriveclient/http/host/path")
        );
//        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                }
                break;
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                    //        REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            //        REQUEST_CODE_CAPTURE_IMAGE);
           // return;
        }
        if(this.saved == false) {
            System.out.println("saved : "+ this.saved);
            //saveFileToDrive();
            readFileFromGoogleDrive();
            this.saved = true;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }



}
