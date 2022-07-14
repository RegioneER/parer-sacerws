package it.eng.parer.ws.utils;

import it.eng.parer.ws.dto.IRispostaWS;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bonora_L
 */
public class WsTransactionManager {

    private UserTransaction utx;
    private static final Logger log = LoggerFactory.getLogger(WsTransactionManager.class);

    public WsTransactionManager(UserTransaction utx) {
        this.utx = utx;
    }

    public void beginTrans(IRispostaWS rispostaWs) {
        try {
            utx.begin();
        } catch (NotSupportedException e) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di apertura transazione db del EJB " + e.getMessage());
            log.error("Eccezione nell'init ejb ", e);
        } catch (SystemException e) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di apertura transazione db del EJB " + e.getMessage());
            log.error("Eccezione nell'init ejb ", e);
        }
    }

    public void commit(IRispostaWS rispostaWs) {
        try {
            utx.commit();
        } catch (RollbackException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (HeuristicMixedException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (HeuristicRollbackException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (SecurityException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (IllegalStateException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (SystemException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (Exception ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di commit del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        }
    }

    public void rollback(IRispostaWS rispostaWs) {
        try {
            utx.rollback();
        } catch (IllegalStateException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di rollback del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (SecurityException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di rollback del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (SystemException ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di rollback del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        } catch (Exception ex) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
            rispostaWs.setErrorMessage("Errore nella fase di rollback del EJB " + ex.getMessage());
            log.error("Eccezione nell'init ejb ", ex);
        }
    }
}
