package dev.gabul.pagseguro_smart_flutter;

import android.content.Context;

import androidx.annotation.NonNull;

import dev.gabul.pagseguro_smart_flutter.core.PagSeguroSmart;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class PagseguroSmartFlutterPlugin
        implements FlutterPlugin, MethodCallHandler {
    private static final String CHANNEL_NAME = "pagseguro_smart_flutter";
    private MethodChannel channel;
    private PagSeguroSmart pagSeguroSmart;

    public PagseguroSmartFlutterPlugin() {
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
        //Get context to application
        Context context = binding.getApplicationContext();
        channel.setMethodCallHandler(this);
        //Create instance to PagSeguroSmart class
        pagSeguroSmart = new PagSeguroSmart(context, channel);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        //Function responsible for listening to methods called by flutter
        if (call.method.startsWith("payment") || call.method.equals("startPayment")) {
            //Call payment method
            pagSeguroSmart.initPayment(call, result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        //Dispose plugin
        channel.setMethodCallHandler(null);
        channel = null;
        pagSeguroSmart.dispose();
        pagSeguroSmart = null;
    }
}