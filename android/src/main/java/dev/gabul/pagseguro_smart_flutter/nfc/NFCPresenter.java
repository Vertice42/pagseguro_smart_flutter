package dev.gabul.pagseguro_smart_flutter.nfc;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData;
import dev.gabul.pagseguro_smart_flutter.helpers.NFCConstants;
import dev.gabul.pagseguro_smart_flutter.helpers.Utils;
import dev.gabul.pagseguro_smart_flutter.managers.UserDataManager;
import dev.gabul.pagseguro_smart_flutter.nfc.usecase.NFCUseCase;
import dev.gabul.pagseguro_smart_flutter.user.UserData;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NFCPresenter {

    private final NFCUseCase mUseCase;
    private final NFCFragment mFragment;
    private final UserDataManager mUserManager;
    private Disposable mSubscribe;

    @Inject
    public NFCPresenter(NFCFragment mFragment, NFCUseCase nfcUseCase, UserDataManager userManager) {
        this.mUseCase = nfcUseCase;
        this.mFragment = mFragment;
        this.mUserManager = userManager;
    }

    public void readNFC_Card(String idEvento) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getUserData(idEvento)
                );
    }

    public void getUserData(String idEvento) {

        mSubscribe = mUserManager.getUserData(idEvento)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            showSuccessRead(result);
                            this.controlLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
                        },
                        throwable -> {
                            mFragment.showErrorRead(throwable.getMessage());
                            // Thread.sleep(1000);
                            this.controlLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
                        }
                );
    }

    public void showSuccessRead(UserData userData) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mFragment.showSuccess(userData)
                );

    }


    public void writeNFC_Card(String value, String name, String cpf, String numberTag,
                              String cellPhone, String active, String idEvento) {

        UserData userData = new UserData(Utils.addAsterisk(value),
                Utils.addAsterisk(name),
                Utils.addAsterisk(cpf),
                Utils.addAsterisk(numberTag),
                Utils.addAsterisk(cellPhone),
                Utils.addAsterisk(active),
                Utils.addAsterisk(idEvento),
                Utils.addAsterisk(value));

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> writeUserData(userData),
                        throwable -> {
                            mFragment.showErrorRead(throwable.getMessage());
                            this.controlLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
                        }
                );

    }

    public void writeUserData(UserData userData) {

        mSubscribe = mUserManager.writeUserData(userData)
                .lastElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showSuccessWrite,
                        throwable -> {
                            mFragment.showErrorWrite(throwable.getMessage());
                            this.controlLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
                        },
                        () -> {
                            Log.d(NFCPresenter.class.getSimpleName(), "writeUser finished");
                            // mView.onWriteNfcSuccessful();
                        }
                );


    }


    public void showSuccessWrite(Integer res) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mFragment.showSuccessWrite(res);
                            this.controlLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
                        }
                );
    }


    public void showSuccessReFund(String res) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mFragment.showSuccessRefundNfc(res),
                        throwable -> mFragment.showErrorRefundNfc(throwable.getMessage())
                );
    }

    public void showSuccessReWrite(String res) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mFragment.showSuccessReWrite(res),
                        throwable -> mFragment.showErrorReWrite(throwable.getMessage())
                );
    }

    public void showSuccessDebit(String res) {

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mFragment.showSuccessDebitNfc(res),
                        throwable -> mFragment.showErrorDebitNfc(throwable.getMessage())
                );
    }

    public void reFundNFC_Card(String value, String idEvento) {

        UserData userData = new UserData(Utils.addAsterisk(value), null, null,
                null, null, null,
                Utils.addAsterisk(idEvento), null);

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> reFundUserData(userData),
                        throwable -> mFragment.showErrorRefundNfc(throwable.getMessage())
                );
    }

    public void reWriteNFC_Card(String value, String idEvento) {

        UserData userData = new UserData(Utils.addAsterisk(value), null, null,
                null, null, null, Utils.
                addAsterisk(idEvento), Utils.addAsterisk(value));

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> reWriteUserData(userData),
                        throwable -> mFragment.showErrorReWrite(throwable.getMessage())
                );
    }


    public void reFundUserData(UserData userData) {

        mSubscribe = mUserManager.reFundUserData(userData)
                .lastElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showSuccessReFund,
                        throwable -> mFragment.showErrorRefundNfc(throwable.getMessage()),
                        () -> {
                            Log.d(NFCPresenter.class.getSimpleName(), "writeUser finished");
                            // mView.onWriteNfcSuccessful();
                        }
                );

    }


    public void reWriteUserData(UserData userData) {

        mSubscribe = mUserManager.reWriteUserData(userData)
                .lastElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showSuccessReWrite,
                        throwable -> mFragment.showErrorReWrite(throwable.getMessage()),
                        () -> {
                            Log.d(NFCPresenter.class.getSimpleName(), "writeUser finished");
                            // mView.onWriteNfcSuccessful();
                        }
                );

    }

    public void debitNFC_Card(String idEvento, String value) {
        UserData userData = new UserData(Utils.addAsterisk(value), null,
                null, null, null,
                null, Utils.addAsterisk(idEvento), null);

        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> debitUserData(userData),
                        throwable -> mFragment.showErrorDebitNfc(throwable.getMessage())
                );
    }

    public void debitUserData(UserData userData) {

        mSubscribe = mUserManager.debitUserData(userData)
                .lastElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showSuccessDebit,
                        throwable -> mFragment.showErrorDebitNfc(throwable.getMessage()),
                        () -> {
                            Log.d(NFCPresenter.class.getSimpleName(), "writeUser finished");
                            // mView.onWriteNfcSuccessful();
                        }
                );
    }

    public void formatNFCCard() {

        // using ArrayList to have no block limit
        List<Integer> blocks = Arrays.asList(
                NFCConstants.VALUE_BLOCK,
                NFCConstants.NAME_BLOCK,
                NFCConstants.CPF_BLOCK,
                NFCConstants.TAG_BLOCK,
                NFCConstants.CELL_PHONE_BLOCK,
                NFCConstants.CARD_OPENED_BLOCK,
                NFCConstants.EVENT_ID_BLOCK,
                NFCConstants.OPEN_VALUE_CARD_BLOCK
        );


        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_BLUE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> clearBlocks(blocks),
                        throwable -> mFragment.showErrorReWrite(throwable.getMessage())
                );
    }

    public void clearBlocks(List<Integer> blocks) {

        mSubscribe = mUseCase.clearBlocks(blocks)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // .distinct()
                .subscribe(
                        result -> {
                        },
                        throwable -> mFragment.showErrorFormat(throwable.getMessage()),
                        () -> showSuccessFormat(1)
                        //mView::onBlockCleanSuccessful
                );

    }

    public void showSuccessFormat(Integer res) {
        mSubscribe = mUseCase.controlLed(new PlugPagLedData(PlugPagLedData.LED_GREEN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mFragment.showSuccessFormat(res),
                        throwable -> mFragment.showErrorReWrite(throwable.getMessage())
                );

    }

    public void controlLed(PlugPagLedData plugPagLedData) {
        mSubscribe = mUseCase.controlLed(plugPagLedData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result ->
                        Log.d(NFCPresenter.class.getSimpleName(), "control Led finished"));
    }

    public void dispose() {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
    }

    public Completable abort() {
        return mUseCase.abort();
    }
}