package com.example.jeff.swap.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by jeff on 15-03-29.
 */
public class InformationDialogFragment extends DialogFragment {
    public static InformationDialogFragment newInstance(String message, String title) {
        InformationDialogFragment fragment = new InformationDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putString("title",title);
        fragment.setArguments(args);
        return fragment;
    }

    public InformationDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString("message");
        String title = getArguments().getString("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }
}
