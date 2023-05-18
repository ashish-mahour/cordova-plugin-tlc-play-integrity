package com.tlcgroup;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
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

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

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
    final long CLOUD_PROJECT_NUMBER = Long.parseLong(getStringResourceByName("google_cloud_project_number"));
    Log.d(LOG_TAG, "Project Number: " + CLOUD_PROJECT_NUMBER);
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

  private String getStringResourceByName(String aString) {
    Activity activity = cordova.getActivity();
    String packageName = activity.getPackageName();
    int resId = activity.getResources().getIdentifier(aString, "string", packageName);
    return activity.getString(resId);
  }
}
