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

package it.eng.parer.ws.versamentoUpd.ejb;

import static it.eng.ArquillianTestUtils.assertNoErr;
import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.xml.versUpdReq.AggiornamentoProfiloArchivisticoType;
import it.eng.parer.ws.xml.versUpdReq.AggiornamentoUnitaDocumentariaType;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;

@ArquillianTest
public class ControlliUpdVersamentoTest {

    @Deployment
    public static Archive<?> createTestArchive() {
	return createEnterpriseArchive(ControlliUpdVersamentoTest.class.getSimpleName(),
		createSacerWSJavaArchive(
			Arrays.asList("it.eng.parer.ws.versamentoUpd.ext",
				"it.eng.parer.ws.xml.versUpdReq"),
			ControlliUpdVersamentoTest.class, ControlliUpdVersamento.class,
			ControlliSemantici.class),
		createSacerLogJavaArchive());
    }

    @EJB
    private ControlliUpdVersamento controlliUpdVersamento;

    @Test
    public void checkUltimoSIPWithHashBinary() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkUltimoSIPWithHashBinary("descKey", 0l, "sipxml");
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoUDRegIamUserOrganizzazione() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkTipoUDRegIamUserOrganizzazione(mockCsChiave(),
			"descTipologiaUnitaDocumentaria", 0l, 0l, 0l, 0l);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void getUpdFlagsFromTipoUDOrgStrut() {
	RispostaControlli rispostaControlli = controlliUpdVersamento
		.getUpdFlagsFromTipoUDOrgStrut(0l, false);
	assertNoErr(rispostaControlli);
	rispostaControlli = controlliUpdVersamento.getUpdFlagsFromTipoUDOrgStrut(0l, true);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkIdDocumentoInUD() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento.checkIdDocumentoInUD(0l,
		"idDoc", "descChiaveDoc");
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoDocRegIamUserOrganizzazione() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkTipoDocRegIamUserOrganizzazione("tipoDoc", 0l, 0l, 0l);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkIdComponenteInDoc() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkIdComponenteInDoc(0l, 0, "descChiaveComp");
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkAroStrutDocAndTipo() {
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkAroStrutDocAndTipo(0L);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkFascicoliSecondari() {
	final UpdVersamentoExt versamento = new UpdVersamentoExt();
	versamento.setStrutturaUpdVers(new StrutturaUpdVers());
	versamento.getStrutturaUpdVers().setIdStruttura(0L);
	versamento.getStrutturaUpdVers().setIdUd(0L);
	versamento.setVersamento(new IndiceSIPAggiornamentoUnitaDocumentaria());
	versamento.getVersamento().setUnitaDocumentaria(new AggiornamentoUnitaDocumentariaType());
	versamento.getVersamento().getUnitaDocumentaria()
		.setProfiloArchivistico(new AggiornamentoProfiloArchivisticoType());
	versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
		.setFascicoliSecondari(
			new AggiornamentoProfiloArchivisticoType.FascicoliSecondari());
	versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
		.getFascicoliSecondari();
	final RispostaControlli rispostaControlli = controlliUpdVersamento
		.checkFascicoliSecondari(versamento);
	assertNoErr(rispostaControlli);
    }

    @Test
    public void checkChiaveAndTipoDocPrinc() {
	for (ControlliSemantici.TipiGestioneUDAnnullate tipiGestioneUDAnnullate : ControlliSemantici.TipiGestioneUDAnnullate
		.values()) {
	    final RispostaControlli rispostaControlli = controlliUpdVersamento
		    .checkChiaveAndTipoDocPrinc(mockCsChiave(), 0l, tipiGestioneUDAnnullate);
	    assertNoErr(rispostaControlli);

	}
    }

    private CSChiave mockCsChiave() {
	final CSChiave key = new CSChiave();
	key.setNumero("2021");
	key.setTipoRegistro("tipoRegistro");
	key.setAnno(2021L);
	return key;
    }
}
