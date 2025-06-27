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

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import it.eng.parer.entity.DecTipoFascicolo;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsFascicoloKo;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;

@ArquillianTest
public class LogSessioneFascicoliHelperTest {
    @EJB
    private LogSessioneFascicoliHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
	JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(Collections.emptyList(),
		LogSessioneFascicoliHelper.class, LogSessioneFascicoliHelperTest.class,
		XmlFascCache.class, AppServerInstance.class,
		it.eng.parer.ws.ejb.ControlliSemantici.class);
	sacerWSJavaArchive.addPackages(true, "org.apache.commons.io",
		"it.eng.parer.ws.xml.versfascicolo", "it.eng.parer.ws.versFascicoli.dto");
	return createEnterpriseArchive("LogSessioneFascicoliHelperTest", sacerWSJavaArchive,
		createSacerLogJavaArchive());
    }

    @Test
    void verificaPartizioneFascicoloErr_queryIsOk() {
	final RispostaControlli rispostaControlli = helper.verificaPartizioneFascicoloErr();
	assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    void verificaPartizioneFascicoloByAaStrutKo_queryIsOk() {
	final RispostaControlli rispostaControlli = helper
		.verificaPartizioneFascicoloByAaStrutKo(getVersFascicoloExt());
	assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    void cercaFascicoloKo_queryIsOk() {
	final RispostaControlli rispostaControlli = helper.cercaFascicoloKo(getVersFascicoloExt());
	assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    void aggiornaConteggioMonContaFasKo_queryIsOk() {
	VrsFascicoloKo vrsFascicoloKo = new VrsFascicoloKo();
	vrsFascicoloKo.setOrgStrut(new OrgStrut());
	vrsFascicoloKo.getOrgStrut().setIdStrut(0L);
	vrsFascicoloKo.setTsIniLastSes(new Date());
	vrsFascicoloKo.setAaFascicolo(BigDecimal.valueOf(2020));
	vrsFascicoloKo.setTiStatoFascicoloKo("KO");
	vrsFascicoloKo.setDecTipoFascicolo(new DecTipoFascicolo());
	vrsFascicoloKo.getDecTipoFascicolo().setIdTipoFascicolo(0L);
	helper.aggiornaConteggioMonContaFasKo(vrsFascicoloKo);
	assertTrue(true);
    }

    private VersFascicoloExt getVersFascicoloExt() {
	StrutturaVersFascicolo strutturaComponenti = new StrutturaVersFascicolo();
	strutturaComponenti.setIdStruttura(0L);
	CSChiaveFasc chiaveNonVerificata = new CSChiaveFasc();
	chiaveNonVerificata.setAnno(2020);
	chiaveNonVerificata.setNumero("999999");
	strutturaComponenti.setChiaveNonVerificata(chiaveNonVerificata);
	VersFascicoloExt versamento = new VersFascicoloExt();
	versamento.setStrutturaComponenti(strutturaComponenti);
	return versamento;
    }
}
