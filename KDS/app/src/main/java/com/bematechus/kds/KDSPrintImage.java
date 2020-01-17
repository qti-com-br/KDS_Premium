package com.bematechus.kds;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by David.Wong on 2020/1/14.
 * Rev:
 */
public class KDSPrintImage {

    final String TAG = "KDSPrintImage";


    final byte ESC = 0x1b;
    final byte GS = 0x1d;

    static public final int Image_Align_Left = -1;
    static public final int Image_Align_Center = -2;
    static public final int Image_Align_Right = -3;

    private int ratioHeight(Bitmap bitmap, int nTargetWidth)
    {
        int originWidth  = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        float ratio = ((float) originHeight)/((float) originWidth);
        int h = Math.round( nTargetWidth * ratio);
        return h;

    }
    private Bitmap resizeBitmap(Bitmap bitmap, int nWidth)
    {
        if (nWidth <=0) return bitmap;

        int originWidth  = bitmap.getWidth();
        //int originHeight = bitmap.getHeight();

        // no need to resize
        if (originWidth == nWidth ) {
            return bitmap;
        }

        int width  = nWidth;
        int height = ratioHeight(bitmap, nWidth);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        //}

//        // 若图片过长, 则从上端截取
//        if (height > maxHeight) {
//            height = maxHeight;
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//        }

//        Log.i(TAG, width + " width");
//        Log.i(TAG, height + " height");

        return bitmap;

    }
    /**
     *
     * For print picture
     */
    private Bitmap readPicture(String srcImageFile, int nWidth)
    {
//        try
//        {
            Bitmap src = null;
//            if (srcImageFile.indexOf(".bmp") > 0)
//            {

                FileInputStream is = null;
                try
                {
                    is = new FileInputStream(srcImageFile);

                    BitmapFactory.Options options = new BitmapFactory.Options();

                    //options.inSampleSize = smallerScale;
                    options.inMutable = true;
                    src  = BitmapFactory.decodeStream(is, null, options);
                    return resizeBitmap(src, nWidth);

                }
                catch (Exception e)
                {
                    return null;
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (Exception e)
                        {
                            return null;
                        }
                    }
                }


//            }
//            else
//            {
//                src = ImageIO.read(new File(srcImageFile)); // �����ļ�
//            }
            //return src;



//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return null;
    }
    private void applyBayer(Bitmap src)
    {
        int pattern[][] = {
                {   0,192, 48,240, 12,204, 60,252,  3,195, 51,243, 15,207, 63,255 },
                { 128, 64,176,112,140, 76,188,124,131, 67,179,115,143, 79,191,127 },
                {  32,224, 16,208, 44,236, 28,220, 35,227, 19,211, 47,239, 31,223 },
                { 160, 96,144, 80,172,108,156, 92,163, 99,147, 83,175,111,159, 95 },
                {   8,200, 56,248,  4,196, 52,244, 11,203, 59,251,  7,199, 55,247 },
                { 136, 72,184,120,132, 68,180,116,139, 75,187,123,135, 71,183,119 },
                {  40,232, 24,216, 36,228, 20,212, 43,235, 27,219, 39,231, 23,215 },
                { 168,104,152, 88,164,100,148, 84,171,107,155, 91,167,103,151, 87 },
                {   2,194, 50,242, 14,206, 62,254,  1,193, 49,241, 13,205, 61,253 },
                { 130, 66,178,114,142, 78,190,126,129, 65,177,113,141, 77,189,125 },
                {  34,226, 18,210, 46,238, 30,222, 33,225, 17,209, 45,237, 29,221 },
                { 162, 98,146, 82,174,110,158, 94,161, 97,145, 81,173,109,157, 93 },
                {  10,202, 58,250,  6,198, 54,246,  9,201, 57,249,  5,197, 53,245 },
                { 138, 74,186,122,134, 70,182,118,137, 73,185,121,133, 69,181,117 },
                {  42,234, 26,218, 38,230, 22,214, 41,233, 25,217, 37,229, 21,213 },
                { 170,106,154, 90,166,102,150, 86,169,105,153, 89,165,101,149, 85 }
        };



        int i, j;
        int pixel;
        //for ( i = 0; i < (UINT)m_szSize.cy; i++ ) {
        for (i = 0; i < src.getHeight(); i++)
        {
            //for ( j=0; j < (UINT)m_szSize.cx; j++) {
            for (j = 0; j < src.getWidth(); j++)
            {
                int ref = (byte)src.getPixel(j, i);

                pixel = howBlack(ref);

                if (pixel >= pattern[j & 15][i & 15])
                {
                    src.setPixel(j, i, 0x0);//Color.WHITE);//0x0);// ); //use 0/1, it will print to printer directly.
                }
                else
                    src.setPixel(j, i,0x1);// Color.BLACK);// 0x1);//);

            }

        }
    }
    private int howBlack(int color)
    {
        int sum = ((color & 0x000000ff) << 1);
        sum += (((color & 0x0000ff00) >> 8) << 2);
        sum += (((color & 0x00ff0000) >> 16));
        return (int)(sum / 7);

    }

    /**
     *
     * @param srcImageFile
     * @param nWidth
     *
     *          * If set to a value > 1, requests the decoder to subsample the original
     *          * image, returning a smaller image to save memory. The sample size is
     *          * the number of pixels in either dimension that correspond to a single
     *          * pixel in the decoded bitmap. For example, inSampleSize == 4 returns
     *          * an image that is 1/4 the width/height of the original, and 1/16 the
     *          * number of pixels. Any value <= 1 is treated the same as 1. Note: the
     *          * decoder uses a final value based on powers of 2, any other value will
     *          * be rounded down to the nearest power of 2.
     *
     * @return
     */
    private Bitmap monochrome(String srcImageFile, int nWidth)
    {

        Bitmap src = readPicture(srcImageFile, nWidth);
        //test
        //saveBitmap(src, "b0.png");
        //
        if (src == null) return null;
//        int width = src.getWidth(); //
//        int height = src.getHeight();

//        width = width * (scale) / 100;
//        height = height * (scale) / 100;

        //Image image = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
//        Bitmap image = src.
//
//        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = tag.createGraphics();
//        //g.rotate(Math.toRadians(rotation));
//        g.drawImage(image, 0, 0, null);
//        //g.translate(5, 5);
//        //g.drawImage(image, 0, 0, null);
//        g.dispose();

        //test
//        Bitmap bm = resizeBitmap(src, 200);
//        saveBitmap(bm, "b0.png");
        //
        //applyBayer(src);

        return src;
        //ImageIO.write(tag, "JPEG", new File(result));// ������ļ���
        //return null;
    }

    /**
     *
     *
     *    x: 24 pixel
     *    y: 24 pixel
     *    Get data to each block, just for speed. Actually, all data is in one buffer.
     *
     *    .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  . //24 bytes/row
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .byte 0
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .byte 1
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    .
     *    . byte 2
     *
     * @param src
     * @param paperDotWidth
     *      LR2000, it is 512dots/line
     * @param charDotWidth
     * @param nAlign
     * @return
     */
    private int PrintBitMap(Bitmap src, int paperDotWidth, int charDotWidth, int nAlign)
    {
        //debugmsg("PrintBitMap: enter");

        byte buffer[];
        int bufferSize;
        byte spaces[] = new byte[256];
        int i = 0;
        for (i = 0; i < 256; i++)
        {
            spaces[i] = ' ';
        }
        //debugmsg("PrintBitMap: 1");

        int width = paperDotWidth;//m_pPrinterProperties->m_RecLineWidth;//mbdc.GetMaxColumns();
        byte Header[] = new byte[5];
        byte Data[] = new byte[3*832]; //each time save 24 lines picture pixel data. 832 is the max paper dots size
        byte CR;

        int nRet = 1;
        int LineSize = width;
        int BlockSize = 24;//24 lines, use 3 byte.
        int nCyBlockCount = (src.getHeight() + BlockSize-1) / BlockSize;
        int BlockCount = LineSize / BlockSize;//src.getWidth()/BlockSize;// use the line width each page blockcount

        if (LineSize % BlockSize > 0)
            BlockCount++;

        int NumOfPages = src.getWidth() / LineSize;

        if  ((src.getWidth() % LineSize) >0)
            NumOfPages ++;

        int spacescount = 0;

        if (NumOfPages == 1) //just one page, we calculate the space count
        {
            int n = LineSize - src.getWidth(); //dots
            int chardots = charDotWidth;//m_pPrinterProperties->GetCharDotsWidth();
            //int halfchar = chardots / 2;

            if (nAlign == Image_Align_Left) //left
                spacescount = 0;
            else if (nAlign == Image_Align_Center)
                spacescount = n / 2;
            else if (nAlign == Image_Align_Right)
                spacescount = n;
            else
            {
                int nl = Math.abs(nAlign);
                spacescount = nl > n ? n : nl;
            }
            spacescount = spacescount / chardots;

        }

        //int height = src.getHeight();
        Header[0] = ESC;//; //esc, 24 bits bitmap
        Header[1] = '*';
        Header[2] = '!'; //33, 24 dots in vertical, 180dpi
        Header[3] = (byte)((BlockSize * BlockCount) & 0xFF);
        Header[4] = (byte)((BlockSize * BlockCount) >> 8);
        CR = '\n';

        //byte fillLine[] = { ESC, '$', (byte)((short)width & 0x00ff), (byte)(((short)width & 0xff00) >> 8) };

        int y;
        int x;
        int pixely;

        int j;
        int page;
        int tamanho = 0;

        int xx = 0;
        int count = 0;
        int picwidth = src.getWidth();
        int picheight = src.getHeight();
        //for buffering printing, improve speed
        int eachPrintingRow = 30;
        byte BufferPrintingData[] = new byte[3 * eachPrintingRow *832+256]; //each time save 24 lines picture pixel data.
        int nBufferPrintingBufferPos = 0;
        int nPrintingRowCounter = 0;
        //////////////////////////////////////
        for (page = 0; page < NumOfPages; page++)
        {
            //debugmsg("CyBlockCount="+nCyBlockCount);
            for (y = 0; y < nCyBlockCount; ++y) //height loops
            {
                count = 0;
                for (j = 0; j < BlockCount; ++j) //width loops, max width each printing is the page width
                {
                    for (x = j * BlockSize; x < (j + 1) * BlockSize; ++x) //24dots each block
                    {
                        xx = x + width * page; //page width
                        for (i = 0; i < 3; ++i)
                        {
                            pixely = y * 24 + i * 8;
                            int yy = 0;
                            byte val = 0;
                            int steps = 0;
                            for (int yindex = 0 ; yindex < 8; yindex++)
                            {
                                yy = pixely + yindex;
                                steps = 7 - yindex;
                                if (yy < picheight && xx < picwidth)
                                    val |=(byte)(((byte)src.getPixel(xx, yy )) << steps);

                            }
                            Data[count++] = val;//(byte)(b7 | b6 | b5 | b4 | b3 | b2 | b1| b0);

                        }
                    }

                }
                //24 lines data is ready, save to buffer.
                //add command to first.
                //debugmsg("PrintBitMap: 6");
                tamanho = (int)((Header.length) + (3 * BlockSize) * BlockCount); // row=3, col = blocksize.
                bufferSize = tamanho + 3 + spacescount ;//+ 1;
                //				end,	spaces for move pic, cr

                buffer = new byte[bufferSize]; //add the space for align

                int counter = 0;
                System.arraycopy(spaces, 0, buffer, counter, spacescount);
                counter += spacescount;
                //add command
                System.arraycopy(Header, 0, buffer, counter, Header.length);
                counter += Header.length;
                //add 24lines pixels data
                System.arraycopy(Data, 0, buffer, counter, count);

                //Print and feed paper
                buffer[bufferSize - 3] = ESC;
                buffer[bufferSize - 2] = 'J';
                buffer[bufferSize - 1] = 0x00;
                //commandTX(buffer, bufferSize);


                ///save data, in order to print n row each time, see "eachPrintingRow" value
                System.arraycopy(buffer, 0, BufferPrintingData, nBufferPrintingBufferPos, bufferSize);
                nBufferPrintingBufferPos += bufferSize;
                nPrintingRowCounter++;
                if (nPrintingRowCounter >= eachPrintingRow || y == (nCyBlockCount-1))
                {
                    commandTX(BufferPrintingData, nBufferPrintingBufferPos);
                    nBufferPrintingBufferPos = 0;
                    nPrintingRowCounter = 0;
                }
                ///////////////////
                buffer = null;


            }
        }
        //debugmsg("PrintBitMap: exit");
        return 1;
    }

    final int LR2000_WIDTH_DOTS = 504;
    final int LR2000_WIDTH_CHARS = 42;
    /**
     *
     * @param srcImageFile
     * @param nWidth
     *  -1: change nothing
     * @param nAlign
     *
     * @return
     */
    public int printPicture(String srcImageFile, int nWidth, int nAlign)
    {
        clear();

        int nPaperDotWidth = LR2000_WIDTH_DOTS;//In lr2000, it is fixed value, 504.
        int nCharDotWidth = LR2000_WIDTH_CHARS; //fixed value: 42
        Bitmap img = monochrome(srcImageFile, nWidth);
        if (img == null)
            return 0;
//        //debugmsg("printPicture:1");
//        if (GetPrinterModel() == PRN_MP4000TH) //2009-09-09 modify for pireus
//        {
//            if (_TEMP_CMD)
//                StartEscBemaCommandSet(null);
//        }
        int nret = PrintBitMap(img, nPaperDotWidth, nCharDotWidth, nAlign);

//        if (GetPrinterModel() == PRN_MP4000TH)
//        {
//            if (_TEMP_CMD)
//                ReturnToPrevCommandSet(null);
//        }
        return nret;

    }

    ArrayList<Byte> m_imagePrintingData = new ArrayList<>();
    /**
     * save all data to buffer, we can use it repeatly.
     * @param buffer
     * @param nlen
     * @return
     */
    private int commandTX(byte buffer[], int nlen)
    {
        for (int i=0; i< nlen; i++)
            m_imagePrintingData.add(buffer[i]);
        return nlen;
    }
    public int getDataSize()
    {
        return m_imagePrintingData.size();
    }
    public int getData(byte[] buffer)
    {
        for (int i=0; i < m_imagePrintingData.size(); i++)
        {
            buffer[i] = m_imagePrintingData.get(i).byteValue();
        }
        return m_imagePrintingData.size();
    }
    public void clear()
    {
        m_imagePrintingData.clear();
    }

    public void test()
    {
        String file = "/sdcard/Pictures/a.png";

//        Bitmap img = monochrome(file, 1);
//        saveBitmap(img, "bb.png");
        printPicture(file, -1, Image_Align_Center);

    }

    public void saveBitmap(Bitmap bm, String fileName) {
        Log.e(TAG, "save image");
        File f = new File("/sdcard/Pictures/", fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.i(TAG, "image saved");
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
