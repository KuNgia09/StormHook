package de.larma.arthook.test;

import android.app.Activity;
import android.app.Application;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.sip.SipAudioCall;
import android.util.Log;

import com.example.DalvikHook.DalvikHookManager;
import com.storm.hook.Inject_SaveOrigProto;

import java.lang.reflect.Method;

import de.larma.arthook.$;
import de.larma.arthook.ArtHook;
import de.larma.arthook.BackupIdentifier;
import de.larma.arthook.Hook;
import de.larma.arthook.OriginalMethod;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    public static boolean madePiece = false;


    @Override
    public void onCreate() {
        super.onCreate();
        ArtHook.hook(Inject_SaveOrigProto.class);





    }

    public void pieceGame() {
        Log.d(TAG, "broken pieceGame()");
    }

    @Hook("de.larma.arthook.test.MyApplication->pieceGame")
    public static void fix_pieceGame(MyApplication app) {
        Log.d(TAG, "fixed pieceGame()");
        madePiece = true;
        OriginalMethod.by(new $() {}).invoke(app);
    }

    @Hook("android.net.sip.SipAudioCall->startAudio")
    public static void SipAudioCall_startAudio(SipAudioCall call) {
        Log.d(TAG, "SipAudioCall_startAudio");
        OriginalMethod.by(new $() {}).invoke(call);
    }

    @Hook("android.app.Activity-><init>")
    public static void Activity_init(Activity a) {
        Log.d(TAG, "Activity_init");
        OriginalMethod.by(new $() {}).invoke(a);
    }



    /**
     * Sample hook of a static method
     */
    @Hook("android.hardware.Camera->open")
    public static Camera Camera_open() {
        try {
            return OriginalMethod.by(new $() {}).invokeStatic();
        } catch (Exception e) {
            throw new SecurityException("We do not allow Camera access", e);
        }
    }

    /**
     * Sample hook of a static native method
     */
    @Hook("java.lang.System->currentTimeMillis")
    public static long System_currentTimeMillis() {
        Log.d(TAG, "currentTimeMillis is much better in seconds :)");
        return (long) OriginalMethod.by(new $() {}).invokeStatic() / 1000L;
    }

    /**
     * Hooking an empty method
     */
    @Hook("android.net.ConnectivityManager->setNetworkPreference")
    public static void ConnectivityManager_setNetworkPreference(ConnectivityManager manager, int preference) {
        Log.d(TAG, "Making something from nothing!");
        OriginalMethod.by(new $() {}).invoke(manager, preference);
    }

    /**
     * Sample hook of a member method used internally by the system
     * <p/>
     * Note how we use the BackupIdentifier here, because using reflection APIs to access
     * reflection APIs will cause loops...
     */
    @Hook("java.lang.Class->getDeclaredMethod")
    @BackupIdentifier("Class_getDeclaredMethod")
    public static Method Class_getDeclaredMethod(Class cls, String name, Class[] params) {
        Log.d(TAG, "I'm hooked in getDeclaredMethod: " + cls + " -> " + name);
        if (name.contains("War") || name.contains("war")) {
            Log.d(TAG, "make piece not war!"); // This is a political statement!
            name = name.replace("War", "Piece").replace("war", "piece");
        }
        return OriginalMethod.by("Class_getDeclaredMethod").invoke(cls, name, params);
    }
}
