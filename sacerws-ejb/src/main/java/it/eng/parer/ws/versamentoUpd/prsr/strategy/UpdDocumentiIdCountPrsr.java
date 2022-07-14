/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.prsr.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.ParametroApplDB;
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
public class UpdDocumentiIdCountPrsr extends UpdBasePrsr {

    private HashMap<String, UpdComponenteVers> componentiAttesi;
    private HashMap<String, UpdComponenteVers> sottoComponentiAttesi;
    private List<UpdDocumentoVers> documentiAttesi;
    private Map<String, UpdDocumentoVers> tmpDocumentiMap;
    // boolean trovatiIdCompDuplicati;
    private boolean trovatiIdDocDuplicati;
    //
    private UpdVersamentoExt versamento;

    public UpdDocumentiIdCountPrsr(UpdVersamentoExt versamento) {
        this.versamento = versamento;

        this.init();
    }

    private final void init() {
        componentiAttesi = new HashMap<>();
        sottoComponentiAttesi = new HashMap<>();
        documentiAttesi = new ArrayList<>();
        tmpDocumentiMap = new HashMap<>();
        // trovatiIdCompDuplicati = false;
        trovatiIdDocDuplicati = false;

    }

    /**
     * Questa versione della verifica serve per il versamento sincrono. Analizza ed eventualmente integra dei default
     * l'intera unità documentaria versata
     *
     * @return true/false risultato dei controlli
     */
    public boolean eseguiControlli() {
        StrutturaUpdVers strutturaUpdVers = this.versamento.getStrutturaUpdVers();
        AggiornamentoUnitaDocumentariaType aggiornamentoUnitaDocumentaria = this.versamento.getVersamento()
                .getUnitaDocumentaria();

        int progressivo = 0;
        long contaAllegati = 0;
        long contaAnnessi = 0;
        long contaAnnotazioni = 0;

        Boolean corrAllegatiDichiarati = null;
        Boolean corrAnnessiDichiarati = null;
        Boolean corrAnnotazioniDichiarati = null;

        if (aggiornamentoUnitaDocumentaria.getDocumentoPrincipale() != null) {
            progressivo = 1;
            this.aggiungiVerificaDocumento(aggiornamentoUnitaDocumentaria.getDocumentoPrincipale(), progressivo,
                    CategoriaDocumento.Principale);
        }
        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAllegati() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria.getAllegati().getAllegato()
                    .iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                progressivo += 1;
                contaAllegati += 1;
                this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo, CategoriaDocumento.Allegato);
            }
        }

        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAnnessi() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria.getAnnessi().getAnnesso()
                    .iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                progressivo += 1;
                contaAnnessi += 1;
                this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo, CategoriaDocumento.Annesso);
            }
        }

        progressivo = 0;
        if (aggiornamentoUnitaDocumentaria.getAnnotazioni() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = aggiornamentoUnitaDocumentaria.getAnnotazioni()
                    .getAnnotazione().iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                progressivo += 1;
                contaAnnotazioni += 1;
                this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo, CategoriaDocumento.Annotazione);
            }
        }

        if ((aggiornamentoUnitaDocumentaria.getNumeroAllegati() != null)
                && (aggiornamentoUnitaDocumentaria.getNumeroAllegati() != contaAllegati)
                || (aggiornamentoUnitaDocumentaria.getNumeroAllegati() == null && contaAllegati != 0)/* MAC#13460 */) {
            corrAllegatiDichiarati = false;
        }

        if (aggiornamentoUnitaDocumentaria.getNumeroAllegati() != null
                && aggiornamentoUnitaDocumentaria.getNumeroAllegati() == contaAllegati) {
            corrAllegatiDichiarati = true;
        }

        if ((aggiornamentoUnitaDocumentaria.getNumeroAnnessi() != null)
                && (aggiornamentoUnitaDocumentaria.getNumeroAnnessi() != contaAnnessi)
                || (aggiornamentoUnitaDocumentaria.getNumeroAnnessi() == null && contaAnnessi != 0)/* MAC#13460 */) {
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
         * if (versamento.getProfiloUnitaDocumentaria() != null) { if(versamento.getProfiloUnitaDocumentaria().getData()
         * == null) { trovataDataNullaIn = "ProfiloUnitaDocumentaria.Data"; } else { SimpleDateFormat format = new
         * SimpleDateFormat("yyyy-MM-dd"); try { java.util.Date d =
         * format.parse(versamento.getProfiloUnitaDocumentaria().getData()); } catch (ParseException ex) {
         * trovataDataNullaIn = "ProfiloUnitaDocumentaria.Data"; } } }
         */

        strutturaUpdVers.setDocumentiAttesi(documentiAttesi);
        strutturaUpdVers.setComponentiAttesi(componentiAttesi);
        strutturaUpdVers.setSottoComponentiAttesi(sottoComponentiAttesi);
        // strutturaUpdVers.setTrovatiIdCompDuplicati(trovatiIdCompDuplicati);
        strutturaUpdVers.setTrovatiIdDocDuplicati(trovatiIdDocDuplicati);
        // TODO: non si dovrebbe porre il problema con la validazione XSD operata da JAXB
        // strutturaUpdVers.setTrovataDataNullaIn(trovataDataNullaIn);
        strutturaUpdVers.setCorrAllegatiDichiarati(corrAllegatiDichiarati);
        strutturaUpdVers.setCorrAnnessiDichiarati(corrAnnessiDichiarati);
        strutturaUpdVers.setCorrAnnotazioniDichiarati(corrAnnotazioniDichiarati);

        return true;/* fixed non utilizzato nell'ambito di questo controllo */
    }

    /**
     * Questa versione della verifica serve per l'aggiunta allegati. Dal momento che i dati relativi all'unità
     * documentaria vengono versati durante il versamento, questa analizza soltanto il songolo documento che viene
     * versato dal servizio. (che può essere un Allegato, un Annesso, un'Annotazione)
     *
     * @param doc
     *            tipo documento
     * @param progressivo
     *            progressivo documento
     * @param categoria
     *            categoria documento
     * 
     */
    private void aggiungiVerificaDocumento(DocumentoType doc, int progressivo, CategoriaDocumento categoria) {
        UpdDocumentoVers tmpDocumentoVers;
        tmpDocumentoVers = new UpdDocumentoVers();
        tmpDocumentoVers.setProgressivo(progressivo);
        tmpDocumentoVers.setCategoriaDoc(categoria);
        tmpDocumentoVers.setRifUpdDocumento(doc);

        tmpDocumentoVers.setUpdComponentiAttesi(new ArrayList<UpdComponenteVers>());
        tmpDocumentoVers.setUpdSottoComponentiAttesi(new ArrayList<UpdComponenteVers>());

        /*
         * TODO: da verificare se si pono lo stesso tipo di problema in JAXB
         * 
         * verifica se le date dei dati fiscali sono state scritte male: il parser xml di Castor presenta un fastidioso
         * bug per cui accetta un tag di tipo xs:date anche se è vuoto. Questo si traduce in un campo Datetime Java
         * completamente sballato e in un dato salvato sul database con valori altrettanto sballati.
         * 
         * if (doc.getDatiFiscali() != null) { if (doc.getDatiFiscali().getDataEmissione() != null) { if
         * (doc.getDatiFiscali().getDataEmissione() == null) { trovataDataNullaIn =
         * "DatiFiscali.DataEmissione del documento <ID> " + doc.getIDDocumento(); } } if
         * (doc.getDatiFiscali().getDataTermineEmissione() != null) { if (doc.getDatiFiscali().getDataTermineEmissione()
         * == null) { trovataDataNullaIn = "DatiFiscali.DataTermineEmissione del documento <ID> " +
         * doc.getIDDocumento(); } } }
         */

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
        if (doc.getStrutturaOriginale() != null && doc.getStrutturaOriginale().getComponenti() != null) {
            this.verificaComponenti(tmpDocumentoVers);
        }
    }

    private void verificaComponenti(UpdDocumentoVers documentoVersato) {
        UpdComponenteVers tmpComponenteVers;
        UpdComponenteVers tmpSottoComponenteVers;
        DocumentoType documento = documentoVersato.getRifUpdDocumento();

        // impostazione del valore di default per il tipo struttura documento
        // se esiste almeno un componente gestisce la strut
        if (documento.getStrutturaOriginale() != null && documento.getStrutturaOriginale().getTipoStruttura() == null) {
            documento.getStrutturaOriginale()
                    .setTipoStruttura(versamento.getXmlDefaults().get(ParametroApplDB.TIPO_STRUT_DOC));
        }

        Iterator<? extends ComponenteType> tmpEnumCompo = documento.getStrutturaOriginale().getComponenti()
                .getComponente().iterator();
        while (tmpEnumCompo.hasNext()) {
            ComponenteType tmpComponente = (ComponenteType) tmpEnumCompo.next();

            // impostazione valori di default per il componente
            if (tmpComponente.getTipoComponente() == null) {
                tmpComponente.setTipoComponente(versamento.getXmlDefaults().get(ParametroApplDB.TIPO_COMP_DOC));
            }
            // if (tmpComponente.getTipoSupportoComponente() == null) {
            // tmpComponente.setTipoSupportoComponente(TipoSupportoType.valueOf(xmlDefaults.get(CostantiDB.ParametroAppl.TIPO_SUPPORTO_COMP)));
            // }
            // Boolean bool = tmpComponente.isUtilizzoDataFirmaPerRifTemp();
            // if ( (bool != null) && (bool == false) ) {
            // tmpComponente.setUtilizzoDataFirmaPerRifTemp(Boolean.parseBoolean(xmlDefaults.get(CostantiDB.ParametroAppl.USO_DATA_FIRMA)));
            // }

            /*
             * if (fileAttesi.get(tmpComponente.getID()) != null) { trovatiIdCompDuplicati = true; }
             */
            tmpComponenteVers = new UpdComponenteVers();
            /*
             * if (tmpComponente.getID() == null || tmpComponente.getID().length() == 0) { trovatiIdCompDuplicati =
             * true; }
             */
            tmpComponenteVers.setId(tmpComponente.getID());
            tmpComponenteVers.setOrdinePresentazione(tmpComponente.getOrdinePresentazione());
            tmpComponenteVers.setTipoSupporto(tmpComponente.getTipoSupportoComponente() != null
                    ? UpdComponenteVers.TipiSupporto.valueOf(tmpComponente.getTipoSupportoComponente().value()) : null);
            tmpComponenteVers.setPresenteRifMeta(tmpComponente.getRiferimento() != null);
            tmpComponenteVers.setDatiLetti(false);
            tmpComponenteVers.setMyUpdComponente(tmpComponente);
            tmpComponenteVers.setRifUpdDocumentoVers(documentoVersato);
            documentoVersato.getUpdComponentiAttesi().add(tmpComponenteVers);
            componentiAttesi.put(tmpComponente.getID(), tmpComponenteVers);
            if (tmpComponente.getSottoComponenti() != null) {
                Iterator<? extends SottoComponenteType> tmpEnumSottoCompo = tmpComponente.getSottoComponenti()
                        .getSottoComponente().iterator();
                while (tmpEnumSottoCompo.hasNext()) {
                    SottoComponenteType tmpSottoComponente = (SottoComponenteType) tmpEnumSottoCompo.next();

                    // impostazione valori di default per il sottocomponente
                    // if (tmpSottoComponente.getTipoSupportoComponente() == null) {
                    // tmpSottoComponente.setTipoSupportoComponente(TipoSupportoType.valueOf(xmlDefaults.get(CostantiDB.ParametroAppl.TIPO_SUPPORTO_COMP)));
                    // }

                    /*
                     * if (fileAttesi.get(tmpSottoComponente.getID()) != null) { trovatiIdCompDuplicati = true; }
                     */
                    tmpSottoComponenteVers = new UpdComponenteVers();
                    /*
                     * if (tmpSottoComponente.getID() == null || tmpSottoComponente.getID().length() == 0) {
                     * trovatiIdCompDuplicati = true; }
                     */
                    tmpSottoComponenteVers.setId(tmpSottoComponente.getID());
                    tmpSottoComponenteVers.setOrdinePresentazione(tmpComponente.getOrdinePresentazione());
                    tmpSottoComponenteVers.setTipoSupporto(UpdComponenteVers.TipiSupporto
                            .valueOf(tmpSottoComponente.getTipoSupportoComponente().value()));
                    tmpSottoComponenteVers.setPresenteRifMeta(tmpSottoComponente.getRiferimento() != null);
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