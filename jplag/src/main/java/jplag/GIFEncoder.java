package jplag;

import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class <CODE>GIFEncoder</CODE> takes an <CODE>Image</CODE> and saves it to a
 * file using the <CODE>GIF</CODE> file format (<A
 * HREF="http://www.dcs.ed.ac.uk/%7Emxr/gfx/">Graphics Interchange Format</A>).
 * A <CODE>GIFEncoder</CODE> object is constructed with either an
 * <CODE>Image</CODE> object (which must be fully loaded), or a set of three
 * 2-dimensional <CODE>byte</CODE> arrays. The image file can be written out
 * with a call to {@link #write(OutputStream) write()}.
 * <p>
 * Three caveats:
 * <UL>
 * <LI>Class <CODE>GIFEncoder</CODE> will convert the image to indexed color
 * upon construction. This will take some time, depending on the size of the
 * image. Also, the act of writing the image to disk will take some time.</LI>
 * <LI>The image cannot have more than 256 colors, since GIF is an 8 bit format.
 * For a 24 bit to 8 bit quantization algorithm, see Graphics Gems II III.2 by
 * <A HREF="http://www.csd.uwo.ca/faculty/wu/">Xialoin Wu</A>. Or check out his
 * <A HREF="http://www.csd.uwo.ca/faculty/wu/cq.c">C source</A>.</LI>
 * <LI>Since the image must be completely loaded into memory,
 * <CODE>GIFEncoder</CODE> may have problems with large images. Attempting to
 * encode an image which will not fit into memory will probably result in the
 * following exception:<BR>
 * <CODE>java.awt.AWTException: Grabber returned false:
 * 192</CODE></LI>
 * </UL>
 * <CODE>GIFEncoder</CODE> is based upon gifsave.c, which was written and
 * released by:
 * <p>
 * <DIV ALIGN="CENTER"> Sverre H. Huseby<BR>
 * Bjoelsengt. 17<BR>
 * N-0468 Oslo<BR>
 * Norway
 * <p>
 * Phone: +47 2 230539<BR>
 * <A HREF="mailto:sverrehu@ifi.uio.no">sverrehu@ifi.uio.no</A><BR>
 * </DIV>
 *
 * @author Adam Doppelt (dead link <A
 * @author Greg Faron - Integre Technical Publishing -
 * @version 0.90 21 Apr 1996
 */
public class GIFEncoder {
    /**
     * image height in pixels
     */
    short imageWidth;
    short imageHeight;
    /**
     * number of different colours in image
     */
    int numberOfColors;
    /**
     * linear array of all pixels in row major order.
     */
    byte[] allPixels = null;
    /**
     * list of all colours used in the image.
     */
    byte[] allColors = null;

    private static final Logger LOGGER = Logger.getLogger(GIFEncoder.class.getName());

    /**
     * Convenience constructor for class <CODE>GIFEncoder</CODE>. The argument
     * will be converted to an indexed color array. <B>This may take some
     * time.</B>
     *
     * @param image The image to encode. The image <B>must</B> be completely
     *              loaded.
     * @throws AWTException Will be thrown if the pixel grab fails. This can happen if
     *                      Java runs out of memory. It may also indicate that the
     *                      image contains more than 256 colors.
     */
    public GIFEncoder(Image image) throws AWTException {
        this.imageWidth = (short) image.getWidth(null);
        this.imageHeight = (short) image.getHeight(null);

        int[] values = new int[this.imageWidth * this.imageHeight];
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, this.imageWidth, this.imageHeight, values, 0, this.imageWidth);

        try {
            if (!grabber.grabPixels())
                throw new AWTException("Grabber returned false: " + grabber.status());
        } // ends try
        catch (InterruptedException ie) {
            LOGGER.log(Level.SEVERE, "Exception occure in GIFEncoder", ie);
            Thread.currentThread().interrupt();
        }

        byte[][] r = new byte[this.imageWidth][this.imageHeight];
        byte[][] g = new byte[this.imageWidth][this.imageHeight];
        byte[][] b = new byte[this.imageWidth][this.imageHeight];
        int index = 0;

        for (int y = 0; y < this.imageHeight; y++) {
            for (int x = 0; x < this.imageWidth; x++, index++) {
                r[x][y] = (byte) ((values[index] >> 16) & 0xFF);
                g[x][y] = (byte) ((values[index] >> 8) & 0xFF);
                b[x][y] = (byte) ((values[index]) & 0xFF);
            } // ends for

        } // ends for

        this.toIndexColor(r, g, b);
    } // ends constructor GIFEncoder(Image)

    /**
     * Standard constructor for class <CODE>GIFEncoder</CODE>. Each array stores
     * intensity values for the image. In other words, <NOBR>
     * <CODE>r[x][y]</CODE></NOBR> refers to the red intensity of the pixel at
     * column <CODE>x</CODE>, row <CODE>y</CODE>.
     *
     * @param r A 2-dimensional array containing the red intensity values.
     * @param g A 2-dimensional array containing the green intensity values.
     * @param b A 2-dimensional array containing the blue intensity values.
     * @throws AWTException Thrown if the image contains more than 256 colors.
     */
    public GIFEncoder(byte[][] r, byte[][] g, byte[][] b) throws AWTException {
        this.imageWidth = (short) (r.length);
        this.imageHeight = (short) (r[0].length);

        this.toIndexColor(r, g, b);
    } // ends constructor GIFEncoder(byte[][], byte[][], byte[][])

    /**
     * Writes the image out to a stream in the <CODE>GIF</CODE> file format.
     * This will be a single GIF87a image, non-interlaced, with no background
     * color. <B>This may take some time.</B>
     *
     * @param output The stream to which to output. This should probably be a
     *               buffered stream.
     * @throws IOException Thrown if a write operation fails.
     */
    public final void write(OutputStream output) throws IOException {
        BitUtils.writeString(output);
        ScreenDescriptor sd = new ScreenDescriptor(this.imageWidth, this.imageHeight, this.numberOfColors);
        sd.write(output);

        output.write(this.allColors, 0, this.allColors.length);

        ImageDescriptor id = new ImageDescriptor(this.imageWidth, this.imageHeight, ',');
        id.write(output);

        byte codesize = BitUtils.bitsNeeded(this.numberOfColors);
        if (codesize == 1)
            codesize++;
        output.write(codesize);

        LZWCompressor.lzwCompress(output, codesize, this.allPixels);
        output.write(0);

        id = new ImageDescriptor((byte) 0, (byte) 0, ';');
        id.write(output);
        output.flush();
    } // ends write(OutputStream)

    /**
     * Converts rgb desrcription of image to colour number description used by
     * GIF.
     *
     * @param r red array of pixels
     * @param g green array of pixels
     * @param b blue array of pixels
     * @throws AWTException Thrown if too many different colours in image.
     */
    final void toIndexColor(byte[][] r, byte[][] g, byte[][] b) throws AWTException {
        this.allPixels = new byte[this.imageWidth * this.imageHeight];
        this.allColors = new byte[256 * 3];
        int colornum = 0;
        for (int x = 0; x < this.imageWidth; x++) {
            for (int y = 0; y < this.imageHeight; y++) {
                int search;
                for (search = 0; search < colornum; search++) {
                    if (this.allColors[search * 3] == r[x][y] && this.allColors[search * 3 + 1] == g[x][y]
                            && this.allColors[search * 3 + 2] == b[x][y]) {
                        break;
                    } // ends if

                } // ends for

                if (search > 255)
                    throw new AWTException("Too many colors.");
                // row major order y=row x=col
                this.allPixels[y * this.imageWidth + x] = (byte) search;

                if (search == colornum) {
                    this.allColors[search * 3] = r[x][y]; // [col][row]
                    this.allColors[search * 3 + 1] = g[x][y];
                    this.allColors[search * 3 + 2] = b[x][y];
                    colornum++;
                } // ends if

            } // ends for

        } // ends for

        this.numberOfColors = 1 << BitUtils.bitsNeeded(colornum);
        byte[] copy = new byte[this.numberOfColors * 3];
        System.arraycopy(this.allColors, 0, copy, 0, this.numberOfColors * 3);
        this.allColors = copy;
    } // ends toIndexColor(byte[][], byte[][], byte[][])

} // ends class GIFEncoder

/**
 * Used to write the bits composing the GIF image.
 */
class BitFile {
    /**
     * The outputstream where the data is written.
     */
    OutputStream output;
    /**
     * buffer for bits to write.
     */
    byte[] buffer;
    /**
     *
     */
    int indexIntoOutputStream;
    int bitsLeft;

    /**
     * constructor
     *
     * @param output Where image will be written
     */
    public BitFile(OutputStream output) {
        this.output = output;
        this.buffer = new byte[256];
        this.indexIntoOutputStream = 0;
        this.bitsLeft = 8;
    } // ends constructor BitFile(OutputStream)

    /**
     * Ensures image in ram gets to disk.
     *
     * @throws IOException io exception
     */
    public final void flush() throws IOException {
        int numBytes = this.indexIntoOutputStream + ((this.bitsLeft == 8) ? 0 : 1);
        if (numBytes > 0) {
            this.output.write(numBytes);
            this.output.write(this.buffer, 0, numBytes);
            this.buffer[0] = 0;
            this.indexIntoOutputStream = 0;
            this.bitsLeft = 8;
        } // ends if

    } // ends flush(void)

    /**
     * Write bits to stream.
     *
     * @param bits    source of bits, low/high order?
     * @param numbits how many bits to write.
     * @throws IOException io execution
     */
    public final void writeBits(int bits, int numbits) throws IOException {
        int numBytes = 255;
        do {
            if ((this.indexIntoOutputStream == 254 && this.bitsLeft == 0) || this.indexIntoOutputStream > 254) {
                this.output.write(numBytes);
                this.output.write(this.buffer, 0, numBytes);

                this.buffer[0] = 0;
                this.indexIntoOutputStream = 0;
                this.bitsLeft = 8;
            } // ends if

            if (numbits <= this.bitsLeft) {
                this.buffer[this.indexIntoOutputStream] |= (bits & ((1 << numbits) - 1)) << (8 - this.bitsLeft);
                this.bitsLeft -= numbits;
                numbits = 0;
            } // ends if

            else {
                this.buffer[this.indexIntoOutputStream] |= (bits & ((1 << this.bitsLeft) - 1)) << (8 - this.bitsLeft);
                bits >>= this.bitsLeft;
                numbits -= this.bitsLeft;
                this.buffer[++this.indexIntoOutputStream] = 0;
                this.bitsLeft = 8;
            } // ends else

        } while (numbits != 0);

    } // ends writeBits(int, int)

} // ends class BitFile

/**
 * Used to compress the image by looking for repeating elements.
 */
class LZWStringTable {
    private static final int RES_CODES = 2;
    private static final short HASH_FREE = (short) 0xFFFF;
    private static final short NEXT_FIRST = (short) 0xFFFF;
    private static final int MAXBITS = 12;
    private static final int MAXSTR = (1 << MAXBITS);
    private static final short HASHSIZE = 9973;
    private static final short HASHSTEP = 2039;

    byte[] strChr;
    short[] strNxt;
    short[] strHsh;
    short numStrings;

    public LZWStringTable() {
        strChr = new byte[MAXSTR];
        strNxt = new short[MAXSTR];
        strHsh = new short[HASHSIZE];
    } // ends constructor LZWStringTable(void)

    public final int addCharString(short index, byte b) {
        int hshidx;
        if (numStrings >= MAXSTR)
            return 0xFFFF;

        hshidx = calculateHash(index, b);
        while (strHsh[hshidx] != HASH_FREE)
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;

        strHsh[hshidx] = numStrings;
        strChr[numStrings] = b;
        strNxt[numStrings] = (index != HASH_FREE) ? index : NEXT_FIRST;

        return numStrings++;
    } // ends addCharString(short, byte)

    public final short findCharString(short index, byte b) {
        int hshidx;
        int nxtidx;

        if (index == HASH_FREE)
            return b;

        hshidx = calculateHash(index, b);
        while ((nxtidx = strHsh[hshidx]) != HASH_FREE) {
            if (strNxt[nxtidx] == index && strChr[nxtidx] == b)
                return (short) nxtidx;
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        } // ends while

        return (short) 0xFFFF;
    } // ends findCharString(short, byte)

    public final void clearTable(int codesize) {
        numStrings = 0;

        for (int q = 0; q < HASHSIZE; q++)
            strHsh[q] = HASH_FREE;

        int w = (1 << codesize) + RES_CODES;
        for (int q = 0; q < w; q++)
            this.addCharString((short) 0xFFFF, (byte) q);
    } // ends clearTable(int)

    public static int calculateHash(short index, byte lastbyte) {
        return (((short) (lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
    }

} // ends class LZWStringTable

/**
 * Used to compress the image by looking for repeated elements.
 */
class LZWCompressor {

    private void lzwCompress() {
        throw new IllegalStateException("Utility class");
    }

    public static void lzwCompress(OutputStream output, int codesize, byte[] toCompress) throws IOException {
        byte c;
        short localIndex;
        int localClearcode;
        int localEndofInfo;
        int localNumbits;
        int localLimit;

        short prefix = (short) 0xFFFF;

        BitFile bitFile = new BitFile(output);
        LZWStringTable strings = new LZWStringTable();

        localClearcode = 1 << codesize;
        localEndofInfo = localClearcode + 1;

        localNumbits = codesize + 1;
        localLimit = (1 << localNumbits) - 1;

        strings.clearTable(codesize);
        bitFile.writeBits(localClearcode, localNumbits);

        for (byte compress : toCompress) {
            c = compress;
            if ((localIndex = strings.findCharString(prefix, c)) != -1)
                prefix = localIndex;
            else {
                bitFile.writeBits(prefix, localNumbits);
                if (strings.addCharString(prefix, c) > localLimit) {
                    if (++localNumbits > 12) {
                        bitFile.writeBits(localClearcode, localNumbits - 1);
                        strings.clearTable(codesize);
                        localNumbits = codesize + 1;
                    } // ends if

                    localLimit = (1 << localNumbits) - 1;
                } // ends if

                prefix = (short) (c & 0xFF);
            } // ends else

        } // ends for

        if (prefix != -1)
            bitFile.writeBits(prefix, localNumbits);

        bitFile.writeBits(localEndofInfo, localNumbits);
        bitFile.flush();
    } // ends lzwCompress(OutputStream, int, byte[])

} // ends class LWZCompressor

/**
 *
 */
class ScreenDescriptor {
    private final short localScreenWidth;
    private final short localScreenHeight;
    private byte currentByte;
    private final byte backgroundColorIndex;
    private final byte pixelAspectRatio;

    /**
     * tool for dumping current screen image??
     *
     * @param width     width
     * @param height    height
     * @param numColors number of colors
     */
    public ScreenDescriptor(short width, short height, int numColors) {
        this.localScreenWidth = width;
        this.localScreenHeight = height;
        setGlobalColorTableSize((byte) (BitUtils.bitsNeeded(numColors) - 1));
        setGlobalColorTableFlag((byte) 1);
        setSortFlag((byte) 0);
        setColorResolution((byte) 7);
        this.backgroundColorIndex = 0;
        this.pixelAspectRatio = 0;
    } // ends constructor ScreenDescriptor(short, short, int)

    public final void write(OutputStream output) throws IOException {
        BitUtils.writeWord(output, this.localScreenWidth);
        BitUtils.writeWord(output, this.localScreenHeight);
        output.write(this.currentByte);
        output.write(this.backgroundColorIndex);
        output.write(this.pixelAspectRatio);
    } // ends write(OutputStream)

    public final void setGlobalColorTableSize(byte num) {
        this.currentByte |= (num & 7);
    }

    public final void setSortFlag(byte num) {
        this.currentByte |= (num & 1) << 3;
    }

    public final void setColorResolution(byte num) {
        this.currentByte |= (num & 7) << 4;
    }

    public final void setGlobalColorTableFlag(byte num) {
        this.currentByte |= (num & 1) << 7;
    }

} // ends class ScreenDescriptor

/**
 *
 */
class ImageDescriptor {
    private final byte globalSeparator;
    private final short leftPosition;
    private final short topPosition;
    private final short imageWidth;
    private final short imageHeight;
    private byte currentByte;
    /**
     * ???
     *
     * @param width     width
     * @param height    height
     * @param separator separator
     */
    public ImageDescriptor(short width, short height, char separator) {
        globalSeparator = (byte) separator;
        this.leftPosition = 0;
        this.topPosition = 0;
        this.imageWidth = width;
        this.imageHeight = height;
        setLocalColorTableSize((byte) 0);
        setReserved((byte) 0);
        setSortFlag((byte) 0);
        setInterlaceFlag((byte) 0);
        setLocalColorTableFlag((byte) 0);
    } // ends constructor ImageDescriptor(short, short, char)

    public final void write(OutputStream output) throws IOException {
        output.write(globalSeparator);
        BitUtils.writeWord(output, this.leftPosition);
        BitUtils.writeWord(output, this.topPosition);
        BitUtils.writeWord(output, this.imageWidth);
        BitUtils.writeWord(output, this.imageHeight);
        output.write(this.currentByte);
    } // ends write(OutputStream)

    public final void setLocalColorTableSize(byte num) {
        this.currentByte |= (num & 7);
    }

    public final void setReserved(byte num) {
        this.currentByte |= (num & 3) << 3;
    }

    public final void setSortFlag(byte num) {
        this.currentByte |= (num & 1) << 5;
    }

    public final void setInterlaceFlag(byte num) {
        this.currentByte |= (num & 1) << 6;
    }

    public final void setLocalColorTableFlag(byte num) {
        this.currentByte |= (num & 1) << 7;
    }

} // ends class ImageDescriptor

class BitUtils {
    /**
     * Bits needed no represent the number n???
     */

    private BitUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static byte bitsNeeded(int n) {
        byte ret = 1;

        if (n-- == 0)
            return 0;

        while ((n >>= 1) != 0)
            ret++;

        return ret;
    } // ends bitsNeeded(int)

    public static void writeWord(OutputStream output, short w) throws IOException {
        output.write(w & 0xFF);
        output.write((w >> 8) & 0xFF);
    } // ends writeWord(OutputStream, short)

    static void writeString(OutputStream output) throws IOException {
        for (int loop = 0; loop < "GIF87a".length(); loop++)
            output.write((byte) ("GIF87a".charAt(loop)));
    } // ends writeString(OutputStream, String)

} // ends class BitUtils
