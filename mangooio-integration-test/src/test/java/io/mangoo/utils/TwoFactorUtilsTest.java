package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * 
 * @author svenkubiak, WilliamDunne
 *
 */
public class TwoFactorUtilsTest {
    private static final Base32 base32 = new Base32();
    private static final String ACCOUNT = "MyAccount";
    private static final String SECRET = "MySecureSecret";
    private static final String OT_LINK = "otpauth://totp/MyAccount?secret=" + base32.encodeToString(SECRET.getBytes());
    private static final String LINK = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/" + ACCOUNT + "?secret=" + base32.encodeToString(SECRET.getBytes());

    @Test
    public void testGenerateQRCode() {
        //when
        String qrCode = TwoFactorUtils.generateQRCode(ACCOUNT, SECRET);
        
        //then
        assertThat(qrCode, not(equalTo(nullValue())));
        assertThat(qrCode, equalTo(LINK));
    }
    
    @Test
    public void testGenerateCurrentNumber() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);
        
        //then
        assertThat(number, not(nullValue()));
        assertThat(number.length(), equalTo(6));
    }
    
    @Test
    public void testGenerateCurrentNumberWithMillis() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET, 1454934536166L);

        //then
        assertThat(number, not(nullValue()));
        assertThat(number, equalTo("453852"));
    }
    
    @Test
    public void testValidateCurrentNumber() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);

        //then
        assertThat(TwoFactorUtils.validateCurrentNumber((Integer.valueOf(number)), SECRET), equalTo(true));
    }
    
    @Test
    public void testNumberLength() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);

        //then
        for (int i=0 ; i <= 100000; i++) {
            assertThat(number.length(), equalTo(6));
        }
    }
    
    @Test
    public void testValidQRCode() throws IOException, NotFoundException, ChecksumException, FormatException {
        //given
        String qrCode = TwoFactorUtils.generateQRCode(ACCOUNT, SECRET);
        File file = new File(UUID.randomUUID().toString());
        ImageIO.write(ImageIO.read(new URL(qrCode)), "png", file);

        //then
        assertThat(readQRCode(file), equalTo(OT_LINK));
        assertThat(file.delete(), equalTo(true));
    }
    
    private static String readQRCode(File file) throws NotFoundException, ChecksumException, FormatException, IOException {
        BufferedImage image = null;
        BinaryBitmap bitmap = null;
        Result result = null;

        image = ImageIO.read(file);
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        bitmap = new BinaryBitmap(new HybridBinarizer(source));

        QRCodeReader reader = new QRCodeReader();
        result = reader.decode(bitmap);
        
        return result.getText();
    }
}