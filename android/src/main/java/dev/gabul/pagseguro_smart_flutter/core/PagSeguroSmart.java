package dev.gabul.pagseguro_smart_flutter.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import dev.gabul.pagseguro_smart_flutter.payments.PaymentsPresenter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class PagSeguroSmart {
    final PlugPag plugPag;
    final MethodChannel mChannel;

    //FUNCTIONS
     PaymentsPresenter payment;

     //METHODS
     private static final String PAYMENT_DEBIT = "paymentDebit";
     private static final String PAYMENT_CREDIT = "paymentCredit";
     private static final String PAYMENT_CREDIT_PARC = "paymentCreditParc";
     private static final String PAYMENT_VOUCHER = "paymentVoucher";
     private static final String PAYMENT_ABORT = "paymentAbort";
     private static final String LAST_TRANSACTION = "paymentLastTransaction";
    private static final String REFUND = "paymentRefund";

    public PagSeguroSmart(Context context, MethodChannel channel) {
        this.plugPag = new PlugPag(context,new PlugPagAppIdentification("Pagseguro Smart Flutter","0.0.1"));
        this.mChannel = channel;
    }

    public void initPayment(MethodCall call, MethodChannel.Result result){
        if(this.payment == null)
        this.payment = new PaymentsPresenter(this.plugPag,this.mChannel);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(call.method.equals(PAYMENT_DEBIT)){
                    payment.doDebitPayment(call.argument("value"));

                }
                else if(call.method.equals(PAYMENT_CREDIT)){
                   payment.creditPayment(call.argument("value"));

                }
                else if(call.method.equals(PAYMENT_CREDIT_PARC)){
                    payment.creditPaymentParc(call.argument("value"),call.argument("type"),call.argument("parc"));

                }
                else if(call.method.equals(PAYMENT_VOUCHER)){
                    payment.doVoucherPayment(call.argument("value"));

                }
                else if(call.method.equals(PAYMENT_ABORT)){
                    payment.abortTransaction();
                    result.success(true);
                }
                else if(call.method.equals(LAST_TRANSACTION)){
                    payment.getLastTransaction();
                }
                else if(call.method.equals(REFUND)){
                    payment.doRefund(call.argument("transactionCode"),call.argument("transactionId"));
                    result.success(true);
                }
                else{
                    result.notImplemented();
                }
            }
        });



    }


   public void dispose(){
        if(this.payment != null){
            this.payment.dispose();
        }
    }
}


