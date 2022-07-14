/*
 Attenzione
 Il codice di questo modulo software è legato:
 * al DBMS Oracle
 * al driver JDBC proprietario di Oracle (compatibile JDBC 4)
 * alla gestione propria di Oracle delle colonne di tipo BLOB
 * alla gestione propria di Oracle delle sequence per creare le chiavi primarie delle tabelle

 Questo codice non è direttamente usabile su altre architetture di database e presumibilmente
 dovrà essere riscritto in parte o del tutto per gestire la modalità di gestione delle colonne
 di tipo BLOB impiegata da un eventuale altro DBMS.

 La scelta di utilizzare una modalità di accesso non portabile per scrivere su tabella nasce
 dall'esigenza di dover leggere e scrivere colonne di tipo BLOB potenzialmente enormi e
 dall'incapacità dell'architettura JPA di effettuare queste operazioni tramite stream.
 */
package it.eng.parer.ws.versamento.ejb.oracleBlb;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.FileBinario;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "WriteCompBlbOracle")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class WriteCompBlbOracle {

    private static final Logger log = LoggerFactory.getLogger(WriteCompBlbOracle.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;
    //
    private static final String QRY_SARO_CONTENUTO_COMP = "SELECT SARO_CONTENUTO_COMP.NEXTVAL FROM DUAL";
    private static final String QRY_ARO_CONTENUTO_COMP = "INSERT INTO ARO_CONTENUTO_COMP "
            + "(ID_CONTEN_COMP, ID_COMP_STRUT_DOC, BL_CONTEN_COMP, ID_STRUT, MM_VERS, AA_KEY_UNITA_DOC) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    //
    private static final String QRY_SARO_FILE_COMP = "SELECT SARO_FILE_COMP.NEXTVAL FROM DUAL";
    private static final String QRY_ARO_FILE_COMP = "INSERT INTO ARO_FILE_COMP "
            + "(ID_CONTEN_COMP, ID_COMP_STRUT_DOC, BL_CONTEN_COMP, ID_STRUT, MM_VERS, AA_KEY_UNITA_DOC) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    //
    private static final String QRY_SVRS_CONTENUTO_FILE = "SELECT SVRS_CONTENUTO_FILE.NEXTVAL FROM DUAL";
    private static final String QRY_VRS_CONTENUTO_FILE = "INSERT INTO VRS_CONTENUTO_FILE "
            + "(ID_CONTENUTO_FILE, ID_FILE_SESSIONE, BL_CONTENUTO_FILE_SESSIONE, ID_STRUT, MM_VERS) "
            + "VALUES (?, ?, ?, ?, ?)";
    //
    private final static String QRY_FIR_CONTENUTO_REPORT = "UPDATE FIR_REPORT " + "SET BL_CONTENUTO_REPORT = ? "
            + "WHERE ID_FIR_REPORT = ?";
    private final static int BUFFERSIZE = 10 * 1024 * 1024;

    public RispostaControlli salvaStreamSuBlobComp(WriteCompBlbOracle.DatiAccessori datiAccessori, FileBinario fb) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        //
        ISequenceGen sequenceGen = new NonMonotonicSequenceGen();
        //
        InputStream inputStream = null;
        BufferedInputStream bufFis = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String queryStrSeq = null;
        String queryStrInsrt = null;
        long sequenceVal = 0;

        switch (datiAccessori.getTabellaBlob()) {
        case ARO_CONTENUTO_COMP:
            queryStrSeq = QRY_SARO_CONTENUTO_COMP;
            queryStrInsrt = QRY_ARO_CONTENUTO_COMP;
            break;
        case ARO_FILE_COMP:
            queryStrSeq = QRY_SARO_FILE_COMP;
            queryStrInsrt = QRY_ARO_FILE_COMP;
            break;
        case VRS_CONTENUTO_FILE:
            queryStrSeq = QRY_SVRS_CONTENUTO_FILE;
            queryStrInsrt = QRY_VRS_CONTENUTO_FILE;
            break;
        }

        try {
            java.sql.Connection connection = entityManager.unwrap(java.sql.Connection.class);
            //
            try { // ottengo il nuovo valore per la sequence
                log.debug(queryStrSeq);
                pstmt = (PreparedStatement) connection.prepareStatement(queryStrSeq);
                rs = (ResultSet) pstmt.executeQuery();
                while (rs.next()) {
                    sequenceVal = rs.getLong(1);
                }
            } finally {
                rs.close();
                pstmt.close();
            }
            //
            try { // scrivo il blob ed i dati accessori sulla tabella
                inputStream = new FileInputStream(fb.getFileSuDisco());
                bufFis = new BufferedInputStream(inputStream, BUFFERSIZE);
                log.debug("Aperto il BufferedInputStream.");
                log.debug("Query: {}", queryStrInsrt);
                pstmt = (PreparedStatement) connection.prepareStatement(queryStrInsrt);
                pstmt.setLong(1, sequenceGen.newSequenceNum(sequenceVal));
                pstmt.setLong(2, datiAccessori.getIdPadre());
                log.debug("Prima di setBlob");
                pstmt.setBlob(3, bufFis, fb.getFileSuDisco().length());
                log.debug("Dopo di setBlob");
                if (datiAccessori.getIdStruttura() != 0) {
                    pstmt.setLong(4, datiAccessori.getIdStruttura());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                pstmt.setLong(5, datiAccessori.calcolaProgAnnoMeseVers());
                if (datiAccessori.getTabellaBlob() == WriteCompBlbOracle.TabellaBlob.ARO_CONTENUTO_COMP
                        || datiAccessori.getTabellaBlob() == WriteCompBlbOracle.TabellaBlob.ARO_FILE_COMP) {
                    pstmt.setLong(6, datiAccessori.getAaKeyUnitaDoc());
                }
                log.debug("Prima di executeUpdate");
                int count = pstmt.executeUpdate();
                log.debug("Record scritti/aggiornati: {}", count);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(bufFis);
                pstmt.close();
            }
            //
            rispostaControlli.setrBoolean(true);
        } catch (Exception ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione WriteCompBlbOracle.salvaStreamSuBlobComp " + ex.getMessage()));
            log.error("Eccezione WriteCompBlbOracle.salvaStreamSuBlobComp ", ex);
        }

        return rispostaControlli;
    }

    public RispostaControlli aggiornaStreamSuBlobComp(WriteCompBlbOracle.DatiAccessori datiAccessori, FileBinario fb) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        //
        String queryStrUpd = null;
        //
        switch (datiAccessori.getTabellaBlob()) {
        case FIR_REPORT:
            queryStrUpd = QRY_FIR_CONTENUTO_REPORT;
            break;
        }
        // get connection
        java.sql.Connection connection = entityManager.unwrap(java.sql.Connection.class);

        try (BufferedInputStream bufFis = new BufferedInputStream(new FileInputStream(fb.getFileSuDisco()), BUFFERSIZE);
                PreparedStatement pstmt = connection.prepareStatement(queryStrUpd);) {
            //
            log.debug("Aperto il BufferedInputStream.");
            log.debug("Query: {}", queryStrUpd);
            pstmt.setBlob(1, bufFis, fb.getFileSuDisco().length());
            pstmt.setLong(2, datiAccessori.getIdPadre());
            log.debug("Prima di executeUpdate");
            int count = pstmt.executeUpdate();
            log.debug("Record scritti/aggiornati: {}", count);
            //
            rispostaControlli.setrBoolean(true);
        } catch (IOException | SQLException ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione WriteCompBlbOracle.aggiornaStreamSuBlobComp " + ex.getMessage()));
            log.error("Eccezione WriteCompBlbOracle.aggiornaStreamSuBlobComp ", ex);
        }

        return rispostaControlli;
    }

    public enum TabellaBlob {

        ARO_CONTENUTO_COMP, ARO_FILE_COMP, VRS_CONTENUTO_FILE, FIR_REPORT
    }

    public class DatiAccessori {

        private WriteCompBlbOracle.TabellaBlob tabellaBlob;
        private long idPadre;
        private long idStruttura;
        private long aaKeyUnitaDoc;
        private ZonedDateTime dtVersamento;

        public WriteCompBlbOracle.TabellaBlob getTabellaBlob() {
            return tabellaBlob;
        }

        public void setTabellaBlob(WriteCompBlbOracle.TabellaBlob tabellaBlob) {
            this.tabellaBlob = tabellaBlob;
        }

        public long getIdPadre() {
            return idPadre;
        }

        public void setIdPadre(long idPadre) {
            this.idPadre = idPadre;
        }

        public long getIdStruttura() {
            return idStruttura;
        }

        public void setIdStruttura(long idStruttura) {
            this.idStruttura = idStruttura;
        }

        public long getAaKeyUnitaDoc() {
            return aaKeyUnitaDoc;
        }

        public void setAaKeyUnitaDoc(long aaKeyUnitaDoc) {
            this.aaKeyUnitaDoc = aaKeyUnitaDoc;
        }

        public ZonedDateTime getDtVersamento() {
            return dtVersamento;
        }

        public void setDtVersamento(ZonedDateTime dtVersamento) {
            this.dtVersamento = dtVersamento;
        }

        public long calcolaProgAnnoMeseVers() {
            return MessaggiWSFormat.formattaKeyPartAnnoMeseVers(dtVersamento);
        }

    }
}
