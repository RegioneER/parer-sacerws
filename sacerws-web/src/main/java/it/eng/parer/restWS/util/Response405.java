/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.restWS.util;

import java.io.IOException;
import java.text.MessageFormat;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Fioravanti_F
 */
public class Response405 {

    public enum NomeWebServiceRest {

        VERSAMENTO_SYNC("VersamentoSync"), AGGIUNTA_ALLEGATI_SYNC("AggiuntaAllegatiSync"), /**/
        VERSAMENTO_MM("VersamentoMultiMedia"), VERSAMENTO_FASCICOLO("VersamentoFascicoloSync"), /**/
        AGGIORNAMENTO_VERSAMENTO("AggiornamentoVersamentoSync"),
        //
        REC_STATO_CONSERVAZIONE_SYNC("RecDIPStatoConservazioneSync"),
        REC_UNITA_DOC_SYNC("RecDIPUnitaDocumentariaSync"), /**/
        REC_PROVE_CONSERV_SYNC("RecDIPProveConservSync"), REC_RAPP_VERS_SYNC("RecDIPRapportiVersSync"), /**/
        REC_AIP_UNITA_DOC_SYNC("RecAIPUnitaDocumentariaSync"), REC_DIP_ESIBIZIONE_SYNC("RecDIPEsibizioneSync"),
        //
        REC_UNITA_DOC_MM("RecUniDocMultiMedia"), REC_PROVE_CONSERV_MM("RecPCUniDocMultiMedia"),
        //
        REC_UD_CONVERT_PDF_SYNC("RecDIPComponenteTrasformatoSync"),
        //
        INVIO_RICHIESTA_ANNULLAMENTO_VERSAMENTI("InvioRichiestaAnnullamentoVersamenti"),
        //
        WS_STATUS_MONITOR("StatusMonitor"),;

        private final String valore;

        private NomeWebServiceRest(String val) {
            this.valore = val;
        }

        @Override
        public String toString() {
            return valore;
        }
    }

    private final static String messaggio = "<html><body><h1>{0}</h1>\n" + "<p><strong>Congratulazioni!</strong></p>\n"
            + "<ul>\n" + "<li><p>Avete appena raggiunto l''entry point del web service, "
            + "la vostra connessione sembra funzionare.</p>\n" + "</li>\n"
            + "<li><p>Sfortunatamente avete raggiunto questo indirizzo tramite un browser "
            + "e non potete effettuare altre operazioni.</p>\n" + "</li>\n"
            + "<li><p><strong>Per gli utenti</strong>: per utilizzare questo servizio "
            + "dovrete usare il vostro programma client oppure un form web dedicato.</p>\n" + "</li>\n"
            + "<li><p><strong>Per gli sviluppatori</strong>: le specifiche di "
            + "chiamata ed i documenti <em>XML Schema</em> "
            + "relativi a questo web service sono disponibili contattando "
            + "gli amministratori del servizio.</p></body></html>";

    public static void fancy405(HttpServletResponse resp, NomeWebServiceRest nomeServizio) throws IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.getWriter().println(MessageFormat.format(messaggio, nomeServizio));
    }

}
