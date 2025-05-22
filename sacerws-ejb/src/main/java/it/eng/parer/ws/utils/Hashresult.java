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

/**
 *
 * @author fioravanti_f
 */
public class Hashresult {

    private byte[] hashMd5;
    private byte[] hashSha1;
    private byte[] hashSha224;
    private byte[] hashSha256;
    private byte[] hashSha384;
    private byte[] hashSha512;

    public byte[] getHashMd5() {
	return hashMd5;
    }

    public void setHashMd5(byte[] hashMd5) {
	this.hashMd5 = hashMd5;
    }

    public byte[] getHashSha1() {
	return hashSha1;
    }

    public void setHashSha1(byte[] hashSha1) {
	this.hashSha1 = hashSha1;
    }

    public byte[] getHashSha224() {
	return hashSha224;
    }

    public void setHashSha224(byte[] hashSha224) {
	this.hashSha224 = hashSha224;
    }

    public byte[] getHashSha256() {
	return hashSha256;
    }

    public void setHashSha256(byte[] hashSha256) {
	this.hashSha256 = hashSha256;
    }

    public byte[] getHashSha384() {
	return hashSha384;
    }

    public void setHashSha384(byte[] hashSha384) {
	this.hashSha384 = hashSha384;
    }

    public byte[] getHashSha512() {
	return hashSha512;
    }

    public void setHashSha512(byte[] hashSha512) {
	this.hashSha512 = hashSha512;
    }

}
