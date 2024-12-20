package dev.gabul.pagseguro_smart_flutter.core;

import android.content.Context;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import dev.gabul.pagseguro_smart_flutter.managers.UserDataManager;
import dev.gabul.pagseguro_smart_flutter.nfc.NFCFragment;
import dev.gabul.pagseguro_smart_flutter.nfc.NFCPresenter;
import dev.gabul.pagseguro_smart_flutter.nfc.usecase.NFCUseCase;
import dev.gabul.pagseguro_smart_flutter.payments.PaymentsPresenter;
import dev.gabul.pagseguro_smart_flutter.printer.PrinterPresenter;
import dev.gabul.pagseguro_smart_flutter.user.usecase.DebitUserUseCase;
import dev.gabul.pagseguro_smart_flutter.user.usecase.EditUserUseCase;
import dev.gabul.pagseguro_smart_flutter.user.usecase.GetUserUseCase;
import dev.gabul.pagseguro_smart_flutter.user.usecase.NewUserUseCase;
import dev.gabul.pagseguro_smart_flutter.user.usecase.RefundUserUseCase;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class PagSeguroSmart {
    private static final String INVALID_ARGUMENT = "ArgumentInvalid";
    private final PlugPag plugPag;
    private final MethodChannel mChannel;
    private final NFCPresenter mNfcPresenter;
    private final PaymentsPresenter mPayment;
    // METHODS
    private static final String PAYMENT_DEBIT = "paymentDebit";
    private static final String PAYMENT_CREDIT = "paymentCredit";
    private static final String PAYMENT_CREDIT_PARC = "paymentCreditParc";
    private static final String PAYMENT_VOUCHER = "paymentVoucher";
    private static final String PAYMENT_ABORT = "paymentAbort";
    private static final String LAST_TRANSACTION = "paymentLastTransaction";
    private static final String REFUND = "paymentRefund";
    private static final String PAYMENT_PIX = "paymentPix";
    private static final String START_PAYMENT = "startPayment";

    private static final String ACTIVE_PINPAD = "paymentActivePinpad";
    private static final String PINPAD_AUTHENTICATED = "paymentIsAuthenticated";
    private static final String REBOOT_DEVICE = "paymentReboot";
    private static final String GET_DEFAULT_MESSAGE = "getDefaultMessage";
    private static final String BEEP_PAYMENT = "paymentBeep";

    //NFC
    private static final String WRITE_NFC = "paymentWriteNfc";
    private static final String READ_NFC = "paymentReadNfc";
    private static final String FORMAT_NFC = "paymentFormatNfc";
    private static final String REWRITE_NFC = "paymentReWriteNfc";
    private static final String REFUND_NFC = "paymentReFundNfc";
    private static final String DEBIT_NFC = "paymentDebitNfc";
    private static final String ABORT_NFC = "abortNfc";

    //Printer
    private static final String PRINTER_FILE = "paymentPrinterFile";
    private static final String PRINTER = "paymentPrinter";

    private static final String PRINTER_BASIC = "paymentPrinterBasic";
    private static final String PRINTER_FILE_PATH = "paymentPrinterFilePath";

    public PagSeguroSmart(Context context, MethodChannel channel) {
        PlugPag instancePlugPag = new PlugPag(context);
        PlugPagCustomPrinterLayout customDialog = new PlugPagCustomPrinterLayout();
        customDialog.setMaxTimeShowPopup(30);
        instancePlugPag.setPlugPagCustomPrinterLayout(customDialog);
        this.plugPag = instancePlugPag;
        this.mChannel = channel;
        this.mPayment = new PaymentsPresenter(this.plugPag, this.mChannel);
        NFCUseCase useCase = new NFCUseCase(plugPag);
        NFCFragment fragment = new NFCFragment(mChannel);
        UserDataManager mUserManager = new UserDataManager(new GetUserUseCase(useCase),
                new NewUserUseCase(useCase), new EditUserUseCase(useCase),
                new DebitUserUseCase(useCase), new RefundUserUseCase(useCase));
        this.mNfcPresenter = new NFCPresenter(fragment, useCase, mUserManager);
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case PRINTER_FILE: {
                PrinterPresenter printerPresenter = new PrinterPresenter(this.plugPag, this.mChannel);
                String filePath = call.argument("path");
                printerPresenter.printerFromFile(filePath);
                result.success(true);
                return;
            }
            case PRINTER: {
                PrinterPresenter printerPresenter = new PrinterPresenter(this.plugPag, this.mChannel);
                String filePath = call.argument("path");
                printerPresenter.printFile(filePath);
                result.success(true);
                return;
            }
            case PRINTER_BASIC: {
                PrinterPresenter printerPresenter = new PrinterPresenter(this.plugPag, this.mChannel);
                String filePath = call.argument("path");
                printerPresenter.printer(filePath);
                result.success(true);
                return;
            }
            case PRINTER_FILE_PATH: {
                PrinterPresenter printerPresenter = new PrinterPresenter(this.plugPag, this.mChannel);
                String filePath = call.argument("path");
                printerPresenter.printerByFilePath(filePath);
                result.success(true);
                return;
            }
            case REBOOT_DEVICE:
                try {
                    this.mPayment.rebootDevice();
                    result.success(true);
                } catch (Exception e) {
                    result.error("reboot", e.getMessage(), null);
                }
                return;
        }

        Integer value = call.argument("value");
        String userReference = call.argument("userReference");
        Boolean printReceipt = call.argument("printReceipt");
        Boolean partialPay = call.argument("partialPay");
        Boolean isCarne = call.argument("isCarne");
        if (value == null
                || userReference == null
                || printReceipt == null
                || partialPay == null
                || isCarne == null) {
            result.error(INVALID_ARGUMENT, "args is null", null);
            return;
        }

        Integer type = call.argument("type");

        switch (call.method) {
            case GET_DEFAULT_MESSAGE:
                Integer eventCode = call.argument("eventCode");
                if (eventCode == null) {
                    result.error("eventCode", ("eventCode is null"), null);
                } else {
                    result.success(this.mPayment.getDefaultMessage(eventCode));
                }
                return;
            case BEEP_PAYMENT:
                this.mPayment.beep();
                result.success(true);
                return;
            case PAYMENT_DEBIT:
                this.mPayment.doDebitPayment(
                        value,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne
                );
                result.success(true);
                return;
            case ACTIVE_PINPAD:
                this.mPayment.activate(call.argument("code"));
                result.success(true);
                return;
            case PINPAD_AUTHENTICATED:
                this.mPayment.isAuthenticate();
                result.success(true);
                return;
            case PAYMENT_PIX:
                this.mPayment.doPixPayment(
                        value,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne
                );
                result.success(true);
                return;
            case PAYMENT_CREDIT:
                this.mPayment.creditPayment(
                        value,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne
                );
                result.success(true);
                return;
            case PAYMENT_CREDIT_PARC:
                if (type == null) {
                    result.error("type", "type is null", null);
                    return;
                }
                Integer parc = call.argument("parc");
                if (parc == null) {
                    result.error("parc", "parc is null", null);
                    return;
                }
                this.mPayment.creditPaymentParc(
                        value,
                        type,
                        parc,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne
                );
                result.success(true);
                return;
            case PAYMENT_VOUCHER:
                this.mPayment.doVoucherPayment(
                        value,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne
                );
                result.success(true);
                return;
            case START_PAYMENT:
                if (type == null) {
                    result.error(INVALID_ARGUMENT, "type is null", (null));
                    return;
                }
                Integer amount = call.argument("amount");
                if (amount == null) {
                    result.error(INVALID_ARGUMENT, "amount is null", (null));
                    return;
                }
                Integer installmentType = call.argument("installmentType");
                if (installmentType == null) {
                    result.error(INVALID_ARGUMENT, "installmentType is null", (null));
                    return;
                }
                Integer installments = call.argument("installments");
                if (installments == null) {
                    result.error(INVALID_ARGUMENT, "installments is null", (null));
                    return;
                }
                this.mPayment.startPayment(
                        type,
                        amount,
                        installmentType,
                        installments,
                        userReference,
                        printReceipt,
                        partialPay,
                        isCarne);
                result.success(true);
                return;

            case PAYMENT_ABORT:
                this.mPayment.abortTransaction();
                result.success(true);
                return;
            case LAST_TRANSACTION:
                this.mPayment.getLastTransaction();
                result.success(true);
                return;
            case REFUND:
                String transactionCode = call.argument("transactionCode");
                String transactionId = call.argument("transactionId");
                if (transactionCode == null || transactionId == null) {
                    result.error(INVALID_ARGUMENT, "args is null", null);
                    return;
                }
                this.mPayment.doRefund(transactionCode, transactionId);
                result.success(true);
                return;
        }

        this.mNfcPresenter.dispose();
        switch (call.method) {
            case READ_NFC:
                mNfcPresenter.readNFC_Card(call.argument("idEvento"));
                result.success(true);
                return;
            case WRITE_NFC:
                mNfcPresenter.writeNFC_Card(call.argument("valor"),
                        call.argument("nome"),
                        call.argument("cpf"),
                        call.argument("numeroTag"),
                        call.argument("celular"),
                        call.argument("aberto"),
                        call.argument("idEvento"));
                result.success(true);
                return;
            case REWRITE_NFC:
                mNfcPresenter.reWriteNFC_Card(call.argument("valor"),
                        call.argument("idEvento"));
                result.success(true);
                return;
            case REFUND_NFC:
                mNfcPresenter.reFundNFC_Card(call.argument("valor"),
                        call.argument("idEvento"));
                result.success(true);
                return;
            case FORMAT_NFC:
                mNfcPresenter.formatNFCCard();
                return;
            case DEBIT_NFC:
                mNfcPresenter.debitNFC_Card(call.argument("idEvento"),
                        call.argument("valor"));
                result.success(true);
                return;
            case ABORT_NFC:
                mNfcPresenter.abort()
                        .doFinally(() -> result.success(true))
                        .doOnError((throwable) -> result.error("abortError",
                                        throwable.getMessage(),
                                        throwable.getCause()));
                return;
        }
        result.notImplemented();
    }

    public void dispose() {
        mNfcPresenter.dispose();
        mPayment.dispose();
    }
}