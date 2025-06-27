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

package it.eng.parer.util.ejb;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
/**
 *
 * @author fioravanti_f
 */
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.ParametroApplDB;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@LocalBean
@Startup
public class AppServerInstance {

    @EJB
    ControlliSemantici controlliSemantici;

    private static final Logger LOG = LoggerFactory.getLogger(AppServerInstance.class);
    String servername = null;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initSingleton() {
	try {
	    LOG.info("Inizializzazione singleton AppServerInstance...");
	    InetAddress address = this.getMyHostAddress();

	    LOG.info("Indirizzo IP del server: {}", address.getHostAddress());
	    LOG.info("        Nome del server: {}", address.getCanonicalHostName());

	    servername = address.getCanonicalHostName();

	    HashMap<String, String> xmlDefaults = null;
	    RispostaControlli rispostaControlli = controlliSemantici
		    .caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.LOG_APPLIC);
	    if (!rispostaControlli.isrBoolean()) {
		LOG.error("Inizializzazione singleton AppServerInstance fallita! {}",
			rispostaControlli.getDsErr());
		throw new SacerWsRuntimeException(rispostaControlli.getDsErr(),
			SacerWsErrorCategory.INTERNAL_ERROR);
	    } else {
		xmlDefaults = (HashMap<String, String>) rispostaControlli.getrObject();
	    }

	    if (xmlDefaults.get(ParametroApplDB.SERVER_NAME_SYSTEM_PROPERTY) != null) {
		String instance = System
			.getProperty(xmlDefaults.get(ParametroApplDB.SERVER_NAME_SYSTEM_PROPERTY));
		if (instance != null) {
		    servername += "/" + instance;
		}
	    }
	    LOG.info("Il nome completo dell'istanza in esecuzione è {}", servername);
	} catch (UnknownHostException ex) {
	    throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
	}
    }

    @Lock(LockType.READ)
    public String getName() {
	return servername;
    }

    enum AddressTypes {
	// l'ordine di scelta dovrebbe essere quello che segue:
	// priorità ai nomi host, poi gli ipv4, poi gli ipv6.
	// nell'elenco sono presenti anche i due tipi di indirizzi di loopback
	// così che nel caso pessimo sia reso preferibilmente
	// l'indirizzo IPV4. (nel caso di questi due ultimi tipi di
	// indirizzo il nome host non è critico)
	// tendenzialmente è meglio un site local di un ip pubblico.
	// per capire se un ip ha un nome, verifico se questo è diverso dalla
	// rappresentazione in stringa delll'ip. (non molto bello, in effetti)
	SITE_LOCAL_WITH_NAME, NON_SITE_LOCAL_WITH_NAME, SITE_LOCAL_WITHOUT_NAME_IPV4,
	NON_SITE_LOCAL_WITHOUT_NAME_IPV4, SITE_LOCAL_WITHOUT_NAME_IPV6,
	NON_SITE_LOCAL_WITHOUT_NAME_IPV6, LOOPBACK_IPV4, LOOPBACK_IPV6
    }

    private InetAddress getMyHostAddress() throws UnknownHostException {
	try {
	    Map<AddressTypes, InetAddress> map = new EnumMap<>(AddressTypes.class);

	    // Nota se falliscono tutti questi tentativi la macchina è
	    // probabilmente disconnessa dalla rete
	    //
	    // Scorri su tutte le interfacce di rete
	    for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
		    .hasMoreElements();) {
		NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
		// Scorri su tutti gli indirizzi IP associati ad una scheda di rete
		for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs
			.hasMoreElements();) {
		    InetAddress inetAddr = inetAddrs.nextElement();
		    LOG.debug("Verifico l'indirizzo {}", inetAddr.getHostAddress());
		    AddressTypes tipo = decodeAddrType(inetAddr);
		    // se è già presente un indirizzo di questo tipo (per esempio se
		    // ci sono più schede di rete fisiche) lo sovrascrivo. Di fatto prendo
		    // l'ultimo che leggo.
		    map.put(tipo, inetAddr);
		}
	    }

	    // l'enum viene letto nell'ordine in cui è stato dichiarato,
	    // garantendo la preferenza nella scelta del tipo di indirizzo reso
	    for (AddressTypes at : AddressTypes.values()) {
		if (map.get(at) != null) {
		    LOG.info("Ho selezionato l'indirizzo di tipo {}", at);
		    return map.get(at);
		}
	    }

	    // A questo punto, non siamo riusciti a determinare un indirizzo plausibile
	    // Ripieghiamo usando l'API del JDK sperando che il risultato non sia
	    // del tutto inutile (sotto Linux la cosa è frequente)
	    InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	    if (jdkSuppliedAddress == null) {
		throw new UnknownHostException(
			"Il metodo JDK InetAddress.getLocalHost() ha reso null.");
	    }
	    return jdkSuppliedAddress;
	} catch (Exception e) {
	    UnknownHostException unknownHostException = new UnknownHostException(
		    "Impossibile determinare un indirizzo per la macchina: " + e);
	    unknownHostException.initCause(e);
	    throw unknownHostException;
	}
    }

    private AddressTypes decodeAddrType(InetAddress inetAddress) {
	AddressTypes tipo;
	if (!inetAddress.isLoopbackAddress()) {
	    if (inetAddress.isSiteLocalAddress()) {
		if (isAddressWithHostName(inetAddress)) {
		    tipo = AddressTypes.SITE_LOCAL_WITH_NAME;
		} else if (inetAddress instanceof Inet4Address) {
		    tipo = AddressTypes.SITE_LOCAL_WITHOUT_NAME_IPV4;
		} else {
		    tipo = AddressTypes.SITE_LOCAL_WITHOUT_NAME_IPV6;
		}
	    } else {
		if (isAddressWithHostName(inetAddress)) {
		    tipo = AddressTypes.NON_SITE_LOCAL_WITH_NAME;
		} else if (inetAddress instanceof Inet4Address) {
		    tipo = AddressTypes.NON_SITE_LOCAL_WITHOUT_NAME_IPV4;
		} else {
		    tipo = AddressTypes.NON_SITE_LOCAL_WITHOUT_NAME_IPV6;
		}
	    }
	    LOG.debug("è un indirizzo {}", tipo);
	} else {
	    // non mi interessa se questo indirizzo di loopback ha un nome:
	    // nella maggior parte dei casi si chiama "localhost".
	    // questa discriminazione viene fatta solo per dare
	    // priorità nella selezione all'indirizzio ipv4
	    // rispetto a quello ipv6
	    if (inetAddress instanceof Inet4Address) {
		tipo = AddressTypes.LOOPBACK_IPV4;
	    } else {
		tipo = AddressTypes.LOOPBACK_IPV6;
	    }
	    LOG.debug("è un indirizzo di loopback di tipo {}", tipo);
	}
	return tipo;
    }

    private boolean isAddressWithHostName(InetAddress inetAddress) {
	if (inetAddress.getHostName().equals(inetAddress.getHostAddress())) {
	    return false;
	}
	return true;
    }
}
