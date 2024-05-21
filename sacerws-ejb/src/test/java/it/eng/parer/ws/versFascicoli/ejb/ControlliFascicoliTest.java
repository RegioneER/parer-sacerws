/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versFascicoli.ejb;

import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versFascicoli.dto.DatiXmlProfiloArchivistico;
import it.eng.parer.ws.versFascicoli.dto.DatiXmlProfiloGenerale;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static it.eng.ArquillianTestUtils.*;

@RunWith(Arquillian.class)
public class ControlliFascicoliTest {

    @Deployment
    public static Archive<?> createTestArchive_queryIsOk() {
        return createEnterpriseArchive(ControlliFascicoliTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList("it.eng.parer.ws.versFascicoli.dto", "com.fasterxml.uuid.impl"),
                        ControlliFascicoliTest.class, ControlliFascicoli.class),
                createSacerLogJavaArchive());
    }

    @EJB
    private ControlliFascicoli ejb;

    @Test
    public void verificaPartizioniStruttAnnoFascicoli_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.verificaPartizioniStruttAnnoFascicoli("descKey", 0L, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoFascicolo_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkTipoFascicolo("nomeTipoFascicolo", "descKey", 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoFascicoloIamUserOrganizzazione_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkTipoFascicoloIamUserOrganizzazione("descKey", "tipoFasc",
                0L, 0L, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoFascicoloAnno_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkTipoFascicoloAnno("descChiaviFasc", "tipoFasc", 0, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoFascicoloSconosciuto_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkTipoFascicoloSconosciuto(0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkChiave_queryIsOk() {
        final CSChiaveFasc key = new CSChiaveFasc();
        key.setNumero("1");
        key.setAnno(0);
        for (ControlliFascicoli.TipiGestioneFascAnnullati tgfa : ControlliFascicoli.TipiGestioneFascAnnullati
                .values()) {
            final RispostaControlli rispostaControlli = ejb.checkChiave(key, "descKey", 0L, tgfa);
            assertNoErr(rispostaControlli);
        }
    }

    private VersFascicoloExt mockVersFascicoloExt() {
        final VersFascicoloExt versamento = new VersFascicoloExt();
        versamento.setStrutturaComponenti(new StrutturaVersFascicolo());
        versamento.getStrutturaComponenti().setIdStruttura(0L);
        return versamento;
    }

    @Test
    public void verificaSIPTitolario_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.verificaSIPTitolario(mockStrutturaVersFascicolo());
        assertNoErr(rispostaControlli);
    }

    private StrutturaVersFascicolo mockStrutturaVersFascicolo() {
        final StrutturaVersFascicolo svf = new StrutturaVersFascicolo();
        svf.setIdStruttura(0l);
        svf.setDatiXmlProfiloGenerale(new DatiXmlProfiloGenerale());
        svf.getDatiXmlProfiloGenerale().setDataApertura(new Date());
        svf.setDatiXmlProfiloArchivistico(new DatiXmlProfiloArchivistico());
        svf.getDatiXmlProfiloArchivistico().setIndiceClassificazione("indice");
        svf.getDatiXmlProfiloArchivistico().setVociClassificazione(new ArrayList<>());
        return svf;
    }

    @Test
    public void getDecAaTipoFascicolo_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.getDecAaTipoFascicolo(0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkDecVoceTitolWithComp_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkDecVoceTitolWithComp("test", 0l, new Date());
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkDecVoceTitolWithCompAndVoce_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkDecVoceTitolWithCompAndVoce("test", "test", 0L,
                new Date());
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkCdVoceDescDecValVoceTitol_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkCdVoceDescDecValVoceTitol("test", "test", "test", 0L,
                new Date());
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkCdVoceDescDecValVoceTitol2_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkCdVoceDescDecValVoceTitol("test", "test", new Date(), 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkCdVoceDescDecValVoceTitol3_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.checkCdVoceDescDecValVoceTitol("test", "test", "test",
                new Date(), 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void getDecLvlVoceTitolWithNiLivello_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.getDecLvlVoceTitolWithNiLivello(0L, 0l);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void getDecLvlVoceTitolWithNiLivello2_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.getDecLvlVoceTitolWithNiLivello(0L, 0l, new Date());
        assertNoErr(rispostaControlli);
    }

    @Test
    public void getDecTitolStrutt_queryIsOk() {
        final RispostaControlli rispostaControlli = ejb.getDecTitolStrutt(0L, new Date());
        assertNoErr(rispostaControlli);
    }
}
