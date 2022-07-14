/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.prsr.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.ComponenteVers.TipiSupporto;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.ejb.ControlliUpdVersamento;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.IRispostaUpdVersWS;
import it.eng.parer.ws.versamentoUpd.utils.UpdDocumentiUtils;
import it.eng.parer.ws.versamentoUpd.utils.UpdGestioneDatiSpec;
import it.eng.parer.ws.xml.versReq.TipoConservazioneType;
import it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType;
import it.eng.parer.ws.xml.versUpdResp.ComponenteType;
import it.eng.parer.ws.xml.versUpdResp.DocumentoType;
import it.eng.parer.ws.xml.versUpdResp.StrutturaType;
import it.eng.parer.ws.xml.versUpdResp.StrutturaType.Componenti;
import it.eng.parer.ws.xml.versUpdResp.UnitaDocumentariaType.Allegati;
import it.eng.parer.ws.xml.versUpdResp.UnitaDocumentariaType.Annessi;
import it.eng.parer.ws.xml.versUpdResp.UnitaDocumentariaType.Annotazioni;

/**
 *
 * @author sinatti_s
 */
public class UpdDocumentiPrsr extends UpdBasePrsr {

    private static final Logger log = LoggerFactory.getLogger(UpdDocumentiPrsr.class);

    private UpdVersamentoExt versamento; // nel costruttore
    private RispostaControlli rispostaControlli; // interno allocare nel costruttore
    // classe di supporto per la verifica e l'estrazione dei dati specifici
    private UpdGestioneDatiSpec gestioneDatiSpec = null; // allocata nel costruttore
    // stateless ejb per i controlli sul db
    private ControlliUpdVersamento updVersamentoControlli = null;
    // stateless ejb per i controlli sul db
    private ControlliSemantici controlliSemantici = null;
    //
    private CompRapportoUpdVers myEsito;

    public UpdDocumentiPrsr(UpdVersamentoExt versamento, IRispostaUpdVersWS rispostaWs, CompRapportoUpdVers myEsito) {
        this.versamento = versamento;
        this.rispostaWs = rispostaWs;
        this.myEsito = myEsito;

        // init costruttuore
        init();

    }

    private void init() {
        // init controlli on documents
        this.initEsitoAndControlliDocumenti();

        // init documents on resp
        this.buildDocumentoOnResponse();

        // istanzia la risposta controlli
        rispostaControlli = new RispostaControlli();

        // alloca la classe di verifica ed estrazione dei dati specifici e dei dati
        // specifici di migrazione
        // NB: questa allocazione può fallire. Al termine devo verificare lo stato di
        // RispostaWS e gestire il problema
        gestioneDatiSpec = new UpdGestioneDatiSpec(versamento);
    }

    private void initEsitoAndControlliDocumenti() {
        // init NA
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        // doc principale
        if (versamento.hasDocumentoPrincipaleToUpd()) {
            for (UpdDocumentoVers principale : strutturaUpdVers.getDocumentiAttesi().stream()
                    .filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Principale))
                    .collect(Collectors.toList())) {
                for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                        ControlloEseguito.FamigliaControllo.CONTROLLICHIAVEDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliDocPrincipale(controllo,
                            principale.getRifUpdDocumento().getIDDocumento());
                }

                for (ControlloWSResp controllo : ControlliWSBundle
                        .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLIDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliDocPrincipale(controllo,
                            principale.getRifUpdDocumento().getIDDocumento());
                }

                // componenti
                int idx = 0;
                for (UpdComponenteVers componente : principale.getUpdComponentiAttesi().stream()
                        .collect(Collectors.toList())) {
                    // assegno la chiave di controllo
                    componente.setKeyCtrl(UpdDocumentiUtils.buildComponenteKeyCtrl(principale.getCategoriaDoc().name(),
                            String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()), idx));

                    for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                            ControlloEseguito.FamigliaControllo.CONTROLLICHIAVECOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiDocPrincipale(controllo, componente.getKeyCtrl());
                    }
                    for (ControlloWSResp controllo : ControlliWSBundle
                            .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLICOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiDocPrincipale(controllo, componente.getKeyCtrl());
                    }
                    idx++;
                }
            }

        }

        // allegati
        if (versamento.hasAllegatiToUpd()) {
            Allegati allegati = new Allegati();
            myEsito.getUnitaDocumentaria().setAllegati(allegati);

            for (UpdDocumentoVers allegato : strutturaUpdVers.getDocumentiAttesi().stream()
                    .filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Allegato))
                    .collect(Collectors.toList())) {

                for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                        ControlloEseguito.FamigliaControllo.CONTROLLICHIAVEDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAllegati(controllo,
                            allegato.getRifUpdDocumento().getIDDocumento());
                }

                for (ControlloWSResp controllo : ControlliWSBundle
                        .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLIDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAllegati(controllo,
                            allegato.getRifUpdDocumento().getIDDocumento());
                }

                // componenti
                int idx = 0;
                for (UpdComponenteVers componente : allegato.getUpdComponentiAttesi().stream()
                        .collect(Collectors.toList())) {
                    // assegno la chiave di controllo
                    componente.setKeyCtrl(UpdDocumentiUtils.buildComponenteKeyCtrl(allegato.getCategoriaDoc().name(),
                            String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()), idx));

                    for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                            ControlloEseguito.FamigliaControllo.CONTROLLICHIAVECOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAllegati(controllo, componente.getKeyCtrl());
                    }
                    for (ControlloWSResp controllo : ControlliWSBundle
                            .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLICOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAllegati(controllo, componente.getKeyCtrl());
                    }
                    idx++;
                }

            }
        }

        // annessi
        if (versamento.hasAnnessiToUpd()) {
            Annessi annessi = new Annessi();
            myEsito.getUnitaDocumentaria().setAnnessi(annessi);

            for (UpdDocumentoVers annesso : strutturaUpdVers.getDocumentiAttesi().stream()
                    .filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Annesso)).collect(Collectors.toList())) {
                for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                        ControlloEseguito.FamigliaControllo.CONTROLLICHIAVEDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAnnessi(controllo,
                            annesso.getRifUpdDocumento().getIDDocumento());
                }

                for (ControlloWSResp controllo : ControlliWSBundle
                        .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLIDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAnnessi(controllo,
                            annesso.getRifUpdDocumento().getIDDocumento());
                }

                // componenti
                int idx = 0;
                for (UpdComponenteVers componente : annesso.getUpdComponentiAttesi().stream()
                        .collect(Collectors.toList())) {
                    // assegno la chiave di controllo
                    componente.setKeyCtrl(UpdDocumentiUtils.buildComponenteKeyCtrl(annesso.getCategoriaDoc().name(),
                            String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()), idx));
                    for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                            ControlloEseguito.FamigliaControllo.CONTROLLICHIAVECOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAnnessi(controllo, componente.getKeyCtrl());
                    }
                    for (ControlloWSResp controllo : ControlliWSBundle
                            .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLICOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAnnessi(controllo, componente.getKeyCtrl());
                    }
                    idx++;
                }

            }

        }

        // annotazioni
        if (versamento.hasAnnotazioniToUpd()) {
            Annotazioni annotazioni = new Annotazioni();
            myEsito.getUnitaDocumentaria().setAnnotazioni(annotazioni);

            for (UpdDocumentoVers annotazione : strutturaUpdVers.getDocumentiAttesi().stream()
                    .filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Annotazione))
                    .collect(Collectors.toList())) {
                for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                        ControlloEseguito.FamigliaControllo.CONTROLLICHIAVEDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAnnotazioni(controllo,
                            annotazione.getRifUpdDocumento().getIDDocumento());
                }

                for (ControlloWSResp controllo : ControlliWSBundle
                        .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLIDOCUMENTO.name())) {
                    versamento.addControlloNAOnControlliAnnotazioni(controllo,
                            annotazione.getRifUpdDocumento().getIDDocumento());
                }

                // componenti
                int idx = 0;
                for (UpdComponenteVers componente : annotazione.getUpdComponentiAttesi().stream()
                        .collect(Collectors.toList())) {
                    // assegno la chiave di controllo
                    componente.setKeyCtrl(UpdDocumentiUtils.buildComponenteKeyCtrl(annotazione.getCategoriaDoc().name(),
                            String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()), idx));

                    for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                            ControlloEseguito.FamigliaControllo.CONTROLLICHIAVECOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAnnotazioni(controllo, componente.getKeyCtrl());
                    }
                    for (ControlloWSResp controllo : ControlliWSBundle
                            .getControlliByCdFamiglia(ControlloEseguito.FamigliaControllo.CONTROLLICOMPONENTE.name())) {
                        versamento.addControlloNAOnControlliComponentiAnnotazioni(controllo, componente.getKeyCtrl());
                    }
                    idx++;
                }
            }
        }

    }

    private void buildDocumentoOnResponse() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {
            // build on response
            DocumentoType documentoType = new DocumentoType();
            documentoType.setIDDocumento(documento.getRifUpdDocumento().getIDDocumento());
            documentoType.setTipoDocumento((String) documento.getRifUpdDocumento().getTipoDocumento());
            // set response
            documento.setRifUpdDocumentoResp(documentoType);

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (versamento.hasDocumentoPrincipaleToUpd()) {
                    // build on response
                    myEsito.setDocumentoPrincipale(documentoType);
                }
                break;
            case Allegato:
                if (versamento.hasAllegatiToUpd()) {
                    // build on response
                    myEsito.getUnitaDocumentaria().getAllegati().getAllegato().add(documentoType);
                }
                break;
            case Annesso:
                if (versamento.hasAnnessiToUpd()) {
                    // build on response
                    myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso().add(documentoType);
                }
                break;
            case Annotazione:
                if (versamento.hasAnnotazioniToUpd()) {
                    // build on response
                    myEsito.getUnitaDocumentaria().getAnnotazioni().getAnnotazione().add(documentoType);
                }
                break;
            }

            // in cascata per ogni documento si ricostruisce StrutturaOriginale/Componenti

            // build struttura originale
            this.buildStrutturaOrigOnResponse(documento);

            // build componente
            this.buildComponenteOnResponse(documento);
        } // documento
    }

    // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    // INIZIO METODI DI VERIFICA §§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    public boolean eseguiControlli() {
        boolean prosegui = true;
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        // doc principale // allegati // annessi // annotazioni
        // per ogni documento collezionato in precedenza
        for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {

            myAvanzamentoWs.setFase("Verifica semantica documento")
                    .setDocumento(documento.getRifUpdDocumento().getIDDocumento()).logAvanzamento();
            /*
             * documento.setUrnPartDocumento( MessaggiWSFormat.formattaUrnPartDocumento(documento.getCategoriaDoc(),
             * documento.getProgressivo()));
             */
            documento.setUrnPartDocumento(documento.getRifUpdDocumento().getIDDocumento());
            // verifica id e tipo documento
            prosegui = this.verificaIdDocumento(documento);

            // verifico il tipo documento
            if (prosegui) {
                prosegui = this.verificaTipoDocumento(documento);
            }

            // verifico abilitazione al tipo documento
            if (prosegui) {
                prosegui = this.verificaAbilitazioneTipoDocumento(documento);
            }

            // verifico la corrispondenza dei DATI SPECIFICI del Documento
            if (prosegui && documento.getRifUpdDocumento().getDatiSpecifici() != null) {
                prosegui = this.parseDatiSpecDoc(documento);
            }

            // verifico la corrispondenza dei DATI MIGRAZIONE del Documento
            if (prosegui && documento.getRifUpdDocumento().getDatiSpecificiMigrazione() != null) {
                prosegui = this.parseDatiSpecMigrazDoc(documento);
            }

            // verifico ordine componenti
            if (prosegui) {
                prosegui = this.verificaOrdineComponente(documento);
            }

            // verifico componenti
            if (prosegui) {
                prosegui = this.verificaComponenti(documento);
            }

        }

        /*
         * Valutazione esito "complessivo" dei controlli su componenti
         */
        this.evaluteControlliOnComponente(strutturaUpdVers.getDocumentiAttesi());

        /*
         * Valutazione esito "complessivo" dei controlli su documenti eseguiti
         */
        this.evaluteControlliOnDocumento(strutturaUpdVers.getDocumentiAttesi());

        myAvanzamentoWs.resetFase().setFase("verifica semantica documenti - fine").logAvanzamento();

        return prosegui;// TODO: verificare se ha senso ..
    }

    private void evaluteControlliOnDocumento(List<UpdDocumentoVers> documenti) {
        // 0. merge di tutti i controlli fatti sui componenti dei documenti per singolo
        // documento
        // (principale, annessi, allegati, annotazioni ...)
        // per ogni documento
        List<ControlloEseguito> controlliGlobal = new ArrayList<>();
        for (UpdDocumentoVers documento : documenti) {
            Set<ControlloEseguito> controlli = null;
            switch (documento.getCategoriaDoc()) {
            case Principale:
                controlli = versamento.getControlliDocPrincipale(documento.getRifUpdDocumento().getIDDocumento());
                break;
            case Allegato:
                controlli = versamento.getControlliAllegato(documento.getRifUpdDocumento().getIDDocumento());
                break;
            case Annesso:
                controlli = versamento.getControlliAnnesso(documento.getRifUpdDocumento().getIDDocumento());
                break;
            case Annotazione:
                controlli = versamento.getControlliAnnotazione(documento.getRifUpdDocumento().getIDDocumento());
                break;
            }

            // add all
            controlliGlobal.addAll(controlli);

        }
        // 1. verifica "complessiva di tutti i controlli secondo condizioni

        if (versamento.anyMatchEsitoControlli(controlliGlobal, VoceDiErrore.TipiEsitoErrore.NEGATIVO)) {

            // esito generale
            this.setEsitoControlloErrBundle(ControlliWSBundle.CTRL_UD_DOCUMENTI, MessaggiWSBundle.UD_016_001);

            // aggiunta su controlli ud
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DOCUMENTI), SeverityEnum.ERROR,
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_016_001);
        } else /* TODO : i casi di WARNING vanno verificati! */ if (versamento.anyMatchEsitoControlli(controlliGlobal,
                VoceDiErrore.TipiEsitoErrore.WARNING)) {

            // esito generale
            this.setEsitoControlloWarnBundle(ControlliWSBundle.CTRL_UD_DOCUMENTI, MessaggiWSBundle.UD_016_001);

            // aggiunta su warnings
            versamento.addControlloOnWarningsBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DOCUMENTI),
                    MessaggiWSBundle.UD_016_001);

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DOCUMENTI), SeverityEnum.WARNING,
                    TipiEsitoErrore.WARNING, MessaggiWSBundle.UD_016_001);
        } else if (versamento.anyMatchEsitoControlli(controlliGlobal,
                VoceDiErrore.TipiEsitoErrore.POSITIVO)) /*
                                                         * ATTENZIONE: alcuni dei controlli potrebbero essere in
                                                         * NON_ATTIVO ma se i restanti sono tutti positivi (non si sono
                                                         * verificati i primi due casi) allora il controllo generale può
                                                         * essere considerato POSITIVO
                                                         */ {

            // aggiunta su controlli ud
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DOCUMENTI));
        }
    }

    private void evaluteControlliOnComponente(List<UpdDocumentoVers> documenti) {
        // 0. merge di tutti i controlli fatti sui componenti dei documenti per singolo documento
        // (principale, annessi, allegati, annotazioni ...)
        // per ogni documento
        List<ControlloEseguito> controlliGlobal = new ArrayList<>();
        for (UpdDocumentoVers documento : documenti) {
            for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
                Set<ControlloEseguito> controlli = null;
                switch (documento.getCategoriaDoc()) {
                case Principale:
                    controlli = versamento.getControlliComponenteDocPrincipale(componente.getKeyCtrl());
                    break;
                case Allegato:
                    controlli = versamento.getControlliComponenteAllegati(componente.getKeyCtrl());
                    break;
                case Annesso:
                    controlli = versamento.getControlliComponenteAnnessi(componente.getKeyCtrl());
                    break;
                case Annotazione:
                    controlli = versamento.getControlliComponenteAnnotazioni(componente.getKeyCtrl());
                    break;
                }

                // add all
                controlliGlobal.addAll(controlli);
            }
        }
        // 1. verifica "complessiva di tutti i controlli secondo condizioni

        if (versamento.anyMatchEsitoControlli(controlliGlobal, VoceDiErrore.TipiEsitoErrore.NEGATIVO)) {

            // esito generale
            this.setEsitoControlloErrBundle(ControlliWSBundle.CTRL_UD_COMPONENTI, MessaggiWSBundle.UD_016_002);

            // aggiunta su controlli ud
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COMPONENTI), SeverityEnum.ERROR,
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_016_002);
        } else /* TODO : i casi di WARNING vanno verificati! */ if (versamento.allMatchEsitoControlli(controlliGlobal,
                VoceDiErrore.TipiEsitoErrore.WARNING)) {

            // esito generale
            this.setEsitoControlloWarnBundle(ControlliWSBundle.CTRL_UD_COMPONENTI, MessaggiWSBundle.UD_016_002);

            // aggiunta su warnings
            versamento.addControlloOnWarningsBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COMPONENTI), MessaggiWSBundle.UD_016_002);

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COMPONENTI), SeverityEnum.WARNING,
                    TipiEsitoErrore.WARNING, MessaggiWSBundle.UD_016_002);
        } else if (versamento.anyMatchEsitoControlli(controlliGlobal,
                VoceDiErrore.TipiEsitoErrore.POSITIVO)) /*
                                                         * ATTENZIONE: alcuni dei controlli potrebbero essere in
                                                         * NON_ATTIVATO ma se i restanti sono tutti positivi (non si
                                                         * sono verificati i primi due casi) allora il controllo
                                                         * generale può essere considerato POSITIVO
                                                         */ {

            // aggiunta su controlli ud
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COMPONENTI));
        }

    }

    private void buildStrutturaOrigOnResponse(UpdDocumentoVers documento) {

        // if exists ....
        if (documento.getRifUpdDocumento().getStrutturaOriginale() != null) {

            StrutturaType strutturaType = new StrutturaType();
            strutturaType.setTipoStruttura(documento.getRifUpdDocumento().getStrutturaOriginale().getTipoStruttura());

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (versamento.hasDocPStrutturaOriginaleToUpd()) {
                    // build on response
                    myEsito.getDocumentoPrincipale().setStrutturaOriginale(strutturaType);
                }
                break;
            case Allegato:
                if (versamento.hasAllegatiToUpd()) {
                    // build on response (sull'allegato in esame)
                    DocumentoType allegato = myEsito.getUnitaDocumentaria().getAllegati().getAllegato().stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    allegato.setStrutturaOriginale(strutturaType);
                }
                break;
            case Annesso:
                if (versamento.hasAnnessiToUpd()) {
                    // build on response (su annesso in esame)
                    DocumentoType annesso = myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso().stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    annesso.setStrutturaOriginale(strutturaType);
                }
                break;
            case Annotazione:
                if (versamento.hasAnnotazioniToUpd()) {
                    // build on response (su annotazione in esame)
                    DocumentoType annotazione = myEsito.getUnitaDocumentaria().getAnnotazioni().getAnnotazione()
                            .stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    annotazione.setStrutturaOriginale(strutturaType);
                }
                break;
            }

        } // if
    }

    private void buildComponenteOnResponse(UpdDocumentoVers documento) {
        // build on response
        // TODO: set da verificare successivamente ..
        Componenti componenti = new Componenti();
        for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
            ComponenteType componenteType = new ComponenteType();
            componenteType.setTipoComponente(componente.getMyUpdComponente().getTipoComponente());
            componenteType
                    .setOrdinePresentazione(String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()));
            componenti.getComponente().add(componenteType);

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (versamento.hasDocumentoPrincipaleToUpd()) {
                    // build on response
                    myEsito.getDocumentoPrincipale().getStrutturaOriginale().setComponenti(componenti);
                }
                break;
            case Allegato:
                if (versamento.hasAllegatiToUpd()) {
                    // build on response (sull'allegato in esame)
                    DocumentoType allegato = myEsito.getUnitaDocumentaria().getAllegati().getAllegato().stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    allegato.getStrutturaOriginale().setComponenti(componenti);
                }
                break;
            case Annesso:
                if (versamento.hasAnnessiToUpd()) {
                    // build on response (su annesso in esame)
                    DocumentoType annesso = myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso().stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    annesso.getStrutturaOriginale().setComponenti(componenti);
                }
                break;
            case Annotazione:
                if (versamento.hasAnnotazioniToUpd()) {
                    // build on response (su annotazione in esame)
                    DocumentoType annotazione = myEsito.getUnitaDocumentaria().getAnnotazioni().getAnnotazione()
                            .stream()
                            .filter(a -> a.getIDDocumento().equals(documento.getRifUpdDocumento().getIDDocumento()))
                            .collect(Collectors.toList()).get(0);

                    annotazione.getStrutturaOriginale().setComponenti(componenti);
                }
                break;
            }
        }
    }

    private boolean verificaIdDocumento(UpdDocumentoVers documento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        boolean hasDocError = false;
        String cdCtrl = ControlliWSBundle.CTRL_KEYDOC_CHIAVEDOC;

        // recupera l'ejb per i controlli se possibile - altrimenti segnala errore
        try {
            updVersamentoControlli = (ControlliUpdVersamento) new InitialContext()
                    .lookup("java:module/ControlliUpdVersamento");
        } catch (NamingException ex) {
            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR, MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "UpdDocumentoPrsr.init.ControlliUpdVersamento: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB RapportoVersBuilder ", ex);
            return false;
        }

        rispostaControlli = updVersamentoControlli.checkIdDocumentoInUD(strutturaUpdVers.getIdUd(),
                documento.getRifUpdDocumento().getIDDocumento(), documento.getCategoriaDoc().name(),
                documento.getRifUpdDocumento().getTipoDocumento(), documento.getUrnPartDocumento());

        switch (documento.getCategoriaDoc()) {
        case Principale:

            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli documento
                versamento.addControlloOkOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
                strutturaUpdVers.setIdTipoDocPrincipale(rispostaControlli.getrLongExtended());
            } else {
                hasDocError = true;
                // aggiunta su controlli documento
                versamento.addEsitoControlloOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
            break;
        case Allegato:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli allegato
                versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
            break;
        case Annesso:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annesso
                versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
            break;
        case Annotazione:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
            break;
        }
        // esito generale in caso di errore
        if (hasDocError) {
            /*
             * this.setEsitoControlloErr(cdCtrl, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
             */
            return false;// non può effettuare i successivi controlli
        } else {
            // memorizzo l'ID della chiave doc trovata e il tipo da verificare
            documento.setIdRecDocumentoDB(rispostaControlli.getrLong());
            documento.setIdTipoDocumentoDB(rispostaControlli.getrLongExtended());
            documento.setCatDocNonverificato(rispostaControlli.getrString());
            documento.setTipoDocNonverificato(rispostaControlli.getrStringExtended());
        }

        return true;// prosegui con check successivo
    }

    private boolean verificaTipoDocumento(UpdDocumentoVers documento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        String codErr = null, dsErr = null;
        String cdCtrl = ControlliWSBundle.CTRL_DOC_TIPODOC;
        boolean hasDocError = false;

        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR, MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "UpdDocumentoPrsr.init.ControlliSemantici: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB dei controlli semantici in UpdDocumentoPrsr ", ex);
            return false;
        }

        // TODO : logica da verificare ...
        if (!documento.getCategoriaDoc().name().equalsIgnoreCase(documento.getCatDocNonverificato())) {
            // aggiunta su controlli documento
            codErr = MessaggiWSBundle.DOC_010_002;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.DOC_010_002,
                    documento.getRifUpdDocumento().getIDDocumento(), documento.getCategoriaDoc().name());
            hasDocError = true;
        }

        if (!hasDocError && !documento.getRifUpdDocumento().getTipoDocumento()
                .equalsIgnoreCase(documento.getTipoDocNonverificato())) {
            // aggiunta su controlli documento
            codErr = MessaggiWSBundle.DOC_010_003;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.DOC_010_003,
                    documento.getRifUpdDocumento().getIDDocumento(), documento.getRifUpdDocumento().getTipoDocumento());
            hasDocError = true;

        }

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (!hasDocError) {
                // aggiunta su controlli documento
                versamento.addControlloOkOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                /*
                 * rispostaControlli = controlliSemantici.checkTipoDocumento(
                 * documento.getRifUpdDocumento().getTipoDocumento(), strutturaUpdVers.getIdStruttura(), true,
                 * documento.getUrnPartDocumento()); if (rispostaControlli.isrBoolean()) {
                 * documento.setIdTipoDocumentoDB(rispostaControlli.getrLong()); // id tipo doc da aggiornare // setto
                 * l'id anche nella struttura (servirà successivamente)
                 * strutturaUpdVers.setIdTipoDocPrincipale(rispostaControlli.getrLong()); hasDocError = false; } else {
                 * hasDocError = true; // aggiunta su controlli documento
                 * versamento.addEsitoControlloOnControlliDocPrincipale( ControlliWSBundle.getControllo(cdCtrl),
                 * SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                 * codErr, dsErr); }
                 */

                // aggiunta su controlli documento
                versamento.addEsitoControlloOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
            }
            break;
        case Allegato:
            if (!hasDocError) {
                // aggiunta su controlli allegato
                versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                /*
                 * rispostaControlli = controlliSemantici.checkTipoDocumento(
                 * documento.getRifUpdDocumento().getTipoDocumento(), strutturaUpdVers.getIdStruttura(), false,
                 * documento.getUrnPartDocumento()); if (rispostaControlli.isrBoolean()) {
                 * documento.setIdTipoDocumentoDB(rispostaControlli.getrLong()); // id tipo doc da aggiornare
                 * hasDocError = false; } else { hasDocError = true; // aggiunta su controlli allegati
                 * versamento.addEsitoControlloOnControlliAllegati( ControlliWSBundle.getControllo(cdCtrl),
                 * SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                 * codErr, dsErr); }
                 */

                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
            }
            break;
        case Annesso:
            if (!hasDocError) {
                // aggiunta su controlli annesso
                versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());

            } else {
                /*
                 * rispostaControlli = controlliSemantici.checkTipoDocumento(
                 * documento.getRifUpdDocumento().getTipoDocumento(), strutturaUpdVers.getIdStruttura(), false,
                 * documento.getUrnPartDocumento()); if (rispostaControlli.isrBoolean()) {
                 * documento.setIdTipoDocumentoDB(rispostaControlli.getrLong()); // id tipo doc da aggiornare
                 * hasDocError = false; } else { hasDocError = true; // aggiunta su controlli annessi
                 * versamento.addEsitoControlloOnControlliAnnessi( ControlliWSBundle.getControllo(cdCtrl),
                 * SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                 * codErr, dsErr); }
                 */
                // aggiunta su controlli annessi
                versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
            }
            break;
        case Annotazione:
            if (!hasDocError) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                /*
                 * rispostaControlli = controlliSemantici.checkTipoDocumento(
                 * documento.getRifUpdDocumento().getTipoDocumento(), strutturaUpdVers.getIdStruttura(), false,
                 * documento.getUrnPartDocumento()); if (rispostaControlli.isrBoolean()) {
                 * documento.setIdTipoDocumentoDB(rispostaControlli.getrLong()); // id tipo doc da aggiornare
                 * hasDocError = false; } else { hasDocError = true; // aggiunta su controlli annotazione
                 * versamento.addEsitoControlloOnControlliAnnotazioni( ControlliWSBundle.getControllo(cdCtrl),
                 * SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                 * codErr, dsErr); }
                 */

                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
            }
            break;
        }
        // esito generale in caso di errore
        if (hasDocError) {
            /*
             * this.setEsitoControlloErr(cdCtrl, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
             */
            return false; // id tipo documento non trovato quindi non è possibile effettuare il controllo
                          // successivo
        }

        return true;// prosegui con check successivo
    }

    private boolean verificaAbilitazioneTipoDocumento(UpdDocumentoVers documento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        boolean hasDocError = false;
        String cdCtrl = ControlliWSBundle.CTRL_DOC_ABILITAZIONEUTENTETIPO;

        // recupera l'ejb per i controlli se possibile - altrimenti segnala errore
        try {
            updVersamentoControlli = (ControlliUpdVersamento) new InitialContext()
                    .lookup("java:module/ControlliUpdVersamento");
        } catch (NamingException ex) {
            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR, MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "UpdDocumentoPrsr.init.ControlliUpdVersamento: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB RapportoVersBuilder ", ex);
            return false;
        }

        rispostaControlli = updVersamentoControlli.checkTipoDocRegIamUserOrganizzazione(
                documento.getRifUpdDocumento().getTipoDocumento(), documento.getIdTipoDocumentoDB(),
                strutturaUpdVers.getIdStruttura(), strutturaUpdVers.getIdUser());

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli documento
                versamento.addControlloOkOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());

            } else {
                hasDocError = true;
                // aggiunta su controlli documento
                versamento.addEsitoControlloOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            }
            break;
        case Allegato:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli allegato
                versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());

            } else {
                hasDocError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            }
            break;
        case Annesso:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annesso
                versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());

            } else {
                hasDocError = true;
                // aggiunta su controlli annessi
                versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            }
            break;
        case Annotazione:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
            break;
        }
        // esito generale in caso di errore
        if (hasDocError) {
            this.setEsitoControlloErr(cdCtrl, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
        } else {
            // set id abilitazione
            documento.setIdAbilTipoDocumentoDB(rispostaControlli.getrLong());
        }

        return true;// prosegui con check successivo
    }

    private boolean parseDatiSpecDoc(UpdDocumentoVers documento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        String cdCtrlDatiSpec = ControlliWSBundle.CTRL_DOC_DATISPECIFICI;

        boolean hasDocError = false;

        RispostaControlliAttSpec tmpControlliAttSpec = new RispostaControlliAttSpec();
        tmpControlliAttSpec.setrBoolean(false);

        // DatiSpecifici
        JAXBElement<DatiSpecificiType> tmpDatiSpecifici = documento.getRifUpdDocumento().getDatiSpecifici();
        tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.DOC, tmpDatiSpecifici,
                documento.getIdTipoDocumentoDB(), documento.getUrnPartDocumento(),
                documento.getRifUpdDocumento().getTipoDocumento());

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli doc principale
                versamento.addControlloOkOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());
            }
            break;
        case Allegato:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli allegati
                versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());
            }
            break;
        case Annesso:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli annesso
                versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());
            }
            break;
        case Annotazione:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrlDatiSpec),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());
            }
        }

        // se non presenti errori -> dati collezionati
        if (!hasDocError) {
            documento.setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
            documento.setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
        }

        return true;// prosegui
    }

    private boolean parseDatiSpecMigrazDoc(UpdDocumentoVers documento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        String cdCtrlDatiSpecMigraz = ControlliWSBundle.CTRL_DOC_DATISPECIFICIMIGRAZ;

        String codErr = null, dsErr = null;
        boolean hasDocError = false;

        RispostaControlliAttSpec tmpControlliAttSpec = new RispostaControlliAttSpec();
        tmpControlliAttSpec.setrBoolean(false);

        // DatiSpecificiMigrazione
        if (StringUtils.isNotBlank(strutturaUpdVers.getTipoConservazione())
                && TipoConservazioneType.MIGRAZIONE.name().equalsIgnoreCase(strutturaUpdVers.getTipoConservazione())) {

            JAXBElement<DatiSpecificiType> tmpDatiSpecificiMigrazione = documento.getRifUpdDocumento()
                    .getDatiSpecificiMigrazione();
            tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.DOC, tmpDatiSpecificiMigrazione,
                    documento.getIdTipoDocumentoDB(), documento.getUrnPartDocumento(),
                    documento.getRifUpdDocumento().getTipoDocumento());

            // collect cod and desc error (esito generale)
            if (!tmpControlliAttSpec.isrBoolean()) {
                hasDocError = true;
                codErr = tmpControlliAttSpec.getCodErr();
                dsErr = tmpControlliAttSpec.getDsErr();
            }

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli doc principale
                    versamento.addControlloOkOnControlliDocPrincipale(
                            ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    // aggiunta su controlli doc principale
                    versamento.addEsitoControlloOnControlliDocPrincipale(
                            ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz), SeverityEnum.ERROR,
                            TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(), codErr, dsErr);
                }
                break;
            case Allegato:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli allegati
                    versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliAllegati(
                            ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz), SeverityEnum.ERROR,
                            TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(), codErr, dsErr);
                }
                break;
            case Annesso:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli annesso
                    versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    // aggiunta su controlli annesso
                    versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            documento.getRifUpdDocumento().getIDDocumento(), codErr, dsErr);
                }
                break;
            case Annotazione:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli annotazione
                    versamento.addControlloOkOnControlliAnnotazioni(
                            ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    // aggiunta su controlli annotazione
                    versamento.addEsitoControlloOnControlliAnnotazioni(
                            ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz), SeverityEnum.ERROR,
                            TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(), codErr, dsErr);
                }
                break;
            } // switch
        } else {
            //
            hasDocError = true;
            codErr = MessaggiWSBundle.XSD_006_004;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_004);

            switch (documento.getCategoriaDoc()) {
            case Principale:
                // aggiunta su controlli doc principale
                versamento.addEsitoControlloOnControlliDocPrincipale(
                        ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz), SeverityEnum.ERROR,
                        TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(), codErr, dsErr);
                break;
            case Allegato:
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
                break;
            case Annesso:
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
                break;
            case Annotazione:
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrlDatiSpecMigraz),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, documento.getRifUpdDocumento().getIDDocumento(),
                        codErr, dsErr);
                break;
            } // switch
        }

        // se non presenti errori -> dati collezionati
        if (!hasDocError) {
            documento.setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
            documento.setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
        }

        return true; // prosegui
    }

    private boolean verificaOrdineComponente(UpdDocumentoVers documento) {
        boolean prosegui = true; // di default non prosegue, se esitono componenti va avanti ...

        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        String cdCtrl = ControlliWSBundle.CTRL_COMP_ORDINE;
        Set<Long> ordiniDiPresentazioneUnici = new HashSet<>();

        // per ogni componente atteso ...
        for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {

            // salvo il controlloUnivocitaOrdinePresentazione
            if (!ordiniDiPresentazioneUnici
                    .contains(Long.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()))) {
                ordiniDiPresentazioneUnici.add(Long.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()));
            } else {
                // aggiunta su controlli componente
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_007_003);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_007_003,
                        documento.getUrnPartDocumento(), componente.getMyUpdComponente().getOrdinePresentazione()));
                rispostaControlli.setrBoolean(false);
            }

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    prosegui = false;
                    // aggiunta su controlli componente
                    versamento.addEsitoControlloOnControlliDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            documento.getRifUpdDocumento().getIDDocumento(), rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
                break;
            case Allegato:
                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    prosegui = false;
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            documento.getRifUpdDocumento().getIDDocumento(), rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
                break;
            case Annesso:
                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    prosegui = false;
                    // aggiunta su controlli annesso
                    versamento.addEsitoControlloOnControlliAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            documento.getRifUpdDocumento().getIDDocumento(), rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
                break;
            case Annotazione:
                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli annotazione
                    versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    prosegui = false;
                    // aggiunta su controlli annotazione
                    versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            documento.getRifUpdDocumento().getIDDocumento(), rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
                break;
            }

        }

        //
        /*
         * if (!hasDocError) { this.setEsitoControlloErr(cdCtrl, rispostaControlli.getCodErr(),
         * rispostaControlli.getDsErr()); return false;// non può effettuare i successivi controlli }
         */

        return prosegui;
    }

    private boolean verificaComponenti(UpdDocumentoVers documento) {
        boolean prosegui = false; // di default non prosegue, se esitono componenti va avanti ...
        // per ogni componente atteso ...
        for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
            // chiave componente
            /*
             * componente.setChiaveComp(MessaggiWSFormat.formattaChiaveComponente(documento.getUrnPartDocumento(), 1,
             * componente.getMyUpdComponente().getOrdinePresentazione()));
             */
            componente.setChiaveComp(String.valueOf(componente.getMyUpdComponente().getOrdinePresentazione()));

            // verifica id componente
            prosegui = this.verificaIdComponente(componente, componente.getKeyCtrl());

            // verifico tipo struttura e tipo componente
            if (prosegui) {
                prosegui = this.verificaTipoComponente(componente, componente.getKeyCtrl());
            }

            // verifico nome componente
            if (prosegui) {
                prosegui = this.verificaNomeComponenti(componente, componente.getKeyCtrl());
            }

            // verifico dati specifici
            if (prosegui && componente.getMyUpdComponente().getDatiSpecifici() != null) {
                prosegui = this.verificaDatiSpecComponente(componente, componente.getKeyCtrl());
            }

            // verifico dati specifici migrazione
            if (prosegui && componente.getMyUpdComponente().getDatiSpecificiMigrazione() != null) {
                prosegui = this.verificaDatiSpecMigrazComponente(componente, componente.getKeyCtrl());
            }
        }

        return prosegui;
    }

    private boolean verificaIdComponente(UpdComponenteVers componente, String ctrlKey) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        UpdDocumentoVers documento = componente.getRifUpdDocumentoVers();
        boolean hasDocError = false;
        String cdCtrl = ControlliWSBundle.CTRL_KEYDOC_CHIAVECOMP;

        // recupera l'ejb per i controlli se possibile - altrimenti segnala errore
        try {
            updVersamentoControlli = (ControlliUpdVersamento) new InitialContext()
                    .lookup("java:module/ControlliUpdVersamento");
        } catch (NamingException ex) {
            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR, MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "UpdDocumentoPrsr.init.ControlliUpdVersamento: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB RapportoVersBuilder ", ex);
            return false;
        }

        rispostaControlli = updVersamentoControlli.checkIdComponenteInDoc(documento.getIdRecDocumentoDB(),
                componente.getMyUpdComponente().getOrdinePresentazione(), componente.getChiaveComp());

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        ctrlKey);
            } else {
                hasDocError = true;
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Allegato:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Annesso:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocError = true;
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Annotazione:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        }
        // esito generale in caso di errore
        if (hasDocError) {
            /*
             * this.setEsitoControlloErr(cdCtrl, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
             */
            return false;// non può effettuare i successivi controlli
        } else {
            // memorizzo l'ID della chiave doc trovata e il tipo da verificare
            componente.setIdRecDB(rispostaControlli.getrLong());
            // memorizzo il tipo supporto
            componente.setTipoSupporto(TipiSupporto.valueOf(
                    (String) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.TI_SUPPORTO_COMP.name())));
            // memorizzo il tipo componente
            componente.setTipoComponenteNonVerificato(rispostaControlli.getrStringExtended());
            // memorizzo il nome della struttura
            documento.setTipoStrutturaNonverificato(rispostaControlli.getrString());
        }

        return true;// prosegui con check successivo
    }

    private boolean verificaTipoComponente(UpdComponenteVers componente, String ctrlKey) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        UpdDocumentoVers documento = componente.getRifUpdDocumentoVers();
        boolean hasDocError = false;
        String codErr = null, dsErr = null;
        String cdCtrl = ControlliWSBundle.CTRL_COMP_TIPO;

        // controllo tipo struttura componente
        if (!documento.getRifUpdDocumento().getStrutturaOriginale().getTipoStruttura()
                .equalsIgnoreCase(documento.getTipoStrutturaNonverificato())) {
            // aggiunta su controlli componente
            codErr = MessaggiWSBundle.DOC_010_004;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.DOC_010_004,
                    documento.getRifUpdDocumento().getIDDocumento(),
                    documento.getRifUpdDocumento().getStrutturaOriginale().getTipoStruttura());
            hasDocError = true;
        }

        // controllo tipo componente
        if (!hasDocError && !componente.getMyUpdComponente().getTipoComponente()
                .equalsIgnoreCase(componente.getTipoComponenteNonVerificato())) {
            // aggiunta su controlli componente
            codErr = MessaggiWSBundle.COMP_010_003;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.COMP_010_003,
                    componente.getMyUpdComponente().getOrdinePresentazione(),
                    componente.getMyUpdComponente().getTipoComponente());
            hasDocError = true;
        }

        switch (documento.getCategoriaDoc()) {
        case Principale:

            if (!hasDocError) {
                // recupero struttura e tipo componente da DB
                rispostaControlli = updVersamentoControlli.checkAroStrutDocAndTipo(componente.getIdRecDB());

                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocError = true;
                    codErr = rispostaControlli.getCodErr();
                    dsErr = rispostaControlli.getDsErr();
                    // aggiunta su controlli componente
                    versamento.addEsitoControlloOnControlliComponentiDocPrincipale(
                            ControlliWSBundle.getControllo(cdCtrl), SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            ctrlKey, codErr, dsErr);
                }

            } else {
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
            }
            break;
        case Allegato:

            if (!hasDocError) {
                // recupero struttura e tipo componente da DB
                rispostaControlli = updVersamentoControlli.checkAroStrutDocAndTipo(componente.getIdRecDB());

                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocError = true;
                    codErr = rispostaControlli.getCodErr();
                    dsErr = rispostaControlli.getDsErr();
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);

                }
            } else {
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
            }
            break;
        case Annesso:

            if (!hasDocError) {
                // recupero struttura e tipo componente da DB
                rispostaControlli = updVersamentoControlli.checkAroStrutDocAndTipo(componente.getIdRecDB());

                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocError = true;
                    codErr = rispostaControlli.getCodErr();
                    dsErr = rispostaControlli.getDsErr();
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);

                }
            } else {
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
            }
            break;
        case Annotazione:

            if (!hasDocError) {
                // recupero struttura e tipo componente da DB
                rispostaControlli = updVersamentoControlli.checkAroStrutDocAndTipo(componente.getIdRecDB());

                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocError = true;
                    codErr = rispostaControlli.getCodErr();
                    dsErr = rispostaControlli.getDsErr();
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);

                }
            } else {
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
            }
            break;
        }

        // se non presenti errori -> colleziona dati
        if (!hasDocError) {
            // memorizzo l'ID struttura e il l'ID del tipo comp
            documento.setIdRecStrutturaDB(rispostaControlli.getrLong());
            componente.setIdTipoComponente(rispostaControlli.getrLongExtended());
        }

        return true;
    }

    private boolean verificaNomeComponenti(UpdComponenteVers componente, String ctrlKey) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        UpdDocumentoVers documento = componente.getRifUpdDocumentoVers();
        boolean hasDocCompError = false;
        String cdCtrl = ControlliWSBundle.CTRL_COMP_NOME;

        // verifico se il tipo supporto componente è diverso da file, NON deve avere
        // sottocomponenti
        if (componente.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
            if (componente.getMyUpdComponente().getNomeComponente() == null
                    || componente.getMyUpdComponente().getNomeComponente().isEmpty()) {
                // aggiunta su controlli componente
                rispostaControlli.setCodErr(MessaggiWSBundle.COMP_005_001);
                rispostaControlli.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.COMP_005_001, componente.getChiaveComp()));
                rispostaControlli.setrBoolean(false);

            } else {
                rispostaControlli.setrBoolean(true);
            }
        }

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Allegato:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Annesso:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        case Annotazione:
            if (rispostaControlli.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocCompError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
            break;
        }

        // se non presenti errori -> colleziona dati
        if (!hasDocCompError) {
            // memorizzo l'ID struttura e il l'ID del tipo comp
            documento.setIdRecStrutturaDB(rispostaControlli.getrLong());
            componente.setIdTipoComponente(rispostaControlli.getrLongExtended());

        }

        return true;
    }

    private boolean verificaDatiSpecComponente(UpdComponenteVers componente, String ctrlKey) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        UpdDocumentoVers documento = componente.getRifUpdDocumentoVers();
        boolean hasDocCompError = false;
        String cdCtrl = ControlliWSBundle.CTRL_COMP_DATISPEC;

        JAXBElement<DatiSpecificiType> tmpDatiSpecifici = componente.getMyUpdComponente().getDatiSpecifici();
        RispostaControlliAttSpec tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.COMP,
                tmpDatiSpecifici, componente.getIdTipoComponente(), componente.getChiaveComp(),
                componente.getMyUpdComponente().getTipoComponente());

        switch (documento.getCategoriaDoc()) {
        case Principale:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, tmpControlliAttSpec.getCodErr(),
                        tmpControlliAttSpec.getDsErr());
            }
            break;
        case Allegato:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, tmpControlliAttSpec.getCodErr(),
                        tmpControlliAttSpec.getDsErr());
            }
            break;
        case Annesso:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli componente
                versamento.addControlloOkOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl), ctrlKey);
            } else {
                hasDocCompError = true;
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, tmpControlliAttSpec.getCodErr(),
                        tmpControlliAttSpec.getDsErr());
            }
            break;
        case Annotazione:
            if (tmpControlliAttSpec.isrBoolean()) {
                // aggiunta su controlli annotazione
                versamento.addControlloOkOnControlliAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        documento.getRifUpdDocumento().getIDDocumento());
            } else {
                hasDocCompError = true;
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, tmpControlliAttSpec.getCodErr(),
                        tmpControlliAttSpec.getDsErr());
            }
            break;
        }
        // se non presenti errori -> colleziona dati
        if (!hasDocCompError) {
            // dati specifici
            componente.setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
            // id xsd dati specifici
            componente.setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
        }

        return true; // prosegui
    }

    private boolean verificaDatiSpecMigrazComponente(UpdComponenteVers componente, String ctrlKey) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        UpdDocumentoVers documento = componente.getRifUpdDocumentoVers();

        String codErr = null, dsErr = null;
        boolean hasDocCompError = false;

        String cdCtrl = ControlliWSBundle.CTRL_COMP_DATISPECMIGRAZ;

        RispostaControlliAttSpec tmpControlliAttSpec = new RispostaControlliAttSpec();
        tmpControlliAttSpec.setrBoolean(false);

        // DatiSpecificiMigrazione
        if (StringUtils.isNotBlank(strutturaUpdVers.getTipoConservazione())
                && TipoConservazioneType.MIGRAZIONE.name().equalsIgnoreCase(strutturaUpdVers.getTipoConservazione())) {

            JAXBElement<DatiSpecificiType> tmpDatiSpecificiMigrazione = componente.getMyUpdComponente()
                    .getDatiSpecificiMigrazione();
            tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.COMP, tmpDatiSpecificiMigrazione,
                    componente.getIdTipoComponente(), componente.getChiaveComp(),
                    componente.getMyUpdComponente().getTipoComponente());

            // collect cod and desc error (esito generale)
            if (!tmpControlliAttSpec.isrBoolean()) {
                hasDocCompError = true;
                codErr = tmpControlliAttSpec.getCodErr();
                dsErr = tmpControlliAttSpec.getDsErr();
            }

            switch (documento.getCategoriaDoc()) {
            case Principale:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocCompError = true;
                    // aggiunta su controlli componente
                    versamento.addEsitoControlloOnControlliComponentiDocPrincipale(
                            ControlliWSBundle.getControllo(cdCtrl), SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO,
                            ctrlKey, codErr, dsErr);
                }
                break;
            case Allegato:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocCompError = true;
                    // aggiunta su controlli allegati
                    versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                }
                break;
            case Annesso:
                if (rispostaControlli.isrBoolean()) {
                    // aggiunta su controlli componente
                    versamento.addControlloOkOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            ctrlKey);
                } else {
                    hasDocCompError = true;
                    // aggiunta su controlli annesso
                    versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                }
                break;
            case Annotazione:
                if (tmpControlliAttSpec.isrBoolean()) {
                    // aggiunta su controlli annotazione
                    versamento.addControlloOkOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            documento.getRifUpdDocumento().getIDDocumento());
                } else {
                    hasDocCompError = true;
                    // aggiunta su controlli annotazione
                    versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                            SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                }
                break;
            } // switch
        } else {
            //
            hasDocCompError = true;
            codErr = MessaggiWSBundle.XSD_006_004;
            dsErr = MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_004);

            switch (documento.getCategoriaDoc()) {
            case Principale:
                // aggiunta su controlli componente
                versamento.addEsitoControlloOnControlliComponentiDocPrincipale(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                break;
            case Allegato:
                // aggiunta su controlli allegati
                versamento.addEsitoControlloOnControlliComponentiAllegati(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);

                break;
            case Annesso:
                // aggiunta su controlli annesso
                versamento.addEsitoControlloOnControlliComponentiAnnessi(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                break;
            case Annotazione:
                // aggiunta su controlli annotazione
                versamento.addEsitoControlloOnControlliComponentiAnnotazioni(ControlliWSBundle.getControllo(cdCtrl),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, ctrlKey, codErr, dsErr);
                break;
            }
        }

        // se non presenti errori -> colleziona dati
        if (!hasDocCompError) {
            // dati specifici migrazione
            componente.setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
            // id xsd dati specifici
            componente.setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
        }

        return true; // prosegui
    }
}
