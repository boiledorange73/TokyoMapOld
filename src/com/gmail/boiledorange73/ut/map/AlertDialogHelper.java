package com.gmail.boiledorange73.ut.map;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.webkit.WebView;

/**
 * Static methods set for AlertDialog.
 * 
 * @author yellow
 * 
 */
public abstract class AlertDialogHelper {
    /**
     * The interface for {@link AlertDialogHelper#createChoice(Context, String, List, OnChoiceListener)}.
     * @author yellow
     *
     * @param <ItemType> Type of item. 
     */
    public static interface OnChoiceListener<ItemType> {
        public void onChoice(DialogInterface dialog, ItemType item);
    }


    /**
     * Creates confirmation dialog. That prompts to progress and executes the
     * next (if "yes" is clicked) or only closes (if "no" is clicked).
     * 
     * @param context
     *            The context.
     * @param title
     *            Title of the dialog.
     * @param message
     *            Message to be shown.
     * @param listener
     *            The listener which has the next procedure.
     * @return AlertDialog instance.
     */
    public static AlertDialog createConfirmationDialog(Context context,
            String title, String message, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.yes, listener);
        builder.setNegativeButton(android.R.string.no, null);
        return builder.create();
    }

    /**
     * Creates and shows confirmation dialog.
     * 
     * @see {@link #createConfirmationDialog(Context, String, String, OnClickListener)}
     *      .
     * @param context
     *            The context.
     * @param title
     *            Title of the dialog.
     * @param message
     *            Message to be shown.
     * @param listener
     *            The listener which has the next procedure.
     */
    public static void showConfirmationDialog(Context context, String title,
            String message, OnClickListener listener) {
        AlertDialog dialog = AlertDialogHelper.createConfirmationDialog(
                context, title, message, listener);
        dialog.show();
    }

    /**
     * Creates confirmation dialog to exit. That prompts to exit and the process
     * really exits using System.exit(0) (if "yes" is clicked) or the dialog
     * only closes (if "no" is clicked).
     * 
     * @param context
     *            The context.
     * @param wConfirmation
     *            The word which means "Confirmation", written at title.
     * @param pConfirmExit
     *            The phrase which means "Are you sure to exit?", written at
     *            main.
     * @return AlertDialog instance.
     */
    public static AlertDialog createExitConfirmationDialog(Context context,
            String wConfirmation, String pConfirmExit) {
        return AlertDialogHelper.createConfirmationDialog(context,
                wConfirmation, pConfirmExit, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
    }

    /**
     * Creates and shows confirmation dialog to exit.
     * 
     * @param context
     *            The context.
     * @param wConfirmation
     *            The word which means "Confirmation", written at title.
     * @param pConfirmExit
     *            The phrase which means "Are you sure to exit?", written at
     *            main.
     * @see {@link #createExitConfirmationDialog(Context)}.
     * @param context
     *            The context.
     */
    public static void showExitConfirmationDialog(Context context,
            String wConfirmation, String pConfirmExit) {
        AlertDialog dialog = AlertDialogHelper.createExitConfirmationDialog(
                context, wConfirmation, pConfirmExit);
        dialog.show();
    }

    /**
     * Creates "About this software" dialog.
     * 
     * @param context
     *            The context.
     * @param appname
     *            The name of the application.
     * @param version
     *            The version name of the application.
     * @param url
     *            The url directing the resource which is shown as the "about"
     *            message.
     * @param icon
     *            ID of the icon. If 0, icon is not set.
     * @return AlertDialog instance.
     */
    public static AlertDialog createAbout(Context context, String appname,
            String version, String url, Integer icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(version != null && version.length() > 0 ? appname
                + " " + version : appname);
        if (icon != 0) {
            builder.setIcon(icon);
        }
        WebView webView = new WebView(context);
        builder.setView(webView);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);
        webView.loadUrl(url);
        return builder.create();
    }

    /**
     * Creates and shows "About this software" dialog.
     * 
     * @see {@link #createAbout(Context, String, String, String)}.
     * @param context
     *            The context.
     * @param appname
     *            The name of the application.
     * @param version
     *            The version name of the application.
     * @param url
     *            The url directing the resource which is shown as the "about"
     *            message.
     * @param icon
     *            ID of the icon. If 0, icon is not set.
     */
    public static void showAbout(Context context, String appname,
            String version, String url, int icon) {
        AlertDialogHelper.createAbout(context, appname, version, url, icon)
                .show();
    }

    /**
     * Creates choice dialog.
     * @param context The context.
     * @param title The title.
     * @param list List of items. toString() of the item must returns suitable text.
     * @param listener The listener whose method is called when one of items is choiced.
     * @return The dialog.
     */
    public static <ItemType>AlertDialog createChoice (Context context, String title, List<ItemType> list, final OnChoiceListener<ItemType> listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        // listadapter
        if( list != null && list.size() > 0 ) {
            int size = list.size();
            CharSequence[] texts= new CharSequence[size];
            final ArrayList<ItemType> itemList = new ArrayList<ItemType>();
            for( int n = 0; n < size; n++ ) {
                String text = "";
                ItemType item = list.get(n);
                if( item != null ) {
                    text = item.toString();
                }
                if( text == null ) {
                    text = "";
                }
                itemList.add(item);
                texts[n] = text;
            }
            
            builder.setItems(texts, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if( listener != null ) {
                        listener.onChoice(dialog, itemList.get(which));
                    }
                }
            });
        }
        builder.setPositiveButton(android.R.string.cancel, null);
        return builder.create();
    }

    /**
     * Shows choice dialog.
     * @param context The context.
     * @param title The title.
     * @param list List of items. toString() of the item must returns suitable text.
     * @param listener The listener whose method is called when one of items is choiced.
     */
    public static <ItemType>void showChoice (Context context, String title, List<ItemType> list, final OnChoiceListener<ItemType> listener) {
        AlertDialogHelper.<ItemType>createChoice(context, title, list, listener).show();
    }

    public static Dialog createSimple(Context context, String title,
            String message, final OnClickListener listener) {
        AlertDialog.Builder bld = new AlertDialog.Builder(context);
        bld.setTitle(title);
        bld.setMessage(message);
        bld.setCancelable(true);
        if( listener != null ) {
            bld.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    listener.onClick(dialog, 0);
                }
            });
        }
        bld.setPositiveButton(android.R.string.ok, listener);
        return bld.create();
    }

    public static void showSimple(Context context, String title,String message, OnClickListener listener) {
        AlertDialogHelper.createSimple(context,title,message,listener).show();
    }

}
