package com.cb.qiangqiang.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by cb on 2016/6/21.
 */
public class TowBtnDialogFragment extends android.support.v4.app.DialogFragment {
    private static final String PARAM_POSITIVE_NAME = "param_positive_name";
    private static final String PARAM_NEGATIVE_NAME = "param_negative_name";
    private static final String PARAM_LAYOUT_ID = "param_layout_id";

    private String positiveName;
    private String negativeName;
    private int layoutId;
    private OnDialogClickListener clickListener;

    public TowBtnDialogFragment() {
    }

    public TowBtnDialogFragment(OnDialogClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public static TowBtnDialogFragment newInstance(String positiveName, String negativeName, int layoutId, OnDialogClickListener clickListener){
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_POSITIVE_NAME, positiveName);
        bundle.putString(PARAM_NEGATIVE_NAME, negativeName);
        bundle.putInt(PARAM_LAYOUT_ID, layoutId);
        TowBtnDialogFragment fragment = new TowBtnDialogFragment(clickListener);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        positiveName = bundle.getString(PARAM_POSITIVE_NAME);
        negativeName = bundle.getString(PARAM_NEGATIVE_NAME);
        layoutId = bundle.getInt(PARAM_LAYOUT_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layoutId, null);
        builder.setView(view)
                .setPositiveButton(positiveName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickListener.onPositiveClick();
                    }
                })
                .setNegativeButton(negativeName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickListener.onNegativeClick();
                    }
                });
        return builder.create();
    }

    public static class DialogBuilder implements Parcelable{
        private OnDialogClickListener onDialogClickListener;

        public DialogBuilder() {
        }

        public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
            this.onDialogClickListener = onDialogClickListener;
        }

        public OnDialogClickListener getOnDialogClickListener() {
            return onDialogClickListener;
        }

        protected DialogBuilder(Parcel in) {
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
        }
    }

    public interface OnDialogClickListener{
        void onPositiveClick();
        void onNegativeClick();
    }
}
