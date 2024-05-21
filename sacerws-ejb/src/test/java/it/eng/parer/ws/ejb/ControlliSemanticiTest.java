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

package it.eng.parer.ws.ejb;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Date;

import static it.eng.ArquillianTestUtils.*;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ControlliSemanticiTest {

    @Deployment
    public static Archive<?> createTestArchive_queryIsOk() {
        return createEnterpriseArchive(ControlliSemanticiTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList(""), ControlliSemanticiTest.class, ControlliSemantici.class,
                        it.eng.paginator.util.HibernateUtils.class),
                createSacerLogJavaArchive());
    }

    @EJB
    private ControlliSemantici controlliSemantici;

    @Test
    public void caricaDefaultDaDB_string_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.caricaDefaultDaDB("Applicazione");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void caricaDefaultDaDB_list_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici
                .caricaDefaultDaDB(new String[] { "Applicazione", "Paginazione risultati" });
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkIdStrut_queryIsOk() {
        for (Costanti.TipiWSPerControlli aggiornamentoVersamento : Costanti.TipiWSPerControlli.values()) {
            final RispostaControlli rispostaControlli = controlliSemantici.checkIdStrut(mockCsVersatore(),
                    aggiornamentoVersamento, new Date());
            assertNoErr(rispostaControlli);

        }
    }

    private CSVersatore mockCsVersatore() {
        final CSVersatore vers = new CSVersatore();
        vers.setAmbiente("PARER_TEST");
        vers.setEnte("AOSP_BO");
        vers.setStruttura("080-908");
        return vers;
    }

    @Test
    public void checkSistemaMigraz_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkSistemaMigraz("sistemaMigr");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkChiave_queryIsOk() {
        for (ControlliSemantici.TipiGestioneUDAnnullate tipiGestioneUDAnnullate : ControlliSemantici.TipiGestioneUDAnnullate
                .values()) {
            final RispostaControlli rispostaControlli = controlliSemantici.checkChiave(mockCsChiave(), 0l,
                    tipiGestioneUDAnnullate);
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

    @Test
    public void checkTipologiaUD_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkTipologiaUD("tipologiaUd", "descKey", 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoRegistro_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkTipoRegistro("nomeTipoRegistro", "descKey",
                0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoRegistro_descKeyUdColl_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkTipoRegistro("nomeTipoRegistro", "descKey",
                0L, "descKeyUdColl");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoRegistroTipoUD_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkTipoRegistroTipoUD("nomeTipoRegistro",
                "descKey", "tipologiaUd", 0L, 0l);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void caricaRegistroAnno_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.caricaRegistroAnno("nomeTipoRegistro", "descKey",
                2021L, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void caricaRegistroAnno_descKeyUdColl_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.caricaRegistroAnno("nomeTipoRegistro", "descKey",
                2021L, 0L, "descKeyUdColl");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void caricaPartiAARegistro_queryIsOk() {
        controlliSemantici.caricaPartiAARegistro(2021L);
        assertTrue(true);
    }

    @Test
    public void checkTipoDocumento_queryIsOk() {
        RispostaControlli rispostaControlli = controlliSemantici.checkTipoDocumento("nomeTipoDoc", 0L, true,
                "descChiaveDoc");
        assertNoErr(rispostaControlli);
        rispostaControlli = controlliSemantici.checkTipoDocumento("nomeTipoDoc", 0L, false, "descChiaveDoc");
        assertNoErr(rispostaControlli);

    }

    @Test
    public void checkTipoStruttura_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkTipoStruttura("nomeTipoStruttura", 0L,
                "descChiaveDoc");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkTipoComponente_queryIsOk() {
        for (ControlliSemantici.TipologieComponente tipologieComponente : ControlliSemantici.TipologieComponente
                .values()) {
            final RispostaControlli rispostaControlli = controlliSemantici.checkTipoComponente("nomeTipoComponente", 0l,
                    tipologieComponente, "descChiaveComp");
            assertNoErr(rispostaControlli);
        }
    }

    @Test
    public void checkTipoRappComponente_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici
                .checkTipoRappComponente("nomeTipoRapprComponente", 0L, "descChiaveComp");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkPresenzaDatiSpec_queryIsOk() {
        for (CostantiDB.TipiUsoDatiSpec tipiUsoDatiSpec : CostantiDB.TipiUsoDatiSpec.values()) {
            for (CostantiDB.TipiEntitaSacer tipoEntitySacer : CostantiDB.TipiEntitaSacer.values()) {
                final RispostaControlli rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(tipiUsoDatiSpec,
                        tipoEntitySacer, "sistemaMig", 0L, 0L, new Date());
                assertNoErr(rispostaControlli);

            }
        }
    }

    @Test
    public void checkXSDDatiSpec_queryIsOk() {
        for (CostantiDB.TipiUsoDatiSpec tipiUsoDatiSpec : CostantiDB.TipiUsoDatiSpec.values()) {
            for (CostantiDB.TipiEntitaSacer tipoEntitySacer : CostantiDB.TipiEntitaSacer.values()) {
                final RispostaControlli rispostaControlli = controlliSemantici.checkXSDDatiSpec(tipiUsoDatiSpec,
                        tipoEntitySacer, "sistemaMig", 0L, 0L, new Date(), "versioneXsd");
                assertNoErr(rispostaControlli);
            }
        }
    }

    @Test
    public void checkPresenzaVersioneDatiSpec_queryIsOk() {
        for (CostantiDB.TipiUsoDatiSpec tipiUsoDatiSpec : CostantiDB.TipiUsoDatiSpec.values()) {
            for (CostantiDB.TipiEntitaSacer tipoEntitySacer : CostantiDB.TipiEntitaSacer.values()) {
                final RispostaControlli rispostaControlli = controlliSemantici.checkPresenzaVersioneDatiSpec(
                        tipiUsoDatiSpec, tipoEntitySacer, "sistemaMig", 0L, 0L, "versioneXsd");
                assertNoErr(rispostaControlli);
            }
        }
    }

    @Test
    public void checkDatiSpecifici_queryIsOk() {
        for (CostantiDB.TipiUsoDatiSpec tipiUsoDatiSpec : CostantiDB.TipiUsoDatiSpec.values()) {
            for (CostantiDB.TipiEntitaSacer tipoEntitySacer : CostantiDB.TipiEntitaSacer.values()) {
                final RispostaControlliAttSpec rispostaControlli = controlliSemantici
                        .checkDatiSpecifici(tipiUsoDatiSpec, tipoEntitySacer, "sistemaMig", 0L, 0L, 0L);
                assertNoErr(rispostaControlli);

            }
        }
    }

    @Test
    public void checkRiferimentoUD_queryIsOk() {
        for (ControlliSemantici.TipologieComponente tipologieComponente : ControlliSemantici.TipologieComponente
                .values()) {
            final RispostaControlli rispostaControlli = controlliSemantici.checkRiferimentoUD(mockCsChiave(), 0L,
                    tipologieComponente, "descChiaveComp");
            assertNoErr(rispostaControlli);
        }
    }

    @Test
    public void checkFormatoFileVersato_queryIsOk() {
        controlliSemantici.checkFormatoFileVersato("PDF", 0L, 0L);
        assertTrue(true);
    }

    @Test
    public void checkDocumentoInUd_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkDocumentoInUd(0L, "idDocumento", "descUd");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void trovaNuovoProgDocumento_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.trovaNuovoProgDocumento(0L,
                "categoriaDocumento");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void trovaNuovoNiOrdDocumento_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.trovaNuovoNiOrdDocumento(0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkFormatoFileStandard_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkFormatoFileStandard("PDF");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void calcAndcheckCdKeyNormalized_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.calcAndcheckCdKeyNormalized(0L, mockCsChiave(),
                "cdKeyUnitaDocNormaliz");
        assertNoErr(rispostaControlli);
    }

    @Test
    public void checkUniqueCdKeyNormalized_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.checkUniqueCdKeyNormalized(mockCsChiave(), 0l,
                113l, "cdKeyNomarlized", new Date());
        assertNoErr(rispostaControlli);
    }

    @Test
    public void getDtCalcInizioNuoviUrn_queryIsOk() {
        final RispostaControlli rispostaControlli = controlliSemantici.getDtCalcInizioNuoviUrn();
        assertNoErr(rispostaControlli);
    }
}
