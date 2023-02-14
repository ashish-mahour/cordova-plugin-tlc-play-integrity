package com.tlcgroup;

import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class TLCPlayIntegrity extends CordovaPlugin {

  private final String LOG_TAG = "TLCPlayIntegrity";
  private final long CLOUD_PROJECT_NUMBER = 470007365374l;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("certifyKey")) {
      String nonce = Base64.encodeToString(args.getString(0).getBytes(),
        Base64.URL_SAFE | Base64.NO_WRAP);
      this.certifyKey(nonce, callbackContext);
      return true;
    }
    return false;
  }

  private void certifyKey(String nonce, CallbackContext callbackContext) {
    Log.d(LOG_TAG, "Nonce: " + nonce);
    IntegrityManager integrityManager = IntegrityManagerFactory.create(this.cordova.getActivity().getApplicationContext());
    Task<IntegrityTokenResponse> integrityTokenResponse = integrityManager.requestIntegrityToken(IntegrityTokenRequest.builder().setCloudProjectNumber(CLOUD_PROJECT_NUMBER).setNonce(nonce).build());
    integrityTokenResponse.addOnCompleteListener(new OnCompleteListener<IntegrityTokenResponse>() {
      @Override
      public void onComplete(Task<IntegrityTokenResponse> task) {
        if (task.isSuccessful()) {
          IntegrityTokenResponse result = task.getResult();
          String token = result.token();
          Log.d(LOG_TAG, "Result Token: " + token);
          Map<String, String> data = new HashMap<>();
          data.put("status", "Success");
          data.put("result", result.token());

          callbackContext.success(new JSONObject(data));
        } else {
          Log.e(LOG_TAG, "Play Integrity Task Failed." + task.getException().getLocalizedMessage());
          callbackContext.error("Play Integrity Task Failed." + task.getException().getLocalizedMessage());
          task.getException().printStackTrace();
        }
      }
    });
  }
}
