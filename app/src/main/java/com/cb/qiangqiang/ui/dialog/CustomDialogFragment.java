package com.cb.qiangqiang.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by cb on 2016/6/21.
 */
public class CustomDialogFragment extends android.support.v4.app.DialogFragment {
    private static final String PARAM_POSITIVE_NAME = "param_positive_name";
    private static final String PARAM_NEGATIVE_NAME = "param_negative_name";
    private static final String PARAM_MESSAGE = "param_message";
    private static final String PARAM_TITLE = "param_title";
    private static final String PARAM_CANCELABLE = "param_cancel_able";
    private static final String PARAM_DIALOG_BUILDER = "param_dialog_builder";

    private String positiveName;
    private String negativeName;
    private String message;
    private String title;
    private boolean cancelable = true;
    private DialogBuilder mBuilder;

    public CustomDialogFragment() {
    }

    public static CustomDialogFragment newInstance(DialogBuilder builder){
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_DIALOG_BUILDER, builder);
        CustomDialogFragment fragment = new CustomDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mBuilder = bundle.getParcelable(PARAM_DIALOG_BUILDER);
        if (mBuilder != null){
            title = mBuilder.getTitle();
            message = mBuilder.getMessage();
            positiveName = mBuilder.getPositiveName();
            negativeName = mBuilder.getNegativeName();
            cancelable = mBuilder.getCancelable();
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(TextUtils.isEmpty(message)? " " : message);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        if (!TextUtils.isEmpty(positiveName)) builder.setPositiveButton(positiveName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mBuilder != null){
                    mBuilder.onPositiveClick();
                }
            }
        });
        if (!TextUtils.isEmpty(negativeName)) builder.setNegativeButton(negativeName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mBuilder != null){
                    mBuilder.onNegativeClick();
                }
            }
        });
        setCancelable(cancelable);
        return builder.create();
    }

    public static class DialogBuilder implements Parcelable, OnDialogClickListener{
        protected String title = "";
        protected String message = "";
        protected String positiveName = "";
        protected String negativeName = "";
        protected boolean[] cancelable = {true};

        public DialogBuilder() {

        }

        public DialogBuilder message(String message){
            this.message = message;
            return this;
        }

        public DialogBuilder title(String title){
            this.title = title;
            return this;
        }
        public DialogBuilder positiveName(String positiveName){
            this.positiveName = positiveName;
            return this;
        }
        public DialogBuilder negativeName(String negativeName){
            this.negativeName = negativeName;
            return this;
        }
        public DialogBuilder cancelable(boolean cancelable){
            this.cancelable = new boolean[]{cancelable};
            return this;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getPositiveName() {
            return positiveName;
        }

        public String getNegativeName() {
            return negativeName;
        }

        public boolean getCancelable() {
            return cancelable[0];
        }

        protected DialogBuilder(Parcel in) {
            title = in.readString();
            message = in.readString();
            positiveName = in.readString();
            negativeName = in.readString();
            in.readBooleanArray(cancelable);
        }

        public static final Creator<DialogBuilder> CREATOR = new Creator<DialogBuilder>() {
            @Override
            public DialogBuilder createFromParcel(Parcel in) {
                return new DialogBuilder(in);
            }

            @Override
            public DialogBuilder[] newArray(int size) {
                return new DialogBuilder[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(message);
            dest.writeString(positiveName);
            dest.writeString(negativeName);
            dest.writeBooleanArray(cancelable);
        }

        @Override
        public void onPositiveClick() {

        }

        @Override
        public void onNegativeClick() {

        }
    }

    public interface OnDialogClickListener{
        void onPositiveClick();
        void onNegativeClick();
    }
}
