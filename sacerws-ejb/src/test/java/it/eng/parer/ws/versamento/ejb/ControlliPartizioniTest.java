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

package it.eng.parer.ws.versamento.ejb;

import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.xml.versReq.ChiaveType;
import it.eng.parer.ws.xml.versReq.IntestazioneType;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import it.eng.parer.ws.xml.versReq.VersatoreType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static it.eng.ArquillianTestUtils.*;

@RunWith(Arquillian.class)
public class ControlliPartizioniTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(ControlliPartizioniTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList("it.eng.parer.ws.xml.versReq"), ControlliPartizioniTest.class,
                        ControlliPartizioni.class, it.eng.parer.util.DateUtilsConverter.class),
                createSacerLogJavaArchive());
    }

    @EJB
    private ControlliPartizioni controlliPartizioni;

    @Test
    public void verificaPartizioniBlob_queryIsOk() {
        final CSVersatore versatore = new CSVersatore();
        versatore.setStruttura("struttura");
        final StrutturaVersamento strutV = new StrutturaVersamento();
        strutV.setIdStruttura(0L);
        strutV.setTpiAbilitato(false);
        strutV.setDataVersamento(ZonedDateTime.now());
        for (final CostantiDB.TipoSalvataggioFile tipoSalvataggioFile : CostantiDB.TipoSalvataggioFile.values()) {
            strutV.setTipoSalvataggioFile(tipoSalvataggioFile);
            final RispostaControlli rispostaControlli = controlliPartizioni.verificaPartizioniBlob(strutV, versatore);
            assertNoErr(rispostaControlli);
        }
    }

    @Test
    public void verificaPartizioniSubStrutt_queryIsOk() {
        final VersamentoExt versamento = new VersamentoExt();
        versamento.setStrutturaComponenti(new StrutturaVersamento());
        versamento.getStrutturaComponenti().setIdStruttura(0L);
        versamento.getStrutturaComponenti().setIdSubStruttura(0L);
        versamento.getStrutturaComponenti().setDescSubStruttura("subStruttura");
        versamento.setVersamento(new UnitaDocumentaria());
        versamento.getVersamento().setIntestazione(new IntestazioneType());
        versamento.getVersamento().getIntestazione().setChiave(new ChiaveType());
        versamento.getVersamento().getIntestazione().getChiave().setAnno(2021);
        versamento.getVersamento().getIntestazione().getChiave().setNumero("numero");
        versamento.getVersamento().getIntestazione().getChiave().setTipoRegistro("tipoRegistro");
        versamento.getVersamento().getIntestazione().setVersatore(new VersatoreType());
        versamento.getVersamento().getIntestazione().getVersatore().setStruttura("struttura");
        final RispostaControlli rispostaControlli = controlliPartizioni.verificaPartizioniSubStrutt(versamento);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void verificaPartizioniVers_queryIsOk() {
        final StrutturaVersamento strutV = new StrutturaVersamento();
        strutV.setIdStruttura(0L);
        final RispostaControlli rispostaControlli = controlliPartizioni.verificaPartizioniVers(strutV,
                mockSyncFakeSessn(), "descStrutt");
        assertNoErr(rispostaControlli);
    }

    private SyncFakeSessn mockSyncFakeSessn() {
        final SyncFakeSessn sessione = new SyncFakeSessn();
        sessione.setTmApertura(ZonedDateTime.now());
        return sessione;
    }

    @Test
    public void verificaPartizioniVersStruttNull_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliPartizioni.verificaPartizioniVers(null,
                mockSyncFakeSessn(), "descStrutt");
        assertNoErr(rispostaControlli);
    }
}
