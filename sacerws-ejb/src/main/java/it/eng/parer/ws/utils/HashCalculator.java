/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.ws.utils.CostantiDB.TipiHash;

/**
 *
 * @author Fioravanti_F
 */
public class HashCalculator {

    private static final Logger logger = LoggerFactory.getLogger(HashCalculator.class);
    private byte[] hashCalcolato = null;

    public byte[] getHashCalcolato() {
	return hashCalcolato;
    }

    /**
     * @deprecated Vedi MEV#https://redmine.ente.regione.emr.it/issues/17493
     *
     * @param str valore
     *
     * @return HashCalculator oggetto wrapped con calcolo dell'hash {@link HashCalculator}
     *
     * @throws NoSuchAlgorithmException errore generico
     * @throws IOException              errore generico di tipo IO
     */
    @Deprecated
    public HashCalculator calculateHash(String str) throws NoSuchAlgorithmException, IOException {
	try (InputStream is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name())) {
	    return this.calculateSHA1(is);// base
	}
    }

    public HashCalculator calculateHashSHAX(String str, TipiHash tipiHash)
	    throws NoSuchAlgorithmException, IOException {
	try (InputStream is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name())) {
	    return this.calculateSHAX(is, tipiHash);
	}
    }

    /**
     * @deprecated Vedi MEV#https://redmine.ente.regione.emr.it/issues/17493
     *
     * @param buf file buffer
     *
     * @return HashCalculator oggetto wrapped con calcolo dell'hash {@link HashCalculator}
     *
     * @throws NoSuchAlgorithmException errore generico
     * @throws IOException              errore generico di tipo IO
     */
    @Deprecated
    public HashCalculator calculateHash(byte[] buf) throws NoSuchAlgorithmException, IOException {
	try (ByteArrayInputStream bais = new ByteArrayInputStream(buf)) {
	    return this.calculateSHA1(bais);
	}
    }

    public HashCalculator calculateHashSHAX(byte[] buf, TipiHash tipiHash)
	    throws NoSuchAlgorithmException, IOException {
	try (ByteArrayInputStream bais = new ByteArrayInputStream(buf)) {
	    return this.calculateSHAX(bais, tipiHash);
	}
    }

    /**
     * @deprecated Vedi MEV#https://redmine.ente.regione.emr.it/issues/17493
     *
     * @param is valore su inputstream {@link InputStream}
     *
     * @return HashCalculator oggetto wrapped con calcolo dell'hash {@link HashCalculator}
     *
     * @throws NoSuchAlgorithmException errore generico
     * @throws IOException              errore generico di tipo IO
     *
     */
    @Deprecated
    private HashCalculator calculateSHA1(InputStream is)
	    throws NoSuchAlgorithmException, IOException {
	return calculateSHAX(is, TipiHash.SHA_1);
    }

    /**
     * Note: aggiunto metodo "generico" e per non creare impatti negativi è stato lasciato anche il
     * precedente con SHA-1 di base
     *
     * @param is       valore su inputstream {@link InputStream}
     * @param tipiHash tipo hash (vedi enum {@link TipiHash})
     *
     * @return HashCalculator oggetto wrapped con calcolo dell'hash {@link HashCalculator}
     *
     * @throws NoSuchAlgorithmException errore generico
     * @throws IOException              errore generico di tipo IO
     */
    public HashCalculator calculateSHAX(InputStream is, TipiHash tipiHash)
	    throws NoSuchAlgorithmException, IOException {
	MessageDigest md = MessageDigest.getInstance(tipiHash.descrivi());
	int ch;
	int BUFFER_SIZE = 10 * 1024 * 1024;

	try (DigestInputStream dis = new DigestInputStream(is, md)) {
	    logger.debug("Provider {}, {}", md.getProvider(), md.getAlgorithm());

	    byte[] buffer = new byte[BUFFER_SIZE];
	    while ((ch = dis.read(buffer)) != -1) {
		logger.trace("Letti {} bytes", ch);
	    }
	}
	hashCalcolato = md.digest();
	return this;
    }

    public String toHexBinary() {
	return BinEncUtility.encodeUTF8HexString(hashCalcolato);
    }

}
