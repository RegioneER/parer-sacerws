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
package it.eng.parer.ws.versamentoUpd.ejb.prs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.xml.versUpdReq.AggiornamentoUnitaDocumentariaType;
import it.eng.parer.ws.xml.versUpdReq.ComponenteType;
import it.eng.parer.ws.xml.versUpdReq.DocumentoType;
import it.eng.parer.ws.xml.versUpdReq.SottoComponenteType;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "UpdDocumentiIdCountPrsr")
@LocalBean
public class UpdDocumentiIdCountPrsr extends UpdBasePrsr {

    public boolean eseguiControlli(UpdVersamentoExt versamento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        AggiornamentoUnitaDocumentariaType aggiornamentoUnitaDocumentaria = versamento
                .getVersamento().getUnitaDocumentaria();

        int progressivo = 0;
        long contaAllegati = 0;
        long contaAnnessi = 0;
        long contaAnnotazioni = 0;

        Boolean corrAllegatiDichiarati = null;
        Boolean corrAnnessiDichiarati = null;
        Boolean corrAnnotazioniDichiarati = null;

        HashMap<String, UpdComponenteVers> componentiAttesi = new HashMap<>();
        HashMap<String, UpdComponenteVers> sottoComponentiAttesi = new HashMap<>();
        List<UpdDocumentoVers> documentiAttesi = new ArrayList<>();
        Map<String, UpdDocumentoVers> tmpDocumentiMap = new HashMap<>();
        boolean trovatiIdDocDuplicati = false;

        if (aggiornamentoUnitaDocumentaria.getDocumentoPrincipale() != null) {
            progressivo = 1;
            boolean result = this.aggiungiVerificaDocumento(
                    aggiornamentoUnitaDocumentaria.getDocumentoPrincipale(), progressivo,
                    CategoriaDocumento.Principale, tmpDocumentiMap, documentiAttesi, versamento,
                    componentiAttesi, sottoComponentiAttesi);
            if (result) {
                trovatiIdDocDuplicati = true;
            }
        }
        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAllegati() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria
                    .getAllegati().getAllegato().iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = tmpEnumDoc.next();
                progressivo += 1;
                contaAllegati += 1;
                boolean result = this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
                        CategoriaDocumento.Allegato, tmpDocumentiMap, documentiAttesi, versamento,
                        componentiAttesi, sottoComponentiAttesi);
                if (result) {
                    trovatiIdDocDuplicati = true;
                }
            }
        }

        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAnnessi() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria
                    .getAnnessi().getAnnesso().iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = tmpEnumDoc.next();
                progressivo += 1;
                contaAnnessi += 1;
                boolean result = this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
                        CategoriaDocumento.Annesso, tmpDocumentiMap, documentiAttesi, versamento,
                        componentiAttesi, sottoComponentiAttesi);
                if (result) {
                    trovatiIdDocDuplicati = true;
                }
            }
        }

        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAnnotazioni() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria
                    .getAnnotazioni().getAnnotazione().iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = tmpEnumDoc.next();
                progressivo += 1;
                contaAnnotazioni += 1;
                boolean result = this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
                        CategoriaDocumento.Annotazione, tmpDocumentiMap, documentiAttesi,
                        versamento, componentiAttesi, sottoComponentiAttesi);
                if (result) {
                    trovatiIdDocDuplicati = true;
                }
            }
        }

        if ((aggiornamentoUnitaDocumentaria.getNumeroAllegati() != null)
                && (aggiornamentoUnitaDocumentaria.getNumeroAllegati() != contaAllegati)
                || (aggiornamentoUnitaDocumentaria.getNumeroAllegati() == null
                        && contaAllegati != 0)/* MAC#13460 */) {
            corrAllegatiDichiarati = false;
        }

        if (aggiornamentoUnitaDocumentaria.getNumeroAllegati() != null
                && aggiornamentoUnitaDocumentaria.getNumeroAllegati() == contaAllegati) {
            corrAllegatiDichiarati = true;
        }

        if ((aggiornamentoUnitaDocumentaria.getNumeroAnnessi() != null)
                && (aggiornamentoUnitaDocumentaria.getNumeroAnnessi() != contaAnnessi)
                || (aggiornamentoUnitaDocumentaria.getNumeroAnnessi() == null
                        && contaAnnessi != 0)/* MAC#13460 */) {
            corrAnnessiDichiarati = false;
        }

        if (aggiornamentoUnitaDocumentaria.getNumeroAnnessi() != null
                && aggiornamentoUnitaDocumentaria.getNumeroAnnessi() == contaAnnessi) {
            corrAnnessiDichiarati = true;
        }

        if ((aggiornamentoUnitaDocumentaria.getNumeroAnnotazioni() != null)
                && (aggiornamentoUnitaDocumentaria.getNumeroAnnotazioni() != contaAnnotazioni)
                || ((aggiornamentoUnitaDocumentaria.getNumeroAnnotazioni() == null
                        && contaAnnotazioni != 0))/* MAC#13460 */) {
            corrAnnotazioniDichiarati = false;
        }

        if (aggiornamentoUnitaDocumentaria.getNumeroAnnotazioni() != null
                && aggiornamentoUnitaDocumentaria.getNumeroAnnotazioni() == contaAnnotazioni) {
            corrAnnotazioniDichiarati = true;
        }

        /*
         * if (versamento.getProfiloUnitaDocumentaria() != null) {
         * if(versamento.getProfiloUnitaDocumentaria().getData() == null) { trovataDataNullaIn =
         * "ProfiloUnitaDocumentaria.Data"; } else { SimpleDateFormat format = new
         * SimpleDateFormat("yyyy-MM-dd"); try { java.util.Date d =
         * format.parse(versamento.getProfiloUnitaDocumentaria().getData()); } catch (ParseException
         * ex) { trovataDataNullaIn = "ProfiloUnitaDocumentaria.Data"; } } }
         */

        strutturaUpdVers.setDocumentiAttesi(documentiAttesi);
        strutturaUpdVers.setComponentiAttesi(componentiAttesi);
        strutturaUpdVers.setSottoComponentiAttesi(sottoComponentiAttesi);
        strutturaUpdVers.setTrovatiIdDocDuplicati(trovatiIdDocDuplicati);
        strutturaUpdVers.setCorrAllegatiDichiarati(corrAllegatiDichiarati);
        strutturaUpdVers.setCorrAnnessiDichiarati(corrAnnessiDichiarati);
        strutturaUpdVers.setCorrAnnotazioniDichiarati(corrAnnotazioniDichiarati);

        return true;/* fixed non utilizzato nell'ambito di questo controllo */
    }

    /*
     * Questa versione della verifica serve per l'aggiunta allegati. Dal momento che i dati relativi
     * all'unità documentaria vengono versati durante il versamento, questa analizza soltanto il
     * songolo documento che viene versato dal servizio. (che può essere un Allegato, un Annesso,
     * un'Annotazione)
     *
     *
     */
    private boolean aggiungiVerificaDocumento(DocumentoType doc, int progressivo,
            CategoriaDocumento categoria, Map<String, UpdDocumentoVers> tmpDocumentiMap,
            List<UpdDocumentoVers> documentiAttesi, UpdVersamentoExt versamento,
            HashMap<String, UpdComponenteVers> componentiAttesi,
            HashMap<String, UpdComponenteVers> sottoComponentiAttesi) {
        boolean trovatiIdDocDuplicati = false;
        //
        UpdDocumentoVers tmpDocumentoVers;
        tmpDocumentoVers = new UpdDocumentoVers();
        tmpDocumentoVers.setProgressivo(progressivo);
        tmpDocumentoVers.setCategoriaDoc(categoria);
        tmpDocumentoVers.setRifUpdDocumento(doc);

        tmpDocumentoVers.setUpdComponentiAttesi(new ArrayList<>());
        tmpDocumentoVers.setUpdSottoComponentiAttesi(new ArrayList<>());

        // test di duplicazione dell'id documento all'interno dell'unità documentaria versata
        if (doc.getIDDocumento() == null || doc.getIDDocumento().length() == 0) {
            trovatiIdDocDuplicati = true;
        }
        if (tmpDocumentiMap.get(doc.getIDDocumento()) != null) {
            trovatiIdDocDuplicati = true;
        }
        tmpDocumentiMap.put(doc.getIDDocumento(), tmpDocumentoVers);

        documentiAttesi.add(tmpDocumentoVers);

        /*
         * se esistono componenti
         */
        if (doc.getStrutturaOriginale() != null
                && doc.getStrutturaOriginale().getComponenti() != null) {
            this.verificaComponenti(tmpDocumentoVers, versamento, componentiAttesi,
                    sottoComponentiAttesi);
        }

        return trovatiIdDocDuplicati;
    }

    private void verificaComponenti(UpdDocumentoVers documentoVersato, UpdVersamentoExt versamento,
            HashMap<String, UpdComponenteVers> componentiAttesi,
            HashMap<String, UpdComponenteVers> sottoComponentiAttesi) {
        UpdComponenteVers tmpComponenteVers;
        UpdComponenteVers tmpSottoComponenteVers;
        DocumentoType documento = documentoVersato.getRifUpdDocumento();

        // impostazione del valore di default per il tipo struttura documento
        // se esiste almeno un componente gestisce la strut
        if (documento.getStrutturaOriginale() != null
                && documento.getStrutturaOriginale().getTipoStruttura() == null) {
            documento.getStrutturaOriginale().setTipoStruttura(
                    versamento.getXmlDefaults().get(ParametroApplDB.TIPO_STRUT_DOC));
        }

        Iterator<? extends ComponenteType> tmpEnumCompo = documento.getStrutturaOriginale()
                .getComponenti().getComponente().iterator();
        while (tmpEnumCompo.hasNext()) {
            ComponenteType tmpComponente = tmpEnumCompo.next();

            // impostazione valori di default per il componente
            if (tmpComponente.getTipoComponente() == null) {
                tmpComponente.setTipoComponente(
                        versamento.getXmlDefaults().get(ParametroApplDB.TIPO_COMP_DOC));
            }

            tmpComponenteVers = new UpdComponenteVers();
            tmpComponenteVers.setId(tmpComponente.getID());
            tmpComponenteVers.setOrdinePresentazione(tmpComponente.getOrdinePresentazione());
            tmpComponenteVers
                    .setTipoSupporto(tmpComponente.getTipoSupportoComponente() != null
                            ? ComponenteVers.TipiSupporto
                                    .valueOf(tmpComponente.getTipoSupportoComponente().value())
                            : null);
            tmpComponenteVers.setPresenteRifMeta(tmpComponente.getRiferimento() != null);
            tmpComponenteVers.setDatiLetti(false);
            tmpComponenteVers.setMyUpdComponente(tmpComponente);
            tmpComponenteVers.setRifUpdDocumentoVers(documentoVersato);
            documentoVersato.getUpdComponentiAttesi().add(tmpComponenteVers);
            componentiAttesi.put(tmpComponente.getID(), tmpComponenteVers);
            if (tmpComponente.getSottoComponenti() != null) {
                Iterator<? extends SottoComponenteType> tmpEnumSottoCompo = tmpComponente
                        .getSottoComponenti().getSottoComponente().iterator();
                while (tmpEnumSottoCompo.hasNext()) {
                    SottoComponenteType tmpSottoComponente = (SottoComponenteType) tmpEnumSottoCompo
                            .next();

                    tmpSottoComponenteVers = new UpdComponenteVers();
                    tmpSottoComponenteVers.setId(tmpSottoComponente.getID());
                    tmpSottoComponenteVers
                            .setOrdinePresentazione(tmpComponente.getOrdinePresentazione());
                    tmpSottoComponenteVers.setTipoSupporto(UpdComponenteVers.TipiSupporto
                            .valueOf(tmpSottoComponente.getTipoSupportoComponente().value()));
                    tmpSottoComponenteVers
                            .setPresenteRifMeta(tmpSottoComponente.getRiferimento() != null);
                    tmpSottoComponenteVers.setDatiLetti(false);
                    tmpSottoComponenteVers.setMyUpdComponente(tmpComponente);
                    tmpSottoComponenteVers.setMyUpdSottoComponente(tmpSottoComponente);
                    tmpSottoComponenteVers.setRifDocumentoVers(documentoVersato);
                    tmpSottoComponenteVers.setRifComponenteVersPadre(tmpComponenteVers);
                    documentoVersato.getUpdSottoComponentiAttesi().add(tmpSottoComponenteVers);
                    sottoComponentiAttesi.put(tmpSottoComponente.getID(), tmpSottoComponenteVers);
                }
            }
        }
    }

}
