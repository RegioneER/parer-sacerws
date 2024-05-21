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

package it.eng.parer.ws.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fioravanti_F
 */
public class RispostaControlli implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private long rLong = -1;
    private long rLongExtended = -1;
    private int rInt = -1;
    private String rString = null;
    private String rStringExtended = null;
    private boolean rBoolean = false;
    private Object rObject = null;
    private Date rDate = null;
    private String codErr = null;
    private String dsErr = null;
    private Map<String, Object> rMap = new HashMap<>();// empty

    public void init() {
        codErr = null;
        dsErr = null;
    }

    public void reset() {
        rLong = -1;
        rLongExtended = -1;
        rInt = -1;
        rBoolean = false;
        rString = null;
        rStringExtended = null;
        codErr = null;
        dsErr = null;
        rObject = null;
        rDate = null;
        rMap = new HashMap<>();// empty
    }

    public String getCodErr() {
        return codErr;
    }

    public void setCodErr(String codErr) {
        this.codErr = codErr;
    }

    public String getDsErr() {
        return dsErr;
    }

    public void setDsErr(String dsErr) {
        this.dsErr = dsErr;
    }

    public boolean isrBoolean() {
        return rBoolean;
    }

    public void setrBoolean(boolean rBoolean) {
        this.rBoolean = rBoolean;
    }

    public Date getrDate() {
        return rDate;
    }

    public void setrDate(Date rDateTime) {
        this.rDate = rDateTime;
    }

    public int getrInt() {
        return rInt;
    }

    public void setrInt(int rInt) {
        this.rInt = rInt;
    }

    public long getrLong() {
        return rLong;
    }

    public void setrLong(long rLong) {
        this.rLong = rLong;
    }

    public long getrLongExtended() {
        return rLongExtended;
    }

    public void setrLongExtended(long rLongExtended) {
        this.rLongExtended = rLongExtended;
    }

    public Object getrObject() {
        return rObject;
    }

    public void setrObject(Object rObject) {
        this.rObject = rObject;
    }

    public String getrString() {
        return rString;
    }

    public void setrString(String rString) {
        this.rString = rString;
    }

    public String getrStringExtended() {
        return rStringExtended;
    }

    public void setrStringExtended(String rStringExtended) {
        this.rStringExtended = rStringExtended;
    }

    public Map<String, Object> getrMap() {
        return rMap;
    }

    public enum ValuesOnrMap {
        ID_TIPO_UD, TI_CONSERVAZIONE, ID_REGISTROUD, NM_SISTEMAMIGRAZ, ID_TIPO_DOC_PRINC, FL_FORZA_COLL,
        TI_SUPPORTO_COMP, CD_KEY_NORMALIZED;
    }
}
