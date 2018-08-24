package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/8/27 0027.
 */
public class KDSUIFontPickerDialog {
    static private final String TAG = "KDSUIFontPickerDialog";
    static public KDSUIFontPickerDialog g_instance = null;
    public interface OnFontPickerDlgListener {
        public void onCancel(KDSUIFontPickerDialog dialog);

        public void onOk(KDSUIFontPickerDialog dialog, KDSViewFontFace ff);
    }

    AlertDialog dialog = null;

    OnFontPickerDlgListener listener= null;
    final View viewHue = null;
    Spinner m_spinnerFontSize = null;
    ListView m_lstFonts = null;
    TextView m_txtDemo = null;
    Button m_btnBG = null;
    Button m_btnFG = null;
    KDSViewFontFace m_ffOriginal = null;
    /*****************************************************************************************/
    private void createDefaultFontFace()
    {
//        Paint p = new Paint();
        m_ffOriginal = new KDSViewFontFace();//p.getTypeface(), (int)p.getTextSize());
//        Typeface t = p.getTypeface();//.toString();
//        String s = t.toString();
//        Log.i("FFP", s);
    }
    public KDSUIFontPickerDialog(final Context context, KDSViewFontFace ff, boolean bChooseColor, OnFontPickerDlgListener listener) {
        g_instance = this;
        this.listener = listener;
        m_ffOriginal = ff;
        if (m_ffOriginal == null)
            createDefaultFontFace();

        final View view = LayoutInflater.from(context).inflate(R.layout.kdsui_font_picker_dlg, null);

        m_txtDemo = (TextView)view.findViewById(R.id.txtDemo);


        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIFontPickerDialog.this.listener != null) {
                            KDSUIFontPickerDialog.this.listener.onOk(KDSUIFontPickerDialog.this, getFontFace());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIFontPickerDialog.this.listener != null) {

                            KDSUIFontPickerDialog.this.listener.onCancel(KDSUIFontPickerDialog.this);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if (KDSUIFontPickerDialog.this.listener != null) {

                            KDSUIFontPickerDialog.this.listener.onCancel(KDSUIFontPickerDialog.this);
                        }

                    }
                })
                .create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                g_instance = null;
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });


        m_lstFonts = (ListView)view.findViewById(R.id.lstFont);
        // Get the fonts on the device
        FontManager fm = new FontManager();
        HashMap< String, String > fonts = fm.enumerateFonts();
        m_fontPaths = new ArrayList< String >();
        m_fontNames = new ArrayList< String >();

        // Get the current value to find the checked item
        //String selectedFontPath = getSharedPreferences().getString( getKey(), "");
        int idx = 0, checked_item = 0;

        for ( String path : fonts.keySet() )
        {
            if ( path.equals( m_ffOriginal.getFontFilePath() ) )
                checked_item = idx;

            m_fontPaths.add( path );
            m_fontNames.add( fonts.get(path) );
            idx++;
        }

        m_lstFonts.setAdapter(new FontAdapter());



        m_lstFonts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = m_fontPaths.get(position);
                Typeface tface = Typeface.createFromFile(s);
                m_ffOriginal.setFontFilePath(s);
                //Typeface tface = Typeface.create.createFromFile(m_fontPaths.get(position));
                m_txtDemo.setTypeface(tface);
            }
        });

        m_lstFonts.setSelection(checked_item);
        m_lstFonts.setItemChecked(checked_item, true);


        // kill all padding from the dialog window
        dialog.setView(view, 0, 0, 0, 0);

        m_spinnerFontSize = (Spinner)view.findViewById(R.id.cmbSize);

        m_spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) m_spinnerFontSize.getSelectedItem();
                int nSize = Integer.parseInt(s);
                m_ffOriginal.setFontSize(nSize);
                m_txtDemo.setTextSize(nSize);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String strSize =(String) m_spinnerFontSize.getItemAtPosition(0);
        int n = Integer.parseInt(strSize);

        m_spinnerFontSize.setSelection(m_ffOriginal.getFontSize() - n);
        m_txtDemo.setTextSize(m_ffOriginal.getFontSize());
        m_txtDemo.setTypeface(m_ffOriginal.getTypeFace());

        m_btnBG = (Button)view.findViewById(R.id.btnBG);
        if (!bChooseColor)
            m_btnBG.setVisibility(View.INVISIBLE);
        m_btnBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.bematechus.kdslib.KDSUIColorPickerDialog d = new com.bematechus.kdslib.KDSUIColorPickerDialog(dialog.getContext(), m_ffOriginal.getBG(), new KDSUIColorPickerDialog.OnColorPickerDlgListener() {
                    @Override
                    public void onCancel(KDSUIColorPickerDialog dialog) {

                    }

                    @Override
                    public void onOk(KDSUIColorPickerDialog dialog, int color) {
                        m_txtDemo.setBackgroundColor(color);
                        m_ffOriginal.setBG(color);
                    }
                });
                d.show();
            }
        });

        m_btnFG = (Button)view.findViewById(R.id.btnFG);
        if (!bChooseColor)
            m_btnFG.setVisibility(View.INVISIBLE);
        m_btnFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KDSUIColorPickerDialog d = new KDSUIColorPickerDialog(dialog.getContext(),m_ffOriginal.getFG(), new KDSUIColorPickerDialog.OnColorPickerDlgListener() {
                    @Override
                    public void onCancel(KDSUIColorPickerDialog dialog) {

                    }

                    @Override
                    public void onOk(KDSUIColorPickerDialog dialog, int color) {
                        m_txtDemo.setTextColor(color);
                        m_ffOriginal.setFG(color);
                    }
                });
                d.show();
            }
        });

        m_txtDemo.setBackgroundColor(m_ffOriginal.getBG());
        m_txtDemo.setTextColor(m_ffOriginal.getFG());
    }


    KDSViewFontFace getFontFace()
    {
        return m_ffOriginal;
    }


    public void show() {
        dialog.show();
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    private List< String >    m_fontPaths;
    private List< String > m_fontNames;
    // Font adaptor responsible for redrawing the item TextView with the appropriate font.
    // We use BaseAdapter since we need both arrays, and the effort is quite small.
    public class FontAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return m_fontNames.size();
        }

        @Override
        public Object getItem(int position)
        {
            return m_fontNames.get( position );
        }

        @Override
        public long getItemId(int position)
        {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            View view = convertView;

            // This function may be called in two cases: a new view needs to be created,
            // or an existing view needs to be reused
            if ( view == null )
            {
                // Since we're using the system list for the layout, use the system inflater
                final LayoutInflater inflater = (LayoutInflater)
                        dialog.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE );

                // And inflate the view android.R.layout.select_dialog_singlechoice
                // Why? See com.android.internal.app.AlertController method createListView()
                view = inflater.inflate( android.R.layout.select_dialog_singlechoice, parent, false);
            }

            if ( view != null )
            {
                // Find the text view from our interface
                CheckedTextView tv = (CheckedTextView) view.findViewById( android.R.id.text1 );

                // Replace the string with the current font name using our typeface
                Typeface tface = Typeface.createFromFile(m_fontPaths.get(position));
                tv.setTypeface( tface );

                // If you want to make the selected item having different foreground or background color,
                // be aware of themes. In some of them your foreground color may be the background color.
                // So we don't mess with anything here and just add the extra stars to have the selected
                // font to stand out.
                tv.setText( m_fontNames.get( position ) );
            }

            return view;
        }
    }




    // The class which loads the TTF file, parses it and returns the TTF font name
    public class TTFAnalyzer
    {
        // This function parses the TTF file and returns the font name specified in the file
        public String getTtfFontName( String fontFilename )
        {
            try
            {
                // Parses the TTF file format.
                // See http://developer.apple.com/fonts/ttrefman/rm06/Chap6.html
                m_file = new RandomAccessFile( fontFilename, "r" );

                // Read the version first
                int version = readDword();

                // The version must be either 'true' (0x74727565) or 0x00010000
                if ( version != 0x74727565 && version != 0x00010000 )
                    return null;

                // The TTF file consist of several sections called "tables", and we need to know how many of them are there.
                int numTables = readWord();

                // Skip the rest in the header
                readWord(); // skip searchRange
                readWord(); // skip entrySelector
                readWord(); // skip rangeShift

                // Now we can read the tables
                for ( int i = 0; i < numTables; i++ )
                {
                    // Read the table entry
                    int tag = readDword();
                    readDword(); // skip checksum
                    int offset = readDword();
                    int length = readDword();

                    // Now here' the trick. 'name' field actually contains the textual string name.
                    // So the 'name' string in characters equals to 0x6E616D65
                    if ( tag == 0x6E616D65 )
                    {
                        // Here's the name section. Read it completely into the allocated buffer
                        byte[] table = new byte[ length ];

                        m_file.seek( offset );
                        read( table );

                        // This is also a table. See http://developer.apple.com/fonts/ttrefman/rm06/Chap6name.html
                        // According to Table 36, the total number of table records is stored in the second word, at the offset 2.
                        // Getting the count and string offset - remembering it's big endian.
                        int count = getWord( table, 2 );
                        int string_offset = getWord( table, 4 );

                        // Record starts from offset 6
                        for ( int record = 0; record < count; record++ )
                        {
                            // Table 37 tells us that each record is 6 words -> 12 bytes, and that the nameID is 4th word so its offset is 6.
                            // We also need to account for the first 6 bytes of the header above (Table 36), so...
                            int nameid_offset = record * 12 + 6;
                            int platformID = getWord( table, nameid_offset );
                            int nameid_value = getWord( table, nameid_offset + 6 );

                            // Table 42 lists the valid name Identifiers. We're interested in 4 but not in Unicode encoding (for simplicity).
                            // The encoding is stored as PlatformID and we're interested in Mac encoding
                            if ( nameid_value == 4 && platformID == 1 )
                            {
                                // We need the string offset and length, which are the word 6 and 5 respectively
                                int name_length = getWord( table, nameid_offset + 8 );
                                int name_offset = getWord( table, nameid_offset + 10 );

                                // The real name string offset is calculated by adding the string_offset
                                name_offset = name_offset + string_offset;

                                // Make sure it is inside the array
                                if ( name_offset >= 0 && name_offset + name_length < table.length )
                                    return new String( table, name_offset, name_length );
                            }
                        }
                    }
                }

                return null;
            }
            catch (FileNotFoundException e)
            {
                KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
                // Permissions?
                return null;
            }
            catch (IOException e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
                // Most likely a corrupted font file
                return null;
            }
        }

        // Font file; must be seekable
        private RandomAccessFile m_file = null;

        // Helper I/O functions
        private int readByte() throws IOException
        {
            return m_file.read() & 0xFF;
        }

        private int readWord() throws IOException
        {
            int b1 = readByte();
            int b2 = readByte();

            return b1 << 8 | b2;
        }

        private int readDword() throws IOException
        {
            int b1 = readByte();
            int b2 = readByte();
            int b3 = readByte();
            int b4 = readByte();

            return b1 << 24 | b2 << 16 | b3 << 8 | b4;
        }

        private void read( byte [] array ) throws IOException
        {
            if ( m_file.read( array ) != array.length )
                throw new IOException();
        }

        // Helper
        private int getWord( byte [] array, int offset )
        {
            int b1 = array[ offset ] & 0xFF;
            int b2 = array[ offset + 1 ] & 0xFF;

            return b1 << 8 | b2;
        }
    }


    public class FontManager
    {
        // This function enumerates all fonts on Android system and returns the HashMap with the font
        // absolute file name as key, and the font literal name (embedded into the font) as value.
        public HashMap< String, String > enumerateFonts()
        {
            String[] fontdirs = { "/system/fonts", "/system/font", "/data/fonts" };
            HashMap< String, String > fonts = new HashMap< String, String >();
            TTFAnalyzer analyzer = new TTFAnalyzer();

            for ( String fontdir : fontdirs )
            {
                File dir = new File( fontdir );

                if ( !dir.exists() )
                    continue;

                File[] files = dir.listFiles();

                if ( files == null )
                    continue;

                for ( File file : files )
                {
                    String fontname = analyzer.getTtfFontName( file.getAbsolutePath() );

                    if ( fontname != null )
                        fonts.put( file.getAbsolutePath(), fontname );
                }
            }

            return fonts.isEmpty() ? null : fonts;
        }
    }
}
