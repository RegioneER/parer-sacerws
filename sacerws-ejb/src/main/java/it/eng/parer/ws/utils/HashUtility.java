/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fioravanti_f
 */
public class HashUtility {

    private static final Logger logger = LoggerFactory.getLogger(HashUtility.class);

    public static Hashresult calculate(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest mdMd5 = MessageDigest.getInstance(TipiHash.MD5.descrivi());
        MessageDigest mdSha1 = MessageDigest.getInstance(TipiHash.SHA_1.descrivi());
        MessageDigest mdSha224 = MessageDigest.getInstance(TipiHash.SHA_224.descrivi());
        MessageDigest mdSha256 = MessageDigest.getInstance(TipiHash.SHA_256.descrivi());
        MessageDigest mdSha384 = MessageDigest.getInstance(TipiHash.SHA_384.descrivi());
        MessageDigest mdSha512 = MessageDigest.getInstance(TipiHash.SHA_512.descrivi());

        Hashresult hashresult = new Hashresult();
        int letti;
        int BUFFER_SIZE = 10 * 1024 * 1024;

        try {
            logger.debug("Provider md5" + mdMd5.getProvider());
            logger.debug("Provider sha1" + mdSha1.getProvider());
            logger.debug("Provider sha224" + mdSha224.getProvider());
            logger.debug("Provider sha256" + mdSha256.getProvider());
            logger.debug("Provider sha384" + mdSha384.getProvider());
            logger.debug("Provider sha512" + mdSha512.getProvider());
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((letti = is.read(buffer)) != -1) {
                mdMd5.update(buffer, 0, letti);
                mdSha1.update(buffer, 0, letti);
                mdSha224.update(buffer, 0, letti);
                mdSha256.update(buffer, 0, letti);
                mdSha384.update(buffer, 0, letti);
                mdSha512.update(buffer, 0, letti);
                logger.trace("Letti " + letti + " bytes");
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        hashresult.setHashMd5(mdMd5.digest());
        hashresult.setHashSha1(mdSha1.digest());
        hashresult.setHashSha224(mdSha224.digest());
        hashresult.setHashSha256(mdSha256.digest());
        hashresult.setHashSha384(mdSha384.digest());
        hashresult.setHashSha512(mdSha512.digest());
        return hashresult;
    }
}
