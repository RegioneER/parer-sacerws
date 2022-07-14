package it.eng.parer.ws.versamento.ejb;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 *
 * @author Fioravanti_F
 */
/**
 * Session Bean implementation class VersamentoSync
 */
@Stateless(mappedName = "VersamentoSync")
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class VersamentoSync extends VersamentoSyncBase {
    // questa classe è completamente vuota e deve restare così.
}
