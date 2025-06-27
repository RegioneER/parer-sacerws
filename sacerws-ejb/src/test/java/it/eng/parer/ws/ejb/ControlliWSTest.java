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

package it.eng.parer.ws.ejb;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import it.eng.parer.idpjaas.logutils.LogDto;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.spagoLite.FrameElement;
import it.eng.spagoLite.security.User;
import it.eng.spagoLite.security.auth.PwdUtil;
import it.eng.spagoLite.security.auth.WSLoginHandler;
import it.eng.spagoLite.security.menu.MenuEntry;
import it.eng.spagoLite.security.menu.impl.Menu;

@ArquillianTest
public class ControlliWSTest {

    @Deployment
    public static Archive<?> createTestArchive() {
	final JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(
		Arrays.asList("it.eng.spagoLite.security.exception", "it.eng.spagoLite.security",
			"org.apache.commons.codec.binary", "org.apache.commons.codec",
			"it.eng.spagoCore.error"),
		ControlliWS.class, ControlliWSTest.class, WsIdpLogger.class,
		ControlliSemantici.class, ConfigurationHelper.class, AppServerInstance.class,
		LogDto.class, Menu.class, WSLoginHandler.class, PwdUtil.class, MenuEntry.class,
		FrameElement.class, it.eng.parer.idpjaas.logutils.IdpConfigLog.class,
		it.eng.spagoCore.util.JpaUtils.class, it.eng.parer.idpjaas.logutils.IdpLogger.class,
		it.eng.parer.idpjaas.queryutils.NamedStatement.class);
	sacerWSJavaArchive.addPackages(true, "it.eng.spagoLite");
	return createEnterpriseArchive(ControlliWSTest.class.getSimpleName(), sacerWSJavaArchive,
		createSacerLogJavaArchive());
    }

    @EJB
    private ControlliWS controlliWS;

    @Test
    public void checkVersione() {
	for (final Costanti.TipiWSPerControlli tipoWS : Costanti.TipiWSPerControlli.values()) {
	    final HashMap<String, String> xmlDefaults = new HashMap<>();
	    final String versioniWsKey = "TEST";
	    xmlDefaults.put(VerificaVersione.elabWsKey(versioniWsKey), "1.4");
	    final RispostaControlli rispostaControlli = controlliWS.checkVersione("1.4",
		    versioniWsKey, xmlDefaults, tipoWS);
	    assertTrue(rispostaControlli.isrBoolean());
	}
    }

    @Test
    public void checkCredenziali() {
	for (final Costanti.TipiWSPerControlli tipoWS : Costanti.TipiWSPerControlli.values()) {
	    final RispostaControlli rispostaControlli = controlliWS
		    .checkCredenziali("admin_generale", "password", "127.0.0.1", tipoWS);
	    assertTrue(rispostaControlli.isrBoolean());

	}
    }

    @Test
    public void checkUtente() {
	final RispostaControlli rispostaControlli = controlliWS.checkUtente("admin_generale");
	assertTrue(rispostaControlli.isrBoolean());
    }

    private User mockUser() {
	final User utente = new User();
	utente.setIdUtente(0L);
	utente.setUsername("admin_generale");
	utente.setIdOrganizzazioneFoglia(BigDecimal.ZERO);
	utente.setIdApplicazione(0L);
	return utente;
    }

    @Test
    public void checkAuthWSNoOrg() {
	for (final Costanti.TipiWSPerControlli tipoWS : Costanti.TipiWSPerControlli.values()) {
	    final RispostaControlli rispostaControlli = controlliWS.checkAuthWSNoOrg(mockUser(),
		    getIwsDesc(), tipoWS);
	    assertTrue(rispostaControlli.isrBoolean());
	}
    }

    @Test
    public void loadWsVersions() {
	final RispostaControlli rispostaControlli = controlliWS.loadWsVersions(getIwsDesc());
	assertTrue(rispostaControlli.isrBoolean());
    }

    private IWSDesc getIwsDesc() {
	return new IWSDesc() {
	    @Override
	    public String getNomeWs() {
		return "AggiuntaAllegatiSync";
	    }

	    @Override
	    public String getVersione() {
		return "1.4";
	    }
	};
    }
}
