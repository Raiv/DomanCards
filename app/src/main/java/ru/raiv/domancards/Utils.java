package ru.raiv.domancards;

import android.os.Environment;

import java.io.File;

/**
 * Created by Raiv on 02.11.2016.
 */

public class Utils {
    public static int getRealDuration(int sbValue){
        return sbValue+5;
    }

    public static  String getDefaultDir(){
        File f =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(f==null){
            f=Environment.getExternalStorageDirectory();
        }
        if (f==null){
            f=Environment.getRootDirectory();
        }
        String result = f.getAbsolutePath();
        return result;
    }
}
