/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoMM.ejb;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.ejb.VersamentoSyncBase;
import it.eng.parer.ws.versamentoMM.dto.VersamentoMMExt;
import it.eng.parer.ws.versamentoMM.utils.AllineaFileComponenti;
import it.eng.parer.ws.versamentoMM.utils.IndiceMMPrsr;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import java.io.File;
import java.io.IOException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "VersamentoSyncMM")
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class VersamentoSyncMM extends VersamentoSyncBase {
    //
    private static final Logger log = LoggerFactory.getLogger(VersamentoSyncMM.class);
    //

    public void parseXMLIndiceMM(SyncFakeSessn sessione, RispostaWS rispostaWs, VersamentoMMExt versamento) {
        log.debug("sono nel metodo parseXMLIndiceMM");
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

        if (versamento.getUtente() == null) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666, "Errore: l'utente non è autenticato.");
            return;
        }

        try {
            IndiceMMPrsr tmpPrsr = new IndiceMMPrsr(versamento, rispostaWs);
            tmpPrsr.parseXML(sessione);
            tmpAvanzamentoWs.resetFase();
        } catch (Exception e) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore nella fase di parsing dell'XML indice del EJB " + e.getMessage());
            log.error("Eccezione nella fase di parsing dell'XML indice del EJB ", e);
        }

        if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
            myEsito.setXMLVersamento(sessione.getDatiPackInfoSipXml());
        }
    }

    public void verificaCoerenzaComponenti(RispostaWS rispostaWs, VersamentoMMExt versamento, SyncFakeSessn sessione,
            String path) {
        log.debug("sono nel metodo verificaCoerenzaComponenti");
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

        if (versamento.getUtente() == null) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666, "Errore: l'utente non è autenticato.");
            return;
        }

        try {
            AllineaFileComponenti tmpAllinea = new AllineaFileComponenti(versamento, rispostaWs, sessione);
            tmpAllinea.verificaCoerenza(path);
            tmpAvanzamentoWs.resetFase();
        } catch (Exception e) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Eccezione nella fase di allineamento file del EJB " + e.getMessage());
            log.error("Eccezione nella fase di allineamento file del EJB ", e);
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // verifica che il payload del versamento possa essere contenuto nel filesystem di destinazione
            if (versamento.getStrutturaComponenti().getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE
                    && versamento.getStrutturaComponenti().isTpiAbilitato()) {
                try {
                    FileServUtils fileServUtils = new FileServUtils();
                    if (!fileServUtils.controllaSpazioLibero(versamento.getStrutturaComponenti().getTpiRootTpiDaSacer(),
                            versamento.getStrutturaComponenti().getTotalSizeInBytes())) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_003_001);
                    }
                } catch (Exception e) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Errore nella fase di controllo dello spazio libero sul file system TPI " + e.getMessage());
                    log.error("Eccezione nella fase di controllo dello spazio libero sul file system TPI ", e);
                }
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
            myEsito.setXMLVersamento(versamento.getDatiXml());
        }
    }

    public void pulisciZipTemp(RispostaWS rispostaWs, VersamentoMMExt versamento, SyncFakeSessn sessione) {
        log.debug("sono nel metodo pulisciZipTemp");
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();

        if (versamento.getUtente() == null) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666, "Errore: l'utente non è autenticato.");
            return;
        }

        if (versamento.isContainerZip() && versamento.getPathLocaleContenutoZip() != null) {
            // rimozione dei file temporanei estratti dallo zip
            for (FileBinario tmpFileDaRimuovere : sessione.getFileBinari()) {
                tmpFileDaRimuovere.getFileSuDisco().delete();
            }
            try {
                FileUtils.deleteDirectory(new File(versamento.getPathLocaleContenutoZip()));
            } catch (IOException ex) {
                log.error("Eccezione nella fase di rimozione della directory temporanea per lo ZIP", ex);
            }
        }
    }
}
