package com.antlabs.fnfc5;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.zweng.emv.provider.Provider;
import at.zweng.emv.utils.NFCUtils;

import at.zweng.emv.utils.SimpleAsyncTask;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;


public class MainActivity extends FlutterActivity {

  private static final String CHANNEL = "nfchelper";

  private static final String TAG = MainActivity.class.getName();
  private NFCUtils nfcUtils;
  private EmvCard mReadCard;
  private Provider mProvider = new Provider();

  static MethodChannel.Result mResult;
  MethodChannel channel;

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
    channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
    channel.setMethodCallHandler(
                    (call, result) -> {
                      // Note: this method is invoked on the main thread.
                      if (call.method.equals("getCardNumber")) {
                         //mResult = result;
                        result.success("Waiting...");
                      } else {
                        result.notImplemented();
                      }
                    }
            );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    nfcUtils = new NFCUtils(this);

  }

  @Override
  protected void onResume() {
    if (!NFCUtils.isNfcAvailable(this)) {
      log("Sorry, this device doesn't seem to support NFC.\nThis app will not work. :-(");
      channel.invokeMethod("backCardNumber", "No NFC support");
    } else if (!NFCUtils.isNfcEnabled(this)) {
      log("NFC is disabled in system settings.\nPlease enable it and restart this app.");
      channel.invokeMethod("backCardNumber", "NFC disabled");
    } else {
      nfcUtils.enableDispatch();
    }

    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    nfcUtils.disableDispatch();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    if (mTag != null) {

      new SimpleAsyncTask() {
        private IsoDep tagIsoDep;
        private EmvCard card;
        private Exception exception;

        @Override
        protected void onPreExecute() {
          super.onPreExecute();
          log("Start reading card. Please wait...");
          channel.invokeMethod("backCardNumber", "Reading...");
        }

        @Override
        protected void doInBackground() {
          tagIsoDep = IsoDep.get(mTag);
          if (tagIsoDep == null) {
            log("Couldn't connect to NFC card. Please try again.");
            channel.invokeMethod("backCardNumber", "Error reading");
            return;
          }
          exception = null;

          try {
            mReadCard = null;
            // Open connection
            tagIsoDep.connect();
            mProvider.setmTagCom(tagIsoDep);
            EmvParser parser = new EmvParser(mProvider, true);
            card = parser.readEmvCard();
          } catch (IOException e) {
            exception = e;
          } finally {
            closeQuietly(tagIsoDep);
          }
        }

        @Override
        protected void onPostExecute(final Object result) {
          log("Reading finished.");
          //mResult.success("1234");
          if (exception == null) {
            if (card != null) {
              //if (StringUtils.isNotBlank(card.getCardNumber())) {
              if(!card.getCardNumber().isEmpty()) {
                mReadCard = card;
                printResults();
                //
                //mResult.success(mReadCard.getCardNumber());
                Date date = mReadCard.getExpireDate();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                String strDate = formatter.format(date);
                channel.invokeMethod("backCardNumber", mReadCard.getCardNumber() + " " + strDate);
                //
              } else {
                Log.w(TAG, "Reading finished, but cardNumber is null or empty..");
                log("Sorry, couldn't parse the card data. Is this an EMV banking card?");
                channel.invokeMethod("backCardNumber", "No data");
              }
            } else {
              Log.w(TAG, "reading finished, no exception but card == null..");
              log("Sorry, couldn't parse the card (card is null). Is this an EMV banking card?");
              channel.invokeMethod("backCardNumber", "Not bank card");
            }
          } else {
            Log.w(TAG, "reading finished with exception.");
            log("Sorry, we got an error while reading: \"" + exception.getLocalizedMessage() +
                    "\"\nDid you remove the card?\n\nPlease try again.");
            channel.invokeMethod("backCardNumber", "Error, card removed?");
          }
        }
      }.execute();
    }
  }

  private void printResults() {
    try {
      log("");
      log("-----------------------------");
      log("-----------------------------");
      log("Card details:");
      log("Cardholder: " + mReadCard.getHolderFirstname() + " " + mReadCard.getHolderLastname());
      log("Track2 service1: " + mReadCard.getService().getServiceCode1().getKey());
      log("Track2 service1: " + mReadCard.getService().getServiceCode1().getInterchange());
      log("Track2 service1: " + mReadCard.getService().getServiceCode1().getTechnology());
      log("Track2 service2: " + mReadCard.getService().getServiceCode2().getKey());
      log("Track2 service2: " + mReadCard.getService().getServiceCode2().getAuthorizationProcessing());
      log("Track2 service2: " + mReadCard.getService().getServiceCode2().name());
      log("Track2 service3: " + mReadCard.getService().getServiceCode3().getKey());
      log("Track2 service3: " + mReadCard.getService().getServiceCode3().getAllowedServices());
      log("Track2 service3: " + mReadCard.getService().getServiceCode3().getPinRequirements());
      log("Expiry date: " + mReadCard.getExpireDate());

      if (mReadCard.getApplicationLabel() != null) {
        log("Application label: " + mReadCard.getApplicationLabel());
      }
      log("Primary account number (PAN): " + mReadCard.getCardNumber());
      log("-----------------------------");
      log("");
    } catch (Exception e) {
      Log.e(TAG, "Exception catched while key validation.", e);
      logException("Exception catched while key validation:\n", e);
    }
  }

  private void log(String msg) {
    Log.i(TAG, msg);
  }

  private void logException(String header, Exception e) {
    log(header);
    log(e.getLocalizedMessage() + "\n\n");
    log("-----------------------------");
    log("-----------------------------");
    log("Technical details below:");
    //log(ExceptionUtils.getStackTrace(e));
    log(e.getStackTrace().toString());
    log("-----------------------------");
    log("-----------------------------");
  }

  private void closeQuietly(IsoDep tagComm) {
    try {
      if (tagComm != null) {
        tagComm.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

}
