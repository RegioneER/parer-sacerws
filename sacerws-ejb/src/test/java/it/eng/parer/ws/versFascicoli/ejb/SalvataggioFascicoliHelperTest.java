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

import java.util.Collections;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.utils.MessaggiWSCache;
import it.eng.parer.ws.versFascicoli.dto.ConfigNumFasc;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.xml.versfascicolo.ChiaveType;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicolo.IntestazioneType;

@ArquillianTest
public class SalvataggioFascicoliHelperTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(Collections.emptyList(),
                SalvataggioFascicoliHelper.class, SalvataggioFascicoliHelperTest.class,
                XmlFascCache.class, AppServerInstance.class, LogSessioneFascicoliHelper.class,
                LogSessioneFascicoliHelperTest.class, XmlFascCache.class, ControlliSemantici.class,
                MessaggiWSCache.class);
        sacerWSJavaArchive.addPackages(true, "org.apache.commons.io",
                "it.eng.parer.ws.xml.versfascicolo", "it.eng.parer.ws.versFascicoli.dto");
        return createEnterpriseArchive("SalvataggioFascicoliHelperTest", sacerWSJavaArchive,
                createSacerLogJavaArchive());
    }

    @EJB
    private SalvataggioFascicoliHelper helper;

    @Test
    void salvaWarningAATipoFascicolo_queryIsOk() {
        VersFascicoloExt versamento = mockVersFascicoloExt();
        final RispostaControlli rispostaControlli = helper.salvaWarningAATipoFascicolo(versamento);
        assertTrue(rispostaControlli.isrBoolean());
    }

    private VersFascicoloExt mockVersFascicoloExt() {
        VersFascicoloExt versamento = new VersFascicoloExt();
        versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
        versamento.getStrutturaComponenti().setConfigNumFasc(new ConfigNumFasc(230L));
        versamento.setVersamento(new IndiceSIPFascicolo());
        versamento.getVersamento().setIntestazione(new IntestazioneType());
        versamento.getVersamento().getIntestazione().setChiave(new ChiaveType());
        versamento.getVersamento().getIntestazione().getChiave().setAnno(2018);
        return versamento;
    }
}
