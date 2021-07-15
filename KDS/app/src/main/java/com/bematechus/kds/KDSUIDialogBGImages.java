package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDlgInternetFile;
import com.bematechus.kdslib.OpenFileDialog;
import com.bematechus.kdslib.OpenSmbFileDialog;

import java.util.ArrayList;
import java.util.List;

public class KDSUIDialogBGImages extends KDSUIDialogBase implements  KDSUIDialogBase.KDSDialogBaseListener,
        MediaHandler.MediaEventReceiver{

    String m_strOriginalFiles = "";
    String m_strFiles = "";

    ArrayList<String> m_arFiles = new ArrayList<>();
    ListView m_lstFiles = null;

    ImageView m_imageView = null;

    public KDSUIDialogBGImages(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, String files) {
        //this.setUseCtrlEnterKey(true);
        m_strOriginalFiles = files;
        this.int_dialog(context, listener, R.layout.kdsui_dlg_images, "");
        Button btn = (Button) this.getView().findViewById(R.id.btnLocal);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLocalFile();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnEthernet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEthernetFile();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnInternet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInternetFile();
            }
        });

        ImageView img = (ImageView) this.getView().findViewById(R.id.btnUp);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewItemUp(m_lstFiles, m_arFiles);
            }
        });
        img = (ImageView) this.getView().findViewById(R.id.btnDown);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewItemDown(m_lstFiles, m_arFiles);
            }
        });

        img = (ImageView) getView().findViewById(R.id.btnDel);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewDel(m_lstFiles, m_arFiles);
            }
        });

        m_imageView = this.getView().findViewById(R.id.imgView);

        m_lstFiles = this.getView().findViewById(R.id.lstImages);
        CSVStrings csv = CSVStrings.parse(files);
        m_arFiles = csv.getArray();
        m_lstFiles.setAdapter(new MyAdapter(context,R.layout.my_list_item_single_choice, m_arFiles));

        m_lstFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                previewImage(m_lstFiles);
            }
        });

    }

    @Override
    public void onOkClicked() {//save data here
        CSVStrings c = new CSVStrings();
        c.addAll(m_arFiles);
        m_strFiles = c.toCSV();
    }

    @Override
    public Object getResult()
    {
        return m_strFiles;


    }

    private void onLocalFile()
    {
        OpenFileDialog dlg = new OpenFileDialog(this.getView().getContext(),getValidImageExtensions(), this, OpenFileDialog.Mode.Choose_File );

        dlg.show();
    }

    private void onEthernetFile()
    {
        OpenSmbFileDialog dlg = new OpenSmbFileDialog(this.getView().getContext(),getValidImageExtensions(), this);


        dlg.show();
    }

    private void onInternetFile()
    {
//        String s = ((TextView) this.getView().findViewById(R.id.imageFileName)).getText().toString();

        KDSUIDlgInternetFile dlg = new KDSUIDlgInternetFile(this.getView().getContext(), getValidImageExtensions(), this);
//        if (ImageUtil.isInternetFile(s))
//        {
//            ((TextView)dlg.getView().findViewById(R.id.txtFileName)).setText(s);
//        }

        dlg.show();
    }
    private String getValidImageExtensions()
    {
        String s = ".jpg;.gif;.png;.bmp;.jpeg;";
        return s;
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {

        if ( (dialog instanceof OpenFileDialog) ||
                (dialog instanceof OpenSmbFileDialog) ||
                (dialog instanceof KDSUIDlgInternetFile) )
        {
            String s = (String)dialog.getResult();
            addImageFile(s);

        }

    }

    private int getListViewSelectedItemIndex(ListView lv)
    {
        for (int i=0; i<lv.getCount(); i++)
        {
            if (lv.isItemChecked(i))
                return i;
        }
        return -1;

    }
    private boolean isValidListIndex(ListView lv, int nIndex)
    {
        if (nIndex >= lv.getAdapter().getCount() || nIndex<0) return false;
        return true;
    }

    public void notifyListViewChanged(ListView lv)
    {
        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
    }

    private void listViewItemUp(ListView lv, List<String> arData)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        if (n<=0) return;
        String s = arData.get(n);
        arData.remove(n);
        arData.add(n-1, s);
        lv.setItemChecked(n, false);
        lv.setItemChecked(n-1, true);
        notifyListViewChanged(lv);
    }
    private void listViewItemDown(ListView lv, List<String> arData)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        if (n>=arData.size()-1) return;
        String s = arData.get(n);
        arData.remove(n);
        arData.add(n+1, s);
        lv.setItemChecked(n, false);
        lv.setItemChecked(n+1, true);
        notifyListViewChanged(lv);
    }

    private void listViewDel(ListView lv, List<String> arData)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        String s = arData.get(n);
        arData.remove(n);
        notifyListViewChanged(lv);
    }

    private void addImageFile(String fileName)
    {
        m_arFiles.add(fileName);
        notifyListViewChanged(m_lstFiles);
    }

    private void previewImage(ListView lv)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        String s = m_arFiles.get(n);

        ImageView v = this.getView().findViewById(R.id.imgView);
        playImage(v, s);

    }


    //MediaHandler m_handler = new MediaHandler(this);

    Bitmap m_internetBmp = null;
    // String m_internetFile = "";
    private boolean playImage(ImageView v, String fileName)
    {



        Bitmap bmp = null;
        if (ImageUtil.isLocalFile(fileName)) {
            //setPauseImageSlipTimer(false);
            bmp = ImageUtil.getLocalBitmap(fileName);
        }
        else if (ImageUtil.isSmbFile(fileName))
        {
            //pauseAllProgress(true);
            //setPauseImageSlipTimer(true);
            ImageUtil.downloadSmbFile(fileName, this);
            return true;
        }
        else { //http
            // m_internetFile = fileName;
            //setPauseImageSlipTimer(true);
            m_internetBmp = null;
            ImageUtil.downloadInternetBitmap(fileName, this);
//            Object[] objs = new Object[]{fileName};
//            AsyncTask task = new AsyncTask() {
//                @Override
//                protected Object doInBackground(Object[] params) {
//                    String httpFileName = (String)params[0];
//                    m_internetBmp = ImageUtil.getHttpBitmap(httpFileName, KDSUIDialogBGImages.this);
//                    //m_handler.sendHttpBitmapDownloadedMessage(m_internetBmp);
//                    return null;
//                }
//            };
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);
            return true;
        }

        v.setImageBitmap(bmp);
        return (bmp != null);
    }

    public void medaievent_onSmbFileDownloaded(String localFileName)
    {
//        if (isImage(localFileName)) {
//            setPauseImageSlipTimer(false);
            Bitmap bmp = ImageUtil.getLocalBitmap(localFileName);
            if (bmp == null) {
                m_imageView.setImageBitmap(null);
                showInfo(this.getView().getContext().getString(R.string.invalid_image_file) + getSelectedFileName());
                return;
            }
            m_imageView.setImageBitmap(bmp);
//        }
    }
    public void medaievent_onHttpBitmapFileDownloaded(Bitmap bmp)
    {
        m_internetBmp = bmp;
        m_imageView.setImageBitmap(m_internetBmp);
        if (m_internetBmp == null)
        {

            showInfo(this.getView().getContext().getString(R.string.invalid_image_file) + getSelectedFileName());
        }
    }

    private void showInfo(String s)
    {
        Toast.makeText(this.getView().getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private String getSelectedFileName()
    {
        int n = getListViewSelectedItemIndex(m_lstFiles);
        if (!isValidListIndex(m_lstFiles, n)) return "";
        String s = m_arFiles.get(n);
        return s;
    }

    private class MyAdapter extends ArrayAdapter {
        public MyAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, 0, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);
            TextView text = (TextView) v;
            String s = (String) this.getItem(position);
            //for smb file, hide its password.
            if (KDSSMBPath.isSmbFile(s))
            {
                KDSSMBPath p = KDSSMBPath.parseString(s);
                s =  p.toDisplayString();
            }

            text.setText(s);


            return v;
        }

    }

}
