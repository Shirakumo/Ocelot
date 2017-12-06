package org.shirakumo.ocelot;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentTransaction;

public class Chat extends Activity implements Output.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        // FIXME: Start service
    }

    @Override
    protected void onPause() {
        super.onPause();
        // FIXME: Disconnect from service
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FIXME: Reconnect to service
    }

    @Override
    protected void onStop() {
        super.onStop();
        // FIXME: Destroy service
    }

    private class Handler extends org.shirakumo.lichat.HandlerAdapter{
        private Handler(){}

        public void handle(org.shirakumo.lichat.updates.Message update){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.output, Output.newInstance(org.shirakumo.lichat.CL.universalToUnix(update.clock), update.from, update.text), "a");
            ft.commit();
        }
    }
}
