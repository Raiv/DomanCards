package ru.raiv.domancards;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

/**
 * Created by Raiv on 02.11.2016.
 */

public class ActivitySettings extends AppCompatActivity implements
        DirectoryChooserFragment.OnFragmentInteractionListener{


    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 666;
    private EditText etDir;
    private Button bChooseDir;
    private CheckBox cbRandom;
    private SeekBar sbDuration;
    private TextView tvSeekBarValue;
    private SharedPreferences prefs;
    private DirectoryChooserFragment mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        etDir= (EditText) findViewById(R.id.etDir);
        bChooseDir= (Button) findViewById(R.id.bChooseDir);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("DialogSample")
                .build();
        mDialog = DirectoryChooserFragment.newInstance(config);

        bChooseDir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(ActivitySettings.this,
                                Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ActivitySettings.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }else{
                            doOpenDirs();
                        }

                    }
                });

        sbDuration= (SeekBar) findViewById(R.id.sbDuration);
        sbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvSeekBarValue.setText(""+Utils.getRealDuration(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tvSeekBarValue= (TextView) findViewById(R.id.tvSeekBarValue);
        cbRandom= (CheckBox) findViewById(R.id.cbRandom);
        prefs=getApplicationContext().getSharedPreferences("main",0);

    }


    @Override
    protected void onResume(){
        super.onResume();
        int duration = prefs.getInt(Const.Prefs.DURATION,25);
        sbDuration.setProgress(duration);
        tvSeekBarValue.setText(""+Utils.getRealDuration(duration));
        etDir.setText(prefs.getString(Const.Prefs.DIR,Utils.getDefaultDir()));
        cbRandom.setChecked(prefs.getBoolean(Const.Prefs.RANDOM,false));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        MenuItem item = menu.findItem(R.id.miConfirm);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                saveAll();
                finish();
                return true;
                
            }
        });

        return true;
    }

    private void doOpenDirs(){
        mDialog.show(getFragmentManager(), null);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doOpenDirs();

                } else {
                    Toast.makeText(this,getString(R.string.on_permission_reject),Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void saveAll() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(Const.Prefs.DURATION,sbDuration.getProgress());
        edit.putString(Const.Prefs.DIR,etDir.getText().toString());
        edit.putBoolean(Const.Prefs.RANDOM,cbRandom.isChecked());
        edit.commit();

    }

    @Override
    public void onSelectDirectory(@NonNull String path) {
        etDir.setText(path);
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }
}
