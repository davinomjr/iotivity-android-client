package br.ufpe.cin.app;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

/**
 * Created by davinomjr on 8/20/17.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }
}
