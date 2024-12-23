package dev.gabul.pagseguro_smart_flutter.printer.usecase;


import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import dev.gabul.pagseguro_smart_flutter.core.ActionResult;
import dev.gabul.pagseguro_smart_flutter.payments.PaymentsFragment;

import io.flutter.plugin.common.MethodChannel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class PrinterUseCase {
    private final PlugPag mPlugpag;
    private final PaymentsFragment mFragment;

    @Inject
    public PrinterUseCase(PlugPag plugPag, MethodChannel channel) {
        this.mPlugpag = plugPag;
        this.mFragment = new PaymentsFragment(channel);
    }

    public void printerFromFile(String path) {
        try {
            mFragment.onAuthProgress("Iniciando impressão");
            mFragment.onMessage("Iniciando impressão");
            File file = new File(path);
            if (!file.exists()) {
                mFragment.onMessage("O arquivo informado não foi encontrado.");
                mFragment.onError("Arquivo não encontrado no diretório: " + file.getAbsolutePath());
                return;
            }

            PlugPagPrinterData printerData = new PlugPagPrinterData(file.getAbsolutePath(),
                    4, 0);
            PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                @Override
                public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                    String message = plugPagPrintResult.getMessage();
                    int resultPrinter = plugPagPrintResult.getResult();
                    mFragment.onMessage(resultPrinter + message);
                    mFragment.onAuthProgress(resultPrinter + message);

                }

                @Override
                public void onError(PlugPagPrintResult plugPagPrintResult) {
                    String errorMessage = "Error message: " + plugPagPrintResult.getMessage();
                    String errorCode = "Error code" + plugPagPrintResult.getErrorCode();
                    mFragment.onError(errorCode + errorMessage);
                    mFragment.onMessage("Erro ao realizar impressão");
                }
            };

            mPlugpag.setPrinterListener(listener);
            mPlugpag.printFromFile(printerData);

            mFragment.onMessage("Impressão finalizada");
            mFragment.onAuthProgress("Impressão finalizada");
        } catch (Exception e) {
            mFragment.onMessage("Erro: " + e.getMessage());
            mFragment.onAuthProgress("Erro ao realizar impressão");
            mFragment.onError("Erro impressão: " + e.getMessage());
        }
    }

    public Observable<ActionResult> printFile(String fileName) {
        final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path = absolutePath + "/Download/" + fileName;
        File file = new File(path);

        return Observable.create((ObservableEmitter<ActionResult> emitter) -> {
            ActionResult actionResult = new ActionResult();
            if (file.exists()) {
                PlugPagPrintResult result = mPlugpag.printFromFile(
                        new PlugPagPrinterData(
                                path,
                                4,
                                0));

                actionResult.setResult(result.getResult());
                actionResult.setMessage(result.getMessage());
                actionResult.setErrorCode(result.getErrorCode());
                setPrintListener(emitter, actionResult);

                emitter.onNext(actionResult);
            } else {
                emitter.onError(new FileNotFoundException());
            }
            emitter.onComplete();
        });
    }

    public Observable<Boolean> printer(String filePath) {

        return Observable.create((ObservableEmitter<Boolean> emitter) -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    mFragment.onMessage("O arquivo informado não foi encontrado.");
                    mFragment.onError("Arquivo não encontrado no diretório: " +
                            file.getAbsolutePath());
                }

                if (file.exists()) {
                    mPlugpag.printFromFile(new PlugPagPrinterData(file.getAbsolutePath(),
                            4,
                            0));
                    emitter.onNext(true);
                } else {
                    emitter.onError(new FileNotFoundException());
                }
                emitter.onComplete();
            } catch (Exception e) {
                mFragment.onMessage("Erro: " + e.getMessage());
                mFragment.onAuthProgress("Erro ao realizar impressão");
                mFragment.onError("Erro impressão: " + e.getMessage());
                emitter.onError(e);
            }
        });
    }

    public Observable<ActionResult> printerByFilePath(String filePath) {

        return Observable.create((ObservableEmitter<ActionResult> emitter) -> {
            ActionResult actionResult = new ActionResult();
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    mFragment.onMessage("O arquivo informado não foi encontrado.");
                    mFragment.onError("Arquivo não encontrado no diretório: "
                            + file.getAbsolutePath());
                }
                if (file.exists()) {
                    PlugPagPrintResult result = mPlugpag.printFromFile(
                            new PlugPagPrinterData(
                                    file.getPath(),
                                    4,
                                    0));

                    setPrintListener(emitter, actionResult);
                    String message = result.getMessage();
                    if (message != null && message.equals("Success")) {
                        actionResult.setResult(0);
                    } else {
                        actionResult.setResult(result.getResult());
                    }

                    actionResult.setMessage(message);

                    emitter.onNext(actionResult);
                } else {
                    emitter.onError(new FileNotFoundException());
                }
                emitter.onComplete();
            } catch (Exception e) {
                mFragment.onMessage("Erro: " + e.getMessage());
                mFragment.onAuthProgress("Erro ao realizar impressão");
                mFragment.onError("Erro impressão: " + e.getMessage());
                emitter.onError(e);
            }
        });
    }

    private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugpag.setPrinterListener(new PlugPagPrinterListener() {
            @Override
            public void onError(@NonNull PlugPagPrintResult printResult) {
                emitter.onError(new PlugPagException(String.format("Error %s %s",
                        printResult.getErrorCode(), printResult.getMessage())));
            }

            @Override
            public void onSuccess(@NonNull PlugPagPrintResult printResult) {
                result.setResult(printResult.getResult());
                result.setMessage(String.format(Locale.getDefault(), "Print OK: Steps [%d]",
                        printResult.getSteps()));
                result.setErrorCode(printResult.getErrorCode());
                emitter.onNext(result);
            }
        });
    }

}
