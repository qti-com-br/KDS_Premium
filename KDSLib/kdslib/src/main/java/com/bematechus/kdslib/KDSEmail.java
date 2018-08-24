package com.bematechus.kdslib;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by David.Wong on 2018/5/4.
 */
public class KDSEmail {


    static public void sendTo(String appName, String appInfo, String strError)
    {
        Intent intent=new Intent(Intent.ACTION_SENDTO);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("mailto:qiu.chen@bematechus.com"));
        //String[] tos = { "way.ping.li1@gmail.com" };
        String[] ccs = { "edmond.cheng@bematechus.com,david.wong@bematechus.com" };
        //String[] bccs = {"way.ping.li3@gmail.com"};
        //intent.putExtra(Intent.EXTRA_EMAIL, tos);
        intent.putExtra(Intent.EXTRA_CC, ccs);
        //intent.putExtra(Intent.EXTRA_BCC, bccs);
        intent.putExtra(Intent.EXTRA_SUBJECT, appName +" critical errors");

        String strContent = "Hi,\r\n\n";

        strContent += appInfo;
        strContent += "\r\n------------------\n\n";
        strContent += strError;
        strContent += "\r\n------------------\n\n";

        intent.putExtra(Intent.EXTRA_TEXT, strContent);
        Intent.createChooser(intent, "Email Client");
        KDSApplication.getContext().startActivity(intent);
    }
}
