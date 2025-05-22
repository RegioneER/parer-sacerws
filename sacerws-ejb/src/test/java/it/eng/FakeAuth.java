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
package it.eng;

import it.eng.spagoLite.security.User;
import it.eng.spagoLite.security.auth.Authenticator;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Iacolucci_M Classe finta creata soltanto per darla come risorsa iniettabile a openejb
 *         perché una classe di spagolite-middle richiede questa risorsa!
 */
@Stateless
@LocalBean
public class FakeAuth extends Authenticator {

    @Override
    protected String getAppName() {
	throw new UnsupportedOperationException("Not supported yet."); // To change body of
								       // generated methods, choose
								       // Tools | Templates.
    }

    @Override
    public User recuperoAutorizzazioni(HttpSession hs) {
	throw new UnsupportedOperationException("Not supported yet."); // To change body of
								       // generated methods, choose
								       // Tools | Templates.
    }

}
