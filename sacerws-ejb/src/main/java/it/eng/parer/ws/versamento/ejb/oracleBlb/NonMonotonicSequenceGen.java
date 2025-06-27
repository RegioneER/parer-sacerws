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
package it.eng.parer.ws.versamento.ejb.oracleBlb;

import java.security.SecureRandom;

/**
 *
 * @author Fioravanti_F
 */
public class NonMonotonicSequenceGen implements ISequenceGen {

    private static SecureRandom rand = new SecureRandom();

    private static final int MAX = 9999;
    private static final int MIN = 1000;

    @Override
    public long newSequenceNum(long baseSequence) {
	long newval = Long.parseLong(randInt() + "" + baseSequence);
	return newval;
    }

    /**
     * Returns a pseudo-random number between MIN and MAX, inclusive. The difference between min and
     * max can be at most <code>Integer.MAX_VALUE - 1</code>.
     *
     * @return Integer between min and max, inclusive.
     *
     * @see java.util.Random#nextInt(int)
     */
    private static int randInt() {
	// nextInt is normally exclusive of the top value,
	// so add 1 to make it inclusive
	return rand.nextInt((MAX - MIN) + 1) + MIN;

    }

}
