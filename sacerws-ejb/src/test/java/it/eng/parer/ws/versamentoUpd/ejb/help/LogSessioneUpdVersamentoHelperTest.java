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

package it.eng.parer.ws.versamentoUpd.ejb.help;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.entity.DecRegistroUnitaDoc;
import it.eng.parer.entity.DecTipoDoc;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.MonKeyTotalUdKo;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsSesUpdUnitaDocKo;
import it.eng.parer.entity.VrsUpdUnitaDocKo;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.ejb.LogSessioneSyncTest;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;

@ArquillianTest
public class LogSessioneUpdVersamentoHelperTest {
    @Test
    public void itWorks() {
        assertTrue(true);
    }

    @EJB
    private LogSessioneUpdVersamentoHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(LogSessioneUpdVersamentoHelperTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList("it.eng.parer.ws.versamentoUpd.ext",
                        "it.eng.parer.ws.xml.versUpdReq", "it.eng.parer.ws.xml.versUpdResp"),
                        LogSessioneUpdVersamentoHelperTest.class,
                        LogSessioneUpdVersamentoHelper.class,
                        it.eng.parer.ws.ejb.XmlUpdVersCache.class,
                        it.eng.parer.util.ejb.AppServerInstance.class,
                        it.eng.parer.ws.ejb.ControlliSemantici.class,
                        it.eng.parer.util.ejb.help.ConfigurationHelper.class,
                        it.eng.parerxml.xsd.FileXSD.class, it.eng.parerxml.xsd.FileXSDUtil.class,
                        it.eng.parer.util.DateUtilsConverter.class)
                        .addAsResource(
                                LogSessioneSyncTest.class.getClassLoader()
                                        .getResource("WSRequestAggiornamentoVersamento_1.5.xsd"),
                                "/it/eng/parer/ws/xml/versAggiornamentoReq/WSRequestAggiornamentoVersamento_1.5..xsd")
                        .addAsResource(
                                LogSessioneSyncTest.class.getClassLoader()
                                        .getResource("WSResponseAggiornamentoVersamento_1.5..xsd"),
                                "it/eng/parer/ws/xml/versAggiornamentoResp/WSResponseAggiornamentoVersamento_1.5.xsd"),
                createSacerLogJavaArchive());
    }

    private UpdVersamentoExt mockUpdVersamentoExt() {
        final UpdVersamentoExt versamento = new UpdVersamentoExt();
        versamento.setStrutturaUpdVers(new StrutturaUpdVers());
        versamento.getStrutturaUpdVers().setIdStruttura(0L);
        versamento.getStrutturaUpdVers().setIdUd(0L);
        versamento.getStrutturaUpdVers().setChiaveNonVerificata(new CSChiave());
        versamento.getStrutturaUpdVers().getChiaveNonVerificata().setAnno(-2020L);
        versamento.getStrutturaUpdVers().getChiaveNonVerificata().setNumero("11051985");
        versamento.getStrutturaUpdVers().getChiaveNonVerificata().setTipoRegistro("NON_ESISTE");
        return versamento;
    }

    @Test
    public void recuperoUpdUnitaDocOk_queryIsOk() {
        final SyncFakeSessn sessione = new SyncFakeSessn();
        sessione.setTmApertura(ZonedDateTime.now());
        final RispostaControlli rispostaControlli = helper
                .recuperoUpdUnitaDocOk(mockUpdVersamentoExt(), sessione);
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    public void cercaAggiornamentoKo_queryIsOk() {
        final RispostaControlli rispostaControlli = helper
                .cercaAggiornamentoKo(mockUpdVersamentoExt());
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    public void lockAndGetMonContaSesUpdUdKo_queryIsOk() {
        final RispostaControlli rispostaControlli = helper
                .lockAndGetMonContaSesUpdUdKo(mockVrsUpdUnitaDocKo());
        assertTrue(rispostaControlli.isrBoolean());
    }

    private VrsUpdUnitaDocKo mockVrsUpdUnitaDocKo() {
        final VrsUpdUnitaDocKo updUnitaDocKo = new VrsUpdUnitaDocKo();
        updUnitaDocKo.setOrgStrut(new OrgStrut());
        updUnitaDocKo.getOrgStrut().setIdStrut(0L);
        updUnitaDocKo.setAaKeyUnitaDoc(BigDecimal.ZERO);
        updUnitaDocKo.setDecRegistroUnitaDocLast(new DecRegistroUnitaDoc());
        updUnitaDocKo.getDecRegistroUnitaDocLast().setIdRegistroUnitaDoc(0L);
        updUnitaDocKo.setDecTipoDocPrincLast(new DecTipoDoc());
        updUnitaDocKo.getDecTipoDocPrincLast().setIdTipoDoc(0L);
        updUnitaDocKo.setDecTipoUnitaDocLast(new DecTipoUnitaDoc());
        updUnitaDocKo.getDecTipoUnitaDocLast().setIdTipoUnitaDoc(0L);
        return updUnitaDocKo;
    }

    @Test
    public void aggiornaConteggioMonContaSesUpdUdKo_queryIsOk() {
        final VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo = new VrsSesUpdUnitaDocKo();
        vrsSesUpdUnitaDocKo.setTsIniSes(new Date());
        final MonKeyTotalUdKo monKeyTotalUdKo = new MonKeyTotalUdKo();
        monKeyTotalUdKo.setIdKeyTotalUdKo(0L);
        for (it.eng.parer.entity.constraint.VrsSesUpdUnitaDocKo.TiStatoSesUpdKo tiStatoSesUpdKo : it.eng.parer.entity.constraint.VrsSesUpdUnitaDocKo.TiStatoSesUpdKo
                .values()) {
            vrsSesUpdUnitaDocKo.setTiStatoSesUpdKo(tiStatoSesUpdKo);
            RispostaControlli rispostaControlli = helper.aggiornaConteggioMonContaSesUpdUdKo(
                    vrsSesUpdUnitaDocKo, monKeyTotalUdKo, false);
            assertTrue(rispostaControlli.isrBoolean());

        }
    }

    @Test
    public void verificaDataAttivazioneJob_queryIsOk() {
        final RispostaControlli rispostaControlli = helper.verificaDataAttivazioneJob();
        assertTrue(rispostaControlli.isrBoolean());
    }

}
