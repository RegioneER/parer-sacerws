/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import java.util.HashMap;

/**
 *
 * @author Fioravanti_F
 */
public interface IDatiSpecEntity {

    public long getIdRecXsdDatiSpec();

    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec);

    public HashMap<String, DatoSpecifico> getDatiSpecifici();

    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici);

    public long getIdRecXsdDatiSpecMigrazione();

    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpec);

    public HashMap<String, DatoSpecifico> getDatiSpecificiMigrazione();

    public void setDatiSpecificiMigrazione(HashMap<String, DatoSpecifico> datiSpecificiMigrazione);
}
