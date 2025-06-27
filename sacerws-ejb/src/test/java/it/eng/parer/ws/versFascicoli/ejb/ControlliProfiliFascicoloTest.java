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

package it.eng.parer.ws.versFascicoli.ejb;

import static it.eng.ArquillianTestUtils.assertNoErr;
import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;

import java.util.Arrays;

import javax.ejb.EJB;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.xml.versfascicolo.ConfigType;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicolo.ProfiloArchivisticoType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType;

@ArquillianTest
public class ControlliProfiliFascicoloTest {

    @Deployment
    public static Archive<?> createTestArchive() {
	return createEnterpriseArchive(ControlliProfiliFascicoloTest.class.getSimpleName(),
		createSacerWSJavaArchive(
			Arrays.asList("it.eng.parer.ws.xml.versfascicolo",
				"it.eng.parer.ws.versFascicoli.dto",
				"it.eng.parer.ws.xml.versfascicoloresp"),
			ControlliProfiliFascicoloTest.class, ControlliProfiliFascicolo.class,
			it.eng.parer.ws.ejb.ControlliSemantici.class),
		createSacerLogJavaArchive());
    }

    @EJB
    private ControlliProfiliFascicolo controlliProfiliFascicolo;

    @Test
    public void verificaProfiloArchivistico_queryIsOk() {
	final VersFascicoloExt versamento = new VersFascicoloExt();
	versamento.setVersamento(new IndiceSIPFascicolo());
	versamento.getVersamento().setParametri(new ConfigType());
	versamento.getVersamento().getParametri()
		.setVersioneProfiloArchivisticoFascicolo("versione");
	versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
	versamento.getStrutturaComponenti().setIdTipoFascicolo(0L);
	versamento.getStrutturaComponenti().setChiaveNonVerificata(new CSChiaveFasc());
	versamento.getStrutturaComponenti().getChiaveNonVerificata().setAnno(2021);
	final RispostaControlli rispostaControlli = controlliProfiliFascicolo
		.verificaProfiloArchivistico(versamento);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void verificaProfiloArchivisticoNoVersione_queryIsOk() {
	final VersFascicoloExt versamento = new VersFascicoloExt();
	versamento.setVersamento(new IndiceSIPFascicolo());
	versamento.getVersamento().setProfiloArchivistico(
		new JAXBElement<>(new QName("http://www.test.eng", "userDN"),
			ProfiloArchivisticoType.class, new ProfiloArchivisticoType()));
	versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
	versamento.getStrutturaComponenti().setIdTipoFascicolo(0L);
	versamento.getStrutturaComponenti().setChiaveNonVerificata(new CSChiaveFasc());
	versamento.getStrutturaComponenti().getChiaveNonVerificata().setAnno(2021);
	final RispostaControlli rispostaControlli = controlliProfiliFascicolo
		.verificaProfiloArchivistico(versamento);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void verificaProfiloGenerale_queryIsOk() {
	final VersFascicoloExt versamento = new VersFascicoloExt();
	versamento.setVersamento(new IndiceSIPFascicolo());
	versamento.getVersamento().setProfiloArchivistico(
		new JAXBElement<>(new QName("http://www.test.eng", "userDN"),
			ProfiloArchivisticoType.class, new ProfiloArchivisticoType()));
	versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
	versamento.getStrutturaComponenti().setIdTipoFascicolo(0L);
	versamento.getStrutturaComponenti().setChiaveNonVerificata(new CSChiaveFasc());
	versamento.getStrutturaComponenti().getChiaveNonVerificata().setAnno(2021);
	final RispostaControlli rispostaControlli = controlliProfiliFascicolo
		.verificaProfiloGenerale(versamento, new ECFascicoloType());
	assertNoErr(rispostaControlli);
    }

    @Test
    public void verificaProfiloGeneraleProfiloNull_queryIsOk() {
	final VersFascicoloExt versamento = new VersFascicoloExt();
	versamento.setVersamento(new IndiceSIPFascicolo());
	versamento.getVersamento().setProfiloArchivistico(null);
	versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
	versamento.getStrutturaComponenti().setIdTipoFascicolo(0L);
	versamento.getStrutturaComponenti().setChiaveNonVerificata(new CSChiaveFasc());
	versamento.getStrutturaComponenti().getChiaveNonVerificata().setAnno(2021);
	final RispostaControlli rispostaControlli = controlliProfiliFascicolo
		.verificaProfiloGenerale(versamento, new ECFascicoloType());
	assertNoErr(rispostaControlli);
    }
}
